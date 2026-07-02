package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.map.MultiKeyMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.helper.PlanCompareExportHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.BenefitsPlanViewService;
import com.trinet.ambis.service.BplService;
import com.trinet.ambis.service.impl.PlanCompareServiceImpl;
import com.trinet.ambis.service.model.EmpBenPlanMapping;
import com.trinet.ambis.service.model.plancompare.Attribute;
import com.trinet.ambis.service.model.plancompare.BenPlanCompareResponse;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.model.plancompare.PlanCompareTemplate;
import com.trinet.ambis.service.prospect.enums.BenefitTypeEnum;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.BplServiceRestClient;
import com.trinet.domain.common.ReturnResponse;

@RunWith(MockitoJUnitRunner.class)
public class PlanCompareServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	PlanCompareServiceImpl planCompareServiceImpl;

	@Mock
	private EmployeeDataDao employeeDataDao;

	@Mock
	private StrategyDataDao strategyDataDao;

	@Mock
	private PlanCompareExportHelper planCompareExportHelper;

	@Mock
	private BenefitsPlanViewService benefitsPlanViewService;
	
	@Mock
	private  BplService bplService;
	
	@Mock
	private BenefitPlanService benefitPlanService;
	
	@Mock
	private HttpServletRequest httpRequest;
	@Mock
	private BplServiceRestClient bplServiceRestClient;

	private Company company;

	private final String COMPANY_CODE = "G48";
	private final long REALM_ID = 58;
	private static String TEMPLATE_TYPE = "bss_export_template";
	
	private MockedStatic<BSSMessageConfig> bssMessageConfigMock;
    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMock;
    private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;
    private Date effectiveDate;

	@Before
	public void setUp() {
		bssMessageConfigMock = Mockito.mockStatic(BSSMessageConfig.class);
        bssSecurityUtilsMock = Mockito.mockStatic(BSSSecurityUtils.class);
        if (mockStaticAppRulesAndConfigsUtils == null) {
			mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
		}
        effectiveDate = new Date();
		company = new Company();
		company.setId(12345L);
		company.setCode(COMPANY_CODE);
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(REALM_ID);
		company.setRealmPlanYear(rpy);
	}
	
	@After
	public void tearDown() {
		bssMessageConfigMock.close();
		bssSecurityUtilsMock.close();
		if (mockStaticAppRulesAndConfigsUtils != null) {
			mockStaticAppRulesAndConfigsUtils.close();
		}
	}

	@Test
	public void findCompanyLevelEnrolledPlans() {
		List<Long> strategyIds = Arrays.asList(Long.valueOf(11111), Long.valueOf(22222));
		when(employeeDataDao.getEmpPlanMapping(COMPANY_CODE, REALM_ID)).thenReturn(prepareEmployeePlanMappings());
		when(strategyDataDao.getEmplStrategyBenGroup(12345L)).thenReturn(prepareEmployeeStrategyBenGroup());
		when(strategyDataDao.getStrategyBenPlans(strategyIds)).thenReturn(prepareStrategyBenefitPlans());

		Map<String,Map<String, Set<String>>> actulResult = planCompareServiceImpl.findCompanyLevelEnrolledPlans(company,
				strategyIds, new HashMap<>(), new HashMap<>());

		Map<String,Map<String, Set<String>>> expectedResult = prepareExpectedResult();
		assertEquals(3, actulResult.size());
		assertEquals(expectedResult, actulResult);
	}
	
	@Test
	public void testGetPlanAttributes_BPLClientEnabled() {
		String planIds = "003GUE,006IGO,002IL9,003GUM,006IGM";
		Set<String> planIdsSet = Set.of(planIds.split(","));

		List<BenefitPlanCompare> expectedList = List.of(new BenefitPlanCompare());
		CompletableFuture<List<BenefitPlanCompare>> future = CompletableFuture.completedFuture(expectedList);

		Mockito.when(BSSSecurityUtils.getAuthenticatedCompanyCode(httpRequest)).thenReturn("001");
		Mockito.when(BSSSecurityUtils.getAuthenticatedEmplId(httpRequest)).thenReturn("101110121");
		Mockito.when(bplService.getBPLAttributes(planIdsSet, effectiveDate, TEMPLATE_TYPE, httpRequest))
				.thenReturn(future);

		// When
		planCompareServiceImpl.getPlanAttributes(planIdsSet, effectiveDate, TEMPLATE_TYPE, httpRequest);

		verify(bplService).getBPLAttributes(any(), any(), any(), any());
	}

	private Map<String, Map<String, Set<String>>> prepareExpectedResult() {
		Map<String, Map<String, Set<String>>> result = new LinkedHashMap<>();
		Map<String, Set<String>> medicalPlans = new LinkedHashMap<>();
		Map<String, Set<String>> dentalPlans = new LinkedHashMap<>();
		Map<String, Set<String>> visionPlans = new LinkedHashMap<>();
		Set<String> mappedPlans = new LinkedHashSet<>();
		mappedPlans.addAll(List.of("006MQU", "006MQUA"));
		medicalPlans.put("006MQU", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.addAll(List.of("003MJW", "003MJWA"));
		medicalPlans.put("003MJW", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.addAll(List.of("000MIP", "000MIQ", "000MIPA"));
		medicalPlans.put("000MIP", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.add("000MILA");
		medicalPlans.put("000MIL", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.addAll(List.of("003MJK", "003MJKA"));
		medicalPlans.put("003MJK", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.addAll(List.of("000DIJ", "000DIJA"));
		dentalPlans.put("000DIJ", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.addAll(List.of("003DJW", "003DJWA"));
		dentalPlans.put("003DJW", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.addAll(List.of("000DIP", "000DIQ", "000DIPA", "000DIQA"));
		dentalPlans.put("000DIP", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.add("000DILA");
		dentalPlans.put("000DIL", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.addAll(List.of("003DJK", "003DJKA"));
		dentalPlans.put("003DJK", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.addAll(List.of("002VXW", "002VXWA"));
		visionPlans.put("002VXW", mappedPlans);

		mappedPlans = new LinkedHashSet<>();
		mappedPlans.addAll(List.of("005VV9", "005VV9A"));
		visionPlans.put("005VV9", mappedPlans);

		result.put("10", medicalPlans);
		result.put("11", dentalPlans);
		result.put("14", visionPlans);
		return result;
	}

	private MultiKeyMap prepareEmployeePlanMappings() {
		MultiKeyMap result = MultiKeyMap.decorate(new LinkedMap());
		EmpBenPlanMapping epm = new EmpBenPlanMapping("10", "006MQU", "006MQU", Arrays.asList("006MQUA"), "4CR");
		result.put("00001016057", "10", epm);
		epm = new EmpBenPlanMapping("10", "003MJW", "003MJW", Arrays.asList("003MJWA"), "4CR");
		result.put("00010301501", "10", epm);
		epm = new EmpBenPlanMapping("10", "000MIP", "000MIP", Arrays.asList("000MIPA"), "4CR");
		result.put("00001765574", "10", epm);
		epm = new EmpBenPlanMapping("10", "000MIL", "000MIL", Arrays.asList("000MILA"), "4CR");
		result.put("00001765578", "10", epm);
		epm = new EmpBenPlanMapping("10", "003MJK", "003MJK", Arrays.asList("003MJKA"), "4CR");
		result.put("00001778940", "10", epm);
		epm = new EmpBenPlanMapping("10", "000MIP", "000MIQ", Arrays.asList("000MIPA"), "4CR");
		result.put("00001788135", "10", epm);
		epm = new EmpBenPlanMapping("10", "Waive", null, null, "4CR");
		result.put("00001807754", "10", epm);

		epm = new EmpBenPlanMapping("11", "000DIJ", "000DIJ", Arrays.asList("000DIJA"), "4CR");
		result.put("00001016057", "11", epm);
		epm = new EmpBenPlanMapping("11", "003DJW", "003DJW", Arrays.asList("003DJWA"), "4CR");
		result.put("00010301501", "11", epm);
		epm = new EmpBenPlanMapping("11", "000DIP", "000DIP", Arrays.asList("000DIPA"), "4CR");
		result.put("00001765574", "11", epm);
		epm = new EmpBenPlanMapping("11", "000DIL", "000DIL", Arrays.asList("000DILA"), "4CR");
		result.put("00001765578", "11", epm);
		epm = new EmpBenPlanMapping("11", "003DJK", "003DJK", Arrays.asList("003DJKA"), "4CR");
		result.put("00001778940", "11", epm);
		epm = new EmpBenPlanMapping("11", "Waive", null, null, "4CR");
		result.put("00001788135", "11", epm);
		epm = new EmpBenPlanMapping("11", "000DIP", "000DIQ", Arrays.asList("000DIQA"), "4CR");
		result.put("00001807754", "11", epm);
		epm = new EmpBenPlanMapping("1D", "000DIP", "000DIQ", Arrays.asList("000DIQA"), "4CR");
		result.put("00001807754", "1D", epm);

		epm = new EmpBenPlanMapping("14", "002VXW", "002VXW", Arrays.asList("002VXWA"), "4CR");
		result.put("00001016057", "14", epm);
		epm = new EmpBenPlanMapping("14", "002VXW", "002VXW", Arrays.asList("002VXWA"), "4CR");
		result.put("00010301501", "14", epm);
		epm = new EmpBenPlanMapping("14", "002VXW", "002VXW", Arrays.asList("002VXWA"), "4CR");
		result.put("00001765574", "14", epm);
		epm = new EmpBenPlanMapping("14", "005VV9", "005VV9", Arrays.asList("005VV9A"), "4CR");
		result.put("00001765578", "14", epm);
		epm = new EmpBenPlanMapping("14", "005VV9", "005VV9", Arrays.asList("005VV9A"), "4CR");
		result.put("00001778940", "14", epm);
		epm = new EmpBenPlanMapping("1V", "005VV9", "005VV9", Arrays.asList("005VV9A"), "4CR");
		result.put("00001778940", "1V", epm);
		epm = new EmpBenPlanMapping("14", "Waive", null, null, "4CR");
		result.put("00001788135", "14", epm);
		epm = new EmpBenPlanMapping("14", "Waive", null, null, "4CR");
		result.put("00001807754", "14", epm);

		return result;
	}

	private MultiKeyMap prepareEmployeeStrategyBenGroup() {
		MultiKeyMap emplStrategyBenGroup = new MultiKeyMap();
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setBenefitProgram("BENPRG1");
		emplStrategyBenGroup.put("00001765578", Long.valueOf(11111), benefitGroup);
		return emplStrategyBenGroup;
	}

	private Map<Long, Map<String, List<String>>> prepareStrategyBenefitPlans() {
		Map<Long, Map<String, List<String>>> strategyBenPlans = new HashMap<>();
		Map<String, List<String>> benProgPlans = new HashMap<>();
		benProgPlans.put("4CR", new ArrayList<>(List.of("006MQU", "003MJW", "000MIP", "000MIL", "003MJK", "000MIQ",
				"000DIJ", "003DJW", "000DIP", "000DIQ", "000DIL", "003DJK", "002VXW", "005VV9")));
		benProgPlans.put("BENPRG1", new ArrayList<>(List.of("000MILA", "000DILA", "005VV9A")));
		strategyBenPlans.put(Long.valueOf(11111), benProgPlans);

		benProgPlans = new HashMap<>();
		benProgPlans.put("4CR", new ArrayList<>(List.of("006MQUA", "003MJWA", "000MIPA", "000MILA", "003MJKA",
				"000DIJA", "003DJWA", "000DIPA", "000DIQA", "000DILA", "003DJKA", "002VXWA", "005VV9A")));
		benProgPlans.put("BENPRG1", new ArrayList<>(List.of("000MIL", "000DILA", "005VV9A")));
		strategyBenPlans.put(Long.valueOf(22222), benProgPlans);

		return strategyBenPlans;
	}
	
	private ReturnResponse<BenPlanCompareResponse> preparePlanResponse() {
		List<BenefitPlanCompare> plansList = Arrays.asList(
				preparePlanCompare("003GUE", "BS-CA HDHP 3500 CA North", "Blue Shield of California", BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc()),
				preparePlanCompare("003GUM", "Kaiser HMO/HDHP 3500 CA North", "Kaiser Permanente",  BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc()),
				preparePlanCompare("006IGO", "Delta Dental 100", "Delta Dental",  BenefitTypeEnum.DENTAL.getBcrBenTypeDesc()),
				preparePlanCompare("006IGM", "Delta Dental 0", "Delta Dental",  BenefitTypeEnum.DENTAL.getBcrBenTypeDesc()),
				preparePlanCompare("002IL9", "VSP Vision Plus", "Vision Service Plan (VSP)",  BenefitTypeEnum.VISION.getBcrBenTypeDesc()),
				preparePlanCompare("004S5I", "Aetna EyeMed", "Aetna",  BenefitTypeEnum.VISION.getBcrBenTypeDesc())
				);

		BenPlanCompareResponse benPlanCompareResponse = new BenPlanCompareResponse();
		benPlanCompareResponse.setPlans(plansList);

		ReturnResponse<BenPlanCompareResponse> response = new ReturnResponse<>();
		response.setData(benPlanCompareResponse);
		response.setStatusCode(String.valueOf(HttpStatus.OK.value()));

		return response;
	}
	
	private BenefitPlanCompare preparePlanCompare(String planId, String planName, String carrier, String planType) {
		return BenefitPlanCompare.builder()
				.planId(planId)
				.name(planName)
				.carrier(carrier)
				.benefitType(planType)
				.template(prepareAttrPlanTemplate())
				.build();
	}
	private List<PlanCompareTemplate> prepareAttrPlanTemplate() {
		Attribute attribute = Attribute.builder()
				.name("Attribute 1")
				.displayName("Attribute 1")
				.value("Value")
				.build();

		PlanCompareTemplate template = PlanCompareTemplate.builder()
				.name("attr1")
				.children(List.of(attribute))
				.build();

		return List.of(template);
	}
	private void verifyBPLAttributes(CompletableFuture<List<BenefitPlanCompare>> bplAttributes) throws ExecutionException, InterruptedException {
		List<BenefitPlanCompare> attributeList = bplAttributes.get();
		assertEquals(6, attributeList.size());
		verifyBenefitPlanCompare(attributeList.get(0), "Medical", "003GUE", "Blue Shield of California", "BS-CA HDHP 3500 CA North");
		verifyBenefitPlanCompare(attributeList.get(1), "Medical", "003GUM", "Kaiser Permanente", "Kaiser HMO/HDHP 3500 CA North");
		verifyBenefitPlanCompare(attributeList.get(2), "Dental", "006IGO", "Delta Dental", "Delta Dental 100");
		verifyBenefitPlanCompare(attributeList.get(3), "Dental", "006IGM", "Delta Dental", "Delta Dental 0");
		verifyBenefitPlanCompare(attributeList.get(4), "Vision", "002IL9", "Vision Service Plan (VSP)", "VSP Vision Plus");
		verifyBenefitPlanCompare(attributeList.get(5), "Vision", "004S5I", "Aetna", "Aetna EyeMed");

	}
	private void verifyBenefitPlanCompare(BenefitPlanCompare planCompare, String expectedBenefitType, String expectedPlanId,
			String expectedCarrier, String expectedName) {
		assertEquals(expectedBenefitType, planCompare.getBenefitType());
		assertEquals(expectedPlanId, planCompare.getPlanId());
		assertEquals(expectedCarrier, planCompare.getCarrier());
		assertEquals(expectedName, planCompare.getName());

		List<PlanCompareTemplate> template = planCompare.getTemplate();
		assertEquals(1, template.size());
		assertEquals("attr1", template.get(0).getName());

		List<Attribute> children = template.get(0).getChildren();
		assertEquals(1, children.size());
		assertEquals("Attribute 1", children.get(0).getName());
	}

}
