package com.trinet.ambis.service.submit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.configuration.AsyncConfig;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.email.EmailService;
import com.trinet.ambis.service.email.dto.StrategySubmissionFailureDto;
import com.trinet.ambis.service.email.dto.SupportEmailDto;
import com.trinet.ambis.service.email.impl.AleServiceImpl;
import com.trinet.ambis.service.impl.submit.SubmitServiceImpl;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;


@RunWith(MockitoJUnitRunner.class)
public class SubmitServiceImplTest extends ServiceUnitTest {

	/*
	 * @Rule public PowerMockRule rule = new PowerMockRule();
	 */

	@InjectMocks
	SubmitServiceImpl submitService;

	@Mock
	private EmailService emailService;

	@Mock
	private EmailGenService emailGenService;

	@Mock
	private SubmitStatusService submitStatusService;

	@Mock
	private StrategyDao strategyDao;

	@Mock
	private RealmPlanYearDao realmPlanYearDao;

	@Mock
	private PsDao psDao;
	
	@Mock
	private CompanyService companyService;

	@Mock
	private FlexRateService flexRateService;

	private Executor executor;

	@Mock
	private AleServiceImpl aleService;
    private MockedStatic<CommonUtils> mockStaticCommonUtils;
    private MockedStatic<RulesAndConfigsUtils> mockStaticRulesAndConfigsUtils;

	@Before
	public void setUp() {
        mockStaticCommonUtils = Mockito.mockStatic(CommonUtils.class);
        mockStaticRulesAndConfigsUtils = Mockito.mockStatic(RulesAndConfigsUtils.class);
        mockStaticRulesAndConfigsUtils.when(() -> RulesAndConfigsUtils.getRenewalRiskType(any(Long.class)))
                .thenReturn(RiskTypeEnum.DIFFERENTIALS);
        AsyncConfig asyncConfig = new AsyncConfig();
		if (executor == null) {
			executor = asyncConfig.getAsyncExecutor();
			submitService.setExecutor(executor);
		}
	}

    @After
    public void tearDown() {
        if (mockStaticCommonUtils != null) mockStaticCommonUtils.close();
        if (mockStaticRulesAndConfigsUtils != null) mockStaticRulesAndConfigsUtils.close();
    }

