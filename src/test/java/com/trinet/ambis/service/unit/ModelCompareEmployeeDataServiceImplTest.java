package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.impl.ModelCompareEmployeeDataServiceImpl;
import com.trinet.ambis.service.model.EmployeeStrategyGroupDetails;
import com.trinet.ambis.service.model.EmployeeStrategyPlanData;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.outputs.EmployeeCostSummaryService;
import com.trinet.ambis.util.BssCoreServiceClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.StrategyGroupEmployeePlanRateData;
import com.trinet.ambis.service.model.BenefitPlanRateData;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import com.trinet.ambis.service.prospect.dto.EmployeeCostRes;
import com.trinet.ambis.service.prospect.service.ProspectEmployeeCostService;

@RunWith(MockitoJUnitRunner.class)
public class ModelCompareEmployeeDataServiceImplTest extends ServiceUnitTest {

    private static final int PROSPECT_BENEFIT_PLAN_ID = 11111;
    private static final String TRINET_BENEFIT_PLAN_ID = "22222";
    private static final String OMS_BENEFIT_PLAN_ID = "123456";
    private static final long PROSPECT_STRATEGY_ID = ProspectConstants.PROSPECT_STRATEGY_ID;
    private static final long TRINET_STRATEGY_ID = 223220;
    private static final List<Long> STRATEGY_LIST = new ArrayList<>(Arrays.asList(TRINET_STRATEGY_ID));

    @Qualifier("modelCompareEmployeeDataServiceImpl")
    @InjectMocks
    ModelCompareEmployeeDataServiceImpl modelCompareEmployeeDataService;
    @Mock
    StrategyDataDao strategyDataDao;
    @Mock
    ProspectEmployeeCostService prospectEmployeeCostService;
    @Mock
    EmployeeDataDao employeeDataDao;
    @Mock
    EmployeeCostSummaryService employeeCostSummaryService;
    @Mock
    BssCoreServiceClient bssCoreServiceClient;
    @Mock
    EmployeeBenefitGroupDao employeeBenefitGroupDao;
    @Mock
    StrategyFundingDataDao strategyFundingDataDao;
    private MockedStatic<CompanyServiceHelper> mockStaticCompanyServiceHelper;

    @Before
    public void setUp() {
        mockStaticCompanyServiceHelper = Mockito.mockStatic(CompanyServiceHelper.class);
    }

    @After
    public void tearDown() {
        if (mockStaticCompanyServiceHelper != null) {
            mockStaticCompanyServiceHelper.close();
            mockStaticCompanyServiceHelper = null;
        }
    }

    @Test
    public void getEmployeeStrategiesPlanCostDataTest_noResults() {
        Company company = new Company();
        company.setCode("PROSPECT");
        company.setProspectCompany(true);

        List<EmployeeCostRes> prospectReturnData = new ArrayList<>();
        List<StrategyGroupEmployeePlanRateData> trinetReturnData = new ArrayList<>();

        Optional<List<EmployeeCostRes>> prospectData = Optional.ofNullable(prospectReturnData);
        Optional<List<StrategyGroupEmployeePlanRateData>> trinetData = Optional.ofNullable(trinetReturnData);

        when(prospectEmployeeCostService.getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(prospectData);
        when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES)).thenReturn(trinetData);

		List<EmployeeStrategyData> result = modelCompareEmployeeDataService.getEmployeeStrategiesPlanCostData(company,
				PROSPECT_STRATEGY_ID, STRATEGY_LIST);

