package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.hrp.impl.EmployeeBenefitGroupDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.EmployeeBenefitGroup;
import com.trinet.ambis.service.model.EmployeeCensusStrategyGroupDetails;
import com.trinet.ambis.service.model.EmployeeStrategyGroupDetails;
import com.trinet.ambis.service.model.StrategyGroupDetails;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class EmployeeBenefitGroupDaoImplTest {

	EmployeeBenefitGroupDaoImpl employeeBenefitGroupDao;
	EntityManager em = null;
	EntityManager hrpEm = null;
	Query mockedQuery = null;
	Company comp = null;
	RealmPlanYear rpy = null;

	@Before
	public void setup() {
		em = mock(EntityManager.class);
		hrpEm = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		when(em.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		when(hrpEm.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		employeeBenefitGroupDao = new EmployeeBenefitGroupDaoImpl();
		employeeBenefitGroupDao.setEntityManager(em);
		employeeBenefitGroupDao.setHrpEntityManager(hrpEm);
		comp = new Company();
		comp.setCode("G48");
		rpy = new RealmPlanYear();
		rpy.setId(10);
		comp.setRealmPlanYear(rpy);
	}

	@Test
	public void getEmployeesByCompany() {
		long groupId = 1111L;
		List<String> listOfEmployeeIds = new ArrayList<String>();
		when(hrpEm.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		when(mockedQuery.getSingleResult()).thenReturn(1);

		employeeBenefitGroupDao.updateEmployees(groupId, listOfEmployeeIds);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void getBenefitProgramDetails() {
		when(mockedQuery.getResultList()).thenReturn(prepareBenefitProgramDetails());

		Map<String, EmployeeBenefitGroup> actualResult = employeeBenefitGroupDao.getBenefitProgramDetails(comp);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResult.size());
		assertEquals("Summit Rock Advisors K1 PGM", actualResult.get("EF1").getBenefitGroupName());
		assertEquals(null, actualResult.get("BENEFIT_PROGRAM").getBenefitGroupName());
	}

	@Test
	public void getStrategyGroupDetailsForCompany() {
		when(mockedQuery.getResultList()).thenReturn(prepareStrategyGroupDetails());

		Map<String, Set<StrategyGroupDetails>> actualResult = employeeBenefitGroupDao
				.getStrategyGroupDetailsForCompany(comp);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResult.size());
		assertEquals(1, actualResult.get("TE5").size());
		assertEquals(2, actualResult.get("001ND2").size());
	}

	@Test
	public void getEmployeeBenefitGroup() {
		when(mockedQuery.getResultList()).thenReturn(prepareEmpSelectionAndBenprogram());

		Map<String, EmployeeBenefitGroup> actualResult = employeeBenefitGroupDao.getEmployeeBenefitGroup(comp);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(3, actualResult.size());
		assertEquals("Test employee ass midyear", actualResult.get("00001529922").getBenefitGroupName());
		assertEquals("UPP", actualResult.get("00001529922").getBenefitProgram());
		assertEquals("All Employees", actualResult.get("00001529929").getBenefitGroupName());
		assertEquals("UPP", actualResult.get("00001529929").getBenefitProgram());
	}

	@Test
	public void getEmployeeStrategyGroupDetails() {
		when(mockedQuery.getResultList()).thenReturn(prepareEmployeeStrategyGroupDetails());

		Map<String, EmployeeStrategyGroupDetails> actualResult = employeeBenefitGroupDao.getEmployeeDetailsByStrategy(0);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResult.size());
		assertEquals("UPP", actualResult.get("00002272285").getCurrentBenefitProgram());
		assertEquals("UPP", actualResult.get("00002272285").getFutureBenefitProgram());
		assertEquals("UPP", actualResult.get("00001529898").getCurrentBenefitProgram());
		assertEquals("EF1", actualResult.get("00001529898").getFutureBenefitProgram());

	}

	@Test
	public void deleteEmployeeStrategyGroups() {
		when(mockedQuery.executeUpdate()).thenReturn(2);
		Set<String> employeeIds = new HashSet<String>();
		employeeIds.add("TEST1");
		employeeIds.add("TEST2");
		employeeBenefitGroupDao.deleteEmployeeStrategyGroups(employeeIds);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteEmployeeStrategyGroups_Company() {
		when(mockedQuery.executeUpdate()).thenReturn(2);
		employeeBenefitGroupDao.deleteEmployeeStrategyGroups(comp);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteEmployees() {
		when(mockedQuery.executeUpdate()).thenReturn(2);
		Set<String> employeeIds = new HashSet<String>();
		employeeIds.add("TEST1");
		employeeIds.add("TEST2");
		employeeBenefitGroupDao.deleteEmployees(employeeIds);
		verify(mockedQuery, times(2)).executeUpdate();
	}

	@Test
	public void updateEmployeesNoResultException() {
		long groupId = 1111L;
		List<String> listOfEmployeeIds = new ArrayList<String>();

		when(hrpEm.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		when(mockedQuery.executeUpdate()).thenThrow(new javax.persistence.NoResultException());

		employeeBenefitGroupDao.updateEmployees(groupId, listOfEmployeeIds);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void getBenefitProgramDetailsNoResultException() {
		when(mockedQuery.getResultList()).thenThrow(new javax.persistence.NoResultException());

		employeeBenefitGroupDao.getBenefitProgramDetails(comp);
		verify(mockedQuery, times(1)).getResultList();
	}

	@Test
	public void getStrategyGroupDetailsForCompanyNoResultException() {
		when(mockedQuery.getResultList()).thenThrow(new javax.persistence.NoResultException());

		employeeBenefitGroupDao.getStrategyGroupDetailsForCompany(comp);
		verify(mockedQuery, times(1)).getResultList();
	}

	@Test
	public void getEmployeeBenefitGroupNoResultException() {
		when(mockedQuery.getResultList()).thenThrow(new javax.persistence.NoResultException());

		employeeBenefitGroupDao.getEmployeeBenefitGroup(comp);
		verify(mockedQuery, times(1)).getResultList();
	}

	@Test
	public void getEmployeeStrategyGroupDetailsNoResultException() {
		when(mockedQuery.getResultList()).thenThrow(new javax.persistence.NoResultException());

		employeeBenefitGroupDao.getEmployeeDetailsByStrategy(0);
		verify(mockedQuery, times(1)).getResultList();
	}

	@Test
	public void deleteEmployeeStrategyGroupsNoResultException() {
		when(mockedQuery.executeUpdate()).thenThrow(new javax.persistence.NoResultException());
		Set<String> employeeIds = new HashSet<String>();
		employeeIds.add("TEST1");
		employeeIds.add("TEST2");
		employeeBenefitGroupDao.deleteEmployeeStrategyGroups(employeeIds);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteEmployeeStrategyGroupsByCompanyNoResultException() {
		when(mockedQuery.executeUpdate()).thenThrow(new javax.persistence.NoResultException());
		Set<String> employeeIds = new HashSet<String>();
		employeeIds.add("TEST1");
		employeeIds.add("TEST2");
		employeeBenefitGroupDao.deleteEmployeeStrategyGroups(comp);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteEmployeesNoResultException() {
		when(mockedQuery.executeUpdate()).thenThrow(new javax.persistence.NoResultException());
		Set<String> employeeIds = new HashSet<String>();
		employeeIds.add("TEST1");
		employeeIds.add("TEST2");
		employeeBenefitGroupDao.deleteEmployees(employeeIds);
		verify(mockedQuery, times(2)).executeUpdate();
	}
	
	@Test
	public void getEmployeeStrategyGroupDetailsTest1() {
		when(mockedQuery.getResultList()).thenReturn(prepareEmployeeStrategyGroupResult());
		Map<String, List<EmployeeCensusStrategyGroupDetails>> actualResult = employeeBenefitGroupDao.getEmployeeStrategyGroupDetails(comp.getCode());
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResult.size());
		assertEquals(3, actualResult.get("0000000123456").size());
		assertEquals(3, actualResult.get("0000000123457").size());
	}
	
	@Test
	public void getEmployeeStrategyGroupDetailsTest2() {
		when(mockedQuery.getResultList()).thenThrow(new javax.persistence.NoResultException());
		employeeBenefitGroupDao.getEmployeeStrategyGroupDetails(comp.getCode());
		verify(mockedQuery, times(1)).getResultList();
	}

	@Test
	public void getStartegyGroupByCompanyAndStrategyTest1() {
		when(mockedQuery.getResultList()).thenReturn(prepareEmployeeStrategyGroupResultForAdd());
		List<EmployeeCensusStrategyGroupDetails> actualResult = employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(comp.getCode());
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(3, actualResult.size());
	}

	@Test
	public void getStartegyGroupByCompanyAndStrategyTest2() {
		when(mockedQuery.getResultList()).thenThrow(new javax.persistence.NoResultException());
		employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(comp.getCode());
		verify(mockedQuery, times(1)).getResultList();
	}

	private List<Object[]> prepareBenefitProgramDetails() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[4];
		r[0] = "EF1";
		r[1] = BigDecimal.valueOf(31078);
		r[2] = "Summit Rock Advisors K1 PGM";
		r[3] = "BG48";
		results.add(r);
		r = new Object[4];
		r[0] = null;
		r[1] = null;
		r[2] = null;
		r[3] = null;
		results.add(r);
		r = new Object[4];
		r[0] = "BENEFIT_PROGRAM";
		r[1] = null;
		r[2] = null;
		r[3] = null;
		results.add(r);
		return results;
	}

	private List<Object[]> prepareEmpSelectionAndBenprogram() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[6];
		r[0] = "00001529922";
		r[1] = "UPP";
		r[2] = "001RS3";
		r[3] = BigDecimal.valueOf(36814);
		r[4] = "Test employee ass midyear";
		r[5] = "CG48";
		results.add(r);
		r = new Object[6];
		r[0] = "00001529929";
		r[1] = "UPP";
		r[2] = null;
		r[3] = BigDecimal.valueOf(31079);
		r[4] = "All Employees";
		r[5] = "AG48";
		results.add(r);
		r = new Object[6];
		r[0] = "EMPLOYEE_3";
		r[1] = null;
		r[2] = null;
		r[3] = null;
		r[4] = null;
		r[5] = null;
		results.add(r);
		r = new Object[6];
		r[0] = null;
		r[1] = null;
		r[2] = null;
		r[3] = null;
		r[4] = null;
		r[5] = null;
		results.add(r);
		return results;
	}

	private List<Object[]> prepareStrategyGroupDetails() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[12];
		r[0] = BigDecimal.valueOf(42145);
		r[1] = BigDecimal.valueOf(51618);
		r[2] = BigDecimal.valueOf(38104);
		r[3] = "TE5";
		r[4] = "K1 Benefit Class";
		r[5] = "STD";
		r[6] = "A";
		r[7] = Character.valueOf('0');
		r[8] = BigDecimal.valueOf(1);
		r[9] = "NONE";
		r[10] = "EQ9B";
		results.add(r);
		r = new Object[12];
		r[0] = BigDecimal.valueOf(42165);
		r[1] = BigDecimal.valueOf(51620);
		r[2] = BigDecimal.valueOf(38103);
		r[3] = "001ND2";
		r[4] = "SALARY";
		r[5] = "STD";
		r[6] = "A";
		r[7] = Character.valueOf('1');
		r[8] = BigDecimal.valueOf(8);
		r[9] = "F60D";
		r[10] = "CQ9B";
		results.add(r);
		r = new Object[12];
		r[0] = BigDecimal.valueOf(42146);
		r[1] = BigDecimal.valueOf(51618);
		r[2] = BigDecimal.valueOf(38103);
		r[3] = "001ND2";
		r[4] = "SALARY";
		r[5] = "STD";
		r[6] = "A";
		r[7] = Character.valueOf('1');
		r[8] = BigDecimal.valueOf(8);
		r[9] = "F60D";
		r[10] = "CQ9B";
		results.add(r);
		r = new Object[12];
		r[0] = null;
		r[1] = BigDecimal.valueOf(51618);
		r[2] = BigDecimal.valueOf(38103);
		r[3] = "001ND2";
		r[4] = "SALARY";
		r[5] = "STD";
		r[6] = "A";
		r[7] = Character.valueOf('1');
		r[8] = BigDecimal.valueOf(8);
		r[9] = "F60D";
		r[10] = "CQ9B";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareEmployeeStrategyGroupDetails() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[12];
		r[0] = "00002272285";
		r[1] = BigDecimal.valueOf(0);
		r[2] = BigDecimal.valueOf(40507);
		r[3] = BigDecimal.valueOf(51684);
		r[4] = "UPP";
		r[5] = "AG48";
		r[6] = "UPP";
		r[7] = "All Employees";
		r[8] = BigDecimal.valueOf(38194);
		r[9] = "AG48";
		results.add(r);
		r = new Object[12];
		r[0] = "00001529898";
		r[1] = BigDecimal.valueOf(0);
		r[2] = BigDecimal.valueOf(40508);
		r[3] = BigDecimal.valueOf(51684);
		r[4] = "UPP";
		r[5] = "AG48";
		r[6] = "EF1";
		r[7] = "Summit Rock Advisors K1 PGM";
		r[8] = BigDecimal.valueOf(38193);
		r[9] = "BG48";
		results.add(r);
		r = new Object[12];
		r[0] = null;
		r[1] = BigDecimal.valueOf(0);
		r[2] = BigDecimal.valueOf(40508);
		r[3] = BigDecimal.valueOf(51684);
		r[4] = "UPP";
		r[5] = "AG48";
		r[6] = "EF1";
		r[7] = "Summit Rock Advisors K1 PGM";
		r[8] = BigDecimal.valueOf(38193);
		r[9] = "BG48";
		results.add(r);
		return results;
	}
	
	private List<Object[]> prepareEmployeeStrategyGroupResult() {
		List<Object[]> result = new ArrayList<>();
		Object[] empStrategyGroupResult = new Object[] { "0000000123456", BigDecimal.valueOf(78981),
				BigDecimal.valueOf(93141), "K1", "K1 Benefit Program", "BENPROG_K1"};
		result.add(empStrategyGroupResult);
		empStrategyGroupResult = new Object[] { "0000000123456", BigDecimal.valueOf(78981), BigDecimal.valueOf(93142),
				"STD", "W2 Benefit Program", "BENPROG_W2"};
		result.add(empStrategyGroupResult);
		empStrategyGroupResult = new Object[] { "0000000123456", BigDecimal.valueOf(78981), BigDecimal.valueOf(93143),
				"STD", "W2 Benefit Program", "BENPROG_W2"};
		result.add(empStrategyGroupResult);
		empStrategyGroupResult = new Object[] { "0000000123457", BigDecimal.valueOf(78981), BigDecimal.valueOf(93141),
				"K1", "K1 Benefit Program", "BENPROG_K1" };
		result.add(empStrategyGroupResult);
		empStrategyGroupResult = new Object[] { "0000000123457", BigDecimal.valueOf(78981), BigDecimal.valueOf(93142),
				"STD", "W2 Benefit Program", "BENPROG_W2" };
		result.add(empStrategyGroupResult);
		empStrategyGroupResult = new Object[] { "0000000123457", BigDecimal.valueOf(78981), BigDecimal.valueOf(93143),
				"STD", "W2 Benefit Program", "BENPROG_W2" };
		result.add(empStrategyGroupResult);
		return result;
	}
	
	private List<Object[]> prepareEmployeeStrategyGroupResultForAdd() {
		List<Object[]> result = new ArrayList<>();
		Object[] empStrategyGroupResult = new Object[] { BigDecimal.valueOf(78981),
				BigDecimal.valueOf(93141), "K1","K1" };
		result.add(empStrategyGroupResult);
		empStrategyGroupResult = new Object[] {BigDecimal.valueOf(78981), BigDecimal.valueOf(93142),
				"STD", "W2" };
		result.add(empStrategyGroupResult);
        empStrategyGroupResult = new Object[] {BigDecimal.valueOf(78981), BigDecimal.valueOf(93143),
                "STD", "W2 MA" };
        result.add(empStrategyGroupResult);
		return result;
	}
}