	private static final long STRATEGY_ID = 987654;
	private static final long SUBMIT_STATUS_ID = 1;
	private static final String USER_ID = "000022233390";
	private static final String COMPANY_CODE = "G48";
	private static final String CONFIRMATION_NUMBER = "UISDNLKANDKNAD";
	private static final Long REALM_PLYR_ID = 32L;
	private static final String SERVICE_ORDER_NUMBER = "ADAS23223";
	private static final String STRATEGY_DATA_JSON = "{\"id\":987654,\"name\":\"Strategy for JUnit Test\",\"type\":\"customized\",\"submitted\":true,\"submitDate\":\"2020-10-21T19:36:59.123-0500\",\"effectiveDate\":\"2021-01-01\",\"endDate\":null,\"comments\":null,\"estimatedTotalCost\":0,\"currentYearTotalCost\":0,\"percentChange\":null,\"totalEmployees\":0,\"headcount\":30,\"totalBudget\":0,\"budgetFactor\":1,\"companyId\":\""
			+ COMPANY_CODE
			+ "\",\"acaFplOpted\":true,\"pkgType\":null,\"costShareType\":\"DFLT\",\"submitStatus\":null,\"canDelete\":false,\"benefitGroups\":[{\"id\":110001,\"name\":\"Staff\",\"type\":\"STD\",\"waitingPeriod\":\"NONE\",\"waitPeriodDescr\":\"Date of hire (DOH)\",\"status\":\"A\",\"benefitProgram\":\"BPG001\",\"companyId\":1001,\"strategyId\":987654,\"strategyGroupId\":2001,\"percentChange\":null,\"estimatedTotalCost\":null,\"headcount\":10,\"benefitOffers\":[{\"type\":\"vision\",\"groupId\":110001,\"description\":\"1V\",\"headcount\":0,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":{\"15\":2.09},\"baseFundingRequired\":true,\"planCarriers\":[{\"id\":15,\"name\":\"EyeMed\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"0052V5\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":6,\"name\":\"VSP\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LXU\",\"parentId\":null,\"regionalCarriers\":[]}],\"planPackage\":{\"id\":0,\"templateId\":0,\"fundingModelId\":0,\"name\":null,\"customized\":false,\"employeePaid\":true,\"fundingBasePlan\":null,\"waiverAllowance\":null,\"strategyId\":null,\"fundingType\":null,\"bsuppExcessOption\":null,\"companyId\":null,\"fundingBasePlans\":[],\"planCarrierIds\":[],\"benefitPlans\":[],\"coverageLevelFunding\":{},\"coverageLevelFundingFlatMax\":{},\"headCountPlans\":[],\"bsuppSelectedVolPlanTypes\":[]},\"benefitPlans\":[{\"id\":\"002LYD\",\"planCarrierId\":6,\"name\":\"VSP Vision Plus Optional\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"002LXW\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020924,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"002LYD\",\"headcount\":4,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":10.2,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020925,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"002LYD\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":20.42,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020927,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"002LYD\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":34.91,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020926,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"002LYD\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":21.83,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"0052WA\",\"planCarrierId\":15,\"name\":\"Aetna EyeMed Plus Opt VA\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"0052VB\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020928,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":12.59,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020929,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":24,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020931,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":36.97,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020930,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":25.15,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"002LYB\",\"planCarrierId\":6,\"name\":\"VSP Vision Optional\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"002LXU\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020932,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"002LYB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":6.62,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020933,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"002LYB\",\"headcount\":1,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":13.23,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020935,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"002LYB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":22.63,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020934,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"002LYB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":14.16,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"0052WB\",\"planCarrierId\":15,\"name\":\"Aetna EyeMed Plus Opt NY\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"0052VC\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020940,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":12.59,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020941,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":24,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020943,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":36.97,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020942,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":25.15,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]}],\"additionalBenefitOffers\":[]},{\"type\":\"medical\",\"groupId\":110001,\"description\":\"10\",\"headcount\":0,\"waiverHeadcount\":23,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":{\"1\":213.5,\"5\":2636.5,\"12\":165.5},\"baseFundingRequired\":true,\"planCarriers\":[{\"id\":30,\"name\":\"Tufts MA\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]},{\"id\":19,\"name\":\"UHC HI\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]},{\"id\":18,\"name\":\"Kaiser HI\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]},{\"id\":12,\"name\":\"Florida Blue\",\"mandatory\":false,\"restricted\":true,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}],\"planPackage\":{\"id\":0,\"templateId\":0,\"fundingModelId\":989691,\"name\":null,\"customized\":true,\"employeePaid\":false,\"fundingBasePlan\":null,\"waiverAllowance\":100,\"strategyId\":987654,\"fundingType\":\"BSUPP\",\"bsuppExcessOption\":2,\"companyId\":null,\"fundingBasePlans\":[],\"planCarrierIds\":[],\"benefitPlans\":[],\"coverageLevelFunding\":{\"employeePlusChild\":200,\"employeePlusSpouse\":200,\"employee\":200,\"employeePlusFamily\":200},\"coverageLevelFundingFlatMax\":{},\"headCountPlans\":[],\"bsuppSelectedVolPlanTypes\":[\"1D\",\"1V\",\"21\",\"25\",\"27\",\"2Y\",\"30\",\"31\"]},\"benefitPlans\":[{\"id\":\"003L9E\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 FL North\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-N\"],\"contributions\":[{\"id\":1452020984,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":430,\"employerPercent\":46.51162791,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020985,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":993,\"employerPercent\":20.14098691,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020987,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1302,\"employerPercent\":15.36098311,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020986,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":877,\"employerPercent\":22.80501711,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHN\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 FL Central\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-C\"],\"contributions\":[{\"id\":1452020980,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":600,\"employerPercent\":33.33333334,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020981,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1386,\"employerPercent\":14.43001444,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020983,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1817,\"employerPercent\":11.00715466,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020982,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1224,\"employerPercent\":16.33986929,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHO\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 FL North\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-N\"],\"contributions\":[{\"id\":1452020988,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":612,\"employerPercent\":32.67973857,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020989,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1415,\"employerPercent\":14.13427562,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020991,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1856,\"employerPercent\":10.77586207,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020990,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1249,\"employerPercent\":16.01281025,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003L9D\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 FL Central\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-C\"],\"contributions\":[{\"id\":1452020992,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9D\",\"headcount\":1,\"hsaHeadcount\":1,\"mirrorHeadCount\":0,\"planCost\":422,\"employerPercent\":47.39336493,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020993,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9D\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":975,\"employerPercent\":20.51282052,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020995,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9D\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1279,\"employerPercent\":15.63721658,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020994,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9D\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":861,\"employerPercent\":23.22880372,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003L9G\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 NTL\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021000,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":474,\"employerPercent\":42.19409283,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021001,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1095,\"employerPercent\":18.26484019,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021003,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1435,\"employerPercent\":13.93728223,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021002,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":967,\"employerPercent\":20.68252327,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHP\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 FL South\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-S\"],\"contributions\":[{\"id\":1452020996,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":648,\"employerPercent\":30.86419754,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020997,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1496,\"employerPercent\":13.36898396,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020999,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1962,\"employerPercent\":10.19367992,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020998,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1322,\"employerPercent\":15.12859305,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHQ\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 NTL\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021004,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":675,\"employerPercent\":29.62962963,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021005,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1560,\"employerPercent\":12.82051283,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021007,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":2046,\"employerPercent\":9.77517107,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021006,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1377,\"employerPercent\":14.52432825,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003L9F\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 FL South\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-S\"],\"contributions\":[{\"id\":1452021008,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":456,\"employerPercent\":43.85964913,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021009,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1053,\"employerPercent\":18.99335233,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021011,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1382,\"employerPercent\":14.47178003,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021010,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":930,\"employerPercent\":21.50537635,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHJ\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1000 FL Central\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-C\"],\"contributions\":[{\"id\":1452021012,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":669,\"employerPercent\":29.89536622,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021013,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1546,\"employerPercent\":12.93661061,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021015,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":2028,\"employerPercent\":9.86193294,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021014,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1365,\"employerPercent\":14.65201466,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]}],\"additionalBenefitOffers\":[]},{\"type\":\"dental\",\"groupId\":110001,\"description\":\"1D\",\"headcount\":0,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":{\"1\":12.09},\"baseFundingRequired\":true,\"planCarriers\":[{\"id\":16,\"name\":\"Delta\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LW0\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":14,\"name\":\"Guardian\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LW5\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":3,\"name\":\"Metlife\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LW7\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":1,\"name\":\"Aetna\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"005387\",\"parentId\":null,\"regionalCarriers\":[]}],\"planPackage\":{\"id\":0,\"templateId\":0,\"fundingModelId\":0,\"name\":null,\"customized\":false,\"employeePaid\":true,\"fundingBasePlan\":null,\"waiverAllowance\":null,\"strategyId\":null,\"fundingType\":null,\"bsuppExcessOption\":null,\"companyId\":null,\"fundingBasePlans\":[],\"planCarrierIds\":[],\"benefitPlans\":[],\"coverageLevelFunding\":{},\"coverageLevelFundingFlatMax\":{},\"headCountPlans\":[],\"bsuppSelectedVolPlanTypes\":[]},\"benefitPlans\":[{\"id\":\"005VN3\",\"planCarrierId\":1,\"name\":\"Aetna Dental 0 Opt NV\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"005VMI\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021160,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":65.46,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021161,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":134.26,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021163,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":209.54,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021162,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":140.79,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"005VN4\",\"planCarrierId\":1,\"name\":\"Aetna Dental 0 Opt NY\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"005VMJ\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021140,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":69.98,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021141,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":143.55,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021143,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":224.02,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021142,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":150.52,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003LQY\",\"planCarrierId\":16,\"name\":\"Delta Dental 50 Opt NV\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"003LQW\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020828,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":53.36,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020829,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":109.66,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020831,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":170.95,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020830,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":114.86,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003LQZ\",\"planCarrierId\":16,\"name\":\"Delta Dental 50 Opt VA\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"003LQX\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020832,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":53.36,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020833,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":109.66,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020835,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":170.95,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020834,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":114.86,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"005VN9\",\"planCarrierId\":16,\"name\":\"Delta Dental 0 Opt VA\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"005VMO\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021184,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":69.39,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021185,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":142.61,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021187,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":222.31,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021186,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":149.38,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]}],\"additionalBenefitOffers\":[]},{\"type\":\"additionalBenefit\",\"groupId\":110001,\"description\":null,\"headcount\":30,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":null,\"baseFundingRequired\":false,\"planCarriers\":null,\"planPackage\":null,\"benefitPlans\":[],\"additionalBenefitOffers\":[{\"type\":\"LIFE\",\"groupId\":110001,\"description\":\"Life and AD and D\",\"headcount\":30,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":null,\"baseFundingRequired\":false,\"additionalBenefitPlans\":[{\"id\":\"000N5P\",\"description\":\"3X Earnings Basic Life & AD&D\",\"region\":null,\"planCost\":14.87,\"annualCap\":1000000,\"planType\":\"23\",\"standAlone\":false,\"offeredGroupType\":null}]},{\"type\":\"DISABILITY\",\"groupId\":110001,\"description\":\"Short & Long Term Disability Plan Options\",\"headcount\":0,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":null,\"baseFundingRequired\":false,\"additionalBenefitPlans\":[{\"id\":\"560\",\"description\":\"STD & LTD Employee Paid\",\"region\":\"FL\",\"planCost\":0,\"monthlyTotalCost\":0,\"annualCap\":null,\"planType\":null,\"standAlone\":false,\"optionPlans\":[{\"id\":\"000N6E\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD SDI6\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6F\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD Opt6\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6G\",\"planType\":\"30\",\"planDesc\":\"60% STD Employee Paid\",\"planShortDesc\":\"STD SDI7\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6J\",\"planType\":\"30\",\"planDesc\":\"60% STD Employee Paid\",\"planShortDesc\":\"STD Opt7\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6K\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD SDI8\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6L\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD Opt8\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6Z\",\"planType\":\"31\",\"planDesc\":\"50% LTD Employee Paid\",\"planShortDesc\":\"LTD Opt6\",\"planCost\":0,\"planHeadCount\":30,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N70\",\"planType\":\"31\",\"planDesc\":\"60% LTD Employee Paid\",\"planShortDesc\":\"LTD Opt7\",\"planCost\":0,\"planHeadCount\":30,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N71\",\"planType\":\"31\",\"planDesc\":\"60% LTD Employee Paid\",\"planShortDesc\":\"LTD Opt9\",\"planCost\":0,\"planHeadCount\":30,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false}],\"offeredGroupType\":\"STD\"}]}]}],\"coverageLevelHeadCounts\":null,\"isDefault\":true,\"region\":\"All\"}]}";

	/*
	 * When statement upload successful and email sent to client then update the
	 * statement upload status to SUCCESS and emailSent to true. Do not send the
	 * submission issue report to support team since email was sent to client.
	 */
	@Test
	public void preSubmit_regularSubmissionAndUploadSuccessAndEmailSentToClient() {
		Company company = new Company();
		company.setAleUpdated(1);
		company.setAleUpdatedNewClient(true);
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().queuedSubmit(true).withEmailInfo()
				.buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPreSubmit();

		Answer<SubmissionInfo> answer = new Answer<SubmissionInfo>() {
			public SubmissionInfo answer(InvocationOnMock invocation) throws Throwable {
				SubmissionInfo submissionInfo = invocation.getArgument(1);
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.SUCCESS);
				submissionInfo.getEmailInfo().setClientEmailSent(true);
				return submissionInfo;
			}
		};
		Mockito.doAnswer(answer).when(emailService).uploadStatementAndSendConfirmation(company, submissionInfo);
		Mockito.doNothing().when(aleService).sendConfirmationEmail(company, submissionInfo);
		Mockito.doNothing().when(companyService).updateAleUpdatedFlag(company, 0);

		CompletableFuture<Void> result = submitService.preSubmit(company, submissionInfo);

		try {
			result.get();
		} catch (InterruptedException | ExecutionException e) {
			fail("Error occured while running preSubmit for regular submission");
		}

		verify(submitStatusService, times(1)).update(BSSApplicationConstants.SUCCESS, true, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), any(Date.class), anyString(),
				anyString());
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, times(0)).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
		verify(aleService, times(1)).sendConfirmationEmail(company, submissionInfo);
		verify(companyService, times(1)).updateAleUpdatedFlag(company, 0);

	}

	/**
	 * Given: AleUpdatedNewClient is false and Statement upload will succeed and email sent to client.
	 * When: preSubmit is called.
	 * Then:
	 *  - Update the statement upload status to SUCCESS
	 *  - Do not send confirmation ALE email.
	 *  - Do not update ALE flag.
	 */
	@Test
	public void preSubmit_regularSubmissionAndUploadSuccessAndEmailSentToClientAndAleEmail() {
		Company company = new Company();
		company.setAleUpdatedNewClient(false);
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().queuedSubmit(true).withEmailInfo()
				.buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPreSubmit();

		Answer<SubmissionInfo> answer = new Answer<SubmissionInfo>() {
			public SubmissionInfo answer(InvocationOnMock invocation) throws Throwable {
				SubmissionInfo submissionInfo = invocation.getArgument(1);
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.SUCCESS);
				submissionInfo.getEmailInfo().setClientEmailSent(true);
				return submissionInfo;
			}
		};
		Mockito.doAnswer(answer).when(emailService).uploadStatementAndSendConfirmation(company, submissionInfo);


		CompletableFuture<Void> result = submitService.preSubmit(company, submissionInfo);

		try {
			result.get();
		} catch (InterruptedException | ExecutionException e) {
			fail("Error occured while running preSubmit for regular submission");
		}
		verify(submitStatusService, times(1)).update(BSSApplicationConstants.SUCCESS, true, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), any(Date.class), anyString(),
				anyString());
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, times(0)).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
		verify(aleService, times(0)).sendConfirmationEmail(company, submissionInfo);
		verify(companyService, times(0)).updateAleUpdatedFlag(company, 0);

	}

	/*
	 * When statement upload fails then update the statement upload status to ERROR
	 * and emailSent to false. Send submission issue report to support team since
	 * email was not sent to client.
	 */
	@Test
	public void preSubmit_regularSubmission_StatementUploadFails() throws InterruptedException {
		Company company = new Company();
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().queuedSubmit(true).withEmailInfo()
				.buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPreSubmit();

		Answer<SubmissionInfo> answer = new Answer<SubmissionInfo>() {
			public SubmissionInfo answer(InvocationOnMock invocation) throws Throwable {
				SubmissionInfo submissionInfo = invocation.getArgument(1);
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.ERROR);
				return submissionInfo;
			}
		};
		Mockito.doAnswer(answer).when(emailService).uploadStatementAndSendConfirmation(company, submissionInfo);

		CompletableFuture<Void> result = submitService.preSubmit(company, submissionInfo);

		try {
			result.get();
		} catch (InterruptedException | ExecutionException e) {
			fail("Error occured while running preSubmit for regular submission");
		}

		verify(submitStatusService, times(1)).update(BSSApplicationConstants.ERROR, false, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), any(Date.class), anyString(),
				anyString());
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, times(1)).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
	}

	/*
	 * When statement upload SUCCESS and client email fails then update the
	 * statement upload status to SUCCESS and emailSent to false. Send submission
	 * issue report to support team since email was not sent to client.
	 */
	@Test
	public void preSubmit_regularSubmission_ClientEmailSubmissionFails() throws InterruptedException {
		Company company = new Company();
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().queuedSubmit(true).withEmailInfo()
				.buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPreSubmit();

		Answer<SubmissionInfo> answer = new Answer<SubmissionInfo>() {
			public SubmissionInfo answer(InvocationOnMock invocation) throws Throwable {
				SubmissionInfo submissionInfo = invocation.getArgument(1);
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.SUCCESS);
				submissionInfo.getEmailInfo().setClientEmailSent(false);
				return submissionInfo;
			}
		};
		Mockito.doAnswer(answer).when(emailService).uploadStatementAndSendConfirmation(company, submissionInfo);

		CompletableFuture<Void> result = submitService.preSubmit(company, submissionInfo);

		try {
			result.get();
		} catch (InterruptedException | ExecutionException e) {
			fail("Error occured while running preSubmit for regular submission");
		}

		verify(submitStatusService, times(1)).update(BSSApplicationConstants.SUCCESS, false, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), any(Date.class), anyString(),
				anyString());
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, times(1)).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
	}

	/*
	 * When the submission is default submit then do not send email to support for
	 * presubmit.
	 */
	@Test
	public void preSubmit_defaultSubmitEmailToClientFailsThenDoNotSendSupportEmail() {
		Company company = new Company();
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().queuedSubmit(true)
				.defaultSubmit(true).withEmailInfo().buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus)
				.buildSubmissionInfo().buildPreSubmit();

		Answer<SubmissionInfo> answer = new Answer<SubmissionInfo>() {
			public SubmissionInfo answer(InvocationOnMock invocation) throws Throwable {
				SubmissionInfo submissionInfo = invocation.getArgument(1);
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.SUCCESS);
				submissionInfo.getEmailInfo().setClientEmailSent(false);
				return submissionInfo;
			}
		};
		Mockito.doAnswer(answer).when(emailService).uploadStatementAndSendConfirmation(company, submissionInfo);

		CompletableFuture<Void> result = submitService.preSubmit(company, submissionInfo);

		try {
			result.get();
		} catch (InterruptedException | ExecutionException e) {
			fail("Error occured while running preSubmit for regular submission");
		}

		verify(submitStatusService, times(1)).update(BSSApplicationConstants.SUCCESS, false, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), any(Date.class), anyString(),
				anyString());
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, times(0)).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
	}

	/*
	 * PreSubmit should handle the exception and log it.
	 */
	@Test
	public void preSubmit_handleException() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setEmplId(USER_ID);
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().queuedSubmit(true)
				.defaultSubmit(false).withEmailInfo().buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus)
				.buildSubmissionInfo().buildPreSubmit();

		Answer<SubmissionInfo> answer = new Answer<SubmissionInfo>() {
			public SubmissionInfo answer(InvocationOnMock invocation) throws Throwable {
				SubmissionInfo submissionInfo = invocation.getArgument(1);
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.SUCCESS);
				submissionInfo.getEmailInfo().setClientEmailSent(false);
				return submissionInfo;
			}
		};
		Mockito.doAnswer(answer).when(emailService).uploadStatementAndSendConfirmation(company, submissionInfo);
		doThrow(new RuntimeException()).when(emailGenService).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
		
		CompletableFuture<Void> result = submitService.preSubmit(company, submissionInfo);

		try {
			result.get();
		} catch (InterruptedException | ExecutionException e) {
			fail("Error occured while running preSubmit for regular submission");
		}

		verify(submitStatusService, times(1)).update(BSSApplicationConstants.SUCCESS, false, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), anyString(),
				anyString());
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, times(1)).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
		CommonUtils.logExceptions(any(Exception.class), any(Logger.class), anyString(), anyString());

	}

	/*
	 * POSTSUBMIT - If regular submission then 1. Update the submit status 2.
	 * Do not upload statement, do not send client email and do not
	 * update confirm statement status.
	 */
	@Test
	public void postSubmit_regularSubmission() {
		Company company = new Company();
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(true)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().bdmCounts(null)
				.buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPostSubmit();

		submitService.postSubmit(company, submissionInfo);

		verify(emailService, times(0)).uploadStatementAndSendConfirmation(any(Company.class),
				any(SubmissionInfo.class));
		verify(submitStatusService, times(1)).update(eq(BSSApplicationConstants.SUCCESS), any(Date.class),
				eq(CONFIRMATION_NUMBER), eq(COMPANY_CODE));
		verify(submitStatusService, times(0)).update(BSSApplicationConstants.SUCCESS, false, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, times(0)).generateSubmissionIssueReport(anyString(), anyString());
	}

	/*
	 * POSTSUBMIT - if resubmit and submit status is SUCCESS and send email flag is
	 * true then 1. Upload statement and send client email 2. Update the submit
	 * status and confirmation statement status.
	 */
	@Test
	public void postSubmit_resubmitSuccess() {
		Company company = new Company();
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(true)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().resubmit(true).withEmailInfo()
				.buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPostSubmit();

		Answer<SubmissionInfo> answer = new Answer<SubmissionInfo>() {
			public SubmissionInfo answer(InvocationOnMock invocation) throws Throwable {
				SubmissionInfo submissionInfo = invocation.getArgument(1);
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.SUCCESS);
				submissionInfo.getEmailInfo().setClientEmailSent(true);
				return submissionInfo;
			}
		};
		Mockito.doAnswer(answer).when(emailService).uploadStatementAndSendConfirmation(company, submissionInfo);

		submitService.postSubmit(company, submissionInfo);

		verify(emailService, times(1)).uploadStatementAndSendConfirmation(company, submissionInfo);
		verify(submitStatusService, times(1)).update(eq(BSSApplicationConstants.SUCCESS), any(Date.class),
				eq(CONFIRMATION_NUMBER), eq(COMPANY_CODE));
		verify(submitStatusService, times(1)).update(BSSApplicationConstants.SUCCESS, true, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, times(0)).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
	}

	/*
	 * POSTSUBMIT - if resubmit and submit status is SUCCESS and send email flag is
	 * false then 1. Upload statement and do not send client email 2. Update the
	 * submit status and confirmation statement status. 3. do not send email to
	 * support
	 */
	@Test
	public void postSubmit_resubmitSuccessEmailFlagFalse() {
		Company company = new Company();
		boolean sendClientEmail = false;

		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(true)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(sendClientEmail).build();
		
		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().resubmit(true).withEmailInfo()
				.buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPostSubmit();

		Answer<SubmissionInfo> answer = new Answer<SubmissionInfo>() {
			public SubmissionInfo answer(InvocationOnMock invocation) throws Throwable {
				SubmissionInfo submissionInfo = invocation.getArgument(1);
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.SUCCESS);
				return submissionInfo;
			}
		};
		Mockito.doAnswer(answer).when(emailService).uploadStatementAndSendConfirmation(company, submissionInfo);

		submitService.postSubmit(company, submissionInfo);

		SupportEmailDto strategySubmissionFailureDto = StrategySubmissionFailureDto.builder()
				.companyCode(company.getCode())
				.confirmationNumber(submissionInfo.getSubmitStatusInfo().getConfirmationNumber())
				.userId(submissionInfo.getSubmitStatusInfo().getUserId())
				.sendToBSS(false)
				.build();

		verify(emailService, times(1)).uploadStatementAndSendConfirmation(company, submissionInfo);
		verify(submitStatusService, times(1)).update(eq(BSSApplicationConstants.SUCCESS), any(Date.class),
				eq(CONFIRMATION_NUMBER), eq(COMPANY_CODE));
		verify(submitStatusService, times(1)).update(BSSApplicationConstants.SUCCESS, false, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, times(0)).createSupportEmail(strategySubmissionFailureDto);
	}

	/*
	 * POSTSUBMIT - if resubmit and submit status is SUCCESS and send email flag is
	 * true but statement upload fails then 1. Do not send client email 2. Update
	 * the submit status and confirmation statement status. 3. Send email to support
	 */
	@Test
	public void postSubmit_resubmitSuccessStatementUploadFails() {
		Company company = new Company();
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(true)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().resubmit(true).withEmailInfo()
				.buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPostSubmit();

		Answer<SubmissionInfo> answer = new Answer<SubmissionInfo>() {
			public SubmissionInfo answer(InvocationOnMock invocation) throws Throwable {
				SubmissionInfo submissionInfo = invocation.getArgument(1);
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.ERROR);
				submissionInfo.getEmailInfo().setClientEmailSent(false);
				return submissionInfo;
			}
		};
		Mockito.doAnswer(answer).when(emailService).uploadStatementAndSendConfirmation(company, submissionInfo);

		Awaitility.await().until(() -> {
			submitService.postSubmit(company, submissionInfo);
			return true;
		});

		verify(emailService, times(1)).uploadStatementAndSendConfirmation(company, submissionInfo);
		verify(submitStatusService, times(1)).update(eq(BSSApplicationConstants.SUCCESS), any(Date.class),
				eq(CONFIRMATION_NUMBER), eq(COMPANY_CODE));
		verify(submitStatusService, times(1)).update(BSSApplicationConstants.ERROR, false, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, timeout(1000).times(1)).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
	}
	

	/*
	 * POSTSUBMIT - if resubmit and submit status is ERROR and send email flag is
	 * true but statement upload fails then 1. Do not send client email 2. Update
	 * the submit status and confirmation statement status. 3. Send two emails to support,
	 * one for submission issue report and one for submission failure.
	 */
