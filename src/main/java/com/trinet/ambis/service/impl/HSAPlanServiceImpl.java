package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.ps.HSAPlansDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.sp.GetNextBenefitPlan;
import com.trinet.ambis.service.HSAPlanService;
import com.trinet.ambis.service.model.BenDefnOptnHSA;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.common.DateUtils;



public class HSAPlanServiceImpl implements HSAPlanService {
	
	private static final Logger logger = LoggerFactory.getLogger( HSAPlanServiceImpl.class );
	private HSAPlansDao hsaPlansDao;
	private GetNextBenefitPlan nextBenefitPlan;
	private HSAPlanMapping hsaPlanMapping;
	private StrategyHsaFundingDto hsaOptions;

	/** this is a list of all current OPTN rows. This is the work area where the new OPTN
	 * rows for the benefit program will be built */
	private List<BenDefnOptnHSA> currHSAOptns;

	/** this is a map of all current OPTN rows, keyed by BENEFIT_PLAN */
	private Map<String,BenDefnOptnHSA> currHSAPlanMap;

	/** Two maps will be build from the prior-year HSA options.
	 * The first map will be initialized to the set of active plans from the prior year.
	 * While plans are being setup, they will be removed from this map.  At the end of setup,
	 * anything remaining in this map is a plan that was active last year and must be inserted
	 * to the benefit program this year with ELIG_RULES_ID = "236Q" */
	private Map<String,BenDefnOptnHSA> priorYearActivePlansMap;

	/** The second map matches the medical plan (CROSS_BENEF_PLAN) to the custom HSA plan that
	 * was previously created for it.  If that same medical plan is in the current benefit program
	 * this map allows us to continue to use the matching HSA benefit plan. */
	private Map<String,BenDefnOptnHSA> level5Map;


	/** 
	 * This allows the calling method to set an implementation for the HSA Plans DAO.
	 * This setter provides flexibility and enables automated unit testing of this class.
	 * @param dao
	 */
	@Override
	public void setHSAPlansDao( HSAPlansDao dao ) {
		this.hsaPlansDao = dao;
	}

	/** 
	 * This allows the calling method to set an implementation for the stored procedure object.
	 * This setter provides flexibility and enables automated unit testing of this class.
	 * @param sp an instance of the stored procedure class to create the next BENEFIT_PLAN
	 * code from PeopleSoft
	 */
	@Override
	public void setNextBenPlanSP( GetNextBenefitPlan sp ) {
		this.nextBenefitPlan = sp;
	}

	/** 
	 * This allows the calling method to set an implementation for the existing-HSA-plan
	 * mapping object.
	 * This setter provides flexibility and enables automated unit testing of this class.
	 * @param hsaMap an instance of the HSAPlanMapping object
	 */
	@Override
	public void setHSAPlanMapping( HSAPlanMapping hsaMap ) {
		this.hsaPlanMapping = hsaMap;
	}


	public HSAPlanServiceImpl() {
		super();
	}



	@Override
	public void setupHSABenefitPlans( Company company, BenefitGroup group, StrategyHsaFundingDto hsaOptions,
			EntityManager em ) {
		// pass entity manager through to the plan mapping object
		this.hsaPlanMapping.setEntityManager( em );
		
		// transform date String to an sql.Date
		java.sql.Date effdt = new java.sql.Date( CommonUtils
				.formatStringToDate( company.getPlanStartDate(), BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY )
				.getTime() );

		if( Arrays.asList( 5, 7 ).contains( hsaOptions.getOptionId() )) {
			this.setupCustomHSA( company, group, hsaOptions, effdt );
		} else if( Arrays.asList( 0, 6 ).contains( hsaOptions.getOptionId() )) {
			this.setupCommunityHSA( group, effdt );
		}
	}


