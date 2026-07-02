package com.trinet.ambis.persistence.dao.ps.impl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenDefnCost;
import com.trinet.ambis.service.model.BenDefnOptn;
import com.trinet.ambis.service.model.BenDefnPlan;
import com.trinet.ambis.util.DaoUtils;

public class SavedPlanOptns {

	private static final Logger logger = LoggerFactory.getLogger(SavedPlanOptns.class);
	private static final BigDecimal NINES = new BigDecimal( "9999" );
	private static final BigDecimal FIFTY = new BigDecimal( "50" );

	// SQL parameters, names and display constants
	public static final String GET_CURRENT_FSA_PLANS = "GET_CURRENT_FSA_PLANS";
	public static final String GET_CURRENT_FSA_OPTNS = "GET_CURRENT_FSA_OPTNS";
	public static final String CLEAN_LEAVE_SVNGS_COST = "CLEAN_LEAVE_SVNGS_COST";
	public static final String CLEAN_LEAVE_SVNGS_OPTN = "CLEAN_LEAVE_SVNGS_OPTN";
	public static final String CLEAN_LEAVE_SVNGS_PLAN = "CLEAN_LEAVE_SVNGS_PLAN";
	public static final String GET_MAX_OPTION_COST = "GET_MAX_OPTION_COST";
	public static final String INSERT_SAVED_COST = "INSERT_SAVED_COST";
	public static final String INSERT_SAVED_OPTN = "INSERT_SAVED_OPTN";
	public static final String INSERT_SAVED_PLAN = "INSERT_SAVED_PLAN";
	public static final String GET_CURRENT_PLANS = "GET_CURRENT_PLANS";
	public static final String GET_BEN_DEFN_LEAVE_PLANS = "GET_BEN_DEFN_LEAVE_PLANS";
	public static final String GET_CURRENT_SAVINGS_OPTNS = "GET_CURRENT_SAVINGS_OPTNS";
	public static final String GET_CURRENT_SAVINGS_PLANS = "GET_CURRENT_SAVINGS_PLANS";
	public static final String DISPLAY_OPT_SEQ = "displayOptSeq";
	public static final String OPTION_TYPE = "optionType";
	public static final String BENEFIT_PLAN = "benefitPlan";
	public static final String COVRG_CODE = "covrgCd";
	public static final String OPTION_CODE = "optionCd";
	public static final String OPTION_LVL = "optionLvl";
	public static final String DEDCD = "dedcd";
	public static final String DFLT_OPTION_IND = "dfltOptionInd";
	public static final String LOCALTION_TBL_ID = "locationTblId";
	public static final String CROSS_PLAN_TYPE = "crossPlanType";
	public static final String CROSS_BENEF_PLAN = "crossBenefPlan";
	public static final String COVERAGE_LIMIT_PCT = "coverageLimitPct";
	public static final String CROSS_PLN_DPND_CHK = "crossPlnDpndChk";
	public static final String COST_ID = "costId";
	public static final String COST_TYPE = "costType";
	public static final String ERN_CD = "erncd";
	public static final String RATE_TYPE = "rateType";
	public static final String CALC_RULES_ID = "calcRulesId";
	public static final String EFF_DT = "effdt";
	public static final String DISPLAY_PLAN_SEQ = "displayPlnSeq";
	public static final String MIN_ANNUAL_CONTRIB = "minAnnualContrib";
	public static final String MIX_ANNUAL_CONTRIB = "maxAnnualContrib";
	public static final String WAIVE_COVERAGE = "waiveCoverage";
	public static final String RESTRICT_ENTRY_MN = "restrictEntryMm";
	public static final String EVENT_RULES_ID = "eventRulesId";
	public static final String COBRA_PLAN = "cobraPlan";
	public static final String HIPAA_PLAN = "hipaaPlan";
	public static final String COLLECT_DEP_BEN = "collectDepben";
	public static final String COLLECT_FUNDS = "collectFunds";
	public static final String SHOW_PLAN_TYPE = "showPlanType";
	public static final String HANDBOOK_URL_ID = "handbookUrlId";
	public static final String DEP_RULE_ID = "depRuleId";
	

	private Company company;
	private String effdtStr;
	private EntityManager em;
	private Set<BenDefnPlan> leavePlanRows;
	private Set<BenDefnOptn> leaveOptnRows;
	private Set<BenDefnPlan> savingsPlanRows;
	private Set<BenDefnOptn> savingsOptnRows;
	private Set<BenDefnPlan> fsaPlanRows;
	private Set<BenDefnOptn> fsaOptnRows;

	public SavedPlanOptns( Company company, String effdtStr, EntityManager em ) {
		this.company = company;
		this.effdtStr = effdtStr;
		this.em = em;
	}

	/**
	 * Tells whether leave plans have been saved in this object.
	 * @return boolean flag set to true if leave plans have been saved
	 */
	public boolean areLeavePlansSaved() {
		return ( this.leaveOptnRows ==  null ) ? false : ( ! this.leaveOptnRows.isEmpty() );
	}

