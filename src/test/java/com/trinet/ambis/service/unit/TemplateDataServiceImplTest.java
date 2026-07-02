package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.persistence.dao.hrp.CompanyOptionsDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.DisabilityOptionService;
import com.trinet.ambis.service.impl.TemplateDataServiceImpl;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.Industry;
import com.trinet.ambis.service.model.NewCompanyOptions;
import com.trinet.ambis.service.model.OptionsNew;
import com.trinet.ambis.service.model.PlanTypeDescription;

@RunWith(JUnit4.class)
public class TemplateDataServiceImplTest {

	@InjectMocks
	TemplateDataServiceImpl templateDataService;

	@Mock
	CompanyOptionsDao companyOptionsDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	CompanyService companyService;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	DisabilityOptionService disabilityOptionService;
	
	@Mock
	BenefitOfferExceptionService benOfferExceptionService;

	Company company;
	long companyId;
	long realmPlanYearId;
	String headQtrState;
	IndustryType industryType;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		company = new Company();
		companyId = 4444;
		company.setId(companyId);
		realmPlanYearId = 21;
		company.setRealmPlanYearId(realmPlanYearId);
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(realmPlanYearId);
		company.setRealmPlanYear(rpy);
		headQtrState = "FL";
		company.setHeadQuatersState(headQtrState);
		industryType = IndustryType.FS;
		Industry industry = new Industry(2);
		industry.setIndustryType(industryType);
		company.setIndustry(industry);
	}
	
	@Test
	public void getNewCompanyOptions1() {
		// to do
	}

	//@Test
	public void getNewCompanyOptions() {
		when(companyOptionsDao.getPackageTypes(realmPlanYearId, "FS", headQtrState)).thenReturn(preparePackageTypes());
		when(companyOptionsDao.getDefaultPortfolios(realmPlanYearId, "FS", headQtrState, true ))
				.thenReturn(prepareDefaultPortfolios());
		when(companyOptionsDao.getTemplateAdditionalPlans(realmPlanYearId, "FS", headQtrState))
				.thenReturn(prepareTemplateAdditionalPlans());
		when(strategyDataDao.getPlanTypeDescriptions(realmPlanYearId)).thenReturn(preparePlanTypeDescription());
		when(disabilityOptionService.getDisabilityOptionByPlans(Arrays.asList("000SRS"), company, false))
				.thenReturn(prepareDisabilityOption());
		when(benOfferExceptionService.findApplicableBy(company)).thenReturn(Collections.emptyMap());

		NewCompanyOptions actualResult = templateDataService.getNewCompanyOptions(company);

		assertEquals("financialServices", actualResult.getVertical());
		assertEquals(3, actualResult.getOptions().size());
		for (OptionsNew option : actualResult.getOptions()) {
			assertTrue(Arrays.asList("PRM", "CON", "INT").contains(option.getId()));
			if ("PRM".equals(option.getId())) {
				assertEquals("Premier", option.getName());
				assertEquals(3, option.getBenefitOfferPackages().size());
			} else if ("CON".equals(option.getId())) {
				assertEquals("Conservative", option.getName());
				assertEquals(2, option.getBenefitOfferPackages().size());
			} else if ("INT".equals(option.getId())) {
				assertEquals("Intermediate", option.getName());
				assertEquals(2, option.getBenefitOfferPackages().size());
			}
		}
	}

	private Map<String, Boolean> preparePackageTypes() {
		Map<String, Boolean> packageTypes = new HashMap<String, Boolean>();
		packageTypes.put("PRM", true);
		packageTypes.put("CON", true);
		packageTypes.put("INT", true);
		return packageTypes;
	}

	private Map<String, Map<String, List<Long>>> prepareDefaultPortfolios() {
		Map<String, Map<String, List<Long>>> portfolioMap = new HashMap<String, Map<String, List<Long>>>();
		Map<String, List<Long>> planPortfolio = new HashMap<String, List<Long>>();
		planPortfolio.put("10", Arrays.asList(10L, 12L));
		planPortfolio.put("11", Arrays.asList(3L));
		planPortfolio.put("1V", Arrays.asList(6L));
		planPortfolio.put("A3", Arrays.asList(7L));
		portfolioMap.put("PRM", planPortfolio);
		planPortfolio = new HashMap<String, List<Long>>();
		planPortfolio.put("10", Arrays.asList(9L));
		planPortfolio.put("14", Arrays.asList(5L, 6L));
		planPortfolio.put("A3", Arrays.asList(7L));
		portfolioMap.put("INT", planPortfolio);
		planPortfolio = new HashMap<String, List<Long>>();
		planPortfolio.put("11", Arrays.asList(3L));
		planPortfolio.put("14", Arrays.asList(5L, 6L));
		planPortfolio.put("A3", Arrays.asList(7L));
		portfolioMap.put("CON", planPortfolio);
		return portfolioMap;
	}

	private Map<String, Map<String, List<String>>> prepareTemplateAdditionalPlans() {
		Map<String, Map<String, List<String>>> additionalPlans = new HashMap<String, Map<String, List<String>>>();
		Map<String, List<String>> planTypePlans = new HashMap<String, List<String>>();
		planTypePlans.put("31", Arrays.asList("000SRT"));
		planTypePlans.put("A3", Arrays.asList("000W46"));
		additionalPlans.put("CON", planTypePlans);
		planTypePlans = new HashMap<String, List<String>>();
		planTypePlans.put("23", Arrays.asList("000TM9"));
		planTypePlans.put("30", Arrays.asList("000SRS"));
		additionalPlans.put("INT", planTypePlans);
		planTypePlans = new HashMap<String, List<String>>();
		planTypePlans.put("A3", Arrays.asList("000W46"));
		planTypePlans.put("30", Arrays.asList("000SRS"));
		additionalPlans.put("PRM", planTypePlans);
		return additionalPlans;
	}

	private Map<String, PlanTypeDescription> preparePlanTypeDescription() {
		Map<String, PlanTypeDescription> planTypeDescMap = new HashMap<String, PlanTypeDescription>();
		PlanTypeDescription planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType("medical");
		planTypeDesc.setDescription("Medical");
		planTypeDescMap.put("10", planTypeDesc);
		planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType("dental");
		planTypeDesc.setDescription("dental");
		planTypeDescMap.put("11", planTypeDesc);
		planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType("vision");
		planTypeDesc.setDescription("Vision");
		planTypeDescMap.put("1V", planTypeDesc);
		planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType("vision");
		planTypeDesc.setDescription("Vision");
		planTypeDescMap.put("14", planTypeDesc);
		planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType("LIFE");
		planTypeDesc.setDescription("Life and AD and D");
		planTypeDescMap.put("23", planTypeDesc);
		planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType("STD");
		planTypeDesc.setDescription("Short-Term Disability");
		planTypeDescMap.put("30", planTypeDesc);
		planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType("LTD");
		planTypeDesc.setDescription("Long-Term Disability");
		planTypeDescMap.put("31", planTypeDesc);
		planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType("CMTR");
		planTypeDesc.setDescription("Commuter Benefits");
		planTypeDescMap.put("A3", planTypeDesc);
		return planTypeDescMap;
	}

	private AdditionalBenefitPlan prepareDisabilityOption() {
		AdditionalBenefitPlan dPlan = new AdditionalBenefitPlan();
		dPlan.setId("1111");
		return dPlan;
	}
}