        assertTrue(result.isEmpty());
        verify(prospectEmployeeCostService, times(1)).getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
        verify(strategyDataDao, times(1)).getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES);

    }

    @Test
    public void getEmployeeStrategiesPlanCostDataTest_onlyProspectResults() {
        Company company = new Company();
        company.setCode("PROSPECT");
        company.setProspectCompany(true);

        List<EmployeeCostRes> prospectReturnData = populateProspectReturnData();
        List<StrategyGroupEmployeePlanRateData> trinetReturnData = new ArrayList<>();

        Optional<List<EmployeeCostRes>> prospectData = Optional.ofNullable(prospectReturnData);
        Optional<List<StrategyGroupEmployeePlanRateData>> trinetData = Optional.ofNullable(trinetReturnData);

        when(prospectEmployeeCostService.getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(prospectData);
        when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES)).thenReturn(trinetData);
        when(employeeDataDao.getEmployeeGroupDetailsByStrategy(anyLong())).thenReturn(prepareEmployeeStrategyGroups());

		List<EmployeeStrategyData> result = modelCompareEmployeeDataService.getEmployeeStrategiesPlanCostData(company,
				PROSPECT_STRATEGY_ID, STRATEGY_LIST);

        verify(prospectEmployeeCostService, times(1)).getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
        verify(strategyDataDao, times(1)).getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES);

        assertEquals(3, result.size());

        // Verify Employee1 Data
        List<BenefitPlanRateData> benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee1"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        BenefitPlanRateData benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(100), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(200), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(25), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(50), benefitPlanRateData.getEmployerContribution());

        // Verify Employee2 Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee2"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("2", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(200), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(400), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("4", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(75), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(100), benefitPlanRateData.getEmployerContribution());

        // Verify Employee3ProspectOnly Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee3ProspectOnly"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(300), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(600), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(10), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(15), benefitPlanRateData.getEmployerContribution());
        
		List<EmployeeStrategyPlanData> employeeStrategyData = result.stream()
				.filter(a -> Objects.equals(a.getEmplId(), "Employee1")).collect(Collectors.toList()).get(0)
				.getStrategyDetails().stream().collect(Collectors.toList());

		assertEquals(2, employeeStrategyData.size());
		assertEquals(String.valueOf(PROSPECT_STRATEGY_ID), String.valueOf(employeeStrategyData.get(0).getStrategyId()));
		assertEquals(String.valueOf(TRINET_STRATEGY_ID), String.valueOf(employeeStrategyData.get(1).getStrategyId()));
    }

    @Test
    public void getEmployeeStrategiesPlanCostDataTest_onlyTrinetResults() {
        Company company = new Company();
        company.setCode("PROSPECT");
        company.setProspectCompany(true);

        List<EmployeeCostRes> prospectReturnData = new ArrayList<>();
        List<StrategyGroupEmployeePlanRateData> trinetReturnData = populateStrategyGroupEmployeePlanRateData(true);

        Optional<List<EmployeeCostRes>> prospectData = Optional.ofNullable(prospectReturnData);
        Optional<List<StrategyGroupEmployeePlanRateData>> trinetData = Optional.ofNullable(trinetReturnData);

        when(prospectEmployeeCostService.getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(prospectData);
        when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES)).thenReturn(trinetData);

		List<EmployeeStrategyData> result = modelCompareEmployeeDataService.getEmployeeStrategiesPlanCostData(company,
				PROSPECT_STRATEGY_ID, STRATEGY_LIST);

        verify(prospectEmployeeCostService, times(1)).getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
        verify(strategyDataDao, times(1)).getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES);

        assertEquals(3, result.size());

        // Verify Employee1 Data
        List<BenefitPlanRateData> benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee1"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        BenefitPlanRateData benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(150), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(250), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("2", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(30), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(80), benefitPlanRateData.getEmployerContribution());

        // Verify Employee2 Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee2"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(250), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(350), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("4", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(40), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(90), benefitPlanRateData.getEmployerContribution());

        // Verify Employee3TrinetOnly Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee3TrinetOnly"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(350), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(700), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(15), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(20), benefitPlanRateData.getEmployerContribution());

    }

    @Test
    public void getEmployeeStrategiesPlanCostDataTest_bothResults() {
        Company company = new Company();
        company.setCode("PROSPECT");
        company.setProspectCompany(true);

        List<EmployeeCostRes> prospectReturnData = populateProspectReturnData();
        List<StrategyGroupEmployeePlanRateData> trinetReturnData = populateStrategyGroupEmployeePlanRateData(true);

        Optional<List<EmployeeCostRes>> prospectData = Optional.ofNullable(prospectReturnData);
        Optional<List<StrategyGroupEmployeePlanRateData>> trinetData = Optional.ofNullable(trinetReturnData);

        when(prospectEmployeeCostService.getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(prospectData);
        when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES)).thenReturn(trinetData);

		List<EmployeeStrategyData> result = modelCompareEmployeeDataService.getEmployeeStrategiesPlanCostData(company,
				PROSPECT_STRATEGY_ID, STRATEGY_LIST);

        verify(prospectEmployeeCostService, times(1)).getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
        verify(strategyDataDao, times(1)).getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES);

        assertEquals(4, result.size());

        // Verify Employee1 Data
        List<BenefitPlanRateData> benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee1"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        BenefitPlanRateData benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(100), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(200), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(25), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(50), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee1"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(150), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(250), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("2", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(30), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(80), benefitPlanRateData.getEmployerContribution());

        // Verify Employee2 Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee2"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("2", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(200), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(400), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("4", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(75), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(100), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee2"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(250), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(350), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("4", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(40), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(90), benefitPlanRateData.getEmployerContribution());


        // Verify Employee3ProspectOnly Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee3ProspectOnly"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(300), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(600), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(10), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(15), benefitPlanRateData.getEmployerContribution());

        // Verify Employee3TrinetOnly Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee3TrinetOnly"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(350), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(700), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(15), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(20), benefitPlanRateData.getEmployerContribution());
    }

    @Test
    public void getEmployeeStrategiesPlanCostDataTest_forOmsAllResults() {
        Company company = new Company();
        company.setCode("PROSPECT");
        company.setProspectCompany(true);

        when(CompanyServiceHelper.isTibProspect(Mockito.any(Company.class))).thenReturn(true);

        List<EmployeeCostRes> prospectReturnData = populateProspectReturnData();
        List<StrategyGroupEmployeePlanRateData> trinetReturnData = populateStrategyGroupEmployeePlanRateData(false);
        List<StrategyGroupEmployeePlanRateData> trinetOmsReturnData = populateStrategyGroupEmployeePlanRateOmsData();

        Optional<List<EmployeeCostRes>> prospectData = Optional.ofNullable(prospectReturnData);
        Optional<List<StrategyGroupEmployeePlanRateData>> trinetData = Optional.ofNullable(trinetReturnData);
        Optional<List<StrategyGroupEmployeePlanRateData>> trinetOmsData = Optional.ofNullable(trinetOmsReturnData);

        when(prospectEmployeeCostService.getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(prospectData);
        when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES)).thenReturn(trinetData);
        when(employeeCostSummaryService.getOmsStrategyCostResponse(STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER, true))
                .thenReturn(trinetOmsData);

        when(employeeDataDao.getEmployeeGroupDetailsByStrategy(anyLong())).thenReturn(prepareEmployeeStrategyGroups());

		List<EmployeeStrategyData> result = modelCompareEmployeeDataService.getEmployeeStrategiesPlanCostData(company,
				PROSPECT_STRATEGY_ID, STRATEGY_LIST);

        verify(prospectEmployeeCostService, times(1)).getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
        verify(strategyDataDao, times(1)).getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
        verify(employeeCostSummaryService, times(1)).getOmsStrategyCostResponse(STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER, true);

        assertEquals(4, result.size());

        // Verify Employee1 Data
        List<BenefitPlanRateData> benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee1"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        BenefitPlanRateData benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(100), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(200), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(25), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(50), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee1"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(OMS_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(150), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(250), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("2", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(30), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(80), benefitPlanRateData.getEmployerContribution());

        // Verify Employee2 Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee2"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("2", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(200), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(400), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("4", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(75), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(100), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee2"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(OMS_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(250), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(350), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("4", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(40), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(90), benefitPlanRateData.getEmployerContribution());


        // Verify Employee3ProspectOnly Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee3ProspectOnly"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(300), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(600), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(10), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(15), benefitPlanRateData.getEmployerContribution());

        // Verify Employee3TrinetOnly Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee3TrinetOnly"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(OMS_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(350), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(700), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(TRINET_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(15), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(20), benefitPlanRateData.getEmployerContribution());
    }

    @Test
    public void getEmployeeStrategiesPlanCostDataTest_forOmsOnlyProspectAndOmsResults() {
        Company company = new Company();
        company.setCode("PROSPECT");
        company.setProspectCompany(true);

        when(CompanyServiceHelper.isTibProspect(Mockito.any(Company.class))).thenReturn(true);

        List<EmployeeCostRes> prospectReturnData = populateProspectReturnData();
        List<StrategyGroupEmployeePlanRateData> trinetReturnData = new ArrayList<>();
        List<StrategyGroupEmployeePlanRateData> trinetOmsReturnData = populateStrategyGroupEmployeePlanRateOmsData();

        Optional<List<EmployeeCostRes>> prospectData = Optional.ofNullable(prospectReturnData);
        Optional<List<StrategyGroupEmployeePlanRateData>> trinetData = Optional.ofNullable(trinetReturnData);
        Optional<List<StrategyGroupEmployeePlanRateData>> trinetOmsData = Optional.ofNullable(trinetOmsReturnData);

        when(prospectEmployeeCostService.getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(prospectData);
        when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES)).thenReturn(trinetData);
        when(employeeCostSummaryService.getOmsStrategyCostResponse(STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER, true))
                .thenReturn(trinetOmsData);

        when(employeeDataDao.getEmployeeGroupDetailsByStrategy(anyLong())).thenReturn(prepareEmployeeStrategyGroups());

		List<EmployeeStrategyData> result = modelCompareEmployeeDataService.getEmployeeStrategiesPlanCostData(company,
				PROSPECT_STRATEGY_ID, STRATEGY_LIST);

        verify(prospectEmployeeCostService, times(1)).getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
        verify(strategyDataDao, times(1)).getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
        verify(employeeCostSummaryService, times(1)).getOmsStrategyCostResponse(STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER, true);

        assertEquals(4, result.size());

        // Verify Employee1 Data
        List<BenefitPlanRateData> benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee1"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        BenefitPlanRateData benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(100), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(200), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(25), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(50), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee1"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(OMS_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(150), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(250), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertNull(benefitPlanRateData.getPlanId());
        assertNull(benefitPlanRateData.getCoverageLevel());
        assertNull(benefitPlanRateData.getEmployeeContribution());
        assertNull(benefitPlanRateData.getEmployerContribution());

        // Verify Employee2 Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee2"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("2", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(200), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(400), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("4", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(75), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(100), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee2"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(OMS_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(250), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(350), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertNull(benefitPlanRateData.getPlanId());
        assertNull(benefitPlanRateData.getCoverageLevel());
        assertNull(benefitPlanRateData.getEmployeeContribution());
        assertNull(benefitPlanRateData.getEmployerContribution());

        // Verify Employee3ProspectOnly Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee3ProspectOnly"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), PROSPECT_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(300), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(600), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(PROSPECT_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(10), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(15), benefitPlanRateData.getEmployerContribution());

        // Verify Employee3TrinetOnly Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee3TrinetOnly"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals(String.valueOf(OMS_BENEFIT_PLAN_ID), benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(350), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(700), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertNull(benefitPlanRateData.getPlanId());
        assertNull(benefitPlanRateData.getCoverageLevel());
        assertNull(benefitPlanRateData.getEmployeeContribution());
        assertNull(benefitPlanRateData.getEmployerContribution());
    }

    @Test
    public void getEmployeeStrategiesPlanCostDataTest_NewClientTest() {
        Company company = new Company();
        company.setCode("G562");
        company.setRealmPlanYear(new RealmPlanYear());
        List<StrategyGroupEmployeePlanRateData> trinetReturnData = populateStrategyGroupEmployeePlanRateDataNewClient();
        Optional<List<StrategyGroupEmployeePlanRateData>> trinetData = Optional.ofNullable(trinetReturnData);
        List<ProspectCensusResponse> demographicsData = new ArrayList<>();
        demographicsData.add(prepareProspectCensusResponse("Employee1", "FEmployee1", "LEmployee1"));
        demographicsData.add(prepareProspectCensusResponse("Employee2", "FEmployee2", "LEmployee2"));
        demographicsData.add(prepareProspectCensusResponse("Employee3", "FEmployee3", "LEmployee3"));
        when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES)).thenReturn(trinetData);
        when(bssCoreServiceClient.getCensusByCompanyCode(company.getCode())).thenReturn(demographicsData);
        when(employeeBenefitGroupDao.getEmployeeDetailsByStrategy(TRINET_STRATEGY_ID)).thenReturn(getStringEmployeeStrategyGroupDetailsMap());

		List<EmployeeStrategyData> result = modelCompareEmployeeDataService.getEmployeeStrategiesPlanCostData(company,
				PROSPECT_STRATEGY_ID, STRATEGY_LIST);

        verify(prospectEmployeeCostService, times(0)).getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
        verify(strategyDataDao, times(1)).getStrategyGroupPlanCostByPlanType(company, STRATEGY_LIST, BSSApplicationConstants.PRIMARY_PLAN_TYPES);

        assertEquals(3, result.size());

        // Verify Employee1 Data
        List<BenefitPlanRateData> benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee1"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        BenefitPlanRateData benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals("TrinetMedicalPlanId1", benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(150), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(250), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals("TrinetDentalPlanId1", benefitPlanRateData.getPlanId());
        assertEquals("1", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(30), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(80), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.VISION_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertNull(benefitPlanRateData.getPlanId());
        assertEquals(null, benefitPlanRateData.getCoverageLevel());
        assertNull(benefitPlanRateData.getEmployeeContribution());
        assertNull(benefitPlanRateData.getEmployerContribution());

        List<EmployeeStrategyData> esd =  result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee1"))
                .collect(Collectors.toList());
        assertEquals("FEmployee1",esd.get(0).getEmplFirstName());
        assertEquals("LEmployee1",esd.get(0).getEmplLastName());
        assertEquals(1111, esd.get(0).getStrategyDetails().get(0).getGroupId().intValue());
        assertEquals("Grp1", esd.get(0).getStrategyDetails().get(0).getGroupName());

        // Verify Employee2 Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee2"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals("TrinetMedicalPlanId2", benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(250), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(350), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals("TrinetDentalPlanId2", benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(40), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(90), benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.VISION_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertEquals("TrinetVisionPlanId1", benefitPlanRateData.getPlanId());
        assertEquals("C", benefitPlanRateData.getCoverageLevel());
        assertEquals(BigDecimal.valueOf(40), benefitPlanRateData.getEmployeeContribution());
        assertEquals(BigDecimal.valueOf(90), benefitPlanRateData.getEmployerContribution());

        esd =  result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee2"))
                .collect(Collectors.toList());
        assertEquals("FEmployee2",esd.get(0).getEmplFirstName());
        assertEquals("LEmployee2",esd.get(0).getEmplLastName());
        assertEquals(2222, esd.get(0).getStrategyDetails().get(0).getGroupId().intValue());
        assertEquals("Grp2", esd.get(0).getStrategyDetails().get(0).getGroupName());

        // Verify Employee3 Data
        benefitPlanRateDataList = result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee3"))
                .collect(Collectors.toList()).get(0)
                .getStrategyDetails().stream()
                .filter(a -> Objects.equals(a.getStrategyId(), TRINET_STRATEGY_ID))
                .collect(Collectors.toList()).get(0)
                .getBenefitPlans().stream().collect(Collectors.toList());

        assertEquals(3, benefitPlanRateDataList.size());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertNull(benefitPlanRateData.getPlanId());
        assertNull(benefitPlanRateData.getCoverageLevel());
        assertNull(benefitPlanRateData.getEmployeeContribution());
        assertNull(benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.DENTAL_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertNull(benefitPlanRateData.getPlanId());
        assertNull(benefitPlanRateData.getCoverageLevel());
        assertNull(benefitPlanRateData.getEmployeeContribution());
        assertNull(benefitPlanRateData.getEmployerContribution());

        benefitPlanRateData = benefitPlanRateDataList.stream()
                .filter(a -> Objects.equals(a.getPlanType(), BSSApplicationConstants.VISION_PLAN_TYPE))
                .collect(Collectors.toList()).get(0);

        assertNull(benefitPlanRateData.getPlanId());
        assertNull(benefitPlanRateData.getCoverageLevel());
        assertNull(benefitPlanRateData.getEmployeeContribution());
        assertNull(benefitPlanRateData.getEmployerContribution());

       esd =  result.stream()
                .filter(a -> Objects.equals(a.getEmplId(), "Employee3"))
                .collect(Collectors.toList());
        assertEquals("FEmployee3",esd.get(0).getEmplFirstName());
        assertEquals("LEmployee3",esd.get(0).getEmplLastName());
        assertEquals(1111, esd.get(0).getStrategyDetails().get(0).getGroupId().intValue());
        assertEquals("Grp1", esd.get(0).getStrategyDetails().get(0).getGroupName());
    }

    private List<EmployeeCostRes> populateProspectReturnData() {
        List<EmployeeCostRes> prospectReturnData = new ArrayList<>();

        // Medical
        EmployeeCostRes employeeCostRes = new EmployeeCostRes();
        employeeCostRes.setBenefitTypeCode(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
        employeeCostRes.setEmployeePlanContribution(new ArrayList<>());

        EmployeeCostRes.EmployeePlanContribution employeePlanContribution = populateEmployeePlanContribution("1", "1");
        EmployeeCostRes.PlanContribution planContributionData = populatePlanContributionData(BigDecimal.valueOf(100), BigDecimal.valueOf(200));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        employeePlanContribution = populateEmployeePlanContribution("2", "2");
        planContributionData = populatePlanContributionData(BigDecimal.valueOf(200), BigDecimal.valueOf(400));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        employeePlanContribution = populateEmployeePlanContribution("3ProspectOnly", "C");
        planContributionData = populatePlanContributionData(BigDecimal.valueOf(300), BigDecimal.valueOf(600));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        prospectReturnData.add(employeeCostRes);

        // Dental
        employeeCostRes = new EmployeeCostRes();
        employeeCostRes.setBenefitTypeCode(BSSApplicationConstants.DENTAL_PLAN_TYPE);
        employeeCostRes.setEmployeePlanContribution(new ArrayList<>());

        employeePlanContribution = populateEmployeePlanContribution("1", "C");
        planContributionData = populatePlanContributionData(BigDecimal.valueOf(25), BigDecimal.valueOf(50));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        employeePlanContribution = populateEmployeePlanContribution("2", "4");
        planContributionData = populatePlanContributionData(BigDecimal.valueOf(75), BigDecimal.valueOf(100));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        employeePlanContribution = populateEmployeePlanContribution("3ProspectOnly", "1");
        planContributionData = populatePlanContributionData(BigDecimal.valueOf(10), BigDecimal.valueOf(15));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        prospectReturnData.add(employeeCostRes);

        return prospectReturnData;
    }

    private EmployeeCostRes.EmployeePlanContribution populateEmployeePlanContribution(String employeeIndex, String coverageLevel) {
        EmployeeCostRes.EmployeePlanContribution employeePlanContribution = new EmployeeCostRes.EmployeePlanContribution();
        employeePlanContribution.setEmployeeId("Employee" + employeeIndex);
        employeePlanContribution.setFirstName("First" + employeeIndex);
        employeePlanContribution.setLastName("Last" + employeeIndex);
        employeePlanContribution.setState("CA");
        employeePlanContribution.setCovgLevel(coverageLevel);
        employeePlanContribution.setGroupId(1111);
        employeePlanContribution.setGroupName("Grp1");
        return employeePlanContribution;
    }

    private EmployeeCostRes.PlanContribution populatePlanContributionData(BigDecimal eeCost, BigDecimal erCost) {
        EmployeeCostRes.PlanContribution planContributionData = new EmployeeCostRes.PlanContribution();
        planContributionData.setBenefitPlanId(PROSPECT_BENEFIT_PLAN_ID);
        planContributionData.setBenefitPlanName("BenPlan1");
        planContributionData.setEeCost(eeCost);
        planContributionData.setErCost(erCost);
        planContributionData.setTotalCost(eeCost.add(erCost));
        return planContributionData;
    }

    private List<StrategyGroupEmployeePlanRateData> populateStrategyGroupEmployeePlanRateData(boolean includeMedical) {
        List<StrategyGroupEmployeePlanRateData> strategyGroupEmployeePlanRateDataList = new ArrayList<>();
        StrategyGroupEmployeePlanRateData strategyGroupEmployeePlanRateData;
        // Employee 1
        if (includeMedical) {
            strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                    .emplId("Employee1").strategyId(TRINET_STRATEGY_ID)
                    .groupId(1111).groupName("Grp1")
                    .planType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).benefitPlan(TRINET_BENEFIT_PLAN_ID).planName("TrinetMedicalPlan1")
                    .coverageCode("1").eeRate(BigDecimal.valueOf(150))
                    .erRate(BigDecimal.valueOf(250)).build();
            strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);
        }

        strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee1").strategyId(TRINET_STRATEGY_ID)
                .groupId(1111).groupName("Grp1")
                .planType(BSSApplicationConstants.DENTAL_PLAN_TYPE).benefitPlan(TRINET_BENEFIT_PLAN_ID).planName("TrinetDentalPlan1")
                .coverageCode("2").eeRate(BigDecimal.valueOf(30))
                .erRate(BigDecimal.valueOf(80)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        // Employee 2
        if (includeMedical) {
            strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                    .emplId("Employee2").strategyId(TRINET_STRATEGY_ID)
                    .groupId(1111).groupName("Grp1")
                    .planType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).benefitPlan(TRINET_BENEFIT_PLAN_ID).planName("TrinetMedicalPlan1")
                    .coverageCode("C").eeRate(BigDecimal.valueOf(250))
                    .erRate(BigDecimal.valueOf(350)).build();
            strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);
        }

        strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee2").strategyId(TRINET_STRATEGY_ID)
                .groupId(1111).groupName("Grp1")
                .planType(BSSApplicationConstants.DENTAL_PLAN_TYPE).benefitPlan(TRINET_BENEFIT_PLAN_ID).planName("TrinetDentalPlan1")
                .coverageCode("4").eeRate(BigDecimal.valueOf(40))
                .erRate(BigDecimal.valueOf(90)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        // Employee 3 Trinet Only
        if (includeMedical) {
            strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                    .emplId("Employee3TrinetOnly").strategyId(TRINET_STRATEGY_ID)
                    .groupId(1111).groupName("Grp1")
                    .planType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).benefitPlan(TRINET_BENEFIT_PLAN_ID).planName("TrinetMedicalPlan1")
                    .coverageCode("1").eeRate(BigDecimal.valueOf(350))
                    .erRate(BigDecimal.valueOf(700)).build();
            strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);
        }
        strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee3TrinetOnly").strategyId(TRINET_STRATEGY_ID)
                .groupId(1111).groupName("Grp1")
                .planType(BSSApplicationConstants.DENTAL_PLAN_TYPE).benefitPlan(TRINET_BENEFIT_PLAN_ID).planName("TrinetDentalPlan1")
                .coverageCode("1").eeRate(BigDecimal.valueOf(15))
                .erRate(BigDecimal.valueOf(20)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        return strategyGroupEmployeePlanRateDataList;
    }

    private List<StrategyGroupEmployeePlanRateData> populateStrategyGroupEmployeePlanRateOmsData() {
        List<StrategyGroupEmployeePlanRateData> strategyGroupEmployeePlanRateDataList = new ArrayList<>();

        // Employee 1
        StrategyGroupEmployeePlanRateData strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee1").strategyId(TRINET_STRATEGY_ID)
                .groupId(1111).groupName("Grp1")
                .planType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).benefitPlan(OMS_BENEFIT_PLAN_ID).planName("OmsMedicalPlan1")
                .coverageCode("1").eeRate(BigDecimal.valueOf(150))
                .erRate(BigDecimal.valueOf(250)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        // Employee 2
        strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee2").strategyId(TRINET_STRATEGY_ID)
                .groupId(1111).groupName("Grp1")
                .planType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).benefitPlan(OMS_BENEFIT_PLAN_ID).planName("OmsMedicalPlan1")
                .coverageCode("C").eeRate(BigDecimal.valueOf(250))
                .erRate(BigDecimal.valueOf(350)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        // Employee 3 Trinet Only
        strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee3TrinetOnly").strategyId(TRINET_STRATEGY_ID)
                .groupId(1111).groupName("Grp1")
                .planType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).benefitPlan(OMS_BENEFIT_PLAN_ID).planName("OmsMedicalPlan1")
                .coverageCode("1").eeRate(BigDecimal.valueOf(350))
                .erRate(BigDecimal.valueOf(700)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        return strategyGroupEmployeePlanRateDataList;
    }

    private List<StrategyGroupEmployeePlanRateData> populateStrategyGroupEmployeePlanRateDataNewClient() {
        List<StrategyGroupEmployeePlanRateData> strategyGroupEmployeePlanRateDataList = new ArrayList<>();
        StrategyGroupEmployeePlanRateData strategyGroupEmployeePlanRateData;
        // Employee 1
        strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee1").strategyId(TRINET_STRATEGY_ID)
                .groupId(1111).groupName("Grp1")
                .planType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).benefitPlan("TrinetMedicalPlanId1").planName("TrinetMedicalPlan1")
                .coverageCode("1").eeRate(BigDecimal.valueOf(150))
                .erRate(BigDecimal.valueOf(250)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee1").strategyId(TRINET_STRATEGY_ID)
                .groupId(1111).groupName("Grp1")
                .planType(BSSApplicationConstants.DENTAL_PLAN_TYPE).benefitPlan("TrinetDentalPlanId1").planName("TrinetDentalPlan1")
                .coverageCode("1").eeRate(BigDecimal.valueOf(30))
                .erRate(BigDecimal.valueOf(80)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee2").strategyId(TRINET_STRATEGY_ID)
                .groupId(2222).groupName("Grp2")
                .planType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).benefitPlan("TrinetMedicalPlanId2").planName("TrinetMedicalPlan2")
                .coverageCode("C").eeRate(BigDecimal.valueOf(250))
                .erRate(BigDecimal.valueOf(350)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee2").strategyId(TRINET_STRATEGY_ID)
                .groupId(2222).groupName("Grp2")
                .planType(BSSApplicationConstants.DENTAL_PLAN_TYPE).benefitPlan("TrinetDentalPlanId2").planName("TrinetVisionPlan2")
                .coverageCode("C").eeRate(BigDecimal.valueOf(40))
                .erRate(BigDecimal.valueOf(90)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        strategyGroupEmployeePlanRateData = StrategyGroupEmployeePlanRateData.builder()
                .emplId("Employee2").strategyId(TRINET_STRATEGY_ID)
                .groupId(2222).groupName("Grp2")
                .planType(BSSApplicationConstants.VISION_PLAN_TYPE).benefitPlan("TrinetVisionPlanId1").planName("TrinetVisionPlan1")
                .coverageCode("C").eeRate(BigDecimal.valueOf(40))
                .erRate(BigDecimal.valueOf(90)).build();
        strategyGroupEmployeePlanRateDataList.add(strategyGroupEmployeePlanRateData);

        return strategyGroupEmployeePlanRateDataList;
    }

    private Set<Employee> prepareEmployeeStrategyGroups() {
        Set<Employee> employeeSet = new HashSet<>();

        Employee employee = new Employee();
        employee.setEmplId("Employee3ProspectOnly");
        employee.setBenefitGroupId(2222);
        employee.setBenefitGroupName("TriNetGrp1");
        employeeSet.add(employee);

        return employeeSet;
    }

    private static Map<String, EmployeeStrategyGroupDetails> getStringEmployeeStrategyGroupDetailsMap() {
        Map<String, EmployeeStrategyGroupDetails> emplGroupDetails = new HashMap<>();
        EmployeeStrategyGroupDetails sgd = new EmployeeStrategyGroupDetails();
        sgd.setFutureGroupId(1111);
        sgd.setFutureGroupName("Grp1");
        emplGroupDetails.put("Employee1", sgd);
        emplGroupDetails.put("Employee3", sgd);
        sgd = new EmployeeStrategyGroupDetails();
        sgd.setFutureGroupId(2222);
        sgd.setFutureGroupName("Grp2");
        emplGroupDetails.put("Employee2", sgd);
        return emplGroupDetails;
    }

    private ProspectCensusResponse prepareProspectCensusResponse(String id, String fname, String lname) {
        return ProspectCensusResponse.builder().employeeId(id).firstName(fname).lastName(lname).build();
    }

}