	/** 
	 * Level-0 & Level-6 HSA will retain the standard lvl 0 plans.  Turn-off other plans and resequence.
	 * @param group
	 * @param effdt
	 */
	private void setupCommunityHSA( BenefitGroup group, java.sql.Date effdt ) {

		// Select all the HSA plans and map BENEFIT_PLAN to the OPTN row
		this.currHSAOptns = hsaPlansDao.getAllHSAPlans( group.getBenefitProgram(), effdt );

		// if there are no HSA options for this client, exit
		if( CollectionUtils.isEmpty( this.currHSAOptns ) ) {
			return;
		}

		this.currHSAPlanMap = new HashMap<>();
		for( BenDefnOptnHSA optn : this.currHSAOptns ) {
			this.currHSAPlanMap.put( optn.getBenefitPlan(), optn );
		}

		// Select all the activated medical plans, for matching corresponding HDHP
		Map<String,String> medPlanEligMap = hsaPlansDao.getActiveMedPlans( group.getBenefitProgram(), effdt );

		// create the maps from all the HSA plans from the prior year
		this.buildPriorYearHSAMaps( group, this.currHSAOptns.get(0).getEffdt() );

		// Clear display sequence and option level for resequencing later
		for( BenDefnOptnHSA hsaOptn : this.currHSAOptns ) {
			hsaOptn.setDisplayOptSeq(null);
			hsaOptn.setOptionLvl(BigDecimal.ONE);

			// keep this HSA plan only if the corresponding HDHP was activated
			if( medPlanEligMap.containsKey( hsaOptn.getCrossBenefPlan() )) {
				// leave plan active
			} else {
				if( hsaOptn.isPlanActive() ) {
					hsaOptn.setEligRulesId( null );
				}
			}
		}

		// Resequencing.  Number all level 0 options that are not inactivated
		int seqNum = 1;
		for( BenDefnOptnHSA seqOptn : this.currHSAOptns ) {
			// remove this plan from the prior-year plans map
			this.priorYearActivePlansMap.remove( seqOptn.getBenefitPlan() );

			if( seqOptn.isOptnHSALevel0() && seqOptn.isPlanActive() ) {
				seqOptn.setDisplayOptSeq(new BigDecimal( seqNum++ ));
			}
		}

		// Resequencing.  Next, number plans that were turned off during this method
		for( BenDefnOptnHSA seqOptn : this.currHSAOptns ) {
			if( seqOptn.getDisplayOptSeq() == null && seqOptn.getEligRulesId() == null ) {
				seqOptn.turnOffHSA();
				seqOptn.setDisplayOptSeq(new BigDecimal( seqNum++ ));
			}
		}

		// add remaining prior year plans to current benefit program. required for payroll adjustments
		this.retainPriorPlans( effdt );

		this.resequenceInactive( seqNum );

		// Write HSA options back to the database.
		hsaPlansDao.updateHSAOptns( this.currHSAOptns );

	}


