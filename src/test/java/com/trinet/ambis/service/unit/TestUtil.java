package com.trinet.ambis.service.unit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanMapping;
import com.trinet.ambis.persistence.model.RealmPlanMappingId;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyFundingModel;
import com.trinet.ambis.service.model.CommonData;
import com.trinet.ambis.service.model.CompanyData;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanYearCommonData;
import com.trinet.ambis.service.model.PlanYearData;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.model.UserData;
import com.trinet.ambis.util.Utils;


 

public class TestUtil {
	private RealmPlanYear realmPlanYear;
    private CompanyData companyCommonData;
    private PlanYearCommonData planYearCommonData;
    private UserData userCommonData;
	
   public Company setupCompany(){	
		Company company = new Company();
		company.setId(new Long(4730));
		company.setCode("FXS");
		company.setName("Test");
		company.setRealmPlanYearId(4);
		company.setPlanStartDate("01-OCT-15");
		company.setPlanEndDate("30-SEP-16");
		company.setActualHeadCount(10);
		company.setPayrollProcessed(true);
		company.setRealmPlanYear(realmPlanYear);
		company.setRenewalCompany(true);
		
		Company bisCompany = Mockito.mock(Company.class);
		Company pSoftCompany = Mockito.mock(Company.class);
		
		Realm realm = new Realm();
		realm.setDescription("Ambrose Product");
		realm.setPeoid("AMB");
		realm.setRealmType("P");
		realm.setVerticalCode("");
		realm.setId(1);
		company.setRealm(realm);
		
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		Date nextPlanYearStartDate = null;
		try {
			nextPlanYearStartDate = df.parse("01-10-2017");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
    	CommonData commonData = new CommonData();
    	commonData.setCompanyCommonData(companyCommonData);
        commonData.setPlanYearCommonData(planYearCommonData);
        commonData.setUserCommonData(userCommonData);
		PlanYearData planYearData = new PlanYearData();
		planYearData.setNextPlanYearStartDate(nextPlanYearStartDate);
        
        pSoftCompany.setRealm(null);
		bisCompany.setRealm(realm);
		bisCompany.setQuater("8Y");

		Date nextYearPlanStartDate = Utils.convertStringToDate("01-OCT-2016", "dd-MMM-yyyy");
		Date nextYearPlanEndDate = Utils.convertStringToDate("30-SEP-2017", "dd-MMM-yyyy");
		Date planStartDate = Utils.convertStringToDate("01-OCT-2016", "dd-MMM-yyyy");
		Date planEndDate = Utils.convertStringToDate("30-SEP-2017", "dd-MMM-yyyy");

		planYearData.setEffectiveDate(planStartDate);
		planYearData.setEndDate(planEndDate);
		planYearData.setNextPlanYearEndDate(nextYearPlanEndDate);
		planYearData.setNextPlanYearStartDate(nextYearPlanStartDate);
		
		company.setRealmPlanYear(realmPlanYear);
		
		return company;
	}
	
	public StrategyData setupStrategyData() {

		InputStream is = getClass().getClassLoader().getResourceAsStream("WEB-INF/StrategyJson_JunitTest.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String json = null;
		try {
			json = org.apache.commons.io.IOUtils.toString(br);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		StrategyData strategyData = jsonToObject(json);
		
		StrategySummary strategySummary = new StrategySummary();
		strategySummary.setHeadcount(10);
		strategySummary.setSubmitted(true);
		strategySummary.setName("Test Summary");
		strategySummary.setComments("In test class");
		strategySummary.setType("current");
		strategySummary.setSubmitDate(new Date());
		strategySummary.setEstimatedTotalCost(new BigDecimal(10000));
		strategySummary.setTotalBudget(new BigDecimal(1000));
		strategySummary.setBudgetFactor(12);
		
		strategyData.setStrategySummary(strategySummary);
		
		return strategyData;
	}
	
	public Strategy setUpStrategy(){
		Strategy strategy = new Strategy();
		strategy.setId(100l);
		strategy.setName("Test Strategy");
		strategy.setType("Current");
		strategy.setSubmitted(false);
		strategy.setSubmitDate(new Date());
		strategy.setEstimatedTotalCost(BigDecimal.ZERO);
		strategy.setTotalBudget(BigDecimal.TEN);
		strategy.setBudgetFactor(12);
		strategy.setCompanyId(100);
		
		return strategy;
	}
	
	public PlanSelection setUpPlanSelection(){
		PlanSelection planSelection = new PlanSelection();
		planSelection.setId(11l);
		
		return planSelection;
	}
	
	public StrategyFundingModel setUpStrategyFundingModel(){
		StrategyFundingModel strategyFundingModel = new StrategyFundingModel();
		strategyFundingModel.setId(12l);
		
		return strategyFundingModel;
	}
	
	public StrategyData jsonToObject(String strJson) {
		ObjectMapper mapper = new ObjectMapper();
		StrategyData dto = null;
		try {
			 dto = mapper.readValue(strJson, StrategyData.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dto;
	}
	
	private Map<String, RealmPlanMapping> getRealmPlanMappings() {
		Map<String, RealmPlanMapping> map = new HashMap<String, RealmPlanMapping>();
		RealmPlanMapping plan = new RealmPlanMapping();
		RealmPlanMappingId id = new RealmPlanMappingId();
		id.setPlan("000SR9");
		id.setRealmYearId(3);
		plan.setOldPlanDesc("UHC Vision");
		plan.setPlanType("14");
		plan.setOldPortfolioId(6);
		plan.setNewPlanDesc("EyeMed Optional Vision Plus");
		plan.setNewPortfolioId(15);
		map.put("000SR9", plan);
		return map;
		
	}
	
	private RealmPlanYear setUpRealmPlanYear() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setCloneProgram("001EKV");
		realmPlanYear.setId(4);
		realmPlanYear.setMinFunding(75);
		realmPlanYear.setOeQuarter("8Y");
		realmPlanYear.setRealmId(4);
		realmPlanYear.setPlanYearStart(new Date());
		realmPlanYear.setPlanYearEnd(new Date());
		
		return realmPlanYear;
	}

	private Map<Long, List<PlanContribution>> setUpContributionMap() {
		List<PlanContribution> contributions = new ArrayList<PlanContribution>();
		
		PlanContribution employeeContribution = new PlanContribution();
		employeeContribution.setId(123l);
		employeeContribution.setType("employee");
		employeeContribution.setEmployerPercent(BigDecimal.TEN);
		employeeContribution.setHeadcount(20);
		employeeContribution.setBssBenefitPlanId("001EKX");
		employeeContribution.setEmployerContribution(new BigDecimal("350"));
		employeeContribution.setEmployeeContribution(new BigDecimal("200"));
		contributions.add(employeeContribution);
		
		PlanContribution emplPlusSpouseContribution = new PlanContribution();
		emplPlusSpouseContribution.setId(124l);
		emplPlusSpouseContribution.setType("employeePlusSpouse");
		emplPlusSpouseContribution.setEmployerPercent(BigDecimal.TEN);
		emplPlusSpouseContribution.setHeadcount(20);
		emplPlusSpouseContribution.setBssBenefitPlanId("001EKX");
		emplPlusSpouseContribution.setEmployerContribution(new BigDecimal("450"));
		emplPlusSpouseContribution.setEmployeeContribution(new BigDecimal("100"));
		contributions.add(emplPlusSpouseContribution);
		
		PlanContribution emplPlusChildContribution = new PlanContribution();
		emplPlusChildContribution.setId(125l);
		emplPlusChildContribution.setType("employeePlusChild");
		emplPlusChildContribution.setEmployerPercent(BigDecimal.TEN);
		emplPlusChildContribution.setHeadcount(21);
		emplPlusChildContribution.setBssBenefitPlanId("001EKX");
		emplPlusChildContribution.setEmployerContribution(new BigDecimal("550"));
		emplPlusChildContribution.setEmployeeContribution(new BigDecimal("0"));
		contributions.add(emplPlusChildContribution);
		
		PlanContribution familyContribution = new PlanContribution();
		familyContribution.setId(126l);
		familyContribution.setType("Family");
		familyContribution.setEmployerPercent(BigDecimal.TEN);
		familyContribution.setHeadcount(22);
		familyContribution.setBssBenefitPlanId("001EKX");
		familyContribution.setEmployerContribution(new BigDecimal("660"));
		familyContribution.setEmployeeContribution(new BigDecimal("200"));
		contributions.add(familyContribution);
		
		Map<Long, List<PlanContribution>> map = new HashMap<Long, List<PlanContribution>>();
		map.put(111l, contributions);
		
		return map;
	}

	private Map<String, Set<PlanCarrier>> setUpPortfolioMap() {
		Map<String, Set<PlanCarrier>> portfolioMap = new HashMap<String, Set<PlanCarrier>>();
	
		Set<PlanCarrier> medicalCarrier= new HashSet<PlanCarrier>();
		PlanCarrier medical = new PlanCarrier();
		medical.setId(9);
		medical.setCode("UHCAM");
		medical.setName("Portfolio A");
		medical.setMandatory(false);
		medical.setRestricted(false);
		medical.setEmployeePaid(false);
		medicalCarrier.add(medical);
		portfolioMap.put("medical", medicalCarrier);
		
		Set<PlanCarrier> dentalCarrier= new HashSet<PlanCarrier>();
		PlanCarrier dental = new PlanCarrier();
		dental.setId(3);
		dental.setCode("METAM");
		dental.setName("Metlife");
		dental.setMandatory(false);
		dental.setRestricted(false);
		dental.setEmployeePaid(false);
		dentalCarrier.add(medical);
		portfolioMap.put("dental", dentalCarrier);
		
		Set<PlanCarrier> visionCarrier= new HashSet<PlanCarrier>();
		PlanCarrier vision = new PlanCarrier();
		vision.setId(3);
		vision.setCode("GUARD");
		vision.setName("Guardian");
		vision.setMandatory(false);
		vision.setRestricted(false);
		vision.setEmployeePaid(false);
		visionCarrier.add(medical);
		portfolioMap.put("vision", visionCarrier);
		
		return portfolioMap;
		
	}


}
