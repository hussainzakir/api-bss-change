package com.trinet.ambis.persistence.dao.ps.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupDao;
import com.trinet.ambis.persistence.dao.ps.LifeAndDisabilityCalcData;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.AdditionalBenefitEmployeeDetails;
import com.trinet.ambis.service.model.AdditionalPlanRate;
import com.trinet.ambis.service.model.EmployeeStrategyGroupDetails;
import com.trinet.ambis.service.model.FormulaDefinition;
import com.trinet.ambis.service.model.FormulaProperties;
import com.trinet.ambis.service.model.RateProperties;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.util.BssCoreServiceClient;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.DaoUtils;

public class LifeAndDisabilityCalcDataImpl implements LifeAndDisabilityCalcData {
	@PersistenceContext(unitName = "bis-sysadm")
    private EntityManager entityManager;
	
	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;
	
	@Autowired
	RealmPlanYearService realmPlanYearService;
	
	@Autowired
	ProspectCensusService prospectCensusService;
	
	@Autowired
	EmployeeStrategyGroupDao employeeStrategyGroupDao;
	
	@Autowired
	EmployeeBenefitGroupDao employeeBenefitGroupDao;
	
	@Autowired
	BssCoreServiceClient bssCoreServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(LifeAndDisabilityCalcDataImpl.class);

    public void setEntityManager(EntityManager em) {
        this.entityManager = em;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }
    
	public void setHrpEntityManager(EntityManager em) {
		this.em = em;
	}

	public EntityManager getHrpEntityManager() {
		return this.em;
	}    