	/** 
	 * Level 5 and 7 HSA are customized plans created from the corresponding Level-0 plan.
	 * The custom plans are sequenced first.  Turn-off other plans and resequence after custom.
	 * @param company
	 * @param group
	 * @param hsaOptions
	 * @param effdt
	 */
	private void setupCustomHSA( Company company, BenefitGroup group, StrategyHsaFundingDto hsaOptions,
			java.sql.Date effdt ) {

		this.hsaOptions = hsaOptions;

		// #1 Select all the HSA plans and map BENEFIT_PLAN to the OPTN row
		this.currHSAOptns = hsaPlansDao.getAllHSAPlans( group.getBenefitProgram(), effdt );

		// if there are no HSA options for this client, exit
		if( CollectionUtils.isEmpty( this.currHSAOptns ) ) {
			return;
		}

		this.currHSAPlanMap = new HashMap<>();
		for( BenDefnOptnHSA optn : this.currHSAOptns ) {
			this.currHSAPlanMap.put( optn.getBenefitPlan(), optn );
		}

		// #2 Select all the activated medical plans, with corresponding ELIG_RULES_ID
		Map<String,String> medPlanEligMap = hsaPlansDao.getActiveMedPlans( group.getBenefitProgram(), effdt );

		// create the maps from all the HSA plans from the prior year
		this.buildPriorYearHSAMaps( group, this.currHSAOptns.get(0).getEffdt() );

		// This list will hold the newly created OPTN rows for custom plans until they can be added back to the
		// current OPTN list
		List<BenDefnOptnHSA> newCustomOptnList = new ArrayList<>();

		// for each current option, if associated with an active medical plan, determine whether a new
		// custom HSA plan should be created
		for( BenDefnOptnHSA hsaOptn : this.currHSAOptns ) {
			// Clear display sequence and option level for resequencing later
			hsaOptn.setDisplayOptSeq(null);
			hsaOptn.setOptionLvl(BigDecimal.ONE);

			// #3 Combine the #1 and #2 lists to find all the activated HSA plans
			if( medPlanEligMap.containsKey( hsaOptn.getCrossBenefPlan() ) ) {
				logger.info( "HSA plan {} selected.  Cross plan was {}.", hsaOptn.getBenefitPlan(), hsaOptn.getCrossBenefPlan() );
				// if LVL0...
				if( hsaOptn.isOptnHSALevel0() ) {
					// create a new instance of this OPTN row in order to build the custom plan
					BenDefnOptnHSA newCustomOptn = SerializationUtils.clone(hsaOptn);

					// #5 Use the new map to apply the correct plans and option codes to the LVL0 plans in the list in #1
					String newBenefitPlan = null;
					BenDefnOptnHSA lastYear = this.level5Map.get( newCustomOptn.getCrossBenefPlan() );
					if( null != lastYear ) {
						newBenefitPlan = lastYear.getBenefitPlan();
					} else {
						// #6 Any remaining LVL0 plans should be used to create a new LVL5 plan
						// first try to get benefit plan from previously mapped plans for this company
						newBenefitPlan = hsaPlanMapping.get( newCustomOptn.getBenefitPlan() );
						if( newBenefitPlan == null ) {
							// finally, if no previous plan was found, create a new plan code
							newBenefitPlan = this.nextBenefitPlan.execute();
						}
					}
					// make sure this pair of clone plan and unique plan are in the client's HSA map
					hsaPlanMapping.put( newCustomOptn.getBenefitPlan(), newBenefitPlan );

					// at this stage, newBenefitPlan contains the client's BENEFIT_PLAN for this HSA and
					// newCustomOptn.benefitPlan contains the clone benefit program BENEFIT_PLAN

					// Remove this BENEFIT_PLAN from the map of plans to be deactivated
					this.priorYearActivePlansMap.remove( newBenefitPlan );

					// #7 Create new rows in benefit plan and limit tables for the customer-specific plans. 
					setupPlanAttributes( newCustomOptn.getBenefitPlan(), newBenefitPlan, company.getPfClient(), newCustomOptn.getEffdt() );

					// now that plan attributes have been setup, the clone plan can be replaced by the new plan
					newCustomOptn.setOptionId(BigDecimal.ZERO);
					newCustomOptn.setBenefitPlan(newBenefitPlan);
					newCustomOptn.setOptionCd( new StringBuilder( newCustomOptn.getOptionCd().substring( 0, newCustomOptn.getOptionCd().length() - 1 ) )
							.append( "5" ).toString());
					newCustomOptn.setEligRulesId(medPlanEligMap.get( newCustomOptn.getCrossBenefPlan() ));
					newCustomOptn.setPfClient(company.getPfClient());

					// add new custom plan to custom plans list
					newCustomOptnList.add( newCustomOptn );
				}
			} else {
				// this HSA optn was not selected.  Should I turn it off?
				hsaOptn.setPfClient("OFF");
			}
		}

		// add remaining prior year plans to current benefit program. required for payroll adjustments
		this.retainPriorPlans( effdt );

		// add the new custom plans to the List of current HSA options
		this.currHSAOptns.addAll( newCustomOptnList );
		
		// Resequencing.  Number all customer-specific plans first.  That means all
		//  level 5 options that are not inactivated
		int seqNum = 1;
		for( BenDefnOptnHSA seqOptn : this.currHSAOptns ) {
			if( seqOptn.isOptnHSALevel5() && seqOptn.isPlanActive() ) {
				seqOptn.setDisplayOptSeq(new BigDecimal( seqNum++ ));
			}
		}

		// Resequence remaining plans
		this.resequenceInactive( seqNum );

		// update saved plan mappings
		hsaPlanMapping.saveAll();

		// at this stage, all plans have been created and attributes setup
		//  the benefit program options have been updated and resequenced
		//  and can be written back to the database.
		hsaPlansDao.updateHSAOptns( this.currHSAOptns );

	}


	/**
	 * When a new HSA benefit plan must be created, this method manages all the DAO calls to 
	 * define the new plan and corresponding limits
	 * @param clonePlan
	 * @param newPlan
	 * @param pfClient
	 * @param effdtStr
	 */
	private void setupPlanAttributes( String clonePlan, String newPlan, String pfClient, String effdtStr ) {
		logger.info( "setupPlanAttributes for {} using {} as clone.", newPlan, clonePlan );
		java.sql.Date effdt = java.sql.Date.valueOf( effdtStr ); 
		
		hsaPlansDao.updateBenefPlanTable( clonePlan, newPlan, pfClient, effdt );
		hsaPlansDao.updateFSABenefTable( clonePlan, newPlan, effdt );
		hsaPlansDao.deleteHSAContribLmt( newPlan, effdt );
		BigDecimal ee = hsaOptions.getMonthlyEeAmount();
		BigDecimal fam = hsaOptions.getMonthlyFamilyAmount();
		hsaPlansDao.insertHSAContribLmt( clonePlan, newPlan, ee, fam, effdt );
		hsaPlansDao.updateLimitIncludeTable( newPlan, effdt );
	}


