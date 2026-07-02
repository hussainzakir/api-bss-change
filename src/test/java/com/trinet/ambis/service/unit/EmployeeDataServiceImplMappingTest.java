/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.RealmPlanMappingId;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.impl.EmployeeDataServiceImpl;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.StrategyGroupPlanRateData;
import com.trinet.ambis.util.Constants;

/**
 * @author hliddle
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EmployeeDataServiceImplMappingTest extends ServiceUnitTest {

	@Autowired
	@InjectMocks
	EmployeeDataServiceImpl employeeDataService;

	@Mock
	@Autowired
	StrategyDataDao strategyDataDao;

	@Mock
	@Autowired
	RealmDataDao realmDataDao;

	@Mock
	@Autowired
	RealmPlanYearService realmPlanYearService;

	@Test
	public void getMappedStrategyPlan_test() {

		long strategyId = 10;
		long groupId = 20;
		long realmYearId = 9;

		StrategyGroupPlanRateData actualReturnData = null;

		// The map that has the already determined maps
		MultiKeyMap strategyGroupPlanMapping = new MultiKeyMap();

		// Setup up a mock realm plan mapping
		Map<String, PlanMapping> realmPlanMappingList = setupInitalRealmPlanMappingList(realmYearId);

		// Setup a mock default plan list
		Map<String, Map<String, String>> portfolioDefaultPlans = setupInitalPortfolioDefaultPlans();

		// Setup a mock ER to EE plan mapping list
		Map<BenefitPlan, BenefitPlan> erEePlanMapping = setupInitalErEePlanMapping();

		// Setup a mock contribution cost map
		MultiKeyMap strategyGroupPlanCostMap = new MultiKeyMap();

		/*
		 * TEST 0 - Test when different maps are null or empty GIVEN
		 */

		// If strategyGroupPlanCostMap is null, null should be returned
		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.MEDICAL_CODE,
				"0038RO", Constants.CVG_CODE_EMPLOYEE, "1", strategyGroupPlanMapping,
				realmPlanMappingList, null, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNull(actualReturnData);

		// If strategyGroupPlanCostMap is empty, null should be returned
		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.MEDICAL_CODE,
				"0038RO", Constants.CVG_CODE_EMPLOYEE, "1", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNull(actualReturnData);

		// If strategyGroupPlanMapping is null, null should be returned
		addStrategyGroupPlanCostMap(strategyGroupPlanCostMap, strategyId, groupId, Constants.MEDICAL_CODE, "0038RO",
				Constants.CVG_CODE_EMPLOYEE, BigDecimal.valueOf(80), BigDecimal.valueOf(800), BigDecimal.valueOf(200),
				"Aetna HMO 0 North CA");
		addStrategyGroupPlanCostMap(strategyGroupPlanCostMap, strategyId, groupId, Constants.MEDICAL_CODE, "0038RP",
				Constants.CVG_CODE_EMPLOYEE, BigDecimal.valueOf(75), BigDecimal.valueOf(600), BigDecimal.valueOf(200),
				"Aetna HMO 0 South CA");
		strategyGroupPlanMapping = null;

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.MEDICAL_CODE,
				"0038RO", Constants.CVG_CODE_EMPLOYEE, "1", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNull(actualReturnData);

		/*
		 * TEST 1 - First, see if this is already in the plan map GIVEN
		 */

		// setup
		strategyGroupPlanMapping = new MultiKeyMap();
		strategyGroupPlanMapping.put(strategyId, groupId, Constants.MEDICAL_CODE, "0038RO",
				new String[] { "0038RO", Constants.MEDICAL_CODE, null });

		// If the plan in question is already in the map, return from the map.
		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.MEDICAL_CODE,
				"0038RO", Constants.CVG_CODE_EMPLOYEE, "1", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.MEDICAL_CODE, actualReturnData.getPlanType());
		assertEquals("0038RO", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Aetna HMO 0 North CA", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(80), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(800), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(200), actualReturnData.getEeRate());
		assertNull(actualReturnData.getMapReason());

		/*
		 * TEST 2 - Second, see if this plan is in the client's strategy for
		 * this group GIVEN If the plan in question is in the
		 * strategyGroupPlanCostMap, return that data
		 */

		// Reset strategyGroupPlanMapping
		strategyGroupPlanMapping.clear();

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.MEDICAL_CODE,
				"0038RO", Constants.CVG_CODE_EMPLOYEE, "1", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.MEDICAL_CODE, actualReturnData.getPlanType());
		assertEquals("0038RO", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Aetna HMO 0 North CA", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(80), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(800), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(200), actualReturnData.getEeRate());
		assertNull(actualReturnData.getMapReason());

		/*
		 * TEST 3 - Third, see if this plan is in the realmPlanMapping GIVEN If
		 * the plan in question is in the realmPlanMapping, return the cost data
		 * for the mapped plan
		 */

		// Reset strategyGroupPlanMapping
		strategyGroupPlanMapping.clear();

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.MEDICAL_CODE,
				"002AHA", Constants.CVG_CODE_EMPLOYEE, "1", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.MEDICAL_CODE, actualReturnData.getPlanType());
		assertEquals("0038RO", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Aetna HMO 0 North CA", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(80), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(800), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(200), actualReturnData.getEeRate());
		assertEquals("TriNet", actualReturnData.getMapReason());

		/*
		 * TEST 4 - Fourth, see if the plan is in the default mapping for the
		 * plan's current portfolio GIVEN If the plan in question is in
		 * portfolioDefaultPlans, return the cost data for the default plan
		 */

		// Reset strategyGroupPlanMapping
		strategyGroupPlanMapping.clear();

		addStrategyGroupPlanCostMap(strategyGroupPlanCostMap, strategyId, groupId, Constants.DENTAL_CODE, "0038Q4",
				Constants.CVG_CODE_EMPLOYEE, BigDecimal.valueOf(60), BigDecimal.valueOf(21), BigDecimal.valueOf(14),
				"Aetna Dental 100 Group");

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.DENTAL_CODE,
				"TESTPLAN", Constants.CVG_CODE_EMPLOYEE, "1", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.DENTAL_CODE, actualReturnData.getPlanType());
		assertEquals("0038Q4", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Aetna Dental 100 Group", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(60), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(21), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(14), actualReturnData.getEeRate());
		assertEquals("Client", actualReturnData.getMapReason());

		/*
		 * TEST 5 - Fifth, see if the plan is in the default mapping for any
		 * portfolio for this planType GIVEN If the plan in question is in
		 * portfolioDefaultPlans, return the cost data for the default plan
		 */

		// Reset strategyGroupPlanMapping
		strategyGroupPlanMapping.clear();

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.DENTAL_CODE,
				"TESTPLAN", Constants.CVG_CODE_EMPLOYEE, "3", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.DENTAL_CODE, actualReturnData.getPlanType());
		assertEquals("0038Q4", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Aetna Dental 100 Group", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(60), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(21), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(14), actualReturnData.getEeRate());
		assertEquals("Client", actualReturnData.getMapReason());

		/*
		 * TEST 6A - Sixth, see if the plan is in the EE to ER mapping in either
		 * direction - The map includes both directions already GIVEN EE to ER
		 * If the plan in question is in erEePlanMapping, return the cost data
		 * for the mapped plan
		 */

		// Reset strategyGroupPlanMapping
		strategyGroupPlanMapping.clear();
		addStrategyGroupPlanCostMap(strategyGroupPlanCostMap, strategyId, groupId, Constants.VISION_CODE, "0038QI",
				Constants.CVG_CODE_EMPLOYEE, BigDecimal.valueOf(60), BigDecimal.valueOf(12), BigDecimal.valueOf(8),
				"Aetna EyeMed Vision Group");

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId,
				Constants.VOLUNTARY_VISION_CODE, "0038QL", Constants.CVG_CODE_EMPLOYEE, "15",
				strategyGroupPlanMapping, realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans,
				erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.VISION_CODE, actualReturnData.getPlanType());
		assertEquals("0038QI", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Aetna EyeMed Vision Group", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(60), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(12), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(8), actualReturnData.getEeRate());
		assertEquals("Client", actualReturnData.getMapReason());

		/*
		 * TEST 6B - Sixth, see if the plan is in the EE to ER mapping in either
		 * direction - The map includes both directions already GIVEN ER to EE
		 * If the plan in question is in erEePlanMapping, return the cost data
		 * for the mapped plan
		 */

		// Reset strategyGroupPlanMapping
		strategyGroupPlanMapping.clear();
		strategyGroupPlanCostMap.remove(strategyId, groupId, Constants.DENTAL_CODE, "0038Q4",
				Constants.CVG_CODE_EMPLOYEE);
		addStrategyGroupPlanCostMap(strategyGroupPlanCostMap, strategyId, groupId, Constants.VOLUNTARY_DENTAL_CODE,
				"002AD4", Constants.CVG_CODE_EMPLOYEE, BigDecimal.valueOf(0), BigDecimal.valueOf(0),
				BigDecimal.valueOf(55), "Guardian Dental EPO Optional");

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.DENTAL_CODE,
				"002ACU", Constants.CVG_CODE_EMPLOYEE, "14", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.VOLUNTARY_DENTAL_CODE, actualReturnData.getPlanType());
		assertEquals("002AD4", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Guardian Dental EPO Optional", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(0), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(0), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(55), actualReturnData.getEeRate());
		assertEquals("Client", actualReturnData.getMapReason());

		/*
		 * TEST 6C - Sixth, see if the plan is in the EE to ER mapping in either
		 * direction - The map includes both directions already GIVEN Realm plan
		 * mapping plan EE to ER If the realmPlanMapping in question is in
		 * erEePlanMapping, return the cost data for the mapped plan
		 */

		// Reset strategyGroupPlanMapping
		strategyGroupPlanMapping.clear();
		realmPlanMappingList.put("002ACQ", prepareRealmPlanMapping(realmYearId, "002ACQ", 15, "EyeMed Optional Vision",
				Constants.VOLUNTARY_VISION_CODE, "0038QL", 15, "Aetna EyeMed Vision Optional"));

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId,
				Constants.VOLUNTARY_VISION_CODE, "002ACQ", Constants.CVG_CODE_EMPLOYEE, "15",
				strategyGroupPlanMapping, realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans,
				erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.VISION_CODE, actualReturnData.getPlanType());
		assertEquals("0038QI", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Aetna EyeMed Vision Group", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(60), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(12), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(8), actualReturnData.getEeRate());
		assertEquals("Client", actualReturnData.getMapReason());

		/*
		 * TEST 6D - Sixth, see if the plan is in the EE to ER mapping in either
		 * direction - The map includes both directions already GIVEN Realm plan
		 * mapping plan ER to EE If the realmPlanMapping in question is in
		 * erEePlanMapping, return the cost data for the mapped plan
		 */

		// Reset maps as needed
		strategyGroupPlanMapping.clear();
		strategyGroupPlanCostMap.clear();
		realmPlanMappingList.put("002ACM", prepareRealmPlanMapping(realmYearId, "002ACM", 15, "EyeMed Group Vision",
				Constants.VISION_CODE, "0038QI", 15, "Aetna EyeMed Vision Group"));
		addStrategyGroupPlanCostMap(strategyGroupPlanCostMap, strategyId, groupId, Constants.VOLUNTARY_VISION_CODE,
				"0038QL", Constants.CVG_CODE_EMPLOYEE, BigDecimal.valueOf(0), BigDecimal.valueOf(0),
				BigDecimal.valueOf(17), "Aetna EyeMed Vision Optional");

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.VISION_CODE,
				"002ACM", Constants.CVG_CODE_EMPLOYEE, "15", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.VOLUNTARY_VISION_CODE, actualReturnData.getPlanType());
		assertEquals("0038QL", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Aetna EyeMed Vision Optional", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(0), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(0), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(17), actualReturnData.getEeRate());
		assertEquals("Client", actualReturnData.getMapReason());

		/*
		 * TEST 6E - Sixth, see if the plan is in the EE to ER mapping in either
		 * direction - The map includes both directions already GIVEN EE to ER -
		 * If the plan and realm plan mapping plan are not found, get the
		 * default plan
		 */

		// Reset strategyGroupPlanMapping
		strategyGroupPlanMapping.clear();
		strategyGroupPlanCostMap.clear();
		addStrategyGroupPlanCostMap(strategyGroupPlanCostMap, strategyId, groupId, Constants.VISION_CODE, "0038QI",
				Constants.CVG_CODE_EMPLOYEE, BigDecimal.valueOf(60), BigDecimal.valueOf(12), BigDecimal.valueOf(8),
				"Aetna EyeMed Vision Group");

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId,
				Constants.VOLUNTARY_VISION_CODE, "0038QM", Constants.CVG_CODE_EMPLOYEE, "15",
				strategyGroupPlanMapping, realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans,
				erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.VISION_CODE, actualReturnData.getPlanType());
		assertEquals("0038QI", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Aetna EyeMed Vision Group", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(60), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(12), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(8), actualReturnData.getEeRate());
		assertEquals("Client", actualReturnData.getMapReason());

		/*
		 * TEST 6F - Sixth, see if the plan is in the EE to ER mapping in either
		 * direction - The map includes both directions already GIVEN ER to EE -
		 * If the plan and realm plan mapping plan are not found, get the
		 * default plan
		 */

		// Reset strategyGroupPlanMapping
		strategyGroupPlanMapping.clear();
		strategyGroupPlanCostMap.clear();
		addStrategyGroupPlanCostMap(strategyGroupPlanCostMap, strategyId, groupId, Constants.VOLUNTARY_VISION_CODE,
				"0038QL", Constants.CVG_CODE_EMPLOYEE, BigDecimal.valueOf(0), BigDecimal.valueOf(0),
				BigDecimal.valueOf(55), "Aetna EyeMed Vision Optional");

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.VISION_CODE,
				"0038QQ", Constants.CVG_CODE_EMPLOYEE, "15", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, portfolioDefaultPlans, erEePlanMapping);

		// then
		assertNotNull(actualReturnData);
		assertEquals(strategyId, actualReturnData.getStrategyId());
		assertEquals(groupId, actualReturnData.getGroupId());
		assertEquals(Constants.VOLUNTARY_VISION_CODE, actualReturnData.getPlanType());
		assertEquals("0038QL", actualReturnData.getBenefitPlan());
		assertEquals(Constants.CVG_CODE_EMPLOYEE, actualReturnData.getCoverageLevel());
		assertEquals("Aetna EyeMed Vision Optional", actualReturnData.getDescription());
		assertEquals(BigDecimal.valueOf(0), actualReturnData.getErContribPercent());
		assertEquals(BigDecimal.valueOf(0), actualReturnData.getErRate());
		assertEquals(BigDecimal.valueOf(55), actualReturnData.getEeRate());
		assertEquals("Client", actualReturnData.getMapReason());

		/*
		 * TEST 6G - Sixth, see if the plan is in the EE to ER mapping in either
		 * direction - The map includes both directions already GIVEN ER to EE -
		 * If the plan and realm plan mapping plan are not found and the
		 * portfolioDefaultPlans is null, return null
		 */

		// Reset strategyGroupPlanMapping
		strategyGroupPlanMapping.clear();
		strategyGroupPlanCostMap.clear();
		addStrategyGroupPlanCostMap(strategyGroupPlanCostMap, strategyId, groupId, Constants.VOLUNTARY_VISION_CODE,
				"0038QL", Constants.CVG_CODE_EMPLOYEE, BigDecimal.valueOf(0), BigDecimal.valueOf(0),
				BigDecimal.valueOf(55), "Aetna EyeMed Vision Optional");

		// when
		actualReturnData = employeeDataService.getMappedStrategyPlan(strategyId, groupId, Constants.VISION_CODE,
				"0038QQ", Constants.CVG_CODE_EMPLOYEE, "15", strategyGroupPlanMapping,
				realmPlanMappingList, strategyGroupPlanCostMap, null, erEePlanMapping);

		// then
		assertNull(actualReturnData);

	}

	/// ********************************SETUP********************//
	private Map<String, PlanMapping> setupInitalRealmPlanMappingList(long realmYearId) {
		Map<String, PlanMapping> realmPlanMappingList = new HashMap<String, PlanMapping>();
		realmPlanMappingList.put("002AHA", prepareRealmPlanMapping(realmYearId, "002AHA", 1, "Aetna HMO 25 1 North CA",
				Constants.MEDICAL_CODE, "0038RO", 1, "Aetna HMO 0 North CA"));
		realmPlanMappingList.put("002AH9", prepareRealmPlanMapping(realmYearId, "002AH9", 1, "Aetna HMO 25 1 South CA",
				Constants.MEDICAL_CODE, "0038RP", 1, "Aetna HMO 0 South CA"));
		return realmPlanMappingList;
	}

	private Map<String, Map<String, String>> setupInitalPortfolioDefaultPlans() {
		Map<String, Map<String, String>> portfolioDefaultPlans = new HashMap<String, Map<String, String>>();
		addPortfolioDefaultPlans(portfolioDefaultPlans, "1", "0038Q4", Constants.DENTAL_CODE);
		addPortfolioDefaultPlans(portfolioDefaultPlans, "14", "002ACW", Constants.DENTAL_CODE);
		addPortfolioDefaultPlans(portfolioDefaultPlans, "15", "0038QI", Constants.VISION_CODE);
		return portfolioDefaultPlans;
	}

	private Map<BenefitPlan, BenefitPlan> setupInitalErEePlanMapping() {
		Map<BenefitPlan, BenefitPlan> erEePlanMapping = new HashMap<BenefitPlan, BenefitPlan>();
		addErEePlanMapping(erEePlanMapping, Constants.DENTAL_CODE, "002ACU", Constants.VOLUNTARY_DENTAL_CODE, "002AD4");
		addErEePlanMapping(erEePlanMapping, Constants.DENTAL_CODE, "002ACV", Constants.VOLUNTARY_DENTAL_CODE, "002AD5");
		addErEePlanMapping(erEePlanMapping, Constants.VISION_CODE, "0038QI", Constants.VOLUNTARY_VISION_CODE, "0038QL");
		addErEePlanMapping(erEePlanMapping, Constants.VISION_CODE, "0038QJ", Constants.VOLUNTARY_VISION_CODE, "0038QM");
		addErEePlanMapping(erEePlanMapping, Constants.VISION_CODE, "0038QQ", Constants.VOLUNTARY_VISION_CODE, "0038QT");
		return erEePlanMapping;
	}

	private PlanMapping prepareRealmPlanMapping(long realmYearId, String plan, long oldPortfolioId,
			String oldPlanDesc, String planType, String newPlan, long newPortfolioId, String newPlanDesc) {

		RealmPlanMappingId planMappingId = new RealmPlanMappingId();
		planMappingId.setRealmYearId(realmYearId);
		planMappingId.setPlan(plan);
		PlanMapping realmPlanMapping = new PlanMapping();
		realmPlanMapping.setOldPortfolioId(oldPortfolioId);
		realmPlanMapping.setPlanType(planType);
		realmPlanMapping.setNewBenefitPlan(newPlan);
		realmPlanMapping.setNewPortfolioId(newPortfolioId);
		return realmPlanMapping;
	}

	private StrategyGroupPlanRateData prepareStrategyGroupPlanRateData(long strategyId, long groupId, String planType,
			String plan, String coverageLevel, BigDecimal erContribPercent, BigDecimal erRate, BigDecimal eeRate,
			String description) {

		StrategyGroupPlanRateData strategyGroupPlanRateData = new StrategyGroupPlanRateData();
		strategyGroupPlanRateData.setStrategyId(strategyId);
		strategyGroupPlanRateData.setGroupId(groupId);
		strategyGroupPlanRateData.setPlanType(planType);
		strategyGroupPlanRateData.setBenefitPlan(plan);
		strategyGroupPlanRateData.setCoverageLevel(coverageLevel);
		strategyGroupPlanRateData.setErContribPercent(erContribPercent);
		strategyGroupPlanRateData.setErRate(erRate);
		strategyGroupPlanRateData.setEeRate(eeRate);
		strategyGroupPlanRateData.setDescription(description);
		return strategyGroupPlanRateData;
	}

	private void addStrategyGroupPlanCostMap(MultiKeyMap strategyGroupPlanCostMap, long strategyId, long groupId,
			String planType, String plan, String coverageLevel, BigDecimal erContribPercent, BigDecimal erRate,
			BigDecimal eeRate, String description) {

		strategyGroupPlanCostMap.put(strategyId, groupId, planType, plan, coverageLevel,
				prepareStrategyGroupPlanRateData(strategyId, groupId, planType, plan, coverageLevel, erContribPercent,
						erRate, eeRate, description));
	}

	private void addPortfolioDefaultPlans(Map<String, Map<String, String>> portfolioDefaultPlans, String portfolioId,
			String plan, String planType) {

		Map<String, String> defaultPlanMap = portfolioDefaultPlans.get(planType);
		if (null == defaultPlanMap) {
			defaultPlanMap = new HashMap<String, String>();
		}
		defaultPlanMap.put(portfolioId, plan);
		portfolioDefaultPlans.put(planType, defaultPlanMap);
	}

	private void addErEePlanMapping(Map<BenefitPlan, BenefitPlan> erEePlanMapping, String erPlanType, String erPlan,
			String eePlanType, String eePlan) {

		BenefitPlan employerPaidPlan = new BenefitPlan();
		employerPaidPlan.setPlanType(erPlanType);
		employerPaidPlan.setId(erPlan);

		BenefitPlan employeePaidPlan = new BenefitPlan();
		employeePaidPlan.setPlanType(eePlanType);
		employeePaidPlan.setId(eePlan);

		erEePlanMapping.put(employerPaidPlan, employeePaidPlan);
		erEePlanMapping.put(employeePaidPlan, employerPaidPlan);

	}

}