	/**
	 * Tells whether savings plans have been saved in this object.
	 * @return boolean flag set to true if savings plans have been saved
	 */
	public boolean areSavingsPlansSaved() {
		return ( this.savingsOptnRows ==  null ) ? false : ( ! this.savingsOptnRows.isEmpty() );
	}

	/**
	 * Tells whether FSA plans have been saved in this object.
	 * @return boolean flag set to true if FSA plans have been saved
	 */
	public boolean areFsaPlansSaved() {
		return ( this.fsaOptnRows ==  null ) ? false : ( ! this.fsaOptnRows.isEmpty() );
	}

	/**
	 * Calls other methods to save certain current benefit plans
	 * @return boolean indicating whether Leave or Savings plan data was saved
	 */
	public boolean saveCurrentPlans() {
		this.saveFsaPlans();
		this.saveLeavePlans();
		this.saveSavingsPlans();
		return this.areLeavePlansSaved() || this.areSavingsPlansSaved();
	}

	/**
	 * Run an SQL statement to get the current leave plans and save in a collection
	 * @return boolean indicating whether plan data was saved
	 */
	public boolean saveLeavePlans() {

		this.leaveOptnRows = new HashSet<>();

		Query query = this.em.createNamedQuery( GET_CURRENT_PLANS );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, company.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, this.effdtStr );
		List<Object[]> list = DaoUtils.getResultList(query, GET_CURRENT_PLANS);

		BigDecimal displayOptionSeq = BigDecimal.ZERO;
		String priorPlanType = " ";

		for( Object[] row : list ) {
			BenDefnOptn optn = new BenDefnOptn();
			optn.setPlanType((String) row[0]);
			optn.setOptionId(BigDecimal.ZERO);

			if( ! priorPlanType.equals( optn.getPlanType() ) ) {
				displayOptionSeq = BigDecimal.ZERO;
				priorPlanType = optn.getPlanType();
			}

			optn.setOptionType((String) row[1]);
			if( "W".equals( optn.getOptionType() ) ) {
				optn.setDisplayOptSeq(NINES);
				optn.setOptionLvl((BigDecimal) row[5]);
			} else {
				displayOptionSeq = displayOptionSeq.add( BigDecimal.ONE );
				optn.setDisplayOptSeq(displayOptionSeq);
				optn.setOptionLvl(FIFTY);
			}

			optn.setBenefitPlan((String) row[2]);
			optn.setCovrgCd((String) row[3]);
			optn.setOptionCd((String) row[4]);
			optn.setDedcd((String) row[6]);
			optn.setDfltOptionInd((String) row[7]);
			optn.setEligRulesId((String) row[8]);
			optn.setLocationTblId((String) row[9]);
			optn.setCrossPlanType((String) row[10]);
			optn.setCrossBenefPlan((String) row[11]);
			optn.setCoverageLimitPct((BigDecimal) row[12]);
			optn.setCrossPlnDpndChk((String) row[13]);

			this.leaveOptnRows.add( optn );
			logger.debug( "plan type:{} :benefit plan:{} :elig rule:{}", optn.getPlanType(), optn.getBenefitPlan() , optn.getEligRulesId() );
		}

		this.leavePlanRows = new HashSet<>();

