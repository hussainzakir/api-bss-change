package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanCompareFacade;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;
import com.trinet.ambis.service.prospect.ProspectPlanCompareService;
import com.trinet.ambis.util.BSSSecurityUtils;

@RunWith(MockitoJUnitRunner.class)
public class PlanCompareFacadeTest {

	@InjectMocks
	PlanCompareFacade planCompareFacade;

	@Mock
	private PlanCompareService planCompareService;

	@Mock
	private BenefitPlanService benefitPlanService;

	@Mock
	private RealmPlanYearService realmPlanYearService;

	@Mock
	private CompanyService companyService;

	@Mock
	private ProspectPlanCompareService prospectPlanCompareService;

	@Mock
	private StrategyDao strategyDao;
	
	@Mock
	private HttpServletRequest httpRequest;
	
	private static final String EMPLID = "234342342344";

    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMockedStatic;

    @Before
    public void setUp() {
        bssSecurityUtilsMockedStatic = Mockito.mockStatic(BSSSecurityUtils.class);
        bssSecurityUtilsMockedStatic.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
    }

    @After
    public void tearDown() {
        bssSecurityUtilsMockedStatic.close();
    }

	@Test
	public void generateEnrolledPlanCompareReport() {
		Company company = new Company();
		List<Long> strategyIds = new ArrayList<>();
		strategyIds.add(11111L);
		strategyIds.add(22222L);
		strategyIds.add(33333L);
		List<Strategy> strategies = new ArrayList<>();
		strategies.add(prepareStrategy(11111L, 11111L));
		strategies.add(prepareStrategy(22222L, 22222L));
		strategies.add(prepareStrategy(33333L, 22222L));

		RealmPlanYear currentPlYr = prepareRealmPlanYear();
		RealmPlanYear futurePlYr = prepareRealmPlanYear();

		Map<String, BenefitPlan> currentRegionalBasePlanMappings = Maps.newHashMap();
		Map<String, BenefitPlan> futureRegionalBasePlanMappings = Maps.newHashMap();
		Map<String, Map<String, Set<String>>> plansToCompare = Maps.newHashMap();
		Workbook workbook = new XSSFWorkbook();

		ArgumentCaptor<List> strategyIdsArgCaptor = ArgumentCaptor.forClass(List.class);

		when(strategyDao.findAllById(strategyIds)).thenReturn(strategies);
		when(realmPlanYearService.getRealmForCompanyId(11111L)).thenReturn(currentPlYr);
		when(realmPlanYearService.getRealmForCompanyId(22222L)).thenReturn(futurePlYr);
		when(benefitPlanService.getRegionalBasePlanMapping(currentPlYr)).thenReturn(currentRegionalBasePlanMappings);
		when(benefitPlanService.getRegionalBasePlanMapping(futurePlYr)).thenReturn(futureRegionalBasePlanMappings);
		when(planCompareService.findCompanyLevelEnrolledPlans(eq(company), strategyIdsArgCaptor.capture(),
				eq(currentRegionalBasePlanMappings), eq(futureRegionalBasePlanMappings))).thenReturn(plansToCompare);
		when(planCompareService.generateWorkbook(company, currentPlYr, futurePlYr, plansToCompare,
				currentRegionalBasePlanMappings, futureRegionalBasePlanMappings, httpRequest)).thenReturn(workbook);

		Workbook resultActual = planCompareFacade.generateEnrolledPlanCompareReport(company, strategyIds, httpRequest);

		assertEquals(workbook, resultActual);
		assertEquals(2, strategyIdsArgCaptor.getValue().size());
		assertEquals(22222L, strategyIdsArgCaptor.getValue().get(0));
		assertEquals(33333L, strategyIdsArgCaptor.getValue().get(1));
	}

	@Test
	public void getPlanCompareDetails() {
		// Given
		String companyCode = "JDFHJSHFJK342424SFSF";
		BenExchngEnums benExchange = BenExchngEnums.getByExchangeId("TNIII");
		Company company = new Company();
		List<Long> trinetStrategyIds = Arrays.asList(1111L);
		List<PlanCompareDetailDto> expectedResult = new ArrayList<>();

		when(companyService.getCompanyDetails(companyCode, false, EMPLID, benExchange)).thenReturn(company);
		when(prospectPlanCompareService.getPlanCompareDetails(company, trinetStrategyIds, httpRequest))
				.thenReturn(expectedResult);

		// when
		List<PlanCompareDetailDto> actualResult = planCompareFacade.getPlanCompareDetails(companyCode, "TNIII",
				trinetStrategyIds, httpRequest);

		// then
		assertEquals(expectedResult, actualResult);
		verify(companyService, times(1)).getCompanyDetails(companyCode, false, EMPLID, benExchange);
		verify(prospectPlanCompareService, times(1)).getPlanCompareDetails(company, trinetStrategyIds, httpRequest);
	}

	private Strategy prepareStrategy(long strategyId, long companyId) {
		Strategy s = new Strategy();
		s.setId(strategyId);
		s.setCompanyId(companyId);
		return s;
	}

	private RealmPlanYear prepareRealmPlanYear() {
		RealmPlanYear rpy = new RealmPlanYear();
		return rpy;
	}

}