	/**
	 * Given a benefit program and effective date, build the prior year HSA plan
	 * maps needed for deactivating prior year plans and matching medical plan to HSA plan
	 * and the prior EFFDT
	 * @param group
	 * @param currEffdtStr
	 * @return
	 */
	private void buildPriorYearHSAMaps( BenefitGroup group, String currEffdtStr ) {
		// Select all the HSA plans from the prior year
		java.sql.Date priorEffdt;
		try {
			priorEffdt = new java.sql.Date( DateUtils.getPreviousDay( DateUtils.convertStringToDate( currEffdtStr ) ).getTime() );
		} catch( Exception e ) {
			throw new BSSApplicationException( e, new BSSApplicationError( "Error parsing EFFDT" ) );
		}

		List<BenDefnOptnHSA> priorHSAOptns = hsaPlansDao.getAllHSAPlans( group.getBenefitProgram(), priorEffdt );
		logger.info( "Returned list with {} rows.", priorHSAOptns.size() );

		// Two maps will be build from the prior-year HSA options.
		// The first map will be initialized to the set of active plans from the prior year.
		// While plans are being setup, they will be removed from this map.  At the end of setup,
		// anything remaining in this map is a plan that was active last year and must be inserted
		// to the benefit program this year with ELIG_RULES_ID = "236Q"
		this.priorYearActivePlansMap = new HashMap<>();
		// The second map matches the medical plan (CROSS_BENEF_PLAN) to the custom HSA plan that
		// was previously created for it.  If that same medical plan is in the current benefit program
		// this map allows us to continue to use the matching HSA benefit plan.
		this.level5Map = new HashMap<>();

		for( BenDefnOptnHSA priorOptn : priorHSAOptns ){
			priorOptn.setDisplayOptSeq(null);
			priorOptn.setOptionLvl(BigDecimal.ONE);
			if( priorOptn.isPlanActive() ) {
				this.priorYearActivePlansMap.put( priorOptn.getBenefitPlan(), priorOptn );
			}
			if( priorOptn.isOptnHSALevel5() ) {
				this.level5Map.put( priorOptn.getCrossBenefPlan(), priorOptn );
			}
		}
	}


	/**
	 * Any plans remaining in the priorYearActivePlansMap must now be deactivated and
	 * added to the set of current options.  Don't add if the plan already exists in
	 * current options. Deactivate the row, set the EFFDT to current, and clear the
	 * OPTION_ID (it will be assigned in the SQL). Clear the display sequence fields
	 * so it can be reassigned during resequencing.
	 */
	private void retainPriorPlans( java.sql.Date effdt ) {
		for( Map.Entry<String,BenDefnOptnHSA> oldPlan : this.priorYearActivePlansMap.entrySet() ) {
			if( ! this.currHSAPlanMap.containsKey( oldPlan.getKey() ) ) {
				BenDefnOptnHSA priorPlan = oldPlan.getValue();
				priorPlan.deactivateHSA();
				priorPlan.setDisplayOptSeq(null);
				priorPlan.setEffdt(effdt.toString());
				priorPlan.setOptionId(BigDecimal.ZERO);
				this.currHSAOptns.add( priorPlan );
			}
		}
	}

	/**
	 * After the active plans have been sequenced, this method resequences and deactivates the remaining plans
	 * @param seqNum
	 */
	private void resequenceInactive( int seqNum ) {
		
		// Resequencing.  Next, turn off and number all community plans that are not inactivated
		for( BenDefnOptnHSA seqOptn : this.currHSAOptns ) {
			if( seqOptn.getDisplayOptSeq() == null && seqOptn.isPlanActive() ) {
				seqOptn.turnOffHSA();
				seqOptn.setDisplayOptSeq(new BigDecimal( seqNum++ ));
			}
		}

		// Resequencing.  Everything left over should be inactive plans
		for( BenDefnOptnHSA seqOptn : this.currHSAOptns ) {
			if( seqOptn.getDisplayOptSeq() == null ) {
				seqOptn.deactivateHSA();
				seqOptn.setDisplayOptSeq(new BigDecimal( seqNum++ ));
			}
		}

	}

}