//	@Test
	public void postSubmit_resubmitErrors() {
		Company company = new Company();
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().id(SUBMIT_STATUS_ID).strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(true)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().resubmit(true).withEmailInfo()
				.resendEmail(true).buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus)
				.buildSubmissionInfo().buildPostSubmit();

		Answer<SubmissionInfo> answer = new Answer<SubmissionInfo>() {
			public SubmissionInfo answer(InvocationOnMock invocation) throws Throwable {
				SubmissionInfo submissionInfo = invocation.getArgument(1);
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.ERROR);
				submissionInfo.getEmailInfo().setClientEmailSent(false);
				return submissionInfo;
			}
		};
		Mockito.doAnswer(answer).when(emailService).uploadStatementAndSendConfirmation(company, submissionInfo);

		Awaitility.await().until(() -> {
			submitService.postSubmit(company, submissionInfo);
			return true;
		});

		SupportEmailDto strategySubmissionFailureDto = StrategySubmissionFailureDto.builder()
				.companyCode(COMPANY_CODE)
				.confirmationNumber(CONFIRMATION_NUMBER)
				.userId(USER_ID)
				.sendToBSS(false)
				.build();

		verifyNoMoreInteractions(emailService);
		verify(submitStatusService, times(1)).update(eq(BSSApplicationConstants.ERROR), eq(CONFIRMATION_NUMBER),
				eq(COMPANY_CODE));
		verify(submitStatusService, times(1)).update(BSSApplicationConstants.ERROR, false, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(emailGenService, times(1)).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
		verify(emailGenService, times(1)).createSupportEmail(strategySubmissionFailureDto);
	}

	@Test
	public void postSubmit_resubmitFailsErrorMessageIsBlank() {
		Company company = new Company();
		SubmitPayload submitPayload = SubmitPayload.builder().payload(STRATEGY_DATA_JSON).build();
		SubmitStatus submitStatus = SubmitStatus.builder().id(SUBMIT_STATUS_ID).strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMPANY_CODE).emailSentStatus(true)
				.confirmationNumber(CONFIRMATION_NUMBER).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().resubmit(true).withEmailInfo()
				.buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus)
				.exception(new NullPointerException("")).buildSubmissionInfo().buildPostSubmit();
		when(submitStatusService.findByConfirmationNumber(COMPANY_CODE, CONFIRMATION_NUMBER)).thenReturn(submitStatus);

		Awaitility.await().until(() -> {
			submitService.postSubmit(company, submissionInfo);
			return true;
		});

		verify(emailService, times(0)).uploadStatementAndSendConfirmation(company, submissionInfo);
		verify(submitStatusService, times(1)).update(eq(BSSApplicationConstants.ERROR), any(Date.class),
				eq(CONFIRMATION_NUMBER), eq(COMPANY_CODE));
		verify(submitStatusService, times(1)).update(BSSApplicationConstants.ERROR, false, CONFIRMATION_NUMBER,
				COMPANY_CODE);
		verify(submitStatusService, times(0)).update(anyString(), anyString(), anyString());
		verify(submitStatusService, times(1)).findByConfirmationNumber(COMPANY_CODE, CONFIRMATION_NUMBER);
		verify(submitStatusService, times(1)).createUpdateSubmitStatus(submitStatus);
		verify(emailGenService, timeout(1000).times(1)).generateSubmissionIssueReport(COMPANY_CODE, USER_ID);
	}
	
	@Test
	public void submit_testNoPresubmitWhenReSubmission() {
		long companyId = 12345;
		Company company = new Company();
		company.setId(companyId);
		company.setCode(COMPANY_CODE);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(REALM_PLYR_ID);
		company.setRealmPlanYear(realmPlanYear);
		StrategyData strategyData = new StrategyData();
		StrategySummary strategySummary = new StrategySummary();
		strategySummary.setId(STRATEGY_ID);
		strategyData.setStrategySummary(strategySummary);
		boolean sendClientEmail = true;
		boolean isResubmit = true;
		Strategy strategy = new Strategy();

		String payload = CommonServiceHelper.objectToJsonString(strategyData);

		ArgumentCaptor<SubmitStatus> submitStatusArgCaptor = ArgumentCaptor.forClass(SubmitStatus.class);

		when(strategyDao.findByIdAndCompanyIdAndStatus(STRATEGY_ID, companyId, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(strategy);
		when(strategyDao.saveAndFlush(strategy)).thenReturn(strategy);
		when(submitStatusService.createUpdateSubmitStatus(submitStatusArgCaptor.capture()))
				.thenReturn(SubmitStatus.builder().build());

		try {
			Awaitility.await().until(() -> {
				submitService.submit(company, strategyData, USER_ID, sendClientEmail, isResubmit);
				return true;
			});
		} catch (Exception e) {
		}

		verify(strategyDao, times(1)).saveAndFlush(strategy);
		verify(submitStatusService, times(1)).createUpdateSubmitStatus(submitStatusArgCaptor.getValue());
		verify(emailService, times(0)).uploadStatementAndSendConfirmation(any(Company.class),
				any(SubmissionInfo.class));

		assertEquals(STRATEGY_ID, submitStatusArgCaptor.getValue().getStrategyId());
		assertEquals("PROCESSING", submitStatusArgCaptor.getValue().getStatus());
		assertEquals(null, submitStatusArgCaptor.getValue().getSubmitError());
		assertEquals(payload, submitStatusArgCaptor.getValue().getSubmitPayload().getPayload());
		assertEquals(USER_ID, submitStatusArgCaptor.getValue().getUserId());
		assertEquals(COMPANY_CODE, submitStatusArgCaptor.getValue().getCompany());
		assertTrue(StringUtils.isNotEmpty(submitStatusArgCaptor.getValue().getConfirmationNumber()));
		assertEquals(REALM_PLYR_ID, submitStatusArgCaptor.getValue().getRealmYrId());
		assertEquals(null, submitStatusArgCaptor.getValue().getServiceOrder());
		assertEquals(null, submitStatusArgCaptor.getValue().getStatementUploadStatus());
		assertEquals(null, submitStatusArgCaptor.getValue().getUpdateTime());
		assertEquals(true, submitStatusArgCaptor.getValue().getSendEmail());

		assertTrue("Strategy should be set submitted", strategy.isSubmitted());
		assertFalse("Strategy should not be set as default submit", strategy.isDefaultSubmit());
	}

	@Test
	public void submit_testPresubmitWhenRegularSubmission() {
		long companyId = 12345;
		Company company = new Company();
		company.setId(companyId);
		company.setCode(COMPANY_CODE);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(REALM_PLYR_ID);
		company.setRealmPlanYear(realmPlanYear);
		StrategyData strategyData = new StrategyData();
		StrategySummary strategySummary = new StrategySummary();
		strategySummary.setId(STRATEGY_ID);
		strategyData.setStrategySummary(strategySummary);
		boolean sendClientEmail = true;
		boolean isResubmit = false;
		Strategy strategy = new Strategy();

		String payload = CommonServiceHelper.objectToJsonString(strategyData);

		ArgumentCaptor<SubmitStatus> submitStatusArgCaptor = ArgumentCaptor.forClass(SubmitStatus.class);

		when(strategyDao.findByIdAndCompanyIdAndStatus(STRATEGY_ID, companyId, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(strategy);
		when(strategyDao.saveAndFlush(strategy)).thenReturn(strategy);
		when(submitStatusService.createUpdateSubmitStatus(submitStatusArgCaptor.capture()))
				.thenReturn(SubmitStatus.builder().build());

		try {
			Awaitility.await().until(() -> {
				submitService.submit(company, strategyData, USER_ID, sendClientEmail, isResubmit);
				return true;
			});
		} catch (Exception e) {
			assertEquals("java.lang.IllegalStateException", e.getClass().getName());
		}

		verify(strategyDao, times(1)).saveAndFlush(strategy);
		verify(submitStatusService, times(1)).createUpdateSubmitStatus(submitStatusArgCaptor.getValue());
			//	any(SubmissionInfo.class));

		assertEquals(STRATEGY_ID, submitStatusArgCaptor.getValue().getStrategyId());
		assertEquals("PROCESSING", submitStatusArgCaptor.getValue().getStatus());
		assertEquals(null, submitStatusArgCaptor.getValue().getSubmitError());
		assertEquals(payload, submitStatusArgCaptor.getValue().getSubmitPayload().getPayload());
		assertEquals(USER_ID, submitStatusArgCaptor.getValue().getUserId());
		assertEquals(COMPANY_CODE, submitStatusArgCaptor.getValue().getCompany());
		assertTrue(StringUtils.isNotEmpty(submitStatusArgCaptor.getValue().getConfirmationNumber()));
		assertEquals(REALM_PLYR_ID, submitStatusArgCaptor.getValue().getRealmYrId());
		assertEquals(null, submitStatusArgCaptor.getValue().getServiceOrder());
		assertEquals(null, submitStatusArgCaptor.getValue().getStatementUploadStatus());
		assertEquals(null, submitStatusArgCaptor.getValue().getUpdateTime());
		assertEquals(true, submitStatusArgCaptor.getValue().getSendEmail());

		assertTrue("Strategy should be set submitted", strategy.isSubmitted());
		assertFalse("Strategy should not be set as default submit", strategy.isDefaultSubmit());
	}

	@Test
	public void submit_testDefaultSubmissionForDefaultCompany() {
		String companyCode = "DEFAULT";
		String quarter = "Q3";
		RealmPlanYear realmPlYr = new RealmPlanYear();
		realmPlYr.setId(32);

		ArgumentCaptor<Date> dateArgCaptor = ArgumentCaptor.forClass(Date.class);

		when(realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter)).thenReturn(realmPlYr);
		when(psDao.getUnsubmittedClients(eq(quarter), eq(32L), dateArgCaptor.capture(), eq("ACTIVE")))
				.thenReturn(new ArrayList<>());
		
		

		try {
			Awaitility.await().until(() -> {
				submitService.defaultSubmit(companyCode, quarter, USER_ID);
				return true;
			});
		} catch (Exception e) {
		}

		verify(psDao, times(1)).getUnsubmittedClients(eq(quarter), eq(32L), any(Date.class), eq("ACTIVE"));
	}

	@Test
	public void submit_testDefaultSubmissionForACompany() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setRealmPlanYearId(123L);
		String quarter = "Q3";
		RealmPlanYear realmPlYr = new RealmPlanYear();
		realmPlYr.setId(32);
		when(companyService.getCompanyDetails(COMPANY_CODE, false, USER_ID, null)).thenReturn(company);
		try {
			Awaitility.await().until(() -> {
				submitService.defaultSubmit(COMPANY_CODE, quarter, USER_ID);
				return true;
			});
		} catch (Exception e) {
		}

		verify(psDao, times(0)).getUnsubmittedClients(anyString(), any(Long.class), any(Date.class), anyString());
	}

	@Test
	public void defaultSubmitTermedClients_nonDefaultCompany_doesNothing() {
		String companyCode = "ABC123";
		String quarter = "Q1";

		submitService.defaultSubmitTermedClients(companyCode, quarter);

		verify(realmPlanYearDao, times(0)).getMaxRealmPlanYearByQuarter(anyString());
		verify(flexRateService, times(0)).getClientsWithRates(anyString(), anyString());
		verify(psDao, times(0)).getUnsubmittedClients(anyString(), any(Long.class), any(Date.class), anyString());
	}

	@Test
	public void defaultSubmitTermedClients_defaultCompany_noRealmPlanYear_doesNothing() {
		String companyCode = "DEFAULT";
		String quarter = "Q1";
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(55L);
		rpy.setPlanYearStart(new Date());

		when(realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter)).thenReturn(rpy);
		when(flexRateService.getClientsWithRates(eq(quarter), anyString()))
				.thenReturn(Arrays.asList("T01", "T02"));
		when(psDao.getUnsubmittedClients(eq(quarter), eq(55L), any(Date.class), eq("TERMED")))
				.thenReturn(new ArrayList<>());

		submitService.defaultSubmitTermedClients(companyCode, quarter);

		verify(realmPlanYearDao, times(1)).getMaxRealmPlanYearByQuarter(quarter);
		verify(flexRateService, times(1)).getClientsWithRates(eq(quarter), anyString());
		verify(psDao, times(1)).getUnsubmittedClients(eq(quarter), eq(55L), any(Date.class), eq("TERMED"));
	}

	@Test
	public void defaultSubmitTermedClients_defaultCompany_noTermedCompanies_doesNothing() {
		String companyCode = "DEFAULT";
		String quarter = "Q1";
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(55L);
		rpy.setPlanYearStart(new Date());

		when(realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter)).thenReturn(rpy);
		when(flexRateService.getClientsWithRates(eq(quarter), anyString()))
				.thenReturn(Arrays.asList("T01", "T02"));
		when(psDao.getUnsubmittedClients(eq(quarter), eq(55L), any(Date.class), eq("TERMED")))
				.thenReturn(new ArrayList<>());

		submitService.defaultSubmitTermedClients(companyCode, quarter);

		verify(realmPlanYearDao, times(1)).getMaxRealmPlanYearByQuarter(quarter);
		verify(flexRateService, times(1)).getClientsWithRates(eq(quarter), anyString());
		verify(psDao, times(1)).getUnsubmittedClients(eq(quarter), eq(55L), any(Date.class), eq("TERMED"));
	}

	@Test
	public void defaultSubmitTermedClients_defaultCompany_submitsQualifiedTermedClients() {
		String companyCode = "DEFAULT";
		String quarter = "Q1";
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(55L);
		rpy.setPlanYearStart(new Date());

		when(realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter)).thenReturn(rpy);
		when(flexRateService.getClientsWithRates(eq(quarter), anyString()))
				.thenReturn(Arrays.asList("T02", "T03", "T04"));
		when(psDao.getUnsubmittedClients(eq(quarter), eq(55L), any(Date.class), eq("TERMED")))
				.thenReturn(Arrays.asList("T01", "T02", "T03"));

		Company termedCompany = new Company();
		termedCompany.setRealmPlanYearId(55L);
		when(companyService.getCompanyDetails(anyString(), eq(false), anyString(), eq(null)))
				.thenReturn(termedCompany);

		submitService.defaultSubmitTermedClients(companyCode, quarter);

		verify(realmPlanYearDao, times(1)).getMaxRealmPlanYearByQuarter(quarter);
		verify(flexRateService, times(1)).getClientsWithRates(eq(quarter), anyString());
		verify(psDao, times(1)).getUnsubmittedClients(eq(quarter), eq(55L), any(Date.class), eq("TERMED"));
		// T02 and T03 are in both sets, so only they should be submitted
		verify(companyService, times(1)).getCompanyDetails(eq("T02"), eq(false), anyString(), eq(null));
		verify(companyService, times(1)).getCompanyDetails(eq("T03"), eq(false), anyString(), eq(null));
	}

	@Test
	public void defaultSubmitTermedClients_defaultCompany_continuesOnFailure() {
		String companyCode = "DEFAULT";
		String quarter = "Q1";
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(55L);
		rpy.setPlanYearStart(new Date());

		when(realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter)).thenReturn(rpy);
		when(flexRateService.getClientsWithRates(eq(quarter), anyString()))
				.thenReturn(Arrays.asList("T01", "T02"));
		when(psDao.getUnsubmittedClients(eq(quarter), eq(55L), any(Date.class), eq("TERMED")))
				.thenReturn(Arrays.asList("T01", "T02"));

		Company termedCompany = new Company();
		termedCompany.setRealmPlanYearId(55L);
		// First call throws, second succeeds
		when(companyService.getCompanyDetails(eq("T01"), eq(false), anyString(), eq(null)))
				.thenThrow(new RuntimeException("boom"));
		when(companyService.getCompanyDetails(eq("T02"), eq(false), anyString(), eq(null)))
				.thenReturn(termedCompany);

		submitService.defaultSubmitTermedClients(companyCode, quarter);

		// Both companies should be attempted even if first one fails
		verify(companyService, times(1)).getCompanyDetails(eq("T01"), eq(false), anyString(), eq(null));
		verify(companyService, times(1)).getCompanyDetails(eq("T02"), eq(false), anyString(), eq(null));
	}
}
