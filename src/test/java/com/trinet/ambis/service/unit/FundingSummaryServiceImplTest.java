package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.trinet.ambis.helper.ModelCompareServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixFilters;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.model.BenefitOfferFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.GroupFunding;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.rest.controllers.dto.outputs.BenefitGroup;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitOffer;
import com.trinet.ambis.rest.controllers.dto.outputs.FundingSummary;
import com.trinet.ambis.service.impl.outputs.FundingSummaryServiceImpl;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FundingSummaryServiceImplTest extends ServiceUnitTest{

	
	@InjectMocks
	FundingSummaryServiceImpl fundingSummaryServiceImpl;

	@Mock
	CompanyService companyService;

	@Mock
	StrategyFundingDataDao strategyFundingDataDao;
    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMockedStatic;
    private MockedStatic<ModelCompareServiceHelper> modelCompareServiceHelperMockedStatic;

    @Before
    public void setUp() {
        bssSecurityUtilsMockedStatic = Mockito.mockStatic(BSSSecurityUtils.class);
        modelCompareServiceHelperMockedStatic = Mockito.mockStatic(ModelCompareServiceHelper.class);
    }

    @After
    public void tearDown() {
        bssSecurityUtilsMockedStatic.close();
        modelCompareServiceHelperMockedStatic.close();
    }

	@Test
	public void getFundingSummaryDataNew() {
		OutputRequest outputRequest = new OutputRequest();
		List<String> templateNames = Arrays.asList("ECC", "PCC", "APX");
		List<String> benefitTypes = Arrays.asList("10", "11", "14");
		List<String> regions = Arrays.asList("element1", "element2", "element3");
		String tNstrategyId = "295309";
		outputRequest.setTemplateNames(templateNames);
		
		outputRequest.setBenefitTypes(benefitTypes);
		outputRequest.setPlanAppendixFilters(new PlanAppendixFilters());
		outputRequest.getPlanAppendixFilters().setRegions(regions);
		outputRequest.setTnStrategyId(tNstrategyId);

		List<String> types = Arrays.asList("Medical", "Dental", "Vision");
		String EMPLID = "0000000123456";
		long COMPANY_ID = 9999;
		long strategyId = 295309;
		String companyCode = "G48";
		int realmPlanYearId = 1234;

		ModelCompareStrategy modelCompareStrategy = new ModelCompareStrategy();
		List<Object[]> data = new ArrayList<>();
		Object[] obj = { 37351, "Group 1", "1st of month on/after DOH", Constants.MEDICAL_CODE, "CFPCT", null,
				"employee", new BigDecimal(100), "Strategy 1", "Employee" };
		data.add(obj);
		Object[] obj1 = { 37351, "Group 1", "1st of month on/after DOH", Constants.DENTAL_CODE, "CFPCT", null,
				"employeePlusSpouse", new BigDecimal(200), "Strategy 1", "Employee + Spouse" };
		data.add(obj1);
		Object[] obj2 = { 37352, "Group 2", "1st of month on/after DOH", null, null, null, "CFPCT", null,
				"Strategy 1", null };
		data.add(obj2);
		setStrategyDetails(modelCompareStrategy, data);
		Map<Long, ModelCompareStrategy> modelCompareStrategyMap = new HashMap<>();
		modelCompareStrategyMap.put(strategyId, modelCompareStrategy);

		Company company = new Company();
		company.setId(COMPANY_ID);
		company.setCode(companyCode);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(realmPlanYearId);
		realmPlanYear.setPlanYearEnd(new Date());
		company.setRealmPlanYear(realmPlanYear);

		Map<Long, Set<String>> planLevelOverrideMap = new HashMap<>();
		
		planLevelOverrideMap.put(37351L, Set.of("medical"));

		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(EMPLID);
		when(strategyFundingDataDao.getFundingDetailsByStrategyId(Arrays.asList(strategyId), company, false, company.getRealmPlanYear().getPlanYearEnd())).thenReturn(modelCompareStrategyMap);
		when(ModelCompareServiceHelper.getStrategyDisplayName("Strategy 1",realmPlanYear,false,true)).thenReturn("Strategy 1");
	    when(strategyFundingDataDao.getPlanLevelOverrides(anyLong())).thenReturn(planLevelOverrideMap);

		CompletableFuture<FundingSummary> actualResultFuture = fundingSummaryServiceImpl.getFundingSummaryData(company, outputRequest);
		FundingSummary actualResult = actualResultFuture.join();
		
		List<BenefitGroup> benefitGroups = actualResult.getBenefitGroups().stream()
				.filter(group -> group.getBenefitGroupName().equals("Group 1")).collect(Collectors.toList());

		BenefitOffer medicalOffer = benefitGroups.get(0).getBenefitOffers().get("Medical");
		BenefitOffer dentalOffer = benefitGroups.get(0).getBenefitOffers().get("Dental");

		assertTrue(medicalOffer.isPlanLevelFundingOverride());
		assertFalse(dentalOffer.isPlanLevelFundingOverride());
		assertEquals(types, actualResult.getOrderedBenefitTypes());




	}

	private void setStrategyDetails(ModelCompareStrategy strategy, List<Object[]> obj) {
		List<GroupFunding> groupFundingList = new ArrayList<>();
		Map<Long, GroupFunding> groupFundingMap = new HashMap<>();
		String strategyName = null;
		for (Object[] r : obj) {
			BigDecimal groupId = new BigDecimal(String.valueOf(r[0]));
			String groupDesc = (String) r[1];
			String waitTime = (String) r[2];
			String planType = r[3] == null ? null : String.valueOf(r[3]);
			String fundingType = (String) r[4];
			String baseFundPlan = (String) r[5];
			String coverageId = (String) r[6];
			BigDecimal contribution = r[7] == null ? null : new BigDecimal(String.valueOf(r[7]));
			strategyName = (String) r[8];
			String cvgLvlDesc = (String) r[9];

			if (Constants.dentalPlanTypeList.contains(planType)) {
				planType = Constants.DENTAL;
			} else if (Constants.visionPlanTypeList.contains(planType)) {
				planType = Constants.VISION;
			} else if (Constants.medicalPlanTypeList.contains(planType)) {
				planType = Constants.MEDICAL;
			}
			CoverageLevel cvgLvl = new CoverageLevel(cvgLvlDesc, coverageId, contribution);
			if (null != groupFundingMap.get(groupId.longValue())) {
				GroupFunding gf = groupFundingMap.get((groupId).longValue());
				if (null != gf.getOfferTypeFunding().get(planType)) {
					gf.getOfferTypeFunding().get(planType).getCoverageLevels().add(cvgLvl);
				} else if (planType != null) {
					BenefitOfferFunding bof = new BenefitOfferFunding();
					bof.setBaseFundPlan(baseFundPlan);
					bof.setFundingType(fundingType);
					bof.setType(planType);
					bof.setOffered(true);
					bof.setCoverageLevels(new ArrayList<CoverageLevel>());
					if (coverageId != null) {
						bof.getCoverageLevels().add(cvgLvl);
					}
					gf.getOfferTypeFunding().put(bof.getType(), bof);
				}
			} else {
				GroupFunding gf = new GroupFunding();
				gf.setId((groupId).longValue());
				gf.setName(groupDesc);
				gf.setWaitTime(waitTime);
				if (planType != null) {
					BenefitOfferFunding bof = new BenefitOfferFunding();
					bof.setBaseFundPlan(baseFundPlan);
					bof.setFundingType(fundingType);
					bof.setType(planType);
					bof.setOffered(true);
					bof.setCoverageLevels(new ArrayList<CoverageLevel>());
					if (coverageId != null) {
						bof.getCoverageLevels().add(cvgLvl);
					}
					gf.getOfferTypeFunding().put(bof.getType(), bof);
				}
				groupFundingMap.put(gf.getId(), gf);
			}
		}
		groupFundingList.addAll(groupFundingMap.values());
		strategy.setName(strategyName);
		strategy.setGroupFundingList(groupFundingList);
	}

}