	@Override
	public Map<String, RateProperties> getRateProperties(String cloneBenenProg, Date effDt, Set<String> planList, String bandCode, String quarter) {	
		try {
			Query query = entityManager.createNamedQuery("LIFE_DISABILITY_RATE_PROPERTIES");
			query.setParameter("benProg", cloneBenenProg);
			SimpleDateFormat formatter = new SimpleDateFormat(BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
			String newDateStr = formatter.format(effDt);
			
			query.setParameter("effdt", newDateStr);
			query.setParameter("benefitPlanList", planList);
			query.setParameter("bandCode", bandCode);
			query.setParameter("oeQuarter", quarter);
	        List<Object[]> results = DaoUtils.getResultList(query, "LIFE_DISABILITY_RATE_PROPERTIES");

			if (CollectionUtils.isEmpty(results))
				return null;
			Map<String, RateProperties> map = new HashMap<>();
			
			for (Object[] r : results) { 
				RateProperties rp = new RateProperties();
				rp.setBenProg((String) r[0]);
				rp.setPlanType((String) r[2]);
				rp.setBenefitPlan((String) r[4]);
				rp.setRateTblID((String) r[5]);
				rp.setRateType(Integer.parseInt((String) r[6]));
				rp.setRatePerUnit((String) r[8]);
				map.put(rp.getBenefitPlan(), rp);
			}
			return map;
		}
		catch(Exception ex) {
			logger.error("ERROR : {}", ex.getMessage());
		}
		return null;
	}

	private Map<String, FormulaProperties> retrieveCalcRuleData( Set<String> planList, java.sql.Date effdt, String cloneBenefitProgram ) {
		Map<String, FormulaProperties> map = new HashMap<>();
		try {
			Query query = entityManager.createNamedQuery( "LIFE_DISB_CALC_RULES" );
			query.setParameter( "cloneBenefitProgram", cloneBenefitProgram );
			query.setParameter( "effdt", effdt );
			query.setParameter( "planList", planList );
	        List<Object[]> queryResult = DaoUtils.getResultList(query, "LIFE_DISB_CALC_RULES");

			for (Object[] r : queryResult) {
				FormulaProperties fp = new FormulaProperties();
				fp.setBenefitPlan( (String) r[0] );
				fp.setFormulaID( (String) r[1] );
				String covrgCd = (String) r[2];
				BigDecimal covrgMM = (BigDecimal) r[3];
				BigDecimal covrgDD = (BigDecimal) r[4];
				fp.setPremiumAsOfDate( buildAsOfDate(effdt, covrgMM, covrgDD, covrgCd) );
				fp.setBaseSource( (String) r[5] );

				map.put(fp.getBenefitPlan(), fp);
			}
		}
		catch(Exception ex) {
			logger.info("ERROR : {}", ex.getMessage());
		}
		return map;
	}

	@Override
	public Map<String, FormulaProperties> getFormulaProperties(Set<String> planList, Date effDt, String queryStr) {		
		try {
			Query query = entityManager.createNamedQuery(queryStr);
			SimpleDateFormat formatter = new SimpleDateFormat(BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
			String newDateStr = formatter.format(effDt);
			
			query.setParameter("effdt", newDateStr);
			query.setParameter("planList", planList);
	        List<Object[]> results = DaoUtils.getResultList(query, queryStr);

			if (CollectionUtils.isEmpty(results))
				return null;
			Map<String, FormulaProperties> map = new HashMap<>();
			
			for (Object[] r : results) { 				
				FormulaProperties fp = new FormulaProperties();
				fp.setPlanType((String) r[0]);
				fp.setBenefitPlan((String) r[1]);
				fp.setFormulaID((String) r[3]);
				try {
					Date newDate = formatter.parse((String) r[4]);
					fp.setFormulaEffDt(newDate);
				} catch (ParseException e) {
					CommonUtils.logExceptions(e, logger,"", "");
				}
				
				fp.setBaseSource((String) r[5]);
				fp.setMaxBenefitBase(((BigDecimal) r[6]));
				fp.setMinCovrg(((BigDecimal) r[7]));
				fp.setMaxCovrg(((BigDecimal) r[8]));

				BigDecimal covrgMM = (BigDecimal) r[9];
				BigDecimal covrgDD = (BigDecimal) r[10];
				String covrgCd = (String) r[11];
				fp.setCoverageAsOfDate( buildAsOfDate(effDt, covrgMM, covrgDD, covrgCd) );

				BigDecimal premMM = (BigDecimal) r[12];
				BigDecimal premDD = (BigDecimal) r[13];
				String premCd = (String) r[14];
				fp.setPremiumAsOfDate( buildAsOfDate(effDt, premMM, premDD, premCd) );
				
				map.put(fp.getBenefitPlan(), fp);
			}
			return map;
		}
		catch(Exception ex) {
			logger.info("ERROR : {}", ex.getMessage());
		}
		return null;
	}

	/**
	 * The coverage formula table contains values that instruct PeopleSoft which date to use when getting salary amounts
	 * for calculating coverage levels and premium amounts.  These values include a code that indicates this year, last year,
	 * check date, or pay period date; a month and a day of month.
	 * This method considers those parameters and builds the date that should be used for obtaining salary or ABBR.
	 * @param effDt the original EFFDT of the query
	 * @param asOfMonth as retrieved from the BN_FORMULA table
	 * @param asOfDay as retrieved from the BN_FORMULA table
	 * @param asOfCode one of the following values: <li>C - check date<li>P - period end date<li>T - this year<li>L - last year
	 * @return the correct asOfDate for this coverage formula
	 */
	private static java.sql.Date buildAsOfDate( java.util.Date effDt, BigDecimal asOfMonth, BigDecimal asOfDay, String asOfCode ) {
		Calendar c = Calendar.getInstance();
		c.setTime( effDt );
		int year;
		int month;
		int day;

		if( "C".equals( asOfCode ) || "P".equals( asOfCode ) ) {
			year  = c.get( Calendar.YEAR );
			month = c.get( Calendar.MONTH ) + 1;   // Calendar.MONTH range is 0 - 11
			day   = c.get( Calendar.DAY_OF_MONTH );
		} else {
			// get the current year in order to build the as-of date
			year = c.get( Calendar.YEAR );
			if( "L".equals( asOfCode ) ) {
				year--;
			}
			month = asOfMonth.intValue();
			day = asOfDay.intValue();
		}
		return java.sql.Date.valueOf( "" + year + "-" + month + "-" + day );
	}

	

	@Override
	public List<FormulaDefinition> getFormulaDefinition(String formulaID, Date formulaEffDt) {

		final String FORM_DEF_QUERY = "COVERAGE_FORMULA_DEFINITION";
		List<FormulaDefinition> list = new ArrayList<>();
		Query query = entityManager.createNamedQuery( FORM_DEF_QUERY );
		SimpleDateFormat formatter = new SimpleDateFormat(BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
		String newDateStr = formatter.format(formulaEffDt);
		query.setParameter("effdt", newDateStr);
		query.setParameter("formulaID", formulaID);
		Map<String,Object> queryMap = DaoUtils.generateQueryMap(query);

		try {
	        List<Object[]> results = DaoUtils.getResultList(query, FORM_DEF_QUERY);

			for (Object[] r : results) {
				FormulaDefinition fd = new FormulaDefinition();
				fd.setBenOperand( (String) r[1] );
				fd.setBnEntryTyp( (String) r[2] );
				fd.setBnValue( (BigDecimal) r[3] );
				fd.setRoundUpAmt( (BigDecimal) r[4] );
				fd.setRoundTo( (BigDecimal) r[5] );
				list.add( fd );
			}

		} catch( Exception ex ) {
			throw new BSSApplicationException( ex, new BSSApplicationError( "BSS_SQL_QUERY_ERROR", BSSHttpStatusConstants.INTERNAL_SERVER_ERROR
					, LifeAndDisabilityCalcDataImpl.class.getName(), "Failed while returning rows from Formula Definition table"
					, FORM_DEF_QUERY, queryMap ) );
		}
		return list;
	}

	@Override
	public Map<String, List<AdditionalPlanRate>> getPlanRates(Set<String> rateIds, Date effDt) {
		Map<String, List<AdditionalPlanRate>> ratesMap = null;
		try {
			Query query = entityManager.createNamedQuery("LIFE_DISABILITY_RATES");
			SimpleDateFormat formatter = new SimpleDateFormat(BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
			String newDateStr = formatter.format(effDt);
			logger.info("RATE TABLE ID LIST : {}", rateIds);
			
			
			query.setParameter("effdt", newDateStr);
			query.setParameter("rateIdList", rateIds);
	        List<Object[]> results = DaoUtils.getResultList(query, "LIFE_DISABILITY_RATES");

			if (CollectionUtils.isEmpty(results))
				return null;
			else {
				ratesMap = new HashMap<>();
			}
			
			for (Object[] r : results) { 
				if ((String) r[0] != null) {
					List<AdditionalPlanRate> list = null;
					AdditionalPlanRate rate = new AdditionalPlanRate();
					rate.setRateTblId((String) r[0]);
					rate.setAge(((BigDecimal) r[3]).intValue());
					rate.setRate(((BigDecimal) r[4]));
					if (ratesMap.get((String) r[0]) != null) {
						list = ratesMap.get((String) r[0]);
						list.add(rate);
						
					}
					else {
						list = new ArrayList<>();
						list.add(rate);
					}
					ratesMap.put((String) r[0], list);					
				}
			}
		}
		catch(Exception ex) {
			logger.error("Error: {}", ex.getMessage());
		}		
		return ratesMap;
	}

	@Override
	public Map<String, AdditionalBenefitEmployeeDetails> getGroupEmployeeSelections(Company company, boolean history,
			long strategyId, boolean isVendorMappingOn) {
		Map<String, AdditionalBenefitEmployeeDetails> map = new HashMap<>();
		java.sql.Date effdt;
		if( history ) {
			effdt = new java.sql.Date( company.getRealmPlanYear().getPlanYearEnd().getTime() );
		} else {
			effdt = new java.sql.Date( company.getRealmPlanYear().getPlanYearStart().getTime() );
		}

		// first step: get all benefit programs and life/disability plans.  Populate the map with this data
		Query benProgQuery = entityManager.createNamedQuery( "GET_BENEFIT_PROGRAMS_FOR_COMPANY" );
		benProgQuery.setParameter( BSSQueryConstants.COMPANY_ID, company.getId() );
        List<Object[]> benProgResult = DaoUtils.getResultList(benProgQuery, "GET_BENEFIT_PROGRAMS_FOR_COMPANY");
		for( Object o : benProgResult ) {
			map.put( (String) o, new AdditionalBenefitEmployeeDetails() );
		}

		// second step: get all the benefit plans offered for life and disability and add to the map
		Query offeredPlanQuery = entityManager.createNamedQuery( "GET_BENEFIT_PLANS_FOR_PLANYEAR" );
		offeredPlanQuery.setParameter( BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYear().getId() );
        List<Object[]> offeredPlanResult = DaoUtils.getResultList(offeredPlanQuery, "GET_BENEFIT_PLANS_FOR_PLANYEAR");
		for( Object o : offeredPlanResult ) {
			for( Map.Entry<String,AdditionalBenefitEmployeeDetails> mapEntry : map.entrySet() ) {
				mapEntry.getValue().addBenefitPlan( (String) o );
			}
		}

		Set<java.sql.Date> collectedAsOfDate = new HashSet<>();

		// once this map has been constructed, a new query can get the as-of date for each plan
		// for each Benefit Program in map...
		for( Map.Entry<String, AdditionalBenefitEmployeeDetails> planMapEntry : map.entrySet() ) {
			// for the array of benefit plans associated with the benefit program...
			Set<String> plans = planMapEntry.getValue().getPlans();

			// get the calc rule codes from the clone benefit program
			Map<String,FormulaProperties> calcRuleProps = this.retrieveCalcRuleData( plans, effdt, company.getRealmPlanYear().getCloneProgram() );
			for( Map.Entry<String,FormulaProperties> calcRuleEntry : calcRuleProps.entrySet() ) {
				planMapEntry.getValue().setPlanFormulaProps( calcRuleEntry.getValue() );
			}

			// get the specific formula properties for life insurance plans
			// find the as-of-date and base/coverage min/max and salary multiplier
			Map<String,FormulaProperties> lifeFormulaProps = this.getFormulaProperties( plans, effdt, BSSQueryConstants.LIFE_CVG_FORMULA_PROPERTIES);
			if (MapUtils.isNotEmpty(lifeFormulaProps)) {
				for (Map.Entry<String, FormulaProperties> lifeFormulaEntry : lifeFormulaProps.entrySet()) {
					planMapEntry.getValue().setPlanFormulaProps(lifeFormulaEntry.getValue());
				}
			}

			// get the specific formula properties for disability plans
			Map<String,FormulaProperties> disbFormulaProps = this.getFormulaProperties( plans, effdt, BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES);
			if(MapUtils.isNotEmpty(disbFormulaProps)) {
				for( Map.Entry<String,FormulaProperties> disbFormulaEntry : disbFormulaProps.entrySet() ) {
					planMapEntry.getValue().setPlanFormulaProps( disbFormulaEntry.getValue() );
				}
			}
			// add the unique as-of dates from this map entry
			collectedAsOfDate.addAll( planMapEntry.getValue().selectDistinctAsOfDate() );
		}

		// finally, a query can get the true salary base for each plan/emplid
		// get the earliest-or-latest JOB record for the as-of date
		// if I run this query outside the prior loop, I can run the query once per EFFDT and then save all the required data by benefit program and plan
		for (java.sql.Date asOfDate : collectedAsOfDate) {
			List<Object[]> result;
			if (company.isProspectCompany() || company.isProspectConvertedOnboardingClient()) {
				result = getBenefitSalary(company, strategyId);
			} else {
				if (history) {
					result = this.runHistoryBenefitSalary(company, asOfDate, isVendorMappingOn);
				} else {
					if (strategyId == 0L) {
						throw new BSSApplicationException(new BSSApplicationError("BSS_INVALID_ARGUMENT",
								BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
								LifeAndDisabilityCalcDataImpl.class.getName(),
								"Called getGroupEmployeeSelections for a current strategy but did not provide strategy ID.  Possible logic error.",
								null, null));
					}
					result = this.runStrategyBenefitSalary(company, asOfDate, strategyId, isVendorMappingOn);
				}
			}

			for (Object[] row : result) {
				String benProg = (String) row[0];
				AdditionalBenefitEmployeeDetails dtls = map.get(benProg);
				String emplid = (String) row[1];
				BigDecimal annualRate = (BigDecimal) row[2];
				BigDecimal abbr = (BigDecimal) row[3];
				dtls.addPlanEmployeeAndRate(emplid, annualRate, abbr, asOfDate);
			}
		}

		return map;
	}
	
	private List<Object[]> getBenefitSalary(Company company, long strategyId) {
		List<ProspectCensusResponse> prospectCensus = company.isProspectConvertedClient()
				? bssCoreServiceClient.getCensusByCompanyCode(company.getCode())
				: prospectCensusService.getProspectCensus(company.getCode());
		
		Map<String, EmployeeStrategyGroupDetails> prospectEmployees = employeeBenefitGroupDao
				.getEmployeeDetailsByStrategy(strategyId);
		
		return prospectCensus == null ? Collections.emptyList() : prospectCensus.stream().filter(census -> {
			boolean exists = prospectEmployees.containsKey(census.getEmployeeId());
			if (!exists) {
				logger.error(String.format("runProspectBenefitSalary: Missing Employee in strategy group: %s",
						census.getEmployeeId()));
			}
			return exists;
		}).map(census -> {
			EmployeeStrategyGroupDetails details = prospectEmployees.get(census.getEmployeeId());
			return new Object[] { details.getFutureBenefitProgram(), census.getEmployeeId(), census.getSalary(),
					BigDecimal.ZERO };
		}).collect(Collectors.toList());
	}	
	
	private List<Object[]> runHistoryBenefitSalary( Company company, java.sql.Date effdt, boolean isVendorMappingOn) {
		List<Object[]> results = new ArrayList<>();
		if (isVendorMappingOn) {
			Query query = em.createNamedQuery("CENSUS_HISTORY_COMPANY_BENEFIT_SALARY_AS_OF");
			query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
			query.setParameter(BSSQueryConstants.EFF_DATE, effdt);
			query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYear().getId());
			results = DaoUtils.getResultList(query, "CENSUS_HISTORY_COMPANY_BENEFIT_SALARY_AS_OF");
		} else {
			Query query = entityManager.createNamedQuery("HISTORY_COMPANY_BENEFIT_SALARY_AS_OF");
			query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
			query.setParameter( "effdt", effdt );
			query.setParameter( "yearEndDate", company.getRealmPlanYear().getPlanYearEnd() );
    		results = DaoUtils.getResultList(query, "HISTORY_COMPANY_BENEFIT_SALARY_AS_OF");			
		}

		return results;
	}

	private List<Object[]> runStrategyBenefitSalary( Company company, java.sql.Date effdt, long strategyId, boolean isVendorMappingOn) {

		List<Object[]> results = new ArrayList<>();
		
		if (isVendorMappingOn) {
			Query query = em.createNamedQuery("CENSUS_STRATEGY_COMPANY_BENEFIT_SALARY_AS_OF");
			query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
			query.setParameter(BSSQueryConstants.EFF_DATE, effdt);
			query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYear().getId());
			query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
			results = DaoUtils.getResultList(query, "CENSUS_STRATEGY_COMPANY_BENEFIT_SALARY_AS_OF");
		} else {
			Query query = entityManager.createNamedQuery("STRATEGY_COMPANY_BENEFIT_SALARY_AS_OF");
			query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
			query.setParameter( "effdt", effdt );
			query.setParameter( BSSQueryConstants.STRATEGY_ID, strategyId );
    		results = DaoUtils.getResultList(query, "STRATEGY_COMPANY_BENEFIT_SALARY_AS_OF");			
		}		

        return results;
	}

}