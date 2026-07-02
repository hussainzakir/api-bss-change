package com.trinet.ambis.service.submit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.SchedMidYearFunding;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.QueuedSubmitService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.impl.submit.ResubmitServiceImpl;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.unit.ServiceUnitTest;

import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class ResubmitServiceImplTest extends ServiceUnitTest {

	/*
	 * @Rule public PowerMockRule rule = new PowerMockRule();
	 */

	TestLogger logger = TestLoggerFactory.getTestLogger(ResubmitServiceImpl.class);

	@InjectMocks
	ResubmitServiceImpl resubmitService;

	@Mock
	private StrategyService strategyService;

	@Mock
	private SubmitService submitService;

	@Mock
	private QueuedSubmitService queuedSubmitService;

	@Mock
	private SubmitStatusService submitStatusService;

	@Mock
	private SchedMidYearFundingDao schedMidYearFundingDao;

	@Mock
	private CompanyService companyService;

	@Mock
	private StrategyDataDao strategyDataDao;
	
	@Mock
	private EmailGenService emailGenService;

    private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;
    private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

    @Before
    public void setUp() {
        mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
        mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);

        mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId)
                .thenReturn(USER_ID);
    }

    @After
    public void tearDown() {
        if (mockStaticAppRulesAndConfigsUtils != null) mockStaticAppRulesAndConfigsUtils.close();
        if (mockStaticBSSSecurityUtils != null) mockStaticBSSSecurityUtils.close();
    }

	@After
	public void clearLoggers() {
		TestLoggerFactory.clear();
	}

	private static final long SUBMITTED_STRATEGY_ID = 987654;
	private static final long UNSUBMITTED_STRATEGY_ID = 987655;
	private static final String USER_ID = "000022233390";
	private static final String COMPANY_CODE = "G48";
	private static final String STRATEGY_DATA_JSON = "{\"id\":987654,\"name\":\"Strategy for JUnit Test\",\"type\":\"customized\",\"submitted\":true,\"submitDate\":\"2020-10-21T19:36:59.123-0500\",\"effectiveDate\":\"2021-01-01\",\"endDate\":null,\"comments\":null,\"estimatedTotalCost\":0,\"currentYearTotalCost\":0,\"percentChange\":null,\"totalEmployees\":0,\"headcount\":30,\"totalBudget\":0,\"budgetFactor\":1,\"companyId\":\""
			+ COMPANY_CODE
			+ "\",\"acaFplOpted\":true,\"pkgType\":null,\"costShareType\":\"DFLT\",\"submitStatus\":null,\"canDelete\":false,\"benefitGroups\":[{\"id\":110001,\"name\":\"Staff\",\"type\":\"STD\",\"waitingPeriod\":\"NONE\",\"waitPeriodDescr\":\"Date of hire (DOH)\",\"status\":\"A\",\"benefitProgram\":\"BPG001\",\"companyId\":1001,\"strategyId\":987654,\"strategyGroupId\":2001,\"percentChange\":null,\"estimatedTotalCost\":null,\"headcount\":10,\"benefitOffers\":[{\"type\":\"vision\",\"groupId\":110001,\"description\":\"1V\",\"headcount\":0,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":{\"15\":2.09},\"baseFundingRequired\":true,\"planCarriers\":[{\"id\":15,\"name\":\"EyeMed\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"0052V5\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":6,\"name\":\"VSP\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LXU\",\"parentId\":null,\"regionalCarriers\":[]}],\"planPackage\":{\"id\":0,\"templateId\":0,\"fundingModelId\":0,\"name\":null,\"customized\":false,\"employeePaid\":true,\"fundingBasePlan\":null,\"waiverAllowance\":null,\"strategyId\":null,\"fundingType\":null,\"bsuppExcessOption\":null,\"companyId\":null,\"fundingBasePlans\":[],\"planCarrierIds\":[],\"benefitPlans\":[],\"coverageLevelFunding\":{},\"coverageLevelFundingFlatMax\":{},\"headCountPlans\":[],\"bsuppSelectedVolPlanTypes\":[]},\"benefitPlans\":[{\"id\":\"002LYD\",\"planCarrierId\":6,\"name\":\"VSP Vision Plus Optional\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"002LXW\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020924,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"002LYD\",\"headcount\":4,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":10.2,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020925,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"002LYD\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":20.42,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020927,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"002LYD\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":34.91,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020926,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"002LYD\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":21.83,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"0052WA\",\"planCarrierId\":15,\"name\":\"Aetna EyeMed Plus Opt VA\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"0052VB\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020928,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":12.59,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020929,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":24,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020931,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":36.97,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020930,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":25.15,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"002LYB\",\"planCarrierId\":6,\"name\":\"VSP Vision Optional\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"002LXU\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020932,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"002LYB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":6.62,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020933,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"002LYB\",\"headcount\":1,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":13.23,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020935,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"002LYB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":22.63,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020934,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"002LYB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":14.16,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"0052WB\",\"planCarrierId\":15,\"name\":\"Aetna EyeMed Plus Opt NY\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"0052VC\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020940,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":12.59,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020941,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":24,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020943,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":36.97,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020942,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":25.15,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]}],\"additionalBenefitOffers\":[]},{\"type\":\"medical\",\"groupId\":110001,\"description\":\"10\",\"headcount\":0,\"waiverHeadcount\":23,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":{\"1\":213.5,\"5\":2636.5,\"12\":165.5},\"baseFundingRequired\":true,\"planCarriers\":[{\"id\":30,\"name\":\"Tufts MA\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]},{\"id\":19,\"name\":\"UHC HI\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]},{\"id\":18,\"name\":\"Kaiser HI\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]},{\"id\":12,\"name\":\"Florida Blue\",\"mandatory\":false,\"restricted\":true,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}],\"planPackage\":{\"id\":0,\"templateId\":0,\"fundingModelId\":989691,\"name\":null,\"customized\":true,\"employeePaid\":false,\"fundingBasePlan\":null,\"waiverAllowance\":100,\"strategyId\":987654,\"fundingType\":\"BSUPP\",\"bsuppExcessOption\":2,\"companyId\":null,\"fundingBasePlans\":[],\"planCarrierIds\":[],\"benefitPlans\":[],\"coverageLevelFunding\":{\"employeePlusChild\":200,\"employeePlusSpouse\":200,\"employee\":200,\"employeePlusFamily\":200},\"coverageLevelFundingFlatMax\":{},\"headCountPlans\":[],\"bsuppSelectedVolPlanTypes\":[\"1D\",\"1V\",\"21\",\"25\",\"27\",\"2Y\",\"30\",\"31\"]},\"benefitPlans\":[{\"id\":\"003L9E\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 FL North\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-N\"],\"contributions\":[{\"id\":1452020984,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":430,\"employerPercent\":46.51162791,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020985,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":993,\"employerPercent\":20.14098691,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020987,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1302,\"employerPercent\":15.36098311,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020986,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":877,\"employerPercent\":22.80501711,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHN\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 FL Central\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-C\"],\"contributions\":[{\"id\":1452020980,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":600,\"employerPercent\":33.33333334,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020981,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1386,\"employerPercent\":14.43001444,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020983,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1817,\"employerPercent\":11.00715466,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020982,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1224,\"employerPercent\":16.33986929,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHO\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 FL North\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-N\"],\"contributions\":[{\"id\":1452020988,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":612,\"employerPercent\":32.67973857,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020989,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1415,\"employerPercent\":14.13427562,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020991,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1856,\"employerPercent\":10.77586207,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020990,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1249,\"employerPercent\":16.01281025,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003L9D\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 FL Central\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-C\"],\"contributions\":[{\"id\":1452020992,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9D\",\"headcount\":1,\"hsaHeadcount\":1,\"mirrorHeadCount\":0,\"planCost\":422,\"employerPercent\":47.39336493,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020993,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9D\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":975,\"employerPercent\":20.51282052,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020995,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9D\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1279,\"employerPercent\":15.63721658,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020994,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9D\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":861,\"employerPercent\":23.22880372,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003L9G\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 NTL\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021000,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":474,\"employerPercent\":42.19409283,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021001,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1095,\"employerPercent\":18.26484019,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021003,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1435,\"employerPercent\":13.93728223,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021002,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":967,\"employerPercent\":20.68252327,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHP\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 FL South\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-S\"],\"contributions\":[{\"id\":1452020996,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":648,\"employerPercent\":30.86419754,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020997,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1496,\"employerPercent\":13.36898396,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020999,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1962,\"employerPercent\":10.19367992,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020998,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1322,\"employerPercent\":15.12859305,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHQ\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 NTL\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021004,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":675,\"employerPercent\":29.62962963,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021005,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1560,\"employerPercent\":12.82051283,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021007,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":2046,\"employerPercent\":9.77517107,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021006,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1377,\"employerPercent\":14.52432825,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003L9F\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 FL South\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-S\"],\"contributions\":[{\"id\":1452021008,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":456,\"employerPercent\":43.85964913,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021009,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1053,\"employerPercent\":18.99335233,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021011,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1382,\"employerPercent\":14.47178003,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021010,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":930,\"employerPercent\":21.50537635,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHJ\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1000 FL Central\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-C\"],\"contributions\":[{\"id\":1452021012,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":669,\"employerPercent\":29.89536622,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021013,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1546,\"employerPercent\":12.93661061,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021015,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":2028,\"employerPercent\":9.86193294,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021014,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1365,\"employerPercent\":14.65201466,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]}],\"additionalBenefitOffers\":[]},{\"type\":\"dental\",\"groupId\":110001,\"description\":\"1D\",\"headcount\":0,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":{\"1\":12.09},\"baseFundingRequired\":true,\"planCarriers\":[{\"id\":16,\"name\":\"Delta\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LW0\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":14,\"name\":\"Guardian\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LW5\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":3,\"name\":\"Metlife\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LW7\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":1,\"name\":\"Aetna\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"005387\",\"parentId\":null,\"regionalCarriers\":[]}],\"planPackage\":{\"id\":0,\"templateId\":0,\"fundingModelId\":0,\"name\":null,\"customized\":false,\"employeePaid\":true,\"fundingBasePlan\":null,\"waiverAllowance\":null,\"strategyId\":null,\"fundingType\":null,\"bsuppExcessOption\":null,\"companyId\":null,\"fundingBasePlans\":[],\"planCarrierIds\":[],\"benefitPlans\":[],\"coverageLevelFunding\":{},\"coverageLevelFundingFlatMax\":{},\"headCountPlans\":[],\"bsuppSelectedVolPlanTypes\":[]},\"benefitPlans\":[{\"id\":\"005VN3\",\"planCarrierId\":1,\"name\":\"Aetna Dental 0 Opt NV\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"005VMI\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021160,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":65.46,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021161,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":134.26,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021163,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":209.54,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021162,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":140.79,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"005VN4\",\"planCarrierId\":1,\"name\":\"Aetna Dental 0 Opt NY\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"005VMJ\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021140,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":69.98,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021141,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":143.55,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021143,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":224.02,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021142,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":150.52,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003LQY\",\"planCarrierId\":16,\"name\":\"Delta Dental 50 Opt NV\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"003LQW\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020828,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":53.36,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020829,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":109.66,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020831,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":170.95,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020830,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":114.86,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003LQZ\",\"planCarrierId\":16,\"name\":\"Delta Dental 50 Opt VA\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"003LQX\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020832,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":53.36,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020833,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":109.66,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020835,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":170.95,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020834,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":114.86,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"005VN9\",\"planCarrierId\":16,\"name\":\"Delta Dental 0 Opt VA\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"005VMO\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021184,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":69.39,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021185,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":142.61,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021187,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":222.31,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021186,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":149.38,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]}],\"additionalBenefitOffers\":[]},{\"type\":\"additionalBenefit\",\"groupId\":110001,\"description\":null,\"headcount\":30,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":null,\"baseFundingRequired\":false,\"planCarriers\":null,\"planPackage\":null,\"benefitPlans\":[],\"additionalBenefitOffers\":[{\"type\":\"LIFE\",\"groupId\":110001,\"description\":\"Life and AD and D\",\"headcount\":30,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":null,\"baseFundingRequired\":false,\"additionalBenefitPlans\":[{\"id\":\"000N5P\",\"description\":\"3X Earnings Basic Life & AD&D\",\"region\":null,\"planCost\":14.87,\"annualCap\":1000000,\"planType\":\"23\",\"standAlone\":false,\"offeredGroupType\":null}]},{\"type\":\"DISABILITY\",\"groupId\":110001,\"description\":\"Short & Long Term Disability Plan Options\",\"headcount\":0,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":null,\"baseFundingRequired\":false,\"additionalBenefitPlans\":[{\"id\":\"560\",\"description\":\"STD & LTD Employee Paid\",\"region\":\"FL\",\"planCost\":0,\"monthlyTotalCost\":0,\"annualCap\":null,\"planType\":null,\"standAlone\":false,\"optionPlans\":[{\"id\":\"000N6E\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD SDI6\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6F\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD Opt6\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6G\",\"planType\":\"30\",\"planDesc\":\"60% STD Employee Paid\",\"planShortDesc\":\"STD SDI7\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6J\",\"planType\":\"30\",\"planDesc\":\"60% STD Employee Paid\",\"planShortDesc\":\"STD Opt7\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6K\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD SDI8\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6L\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD Opt8\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6Z\",\"planType\":\"31\",\"planDesc\":\"50% LTD Employee Paid\",\"planShortDesc\":\"LTD Opt6\",\"planCost\":0,\"planHeadCount\":30,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N70\",\"planType\":\"31\",\"planDesc\":\"60% LTD Employee Paid\",\"planShortDesc\":\"LTD Opt7\",\"planCost\":0,\"planHeadCount\":30,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N71\",\"planType\":\"31\",\"planDesc\":\"60% LTD Employee Paid\",\"planShortDesc\":\"LTD Opt9\",\"planCost\":0,\"planHeadCount\":30,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false}],\"offeredGroupType\":\"STD\"}]}]}],\"coverageLevelHeadCounts\":null,\"isDefault\":true,\"region\":\"All\"}]}";

	@Test
	public void resubmit_whenBandUpdatedCallStrategySummariesForSyncNoSubmittedStrategy() {
		RealmPlanYear rpy = createRealmPlanYar(53, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		boolean submitted = false;
		boolean bandUpdated = true;
		boolean acaStatusUpdated = false;
		Company company = createCompany(bandUpdated, true, acaStatusUpdated, rpy);
		SubmitStatus ss = new SubmitStatus();
		ss.setRealmYrId(Long.valueOf(53));
		StrategyData sd = prepareStrategyData(UNSUBMITTED_STRATEGY_ID, submitted);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, USER_ID, null)).thenReturn(company);
		when(submitStatusService.findLatestSubmitStatusBy(COMPANY_CODE)).thenReturn(ss);
		when(strategyService.getStrategies(company, false, null)).thenReturn(Arrays.asList(sd));

		// Data setup validation.
		assertTrue("Data setup not correct. Company should be renewal", company.isRenewalCompany());
		assertTrue("Data setup not correct. Band code should not be updated", company.isBandCodeUpdated());
		assertFalse("Data setup not correct. ACA status should not be updated", company.isAcaLargeEmplrStatusUpdated());
		assertFalse("Data setup not correct. Strategy should not be submitted", sd.getStrategySummary().isSubmitted());

		StrategyData actualResult = resubmitService.resubmit(COMPANY_CODE, false);

		verify(strategyService, times(1)).getStrategies(company, false, null);
		verify(companyService, times(1)).getCompanyDetails(COMPANY_CODE, false, USER_ID, null);
		verify(submitService, times(0)).submit(any(Company.class), any(StrategyData.class), anyString(), anyBoolean(),
				anyBoolean());
		assertNull("There should not be submitted strategy", actualResult);
	}

	@Test
	public void resubmit_whenBandNotUpdatedForNewCompanyAcaStatusUpdatedCallStrategySummariesForSyncNoSubmittedStrategy() {
		RealmPlanYear rpy = createRealmPlanYar(53, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		boolean submitted = false;
		boolean bandUpdated = false;
		boolean renewalComp = false;
		boolean acaStatusUpdated = true;

		Company company = createCompany(bandUpdated, renewalComp, acaStatusUpdated, rpy);
		SubmitStatus ss = new SubmitStatus();
		ss.setRealmYrId(Long.valueOf(53));
		StrategyData sd = prepareStrategyData(UNSUBMITTED_STRATEGY_ID, submitted);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, USER_ID, null)).thenReturn(company);
		when(submitStatusService.findLatestSubmitStatusBy(COMPANY_CODE)).thenReturn(ss);
		when(strategyService.getStrategies(company, false, null)).thenReturn(Arrays.asList(sd));

		// Data setup validation.
		assertFalse("Data setup not correct. Company should be new", company.isRenewalCompany());
		assertFalse("Data setup not correct. Band code should not be updated", company.isBandCodeUpdated());
		assertTrue("Data setup not correct. ACA status should be updated", company.isAcaLargeEmplrStatusUpdated());
		assertFalse("Data setup not correct. Strategy should not be submitted", sd.getStrategySummary().isSubmitted());

		StrategyData actualResult = resubmitService.resubmit(COMPANY_CODE, false);

		verify(strategyService, times(1)).getStrategies(company, false, null);
		verify(companyService, times(1)).getCompanyDetails(COMPANY_CODE, false, USER_ID, null);
		verify(submitService, times(0)).submit(any(Company.class), any(StrategyData.class), anyString(), anyBoolean(),
				anyBoolean());

		assertNull("There should not be submitted strategy", actualResult);
	}

	@Test
	public void resubmit_whenBandUpdatedForNewCompanyAcaStatusUpdatedCallStrategySummariesForSyncNoSubmittedStrategy() {
		RealmPlanYear rpy = createRealmPlanYar(53, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		boolean submitted = false;
		boolean bandUpdated = true;
		boolean renewalComp = false;
		boolean acaStatusUpdated = true;

		Company company = createCompany(bandUpdated, renewalComp, acaStatusUpdated, rpy);
		SubmitStatus ss = new SubmitStatus();
		ss.setRealmYrId(Long.valueOf(53));
		StrategyData sd = prepareStrategyData(UNSUBMITTED_STRATEGY_ID, submitted);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, USER_ID, null)).thenReturn(company);
		when(submitStatusService.findLatestSubmitStatusBy(COMPANY_CODE)).thenReturn(ss);
		when(strategyService.getStrategies(company, false, null)).thenReturn(Arrays.asList(sd));

		// Data setup validation.
		assertFalse("Data setup not correct. Company should be new", company.isRenewalCompany());
		assertTrue("Data setup not correct. Band code should be updated", company.isBandCodeUpdated());
		assertTrue("Data setup not correct. ACA status should be updated", company.isAcaLargeEmplrStatusUpdated());
		assertFalse("Data setup not correct. Strategy should not be submitted", sd.getStrategySummary().isSubmitted());

		StrategyData actualResult = resubmitService.resubmit(COMPANY_CODE, false);

		verify(strategyService, times(1)).getStrategies(company, false, null);
		verify(companyService, times(1)).getCompanyDetails(COMPANY_CODE, false, USER_ID, null);
		verify(submitService, times(0)).submit(any(Company.class), any(StrategyData.class), anyString(), anyBoolean(),
				anyBoolean());
		assertNull("There should not be submitted strategy", actualResult);
	}

	@Test
	public void resubmit_whenBandUpdatedForRenewalCompanyStrategySummariesForSyncStrategySubmitted() {
		RealmPlanYear rpy = createRealmPlanYar(53, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		boolean bandUpdated = true;
		boolean renewalComp = true;
		boolean acaStatusUpdated = false;

		Company company = createCompany(bandUpdated, renewalComp, acaStatusUpdated, rpy);
		SubmitStatus ss = new SubmitStatus();
		ss.setRealmYrId(Long.valueOf(53));
		StrategyData sd1 = prepareStrategyData(SUBMITTED_STRATEGY_ID, true);
		StrategyData sd2 = prepareStrategyData(UNSUBMITTED_STRATEGY_ID, false);

		// Data setup validation.
		assertTrue("Data setup not correct. Company should be renewal", company.isRenewalCompany());
		assertTrue("Data setup not correct. Band code should be updated", company.isBandCodeUpdated());
		assertTrue("Data setup not correct. Strategy sd1 should be submitted", sd1.getStrategySummary().isSubmitted());
		assertFalse("Data setup not correct. Strategy sd2 should not be submitted",
				sd2.getStrategySummary().isSubmitted());

		when(companyService.getCompanyDetails(COMPANY_CODE, false, USER_ID, null)).thenReturn(company);
		when(submitStatusService.findLatestSubmitStatusBy(COMPANY_CODE)).thenReturn(ss);
		when(strategyService.getStrategies(company, false, null)).thenReturn(Arrays.asList(sd1, sd2));

		StrategyData actualResult = resubmitService.resubmit(COMPANY_CODE, false);

		verify(strategyService, times(1)).getStrategies(company, false, null);
		verify(submitService, times(1)).submit(company, sd1, USER_ID, false, true);
		verify(companyService, times(1)).getCompanyDetails(COMPANY_CODE, false, USER_ID, null);
		assertEquals("Returned strategydata is not correct.", sd1, actualResult);
	}

	@Test
	public void resubmit_whenBandNotUpdatedForNewCompanyAcaStatusUpdatedStrategySummariesForSyncStrategySubmitted() {
		RealmPlanYear rpy = createRealmPlanYar(53, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		boolean bandUpdated = false;
		boolean renewalComp = false;
		boolean acaStatusUpdated = true;

		Company company = createCompany(bandUpdated, renewalComp, acaStatusUpdated, rpy);
		SubmitStatus ss = new SubmitStatus();
		ss.setRealmYrId(Long.valueOf(53));
		StrategyData sd1 = prepareStrategyData(SUBMITTED_STRATEGY_ID, true);
		StrategyData sd2 = prepareStrategyData(UNSUBMITTED_STRATEGY_ID, false);

		// Data setup validation.
		assertFalse("Data setup not correct. Company should be new", company.isRenewalCompany());
		assertFalse("Data setup not correct. Band code should not be updated", company.isBandCodeUpdated());
		assertTrue("Data setup not correct. ACA status updated should be updated",
				company.isAcaLargeEmplrStatusUpdated());
		assertTrue("Data setup not correct. Strategy sd1 should be submitted", sd1.getStrategySummary().isSubmitted());
		assertFalse("Data setup not correct. Strategy sd2 should not be submitted",
				sd2.getStrategySummary().isSubmitted());

		when(companyService.getCompanyDetails(COMPANY_CODE, false, USER_ID, null)).thenReturn(company);
		when(submitStatusService.findLatestSubmitStatusBy(COMPANY_CODE)).thenReturn(ss);
		when(strategyService.getStrategies(company, false, null)).thenReturn(Arrays.asList(sd1, sd2));

		StrategyData actualResult = resubmitService.resubmit(COMPANY_CODE, false);

		verify(strategyService, times(1)).getStrategies(company, false, null);
		verify(submitService, times(1)).submit(company, sd1, USER_ID, false, true);
		verify(companyService, times(1)).getCompanyDetails(COMPANY_CODE, false, USER_ID, null);
		assertEquals("Returned strategydata is not correct.", sd1, actualResult);
	}

	@Test
	public void resubmit_whenBandNotUpdatedForRenewalCompanyStrategySummariesForSyncStrategySubmitted() {
		RealmPlanYear rpy = createRealmPlanYar(53, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		boolean bandUpdated = false;
		boolean renewalComp = true;
		boolean acaStatusUpdated = false;

		Company company = createCompany(bandUpdated, renewalComp, acaStatusUpdated, rpy);
		SubmitStatus ss = new SubmitStatus();
		ss.setRealmYrId(Long.valueOf(53));
		SubmitPayload submitPayload = new SubmitPayload();
		submitPayload.setPayload(STRATEGY_DATA_JSON);		
		ss.setSubmitPayload(submitPayload);
		StrategyData sd = CommonServiceHelper.jsonToObject(STRATEGY_DATA_JSON, StrategyData.class);

		// Data setup validation.
		assertTrue("Data setup not correct. Company should be renewal", company.isRenewalCompany());
		assertFalse("Data setup not correct. Band code should not be updated", company.isBandCodeUpdated());

		when(companyService.getCompanyDetails(COMPANY_CODE, false, USER_ID, null)).thenReturn(company);
		when(submitStatusService.findLatestSubmitStatusBy(COMPANY_CODE)).thenReturn(ss);

		StrategyData actualResult = resubmitService.resubmit(COMPANY_CODE, false);

		verify(submitStatusService, times(1)).findLatestSubmitStatusBy(COMPANY_CODE);
		verify(strategyService, times(0)).getStrategies(company, false, null);
		verify(submitService, times(1)).submit(any(Company.class), any(StrategyData.class), anyString(), anyBoolean(),
				anyBoolean());
		verify(companyService, times(1)).getCompanyDetails(COMPANY_CODE, false, USER_ID, null);
		assertEquals("Returned strategy summary is not correct.", sd.getStrategySummary().getId(),
				actualResult.getStrategySummary().getId());
	}

	@Test
	public void resubmit_whenSubmissionIsForCurrentPlanYearWhenFuturePlanYearExists() {
		RealmPlanYear rpy53 = createRealmPlanYar(53, prepareDate("01-JUL-2022"), prepareDate("30-JUN-2023"));
		RealmPlanYear rpy43 = createRealmPlanYar(43, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		boolean bandUpdated = false;
		boolean renewalComp = false;
		boolean acaStatusUpdated = true;

		Company companyPlanYear53 = createCompany(bandUpdated, renewalComp, acaStatusUpdated, rpy53);
		Company companyPlanYear43 = createCompany(bandUpdated, renewalComp, acaStatusUpdated, rpy43);
		SubmitStatus ss = new SubmitStatus();
		ss.setRealmYrId(Long.valueOf(43));
		StrategyData sd1 = prepareStrategyData(SUBMITTED_STRATEGY_ID, true);

		// Data setup validation.
		assertFalse("Data setup not correct. Company should be new", companyPlanYear43.isRenewalCompany());
		assertFalse("Data setup not correct. Band code should not be updated", companyPlanYear43.isBandCodeUpdated());
		assertTrue("Data setup not correct. ACA status updated should be updated",
				companyPlanYear43.isAcaLargeEmplrStatusUpdated());
		assertTrue("Data setup not correct. Strategy sd1 should be submitted", sd1.getStrategySummary().isSubmitted());

		when(companyService.getCompanyDetails(COMPANY_CODE, false, USER_ID, null)).thenReturn(companyPlanYear53);
		when(companyService.getCompanyDetails(COMPANY_CODE, true, USER_ID, null)).thenReturn(companyPlanYear43);
		when(submitStatusService.findLatestSubmitStatusBy(COMPANY_CODE)).thenReturn(ss);
		when(strategyService.getStrategies(companyPlanYear43, false, null)).thenReturn(Arrays.asList(sd1));

		StrategyData actualResult = resubmitService.resubmit(COMPANY_CODE, false);

		verify(strategyService, times(1)).getStrategies(companyPlanYear43, false, null);
		verify(submitService, times(1)).submit(companyPlanYear43, sd1, USER_ID, false, true);
		verify(companyService, times(1)).getCompanyDetails(COMPANY_CODE, false, USER_ID, null);
		assertEquals("Returned strategydata is not correct.", sd1, actualResult);
	}

	@Test
	public void bandcodeResubmit_renewalCompanyBenStartDtBeforePlanYrStartDtEffDateDoNotMatch() {
		boolean bandCodeUpdate = true;
		String benStartDt = "07-JUL-2020";
		Date effDt = prepareDate("01-AUG-2022");
		RealmPlanYear rpy = createRealmPlanYar(43, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		Company company = createCompany(bandCodeUpdate, true, false, rpy);
		company.setBenefitStartDate(benStartDt);
		company.setTransitionPeriod(false);
		company.setRenewalOpen(false);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, "BANDCHANGE", null)).thenReturn(company);

		resubmitService.bandcodeResubmit(COMPANY_CODE, effDt);

		Mockito.verifyNoMoreInteractions(strategyService);
		Mockito.verifyNoInteractions(submitService);
		Mockito.verifyNoInteractions(queuedSubmitService);

	}

	@Test
	public void bandcodeResubmit_dualCompanyBenStartDtAfterPlanYrStartDtEffDateSameAsPriorPlanYrStartDt() {
		boolean bandCodeUpdate = true;
		String benStartDt = "07-JUL-2021";
		Date effDt = prepareDate("01-JUL-2021");
		RealmPlanYear rpy43 = createRealmPlanYar(43, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		Company company43 = createCompany(bandCodeUpdate, true, false, rpy43);
		company43.setBenefitStartDate(benStartDt);
		company43.setTransitionPeriod(false);
		company43.setRenewalOpen(false);

		RealmPlanYear rpy53 = createRealmPlanYar(53, prepareDate("01-JUL-2022"), prepareDate("30-JUN-2023"));
		Company company53 = createCompany(bandCodeUpdate, true, false, rpy53);
		company53.setBenefitStartDate(benStartDt);
		company53.setTransitionPeriod(false);
		company53.setRenewalOpen(true);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, "BANDCHANGE", null)).thenReturn(company53);
		when(companyService.getCompanyDetails(COMPANY_CODE, true, "BANDCHANGE", null)).thenReturn(company43);

		resubmitService.bandcodeResubmit(COMPANY_CODE, effDt);

		Mockito.verifyNoMoreInteractions(strategyService);
		Mockito.verifyNoInteractions(submitService);
		Mockito.verifyNoInteractions(queuedSubmitService);
	}

	@Test
	public void bandcodeResubmit_newCompanyBenStartDtAfterPlanYrStartDtAndEffDateIsPlanYrStartDt() {
		boolean bandCodeUpdate = true;
		String benStartDt = "07-JUL-2022";
		Date effDt = prepareDate("01-AUG-2022");
		RealmPlanYear rpy = createRealmPlanYar(53, prepareDate("01-JUL-2022"), prepareDate("30-JUN-2023"));
		Company company = createCompany(bandCodeUpdate, false, false, rpy);
		company.setBenefitStartDate(benStartDt);
		company.setTransitionPeriod(false);
		company.setRenewalOpen(false);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, "BANDCHANGE", null)).thenReturn(company);

		resubmitService.bandcodeResubmit(COMPANY_CODE, effDt);

		Mockito.verifyNoMoreInteractions(strategyService);
		Mockito.verifyNoInteractions(submitService);
		Mockito.verifyNoInteractions(queuedSubmitService);

	}

	@Test
	public void bandcodeResubmit_currentRenewalCompanyBandcodeNotUpdated() {
		boolean bandCodeUpdate = false;
		Date effDt = prepareDate("01-JUL-2021");
		RealmPlanYear rpy = createRealmPlanYar(43, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		Company company = createCompany(bandCodeUpdate, true, false, rpy);
		company.setBenefitStartDate("07-JUL-2018");
		company.setTransitionPeriod(false);
		company.setRenewalOpen(false);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, "BANDCHANGE", null)).thenReturn(company);

		resubmitService.bandcodeResubmit(COMPANY_CODE, effDt);

		Mockito.verifyNoMoreInteractions(strategyService);
		Mockito.verifyNoInteractions(submitService);
		Mockito.verifyNoInteractions(queuedSubmitService);

	}

	@Test
	public void bandcodeResubmit_midYearScheduleExistsAfterEffectiveDt() {
		boolean bandCodeUpdate = true;
		Date effDt = prepareDate("01-JUL-2021");
		RealmPlanYear rpy = createRealmPlanYar(43, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		Company company = createCompany(bandCodeUpdate, true, false, rpy);
		company.setBenefitStartDate("07-JUL-2018");
		company.setTransitionPeriod(false);
		company.setRenewalOpen(false);

		SchedMidYearFunding smf = new SchedMidYearFunding();
		smf.setMidYearFundingEffDate(prepareDate("02-JUL-2021"));

		when(companyService.getCompanyDetails(COMPANY_CODE, false, "BANDCHANGE", null)).thenReturn(company);
		when(schedMidYearFundingDao.findByCompanyId(company.getId())).thenReturn(Arrays.asList(smf));

		resubmitService.bandcodeResubmit(COMPANY_CODE, effDt);

		Mockito.verifyNoInteractions(strategyService);
		Mockito.verifyNoInteractions(submitService);
		Mockito.verifyNoInteractions(queuedSubmitService);
    }

	@Test
	public void bandcodeResubmit_doesntHaveAlreadySubmittedStrategy() {
		boolean bandCodeUpdate = true;
		Date effDt = prepareDate("01-JUL-2021");
		RealmPlanYear rpy = createRealmPlanYar(43, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		Company company = createCompany(bandCodeUpdate, true, false, rpy);
		company.setBenefitStartDate("07-JUL-2018");
		company.setTransitionPeriod(false);
		company.setRenewalOpen(false);
		
		StrategyData sd = prepareStrategyData(SUBMITTED_STRATEGY_ID, false);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, "BANDCHANGE", null)).thenReturn(company);
		when(strategyService.getStrategies(company, false, null)).thenReturn(Arrays.asList(sd));

		resubmitService.bandcodeResubmit(COMPANY_CODE, effDt);

		Mockito.verifyNoInteractions(submitService);
		Mockito.verifyNoInteractions(queuedSubmitService);
	}

	@Test
	public void bandcodeResubmit_renewalCompanyBandChangeForPreviousPlanYear() throws Exception {
		boolean bandCodeUpdate43 = true;
		RealmPlanYear rpy43 = createRealmPlanYar(43, prepareDate("01-JUL-2021"), prepareDate("30-JUN-2022"));
		RealmPlanYear rpy53 = createRealmPlanYar(53, prepareDate("01-JUL-2022"), prepareDate("30-JUN-2023"));
		Company company43 = createCompany(bandCodeUpdate43, true, false, rpy43);
		company43.setBenefitStartDate("07-JUL-2018");
		company43.setTransitionPeriod(false);
		company43.setRenewalOpen(false);

		boolean bandCodeUpdate53 = false;
		Company company53 = createCompany(bandCodeUpdate53, true, false, rpy53);
		company53.setBenefitStartDate("07-JUL-2018");
		company53.setTransitionPeriod(true);
		company53.setRenewalOpen(false);

		StrategyData sd = prepareStrategyData(SUBMITTED_STRATEGY_ID, true);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, "BANDCHANGE", null)).thenReturn(company53);
		when(companyService.getCompanyDetails(COMPANY_CODE, true, "BANDCHANGE", null)).thenReturn(company43);
		when(strategyService.getStrategies(company43, false, null)).thenReturn(Arrays.asList(sd));
        mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isSubmitQueuingEnabled).thenReturn(true);

		resubmitService.bandcodeResubmit(COMPANY_CODE, prepareDate("01-JUL-2021"));

		Mockito.verifyNoInteractions(submitService);
		verify(queuedSubmitService, times(1)).createSubmitProcess(company43, sd, "BANDCODE_RESUBMIT", true);

		assertThat(logger.getLoggingEvents().size(), is(0));
	}

	@Test
	public void bandcodeResubmit_newCompanyEffDtIsSameAsBenStartDt() throws Exception {
		RealmPlanYear rpy = createRealmPlanYar(53, prepareDate("01-JUL-2022"), prepareDate("30-JUN-2023"));
		Company company = createCompany(true, false, false, rpy);
		company.setBenefitStartDate("07-JUL-2022");
		company.setTransitionPeriod(false);
		company.setRenewalOpen(false);

		StrategyData sd = prepareStrategyData(SUBMITTED_STRATEGY_ID, true);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, "BANDCHANGE", null)).thenReturn(company);
		when(strategyService.getStrategies(company, false, null)).thenReturn(Arrays.asList(sd));
        mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isSubmitQueuingEnabled).thenReturn(true);

		resubmitService.bandcodeResubmit(COMPANY_CODE, prepareDate("07-JUL-2022"));

		Mockito.verifyNoInteractions(submitService);
		verify(queuedSubmitService, times(1)).createSubmitProcess(company, sd, "BANDCODE_RESUBMIT", true);

		assertThat(logger.getLoggingEvents().size(), is(0));
	}
	
	@Test
	public void testResubmitService_callsCreateAsyncDefaultSubmitProcess() {
		Date effDate = prepareDate("01-Jul-2021");
		Company company = createCompany(true, true, false, createRealmPlanYar(53, effDate, prepareDate("30-Jun-2022")));
		company.setBenefitStartDate("01-Jul-2021");
		StrategyData strategyData = prepareStrategyData(SUBMITTED_STRATEGY_ID, true);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, "BANDCHANGE", null)).thenReturn(company);
		when(strategyService.getStrategies(company, false, null)).thenReturn(Arrays.asList(strategyData));
        mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isSubmitQueuingEnabled).thenReturn(true);
		resubmitService.bandcodeResubmit(COMPANY_CODE, effDate);

		verify(queuedSubmitService, times(1)).createSubmitProcess(eq(company), eq(strategyData),
				eq("BANDCODE_RESUBMIT"), eq(true));
	}

	@Test
	public void testResubmitService_asyncSubmitProcessHandlesException() {
		Date effDate = prepareDate("01-Jul-2021");
		Company company = createCompany(true, true, false, createRealmPlanYar(53, effDate, prepareDate("30-Jun-2022")));
		company.setBenefitStartDate("01-Jul-2021");
		StrategyData strategyData = prepareStrategyData(SUBMITTED_STRATEGY_ID, true);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, "BANDCHANGE", null)).thenReturn(company);
		when(strategyService.getStrategies(company, false, null)).thenReturn(Arrays.asList(strategyData));

		// Simulate exception during submit process
		try {
			resubmitService.bandcodeResubmit(COMPANY_CODE, effDate);
		} catch (RuntimeException e) {
			assertEquals("Simulated failure", e.getMessage());
		}
	}
	
	private RealmPlanYear createRealmPlanYar(long id, Date startDt, Date endDt) {
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(id);
		rpy.setPlanYearStart(startDt);
		rpy.setPlanYearEnd(endDt);
		return rpy;
	}

	private Company createCompany(boolean bandCodeUpdated, boolean renewalComp, boolean acaStatusUpdated,
			RealmPlanYear rpy) {
		Company comp = new Company();
		comp.setId(1111);
		comp.setCode(COMPANY_CODE);
		comp.setBandCodeUpdated(bandCodeUpdated);
		comp.setRenewalCompany(renewalComp);
		comp.setRealmPlanYearId(rpy.getId());
		comp.setAcaLargeEmplrStatusUpdated(acaStatusUpdated);
		comp.setRealmPlanYear(rpy);
		return comp;
	}

	private Date prepareDate(String dateInString) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		try {
			return formatter.parse(dateInString);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private StrategyData prepareStrategyData(long strategyId, boolean submitted) {
		StrategyData sd = new StrategyData();
		StrategySummary sm = new StrategySummary();
		sm.setSubmitted(submitted);
		sm.setId(strategyId);
		sd.setStrategySummary(sm);
		return sd;
	}

//	private void assertLogLevelAndMessage(String msg, Date effDt, String benStartDt) {
//		assertLogLevelAndMessage(msg, effDt);
//		assertEquals(logger.getLoggingEvents().get(0).getArguments().get(2), benStartDt);
//	}
//
//	private void assertLogLevelAndMessage(String msg, Date effDt) {
//		assertThat(logger.getLoggingEvents().get(0).getLevel(), is(Level.ERROR));
//		assertThat(logger.getLoggingEvents().get(0).getMessage(), is(msg));
//
//		assertEquals(logger.getLoggingEvents().get(0).getArguments().get(0), COMPANY_CODE);
//		assertEquals(logger.getLoggingEvents().get(0).getArguments().get(1), effDt);
//	}

}
