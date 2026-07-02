package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.collections.map.MultiKeyMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.dao.hrp.impl.EmployeeDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.EmpBenPlanMapping;
import com.trinet.ambis.service.model.EmployeeBenefitGroup;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class EmployeeDataDaoImplTest {

	@InjectMocks
	EmployeeDataDaoImpl empDataDaoImpl;

	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	XbssRealmPlyrPlanDao realmPlyrPlanDao;

	@Mock
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	Company company = null;
	EntityManager em = null;
	Query mockedQuery = null;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		company = new Company();
		company.setCode("G48");
		company.setPlanStartDate("01-JAN-2018");
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(10);
		company.setRealmPlanYear(rpy);
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);
		em = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		when(em.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
		empDataDaoImpl.setEntityManager(em);
		empDataDaoImpl.setHrpEntityManager(em);
	}

	@Test
	public void getEmployeesByCompany() {

		RealmPlanYear prevRealmPlanYear = new RealmPlanYear();
		prevRealmPlanYear.setId(1);
		when(mockedQuery.getResultList()).thenReturn(prepareEmployeesData());
		Map<String, Employee> actualResults = empDataDaoImpl.getEmployeesByCompany(company);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(1, actualResults.size());
		for (Entry<String, Employee> employee : actualResults.entrySet()) {
			assertEquals("1111111", employee.getValue().getEmplId());
			assertEquals(100L, employee.getValue().getEmplRcd());
			assertEquals("Emp Name", employee.getValue().getEmplName());
			assertEquals("Engineering", employee.getValue().getDepartment());
			assertEquals("Fort Mill", employee.getValue().getLocation());
			assertEquals("engineer", employee.getValue().getJobTitle());
			assertEquals("G48", employee.getValue().getCompany());
			assertEquals("ben prog", employee.getValue().getBenefitProgram());
		}

		when(realmPlanYearService.getPreviousRealmPlanYear(Mockito.any(RealmPlanYear.class)))
				.thenReturn(prevRealmPlanYear);

		actualResults = empDataDaoImpl.getEmployeesByCompany(company);

		verify(mockedQuery, times(2)).getResultList();
		assertEquals(1, actualResults.size());
		for (Entry<String, Employee> employee : actualResults.entrySet()) {
			assertEquals("1111111", employee.getValue().getEmplId());
			assertEquals(100L, employee.getValue().getEmplRcd());
			assertEquals("Emp Name", employee.getValue().getEmplName());
			assertEquals("Engineering", employee.getValue().getDepartment());
			assertEquals("Fort Mill", employee.getValue().getLocation());
			assertEquals("engineer", employee.getValue().getJobTitle());
			assertEquals("G48", employee.getValue().getCompany());
			assertEquals("ben prog", employee.getValue().getBenefitProgram());
		}
	}

	@Test
	public void getEmployeesByCompanyNoResults() {
		when(mockedQuery.getResultList()).thenThrow(new NoResultException());

		Map<String, Employee>  actualResults = empDataDaoImpl.getEmployeesByCompany(company);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(null, actualResults);
	}

	@Test
	public void getEmployeeGroupByStrategy() {
		when(mockedQuery.getResultList()).thenReturn(prepareEmployeeGroupData());
		Set<Employee> actualResults = empDataDaoImpl.getEmployeeGroupDetailsByStrategy(12345L);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(1, actualResults.size());
	}

	@Test
	public void getEmployeeGroupByStrategyNoResults() {
		when(mockedQuery.getResultList()).thenThrow(new NoResultException());
		Set<Employee> actualResults = empDataDaoImpl.getEmployeeGroupDetailsByStrategy(12345L);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(0, actualResults.size());
	}

	@Test
	public void getEmpPlanMapping() {

		when(mockedQuery.getResultList()).thenReturn(prepareEmployeePlanMappingData());

		MultiKeyMap actualResults = empDataDaoImpl.getEmpPlanMapping("COMPANY_CODE", 1L);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(4, actualResults.size());

        // Employee with next and alt plans
		EmpBenPlanMapping actualMedicalEmpBenPlanMapping = (EmpBenPlanMapping) actualResults.get("EMPLOYEE_ID_1",
				BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		assertEquals(BSSApplicationConstants.MEDICAL_PLAN_TYPE, actualMedicalEmpBenPlanMapping.getPlanType());
		assertEquals("CURRENT_BENEFIT_PLAN", actualMedicalEmpBenPlanMapping.getCurBenPlan());
		assertEquals("NEXT_BENEFIT_PLAN", actualMedicalEmpBenPlanMapping.getNextBenPlan());
		assertEquals("NEXT_ALT_BENEFIT_PLAN", actualMedicalEmpBenPlanMapping.getAltBenPlans().get(0));
		assertEquals("CUR_BEN_PROGRAM", actualMedicalEmpBenPlanMapping.getCurBenProgram());

        // Employee with three alt plans and no next plan
		actualMedicalEmpBenPlanMapping = (EmpBenPlanMapping) actualResults.get("EMPLOYEE_ID_2",
				BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		assertEquals("CURRENT_BENEFIT_PLAN", actualMedicalEmpBenPlanMapping.getCurBenPlan());
		assertEquals("", actualMedicalEmpBenPlanMapping.getNextBenPlan());
		assertEquals("NEXT_ALT_BENEFIT_PLAN_1", actualMedicalEmpBenPlanMapping.getAltBenPlans().get(0));
        assertEquals("NEXT_ALT_BENEFIT_PLAN_2", actualMedicalEmpBenPlanMapping.getAltBenPlans().get(1));
        assertEquals("NEXT_ALT_BENEFIT_PLAN_3", actualMedicalEmpBenPlanMapping.getAltBenPlans().get(2));
		assertEquals("CUR_BEN_PROGRAM1", actualMedicalEmpBenPlanMapping.getCurBenProgram());
	}

	@Test
	public void getEmployeeCensusStrategyPlanData() {

		when(mockedQuery.getResultList()).thenReturn(prepareEmployeeCostData());

		List<Object[]> actualResults = empDataDaoImpl.getEmployeeCensusStrategyPlanData(company, 1L, new Date());
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(4, actualResults.size());
		assertEquals("00002090653", actualResults.get(0)[0] );
		assertEquals("00002090653", actualResults.get(1)[0] );
		assertEquals("00002090653", actualResults.get(2)[0] );
		assertEquals("00001403325", actualResults.get(3)[0] );
	}

	@Test
	public void getEmployeeStrategyPlanCostData() {

		when(mockedQuery.getResultList()).thenReturn( prepareEmployeeCostData() );

		List<Object[]> actualResults = empDataDaoImpl.getEmployeeStrategyPlanData(company, 1L);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(4, actualResults.size());
		assertEquals("00002090653", actualResults.get(0)[0] );
		assertEquals("00002090653", actualResults.get(1)[0] );
		assertEquals("00002090653", actualResults.get(2)[0] );
		assertEquals("00001403325", actualResults.get(3)[0] );
	}

	@Test
	public void getEmployeeStrategyPlanCostDataNoResults() {
		when(mockedQuery.getResultList()).thenReturn( new ArrayList<>() );
		List<Object[]> actualResults = empDataDaoImpl.getEmployeeStrategyPlanData(company, 1L);
		assertEquals(0, actualResults.size());
	}

	@Test
	public void getMirrorPlanEnrolledEmployees() {
		// mirror plans no longer exist. Tested method just returns empty map
		MultiKeyMap actualResults = empDataDaoImpl.getMirrorPlanEnrolledEmployees(company);
		assertEquals(0, actualResults.size());
	}

	private List<Object[]> prepareEmployeesData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[7];
		r[0] = "1111111";
		r[1] = new BigDecimal(100);
		r[2] = "Emp Name";
		r[3] = "Engineering";
		r[4] = "Fort Mill";
		r[5] = "engineer";
		r[6] = "ben prog";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareEmployeeGroupData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[12];
		r[0] = "00002090653";
		r[1] = new BigDecimal(0);
		r[2] = new BigDecimal(40507);
		r[3] = "UPP";
		r[4] = "UPP";
		r[5] = "All Employees";
		r[6] = new BigDecimal(1);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareEmployeeCostData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[20];
		r[0] = "00002090653";
		r[1] = "John";
		r[2] = "";
		r[3] = "Doe";
		r[4] = "DEPT1";
		r[5] = "Department One";
		r[6] = "LOC1";
		r[7] = "Location One";
		r[8] = "STAFF";
		r[9] = "10";
		r[10] = "MEDPLAN";
		r[11] = "Medical Plan";
		r[12] = "4";
		r[13] = "E";
		results.add(r);

		r = new Object[20];
		r[0] = "00002090653";
		r[1] = "John";
		r[2] = "";
		r[3] = "Doe";
		r[4] = "DEPT1";
		r[5] = "Department One";
		r[6] = "LOC1";
		r[7] = "Location One";
		r[8] = "STAFF";
		r[9] = "11";
		r[10] = null;
		r[11] = null;
		r[12] = null;
		r[13] = "W";
		results.add(r);

		r = new Object[20];
		r[0] = "00002090653";
		r[1] = "John";
		r[2] = "";
		r[3] = "Doe";
		r[4] = "DEPT1";
		r[5] = "Department One";
		r[6] = "LOC1";
		r[7] = "Location One";
		r[8] = "STAFF";
		r[9] = "14";
		r[10] = "VISPLAN";
		r[11] = "Vision Plan";
		r[12] = "4";
		r[13] = "W";
		results.add(r);

		r = new Object[20];
		r[0] = "00001403325";
		r[1] = "Jane";
		r[2] = "";
		r[3] = "Doe";
		r[4] = "DEPT1";
		r[5] = "Department One";
		r[6] = "LOC1";
		r[7] = "Location One";
		r[8] = "STAFF";
		r[9] = "10";
		r[10] = null;
		r[11] = null;
		r[12] = null;
		r[13] = "W";
		results.add(r);

		return results;
	}

	private List<Object[]> prepareEmployeePlanMappingData() {
		List<Object[]> results = new ArrayList<>();

		Object[] r = new Object[6];
		r[0] = "EMPLOYEE_ID_1";
		r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[2] = "CURRENT_BENEFIT_PLAN";
		r[3] = "NEXT_BENEFIT_PLAN";
		r[4] = "NEXT_ALT_BENEFIT_PLAN";
		r[5] = "CUR_BEN_PROGRAM";
		results.add(r);

		r = new Object[6];
		r[0] = "EMPLOYEE_ID_1";
		r[1] = BSSApplicationConstants.DENTAL_PLAN_TYPE;
		r[2] = "CURRENT_BENEFIT_PLAN";
		r[3] = "NEXT_BENEFIT_PLAN";
		r[4] = "NEXT_ALT_BENEFIT_PLAN";
		r[5] = "CUR_BEN_PROGRAM";
		results.add(r);

		r = new Object[6];
		r[0] = "EMPLOYEE_ID_1";
		r[1] = BSSApplicationConstants.VISION_PLAN_TYPE;
		r[2] = "CURRENT_BENEFIT_PLAN";
		r[3] = "NEXT_BENEFIT_PLAN";
		r[4] = "NEXT_ALT_BENEFIT_PLAN";
		r[5] = "CUR_BEN_PROGRAM";
		results.add(r);

		r = new Object[6];
		r[0] = "EMPLOYEE_ID_2";
		r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[2] = "CURRENT_BENEFIT_PLAN";
		r[3] = null;
		r[4] = "NEXT_ALT_BENEFIT_PLAN_1";
		r[5] = "CUR_BEN_PROGRAM1";
		results.add(r);

		r = new Object[6];
		r[0] = "EMPLOYEE_ID_2";
		r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[2] = "CURRENT_BENEFIT_PLAN";
		r[3] = null;
		r[4] = "NEXT_ALT_BENEFIT_PLAN_2";
		r[5] = "CUR_BEN_PROGRAM1";
		results.add(r);

        r = new Object[6];
        r[0] = "EMPLOYEE_ID_2";
        r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
        r[2] = "CURRENT_BENEFIT_PLAN";
        r[3] = null;
        r[4] = "NEXT_ALT_BENEFIT_PLAN_3";
        r[5] = "CUR_BEN_PROGRAM1";
        results.add(r);

		return results;
	}

	private Map<String, EmployeeBenefitGroup> prepareBenefitGroupMap() {
		Map<String, EmployeeBenefitGroup> benefitGroupMap = new HashMap<>();
		EmployeeBenefitGroup employeeBenefitGroup = new EmployeeBenefitGroup();
		employeeBenefitGroup.setBenefitGroupId(1L);
		employeeBenefitGroup.setBenefitGroupName("STAFF");
		benefitGroupMap.put("STAFF", employeeBenefitGroup);
		return benefitGroupMap;
	}

	private List<XbssRealmPlyrPlan> prepareRealmPlanList() {
		List<XbssRealmPlyrPlan> returnList = new ArrayList<>();
		XbssRealmPlyrPlan planYearPlan = new XbssRealmPlyrPlan();
		planYearPlan.setBenefitPlan("BENEFIT_PLAN_1");
		planYearPlan.setPortfolioId(BigDecimal.ONE);
		returnList.add(planYearPlan);
		return returnList;
	}

	private List<Object[]> prepareEmployeeData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[1];
		r[0] = "00001520518";
		Object[] r1 = new Object[1];
		r1[0] = "00001523259";
		results.add(r);
		results.add(r1);
		return results;
	}

	@Test
	public void getEmployeesBy() {		
		when(mockedQuery.getResultList()).thenReturn(prepareEmployeeData());
		List<String> mockedResults = new ArrayList<>();
		mockedResults.add("00001520518");
		List<String> actualResults = empDataDaoImpl.getEmployeesBy("G48");
		assertEquals(mockedResults.get(0), actualResults.get(0));
	}

	@Test
	public void getEmployeesByNoResults() {		
		when(mockedQuery.getResultList()).thenReturn(prepareEmployeeData());
		List<String> mockedResults = new ArrayList<>();
		mockedResults.add("0000");
		List<String> actualResults = empDataDaoImpl.getEmployeesBy("G48");
		assertNotEquals(mockedResults.get(0), actualResults.get(0));
	}
}