		Query query1 = this.em.createNamedQuery( GET_BEN_DEFN_LEAVE_PLANS );
		query1.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, company.getBenefitProgram() );
		query1.setParameter( BSSQueryConstants.EFF_DATE_STR, this.effdtStr );
		List<Object[]> list1 = DaoUtils.getResultList(query1, GET_BEN_DEFN_LEAVE_PLANS );

		for( Object[] row : list1 ) {
			BenDefnPlan plan = new BenDefnPlan();
			plan.setPlanType( (String) row[0] );
			plan.setDisplayPlnSeq( (String) row[1] );
			plan.setMinAnnualContrib( (BigDecimal) row[2] );
			plan.setMaxAnnualContrib( (BigDecimal) row[3] );
			plan.setWaiveCoverage( (String) row[4] );
			plan.setRestrictEntryMm( (BigDecimal) row[5] );
			plan.setEventRulesId( (String) row[6] );
			plan.setCobraPlan( (String) row[7] );
			plan.setHipaaPlan( (String) row[8] );
			plan.setCollectDepben( (String) row[9] );
			plan.setCollectFunds( (String) row[10] );
			plan.setShowPlanType( (String) row[11] );
			plan.setHandbookUrlId( (String) row[12] );
			plan.setDepRuleId( (String) row[13] );

			this.leavePlanRows.add( plan );
		}

		return ( !this.leaveOptnRows.isEmpty() );
	}


	/**
	 * Run an SQL statement to get and save the current savings plans.  Map these to the
	 * corresponding benefit program.  Later they can be restored to the same benefit program.
	 * @return boolean flag indicating whether data was saved
	 */
	private boolean saveSavingsPlans() {

		// get and save the PLAN rows
		this.savingsPlanRows = new HashSet<>();
		Query planQuery = this.em.createNamedQuery( GET_CURRENT_SAVINGS_PLANS );
		planQuery.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, company.getBenefitProgram() );
		planQuery.setParameter( BSSQueryConstants.EFF_DATE_STR, this.effdtStr );
		List<Object[]> list = DaoUtils.getResultList(planQuery, GET_CURRENT_SAVINGS_PLANS);

		for( Object[] row : list ) {
			BenDefnPlan plan = new BenDefnPlan();
			plan.setBenefitProgram( (String) row[0] );
			plan.setPlanType( (String) row[1] );
			plan.setDisplayPlnSeq( (String) row[2] );
			plan.setMinAnnualContrib( (BigDecimal) row[3] );
			plan.setMaxAnnualContrib( (BigDecimal) row[4] );
			plan.setWaiveCoverage( (String) row[5] );
			plan.setRestrictEntryMm( (BigDecimal) row[6] );
			plan.setEventRulesId( (String) row[7] );
			plan.setCobraPlan( (String) row[8] );
			plan.setHipaaPlan( (String) row[9] );
			plan.setCollectDepben( (String) row[10] );
			plan.setCollectFunds( (String) row[11] );
			plan.setShowPlanType( (String) row[12] );
			plan.setHandbookUrlId( (String) row[13] );
			plan.setDepRuleId( (String) row[14] );

			this.savingsPlanRows.add( plan );
		}


		// get and save the OPTN rows
		this.savingsOptnRows = new HashSet<>();
		Query query = this.em.createNamedQuery( GET_CURRENT_SAVINGS_OPTNS );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, company.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, this.effdtStr );
		List<Object[]> optList = DaoUtils.getResultList(query, GET_CURRENT_SAVINGS_OPTNS);

		BigDecimal displayOptionSeq = BigDecimal.ZERO;
		String priorPlanType = " ";

		for( Object[] row : optList ) {
			BenDefnOptn optn = new BenDefnOptn();
			optn.setPlanType((String) row[1]);
			optn.setOptionId(BigDecimal.ZERO);

			if( ! priorPlanType.equals( optn.getPlanType() ) ) {
				displayOptionSeq = BigDecimal.ZERO;
				priorPlanType = optn.getPlanType();
			}

			optn.setOptionType((String) row[4]);
			if( "W".equals( optn.getOptionType() ) ) {
				optn.setDisplayOptSeq(NINES);
				optn.setOptionLvl((BigDecimal) row[8]);
			} else {
				displayOptionSeq = displayOptionSeq.add( BigDecimal.ONE );
				optn.setDisplayOptSeq(displayOptionSeq);
				optn.setOptionLvl(FIFTY);
			}

			optn.setBenefitPlan((String) row[5]);
			optn.setCovrgCd((String) row[6]);
			optn.setOptionCd((String) row[7]);
			optn.setDedcd((String) row[9]);
			optn.setDfltOptionInd((String) row[10]);
			optn.setEligRulesId((String) row[11]);
			optn.setLocationTblId((String) row[12]);
			optn.setCrossPlanType((String) row[13]);
			optn.setCrossBenefPlan((String) row[14]);
			optn.setCoverageLimitPct((BigDecimal) row[15]);
			optn.setCrossPlnDpndChk((String) row[16]);

			BigDecimal costId = (BigDecimal) row[17];
			if( costId != null ) {
				BenDefnCost benDefnCost = new BenDefnCost();
				benDefnCost.setCostType((String) row[18]);
				benDefnCost.setErncd((String) row[19]);
				benDefnCost.setRateType((String) row[20]);
				benDefnCost.setRateTblId((String) row[21]);
				benDefnCost.setCalcRulesId((String) row[22]);
				optn.setBenDefnCost(benDefnCost);
			}

			this.savingsOptnRows.add( optn );
			
			logger.debug( "benefit program:{} :plan type:{} :benefit plan:{} :elig rule:{}", optn.getBenefitProgram(), optn.getPlanType(), optn.getBenefitPlan() , optn.getEligRulesId() );

		}
		return ( !this.savingsOptnRows.isEmpty() );
	}


	/**
	 * Run an SQL statement to get and save the current FSA plans.
	 * @return boolean flag indicating whether data was saved
	 */
	private boolean saveFsaPlans() {

		// get and save the PLAN rows
		this.fsaPlanRows= new HashSet<>();
		Query planQuery = this.em.createNamedQuery( GET_CURRENT_FSA_PLANS );
		planQuery.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, company.getBenefitProgram() );
		planQuery.setParameter( BSSQueryConstants.EFF_DATE_STR, this.effdtStr );
		List<Object[]> list = DaoUtils.getResultList( planQuery, GET_CURRENT_FSA_PLANS );

		for( Object[] row : list ) {
			BenDefnPlan plan = new BenDefnPlan();
			plan.setBenefitProgram( (String) row[0] );
			plan.setPlanType( (String) row[1] );
			plan.setDisplayPlnSeq( (String) row[2] );
			plan.setMinAnnualContrib( (BigDecimal) row[3] );
			plan.setMaxAnnualContrib( (BigDecimal) row[4] );
			plan.setWaiveCoverage( (String) row[5] );
			plan.setRestrictEntryMm( (BigDecimal) row[6] );
			plan.setEventRulesId( (String) row[7] );
			plan.setCobraPlan( (String) row[8] );
			plan.setHipaaPlan( (String) row[9] );
			plan.setCollectDepben( (String) row[10] );
			plan.setCollectFunds( (String) row[11] );
			plan.setShowPlanType( (String) row[12] );
			plan.setHandbookUrlId( (String) row[13] );
			plan.setDepRuleId( (String) row[14] );

			this.fsaPlanRows.add( plan );
		}


		// get and save the OPTN rows
		this.fsaOptnRows = new HashSet<>();
		Query query = this.em.createNamedQuery( GET_CURRENT_FSA_OPTNS );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, company.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, this.effdtStr );
		List<Object[]> optList = DaoUtils.getResultList( query, GET_CURRENT_FSA_OPTNS );

		for( Object[] row : optList ) {
			BenDefnOptn optn = new BenDefnOptn();
			optn.setPlanType((String) row[1]);
			optn.setOptionId( (BigDecimal) row[2] );
			optn.setDisplayOptSeq( (BigDecimal) row[3] );
			optn.setOptionType((String) row[4]);
			optn.setBenefitPlan((String) row[5]);
			optn.setCovrgCd((String) row[6]);
			optn.setOptionCd((String) row[7]);
			optn.setOptionLvl((BigDecimal) row[8]);
			optn.setDedcd((String) row[9]);
			optn.setDfltOptionInd((String) row[10]);
			optn.setEligRulesId((String) row[11]);
			optn.setLocationTblId((String) row[12]);
			optn.setCrossPlanType((String) row[13]);
			optn.setCrossBenefPlan((String) row[14]);
			optn.setCoverageLimitPct((BigDecimal) row[15]);
			optn.setCrossPlnDpndChk((String) row[16]);

			BigDecimal costId = (BigDecimal) row[17];
			if( costId != null ) {
				BenDefnCost benDefnCost = new BenDefnCost();
				benDefnCost.setCostId( costId );
				benDefnCost.setCostType((String) row[18]);
				benDefnCost.setErncd((String) row[19]);
				benDefnCost.setRateType((String) row[20]);
				benDefnCost.setRateTblId((String) row[21]);
				benDefnCost.setCalcRulesId((String) row[22]);
				optn.setBenDefnCost(benDefnCost);
			}

			this.fsaOptnRows.add( optn );

		}
		return ( ! this.fsaOptnRows.isEmpty() );
	}

	/**
	 * Inserts the saved LEAVE rows into the requested benefit program for the effective date saved in this object
	 * @param benefitProgram
	 * @return the number of rows inserted
	 */
	public int restoreLeavePlans( String benefitProgram ) {
		return this.restoreLeavePlans( benefitProgram, this.effdtStr );
	}

	/**
	 * Inserts the saved LEAVE rows into the requested benefit program for the requested effective date
	 * @param benefitProgram
	 * @param effdtString
	 * @return the number of rows inserted
	 */
	public int restoreLeavePlans( String benefitProgram, String effdtString ) {
		logger.debug("RESTORE LEAVE PLANS : {}\t DATE : {}", benefitProgram, effdtString );

		// get me a list of all the plan types for which OPTN rows were saved/restored
		Set<String> planTypes = this.getSavedLeavePlanTypes();


		final String leaveRegex = "5.";
		cleanPlanRows( benefitProgram, effdtString, leaveRegex );
		cleanOptnRows( benefitProgram, effdtString, leaveRegex );
		cleanCostRows( benefitProgram, effdtString, leaveRegex );

		// get current max option_id and cost_id
		BigDecimal maxOptionId = BigDecimal.ZERO;
		BigDecimal maxCostId = BigDecimal.ZERO;
		Query maxOptionCost = this.em.createNamedQuery( GET_MAX_OPTION_COST );
		maxOptionCost.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
		maxOptionCost.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtString );
		List<Object[]> maxId = DaoUtils.getResultList(maxOptionCost, GET_MAX_OPTION_COST);
		for( Object[] row : maxId ) {
			maxOptionId = (BigDecimal) row[0];
			maxCostId = (BigDecimal) row[1];
		}


		int numRows = 0;
		Query insertOptn = this.em.createNamedQuery(INSERT_SAVED_OPTN);
		Query insertCost = this.em.createNamedQuery(INSERT_SAVED_COST);

		// first restore the leave plan type rows
		for( BenDefnOptn optn : this.leaveOptnRows ) {
			insertOptn.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
			insertOptn.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtString );
			insertOptn.setParameter( BSSQueryConstants.PLAN_TYPE, optn.getPlanType() );
			maxOptionId = maxOptionId.add( BigDecimal.ONE );
			insertOptn.setParameter( BSSQueryConstants.OPTION_ID, maxOptionId );
			insertOptn.setParameter( DISPLAY_OPT_SEQ, optn.getDisplayOptSeq() );
			insertOptn.setParameter( OPTION_TYPE, optn.getOptionType() );
			insertOptn.setParameter( BENEFIT_PLAN, optn.getBenefitPlan() );
			insertOptn.setParameter( COVRG_CODE, optn.getCovrgCd() );
			insertOptn.setParameter( OPTION_CODE, optn.getOptionCd() );
			insertOptn.setParameter( OPTION_LVL, optn.getOptionLvl() );
			insertOptn.setParameter( DEDCD, optn.getDedcd() );
			insertOptn.setParameter( DFLT_OPTION_IND, optn.getDfltOptionInd() );
			insertOptn.setParameter( BSSQueryConstants.ELIG_RULES_ID, optn.getEligRulesId() );
			insertOptn.setParameter( LOCALTION_TBL_ID, optn.getLocationTblId() );
			insertOptn.setParameter( CROSS_PLAN_TYPE, optn.getCrossPlanType() );
			insertOptn.setParameter( CROSS_BENEF_PLAN, optn.getCrossBenefPlan() );
			insertOptn.setParameter( COVERAGE_LIMIT_PCT, optn.getCoverageLimitPct() );
			insertOptn.setParameter( CROSS_PLN_DPND_CHK, optn.getCrossPlnDpndChk() );
			numRows += DaoUtils.executeUpdate(insertOptn, INSERT_SAVED_OPTN);

			if( optn.getBenDefnCost() != null ) {
				insertCost.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
				insertCost.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtString );
				insertCost.setParameter( BSSQueryConstants.PLAN_TYPE, optn.getPlanType() );
				insertCost.setParameter( BSSQueryConstants.OPTION_ID, maxOptionId );
				maxCostId = maxCostId.add( BigDecimal.ONE );
				insertCost.setParameter( COST_ID, maxCostId );
				insertCost.setParameter( COST_TYPE, optn.getBenDefnCost().getCostType() );
				insertCost.setParameter( ERN_CD, optn.getBenDefnCost().getErncd() );
				insertCost.setParameter( RATE_TYPE, optn.getBenDefnCost().getRateType() );
				insertCost.setParameter( BSSQueryConstants.RATE_TBL_ID, optn.getBenDefnCost().getRateTblId() );
				insertCost.setParameter( CALC_RULES_ID, optn.getBenDefnCost().getCalcRulesId() );
				numRows += DaoUtils.executeUpdate(insertCost, INSERT_SAVED_COST);
			}
		}


		Query insertPlan = this.em.createNamedQuery(INSERT_SAVED_PLAN);

		for (BenDefnPlan plan : this.leavePlanRows) {
			// only restore this PLAN row if there were OPTN rows for the plan type
			if( planTypes.contains( plan.getPlanType() )) {
				insertPlan.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram);
				insertPlan.setParameter(EFF_DT, effdtString );
				insertPlan.setParameter(BSSQueryConstants.PLAN_TYPE, plan.getPlanType());
				insertPlan.setParameter(DISPLAY_PLAN_SEQ, plan.getDisplayPlnSeq());
				insertPlan.setParameter(MIN_ANNUAL_CONTRIB, plan.getMinAnnualContrib());
				insertPlan.setParameter(MIX_ANNUAL_CONTRIB, plan.getMaxAnnualContrib());
				insertPlan.setParameter(WAIVE_COVERAGE, plan.getWaiveCoverage());
				insertPlan.setParameter(RESTRICT_ENTRY_MN, plan.getRestrictEntryMm());
				insertPlan.setParameter(EVENT_RULES_ID, plan.getEventRulesId());
				insertPlan.setParameter(COBRA_PLAN, plan.getCobraPlan());
				insertPlan.setParameter(HIPAA_PLAN, plan.getHipaaPlan());
				insertPlan.setParameter(COLLECT_DEP_BEN, plan.getCollectDepben());
				insertPlan.setParameter(COLLECT_FUNDS, plan.getCollectFunds());
				insertPlan.setParameter(SHOW_PLAN_TYPE, plan.getShowPlanType());
				insertPlan.setParameter(HANDBOOK_URL_ID, plan.getHandbookUrlId());
				insertPlan.setParameter(DEP_RULE_ID, plan.getDepRuleId());

				numRows += DaoUtils.executeUpdate(insertPlan, INSERT_SAVED_PLAN);
			}
		}
		return numRows;
	}


	/**
	 * Inserts the saved SAVINGS rows into the requested benefit program for the effective
	 * date saved in this object.
	 * @param benefitProgram
	 * @return the number of rows inserted
	 */
	public int restoreSavingsPlans( String benefitProgram ) {
		return this.restoreSavingsPlans( benefitProgram, this.effdtStr );
	}

	/**
	 * Inserts the saved SAVINGS rows into the requested benefit program for the requested
	 * effective date.
	 * @param benefitProgram
	 * @param effdtString
	 * @return
	 */
	public int restoreSavingsPlans( String benefitProgram, String effdtString ) {

		final String savingsRegex = "4.";
		cleanPlanRows( benefitProgram, effdtString, savingsRegex );
		cleanOptnRows( benefitProgram, effdtString, savingsRegex );
		cleanCostRows( benefitProgram, effdtString, savingsRegex );

		// get current max option_id and cost_id
		BigDecimal maxOptionId = BigDecimal.ZERO;
		BigDecimal maxCostId = BigDecimal.ZERO;
		Query maxOptionCost = this.em.createNamedQuery( GET_MAX_OPTION_COST );
		maxOptionCost.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
		maxOptionCost.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtString );
		List<Object[]> maxId = DaoUtils.getResultList(maxOptionCost, GET_MAX_OPTION_COST);
		for( Object[] row : maxId ) {
			maxOptionId = (BigDecimal) row[0];
			maxCostId = (BigDecimal) row[1];
		}

		int numRows = 0;
		Query insertPlan = this.em.createNamedQuery(INSERT_SAVED_PLAN);
		Query insertOptn = this.em.createNamedQuery(INSERT_SAVED_OPTN);
		Query insertCost = this.em.createNamedQuery(INSERT_SAVED_COST);

		// next, restore the savings plan type PLAN rows and retain the relationship
		// to the benefit program
		for( BenDefnPlan plan : this.savingsPlanRows ) {
			insertPlan.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
			insertPlan.setParameter( EFF_DT, effdtString );
			insertPlan.setParameter( BSSQueryConstants.PLAN_TYPE, plan.getPlanType() );
			insertPlan.setParameter( DISPLAY_PLAN_SEQ, plan.getDisplayPlnSeq() );
			insertPlan.setParameter( MIN_ANNUAL_CONTRIB, plan.getMinAnnualContrib() );
			insertPlan.setParameter( MIX_ANNUAL_CONTRIB, plan.getMaxAnnualContrib() );
			insertPlan.setParameter( WAIVE_COVERAGE, plan.getWaiveCoverage() );
			insertPlan.setParameter( RESTRICT_ENTRY_MN, plan.getRestrictEntryMm() );
			insertPlan.setParameter( EVENT_RULES_ID, plan.getEventRulesId() );
			insertPlan.setParameter( COBRA_PLAN, plan.getCobraPlan() );
			insertPlan.setParameter( HIPAA_PLAN, plan.getHipaaPlan() );
			insertPlan.setParameter( COLLECT_DEP_BEN, plan.getCollectDepben() );
			insertPlan.setParameter( COLLECT_FUNDS, plan.getCollectFunds() );
			insertPlan.setParameter( SHOW_PLAN_TYPE, plan.getShowPlanType() );
			insertPlan.setParameter( HANDBOOK_URL_ID, plan.getHandbookUrlId() );
			insertPlan.setParameter( DEP_RULE_ID, plan.getDepRuleId() );

			numRows += DaoUtils.executeUpdate(insertPlan, INSERT_SAVED_PLAN);
		}


		// next, restore the savings plan type OPTN rows and retain the relationship
		// to the benefit program
		for( BenDefnOptn optn : this.savingsOptnRows ) {
			insertOptn.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
			insertOptn.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtString );
			insertOptn.setParameter( BSSQueryConstants.PLAN_TYPE, optn.getPlanType() );
			maxOptionId = maxOptionId.add( BigDecimal.ONE );
			insertOptn.setParameter( BSSQueryConstants.OPTION_ID, maxOptionId );
			insertOptn.setParameter( DISPLAY_OPT_SEQ, optn.getDisplayOptSeq() );
			insertOptn.setParameter( OPTION_TYPE, optn.getOptionType() );
			insertOptn.setParameter( BENEFIT_PLAN, optn.getBenefitPlan() );
			insertOptn.setParameter( COVRG_CODE, optn.getCovrgCd() );
			insertOptn.setParameter( OPTION_CODE, optn.getOptionCd() );
			insertOptn.setParameter( OPTION_LVL, optn.getOptionLvl() );
			insertOptn.setParameter( DEDCD, optn.getDedcd() );
			insertOptn.setParameter( DFLT_OPTION_IND, optn.getDfltOptionInd() );
			insertOptn.setParameter( BSSQueryConstants.ELIG_RULES_ID, optn.getEligRulesId() );
			insertOptn.setParameter( LOCALTION_TBL_ID, optn.getLocationTblId() );
			insertOptn.setParameter( CROSS_PLAN_TYPE, optn.getCrossPlanType() );
			insertOptn.setParameter( CROSS_BENEF_PLAN, optn.getCrossBenefPlan() );
			insertOptn.setParameter( COVERAGE_LIMIT_PCT, optn.getCoverageLimitPct() );
			insertOptn.setParameter( CROSS_PLN_DPND_CHK, optn.getCrossPlnDpndChk() );
			numRows += DaoUtils.executeUpdate(insertOptn, INSERT_SAVED_OPTN);

			if( optn.getBenDefnCost() != null ) {
				insertCost.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
				insertCost.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtString );
				insertCost.setParameter( BSSQueryConstants.PLAN_TYPE, optn.getPlanType() );
				insertCost.setParameter( BSSQueryConstants.OPTION_ID, maxOptionId );
				maxCostId = maxCostId.add( BigDecimal.ONE );
				insertCost.setParameter( COST_ID, maxCostId );
				insertCost.setParameter( COST_TYPE, optn.getBenDefnCost().getCostType() );
				insertCost.setParameter( ERN_CD, optn.getBenDefnCost().getErncd() );
				insertCost.setParameter( RATE_TYPE, optn.getBenDefnCost().getRateType() );
				insertCost.setParameter( BSSQueryConstants.RATE_TBL_ID, optn.getBenDefnCost().getRateTblId() );
				insertCost.setParameter( CALC_RULES_ID, optn.getBenDefnCost().getCalcRulesId() );
				numRows += DaoUtils.executeUpdate(insertCost, INSERT_SAVED_COST);
			}
		}
		return numRows;
	}


	/**
	 * Inserts the saved FSA rows into the requested benefit program for the effective
	 * date saved in this object.
	 * @param benefitProgram
	 * @return the number of rows inserted
	 */
	public int restoreFsaPlans( String benefitProgram ) {
		return this.restoreFsaPlans( benefitProgram, this.effdtStr );
	}

	/**
	 * Inserts the saved FSA rows into the requested benefit program for the requested
	 * effective date.
	 * @param benefitProgram
	 * @param effdtString
	 * @return
	 */
	public int restoreFsaPlans( String benefitProgram, String effdtString ) {

		final String fsaRegex = "6(0|1)";
		cleanPlanRows( benefitProgram, effdtString, fsaRegex );
		cleanOptnRows( benefitProgram, effdtString, fsaRegex );
		cleanCostRows( benefitProgram, effdtString, fsaRegex );

		int numRows = 0;
		Query insertPlan = this.em.createNamedQuery(INSERT_SAVED_PLAN);
		Query insertOptn = this.em.createNamedQuery(INSERT_SAVED_OPTN);
		Query insertCost = this.em.createNamedQuery(INSERT_SAVED_COST);

		// restore the PLAN rows and retain the relationship to the benefit program
		for( BenDefnPlan plan : this.fsaPlanRows ) {
			insertPlan.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
			insertPlan.setParameter( EFF_DT, effdtString );
			insertPlan.setParameter( BSSQueryConstants.PLAN_TYPE, plan.getPlanType() );
			insertPlan.setParameter( DISPLAY_PLAN_SEQ, plan.getDisplayPlnSeq() );
			insertPlan.setParameter( MIN_ANNUAL_CONTRIB, plan.getMinAnnualContrib() );
			insertPlan.setParameter( MIX_ANNUAL_CONTRIB, plan.getMaxAnnualContrib() );
			insertPlan.setParameter( WAIVE_COVERAGE, "X" );
			insertPlan.setParameter( RESTRICT_ENTRY_MN, plan.getRestrictEntryMm() );
			insertPlan.setParameter( EVENT_RULES_ID, plan.getEventRulesId() );
			insertPlan.setParameter( COBRA_PLAN, plan.getCobraPlan() );
			insertPlan.setParameter( HIPAA_PLAN, plan.getHipaaPlan() );
			insertPlan.setParameter( COLLECT_DEP_BEN, plan.getCollectDepben() );
			insertPlan.setParameter( COLLECT_FUNDS, plan.getCollectFunds() );
			insertPlan.setParameter( SHOW_PLAN_TYPE, plan.getShowPlanType() );
			insertPlan.setParameter( HANDBOOK_URL_ID, plan.getHandbookUrlId() );
			insertPlan.setParameter( DEP_RULE_ID, plan.getDepRuleId() );

			numRows += DaoUtils.executeUpdate(insertPlan, INSERT_SAVED_PLAN);
		}


		// restore the OPTN rows and retain the relationship to the benefit program
		for( BenDefnOptn optn : this.fsaOptnRows ) {
			if( optn.isWaive() ) {
				// skip this waive row
			} else {
				insertOptn.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
				insertOptn.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtString );
				insertOptn.setParameter( BSSQueryConstants.PLAN_TYPE, optn.getPlanType() );
				insertOptn.setParameter( BSSQueryConstants.OPTION_ID, optn.getOptionId() );
				insertOptn.setParameter( DISPLAY_OPT_SEQ, optn.getDisplayOptSeq() );
				insertOptn.setParameter( OPTION_TYPE, optn.getOptionType() );
				insertOptn.setParameter( BENEFIT_PLAN, optn.getBenefitPlan() );
				insertOptn.setParameter( COVRG_CODE, optn.getCovrgCd() );
				insertOptn.setParameter( OPTION_CODE, optn.getOptionCd() );
				insertOptn.setParameter( OPTION_LVL, optn.getOptionLvl() );
				insertOptn.setParameter( DEDCD, optn.getDedcd() );
				insertOptn.setParameter( DFLT_OPTION_IND, optn.getDfltOptionInd() );
				insertOptn.setParameter( BSSQueryConstants.ELIG_RULES_ID, "2009" );
				insertOptn.setParameter( LOCALTION_TBL_ID, optn.getLocationTblId() );
				insertOptn.setParameter( CROSS_PLAN_TYPE, optn.getCrossPlanType() );
				insertOptn.setParameter( CROSS_BENEF_PLAN, optn.getCrossBenefPlan() );
				insertOptn.setParameter( COVERAGE_LIMIT_PCT, optn.getCoverageLimitPct() );
				insertOptn.setParameter( CROSS_PLN_DPND_CHK, optn.getCrossPlnDpndChk() );
				numRows += DaoUtils.executeUpdate(insertOptn, INSERT_SAVED_OPTN);

				if( optn.getBenDefnCost() != null ) {
					insertCost.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
					insertCost.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtString );
					insertCost.setParameter( BSSQueryConstants.PLAN_TYPE, optn.getPlanType() );
					insertCost.setParameter( BSSQueryConstants.OPTION_ID, optn.getOptionId() );
					insertCost.setParameter( COST_ID, optn.getBenDefnCost().getCostId() );
					insertCost.setParameter( COST_TYPE, optn.getBenDefnCost().getCostType() );
					insertCost.setParameter( ERN_CD, optn.getBenDefnCost().getErncd() );
					insertCost.setParameter( RATE_TYPE, optn.getBenDefnCost().getRateType() );
					insertCost.setParameter( BSSQueryConstants.RATE_TBL_ID, optn.getBenDefnCost().getRateTblId() );
					insertCost.setParameter( CALC_RULES_ID, optn.getBenDefnCost().getCalcRulesId() );
					numRows += DaoUtils.executeUpdate(insertCost, INSERT_SAVED_COST);
				}
			}
		}
		return numRows;
	}

	/**
	 * A private method to get the Set of leave PLAN_TYPE values saved by the object.
	 * @return the Set of plan type values for which leave OPTN rows were saved
	 */
	private Set<String> getSavedLeavePlanTypes() {
		Set<String> planTypes = new HashSet<>();
		// ensure collection is not empty or SQL error ORA-00936 will result
		planTypes.add( ".." );
		for( BenDefnOptn optn : this.leaveOptnRows ) {
			planTypes.add( optn.getPlanType() );
		}
		return planTypes;
	}


	/**
	 * Clean rows from PLAN table about to be reinserted
	 */
	private void cleanPlanRows( String benProg, String effdtStr, String regex ) {
		// Prepare a clean space to insert savings PLAN and OPTN rows
		Query cleanSavingsPlans = this.em.createNamedQuery( CLEAN_LEAVE_SVNGS_PLAN );
		cleanSavingsPlans.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benProg );
		cleanSavingsPlans.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		cleanSavingsPlans.setParameter( BSSQueryConstants.PLAN_TYPE_PATTERN, regex );
		int num = DaoUtils.executeUpdate(cleanSavingsPlans, CLEAN_LEAVE_SVNGS_PLAN);
		logger.debug("NUMBER OF ROWS DELETED FROM BEN_DEFN_PLAN : {}", num);
	}

	/**
	 * Clean rows from OPTN table about to be reinserted
	 */
	private void cleanOptnRows( String benProg, String effdtStr, String regex ) {
		Query cleanSavingsOptions = this.em.createNamedQuery( CLEAN_LEAVE_SVNGS_OPTN );
		cleanSavingsOptions.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benProg );
		cleanSavingsOptions.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		cleanSavingsOptions.setParameter( BSSQueryConstants.PLAN_TYPE_PATTERN, regex );
		int num = DaoUtils.executeUpdate(cleanSavingsOptions, CLEAN_LEAVE_SVNGS_OPTN);
		logger.debug("NUMBER OF ROWS DELETED FROM BEN_DEFN_OPTN : {}", num );
	}

	/**
	 * Clean rows from COST table about to be reinserted
	 */
	private void cleanCostRows( String benProg, String effdtStr, String regex ) {
		Query cleanSavingsCost = this.em.createNamedQuery( CLEAN_LEAVE_SVNGS_COST );
		cleanSavingsCost.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benProg );
		cleanSavingsCost.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		cleanSavingsCost.setParameter( BSSQueryConstants.PLAN_TYPE_PATTERN, regex );
		int num = DaoUtils.executeUpdate(cleanSavingsCost, CLEAN_LEAVE_SVNGS_COST);
		logger.debug("NUMBER OF ROWS DELETED FROM BEN_DEFN_COST : {}", num );
	}
}
