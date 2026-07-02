package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.service.EmployerEmployeePlansMappingService;
import com.trinet.ambis.service.impl.EmployerEmployeePlansMappingServiceImpl;
import com.trinet.ambis.service.model.BenefitPlan;

/**
 * @author mpulipaka
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class EmployerEmployeePlansMappingServiceImplTest {
	private static final long REALM_YEAR_ID = 3L;

	@Mock
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;
	EmployerEmployeePlansMappingService employerEmployeePlansMappingService;

	@Before
	public void setup() throws ParseException {
		employerEmployeePlansMappingDao = mock(EmployerEmployeePlansMappingDao.class);
		employerEmployeePlansMappingService = new EmployerEmployeePlansMappingServiceImpl();
		employerEmployeePlansMappingService.setEmployerEmployeePlansMappingDao(employerEmployeePlansMappingDao);
	}

	@Test
	public void getEmployerEmployeePlansMappingByRealmYearIdTest() {
		Map<BenefitPlan, BenefitPlan> employeeEmployerPlansMappingMap = new HashMap<BenefitPlan, BenefitPlan>();
		BenefitPlan er = new BenefitPlan();
		BenefitPlan ee = new BenefitPlan();
		er.setId("XYZ12");
		er.setPlanType("11");
		ee.setId("XYZ12E");
		ee.setPlanType("1D");
		employeeEmployerPlansMappingMap.put(er, ee);
		Mockito.when(employerEmployeePlansMappingDao.getEmployerEmployeePlansMappingByRealmYearId(REALM_YEAR_ID))
				.thenReturn(employeeEmployerPlansMappingMap);

		Map<BenefitPlan, BenefitPlan> result = employerEmployeePlansMappingService
				.getEmployerEmployeePlansMappingByRealmYearId(REALM_YEAR_ID);
		assertEquals(1, result.size());
	}

	@Test
	public void isEmployerEmployeePlansMappingByRealmYearIdOfferedTest() {
		long realmPlanYearId = 4;
		Mockito.when(employerEmployeePlansMappingDao
				.getEmployerEmployeePlansMappingByRealmYearIdOfferedCount(Mockito.anyLong())).thenReturn(9);
		boolean result = employerEmployeePlansMappingService
				.isEmployerEmployeePlansMappingByRealmYearIdOffered(realmPlanYearId);
		assertEquals(true, result);
	}

	@Test
	public void isEmployerEmployeePlansMappingByRealmYearIdOfferedTest1() {
		long realmPlanYearId = 4;
		Mockito.when(employerEmployeePlansMappingDao
				.getEmployerEmployeePlansMappingByRealmYearIdOfferedCount(Mockito.anyLong())).thenReturn(0);
		boolean result = employerEmployeePlansMappingService
				.isEmployerEmployeePlansMappingByRealmYearIdOffered(realmPlanYearId);

		assertEquals(false, result);
	}
}
