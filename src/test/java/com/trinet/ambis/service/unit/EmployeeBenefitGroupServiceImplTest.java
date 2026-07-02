package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.persistence.model.EmployeeStrategyGroup;
import com.trinet.ambis.service.impl.EmployeeBenefitGroupServiceImpl;
import com.trinet.ambis.service.model.StrategyGroupDetails;


@RunWith(MockitoJUnitRunner.class)
public class EmployeeBenefitGroupServiceImplTest extends ServiceUnitTest {

	@Qualifier("employeeBenefitGroupServiceImpl")
	@InjectMocks
	EmployeeBenefitGroupServiceImpl benefitGroupService;

	@Mock
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Mock
	EmployeeStrategyGroupDao employeeStrategyGroupDao;

	@Mock
	EmployeeDao employeeDao;

	@Mock
	EmployeeSelectionDao employeeSelectionDao;
	
	@Mock
	EmployeeDataDao employeeDataDao;
	
	EntityManager em = null;
	Query mockedQuery = null;

	@Before
	public void setUp() {
		em = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
	}

	@Test
	public void insertNewEmployeeStrategyGroups() {

		Map<String, Set<StrategyGroupDetails>> employeeStrategyGroupMap = prepareNewEmployeeStrategyGroups();
		List<EmployeeStrategyGroup> employeeStrategyGroups = new ArrayList<EmployeeStrategyGroup>();
		for (Map.Entry<String, Set<StrategyGroupDetails>> entry : employeeStrategyGroupMap.entrySet()) {
			for (StrategyGroupDetails strategyGroupDetails : entry.getValue()) {
				EmployeeStrategyGroup employeeStrategyGroup = new EmployeeStrategyGroup();
				employeeStrategyGroup.setEmplId(entry.getKey());
				employeeStrategyGroup.setStrategyGroupId(strategyGroupDetails.getStrategyGroupId());
				employeeStrategyGroups.add(employeeStrategyGroup);
			}
		}
		benefitGroupService.insertNewEmployeeStrategyGroups(employeeStrategyGroupMap);
	}
	
	@Test
	public void loadStrategyEmployeeData() {
		Company company = new Company();
		long strategyId = 1111L;
		Set<Employee> strategyEmployeeData = new HashSet<>();
		Employee emp = new Employee();
		emp.setEmplId("00002222268");
		emp.setBenefitProgram("BENPROG1");
		strategyEmployeeData.add(emp);
		emp = new Employee();
		emp.setEmplId("00002222269");
		emp.setBenefitProgram("BENPROG2");
		strategyEmployeeData.add(emp);
		Map<String, Long> benefitProgramStrategyGroupId = new HashMap<>();
		benefitProgramStrategyGroupId.put("BENPROG1", 2222L);
		benefitProgramStrategyGroupId.put("BENPROG2", 3333L);
		
		ArgumentCaptor<EmployeeStrategyGroup> emplSGArgCaptor = ArgumentCaptor.forClass(EmployeeStrategyGroup.class);
		
		when(employeeDataDao.getEmployeeGroupDetailsByStrategy(strategyId )).thenReturn(strategyEmployeeData);
		when(employeeStrategyGroupDao.saveAndFlush(emplSGArgCaptor.capture())).thenReturn(null);
		
		benefitGroupService.loadStrategyEmployeeData(company, benefitProgramStrategyGroupId, strategyId);
		
		assertEquals(2, emplSGArgCaptor.getAllValues().size());
		for (EmployeeStrategyGroup employeeSG : emplSGArgCaptor.getAllValues()) {
			assertTrue(Arrays.asList("00002222268", "00002222269").contains(employeeSG.getEmplId()));
			if("00002222268".equals(employeeSG.getEmplId())) {
				assertEquals(2222L, employeeSG.getStrategyGroupId());
			}
			if("00002222269".equals(employeeSG.getEmplId())) {
				assertEquals(3333L, employeeSG.getStrategyGroupId());
			}
		}
	}

	private Map<String, Set<StrategyGroupDetails>> prepareNewEmployeeStrategyGroups() {
		Map<String, Set<StrategyGroupDetails>> employeeMap = new HashMap<String, Set<StrategyGroupDetails>>();
		Set<StrategyGroupDetails> strategyGroupDetailsSet = new HashSet<StrategyGroupDetails>();
		StrategyGroupDetails strategyGroupDetails = new StrategyGroupDetails();
		strategyGroupDetails.setBenefitProgram("GROUP1");
		strategyGroupDetailsSet.add(strategyGroupDetails);
		strategyGroupDetails = new StrategyGroupDetails();
		strategyGroupDetails.setBenefitProgram("GROUP2");
		strategyGroupDetailsSet.add(strategyGroupDetails);
		employeeMap.put("EMPLOYEE 1", strategyGroupDetailsSet);
		return employeeMap;

	}

}