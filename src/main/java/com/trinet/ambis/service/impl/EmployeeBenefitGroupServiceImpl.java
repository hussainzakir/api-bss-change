package com.trinet.ambis.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupTransactionDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.persistence.model.EmployeeStrategyGroup;
import com.trinet.ambis.persistence.model.EmployeeStrategyGroupTransaction;
import com.trinet.ambis.service.EmployeeBenefitGroupService;
import com.trinet.ambis.service.model.StrategyGroupDetails;
import com.trinet.ambis.util.CommonUtils;

/**
 * @author mpulipaka
 */
@Service
public class EmployeeBenefitGroupServiceImpl implements EmployeeBenefitGroupService {

	@Autowired
	EmployeeDao employeeDao;

	@Autowired
	EmployeeDataDao employeeDataDao;

	@Autowired
	EmployeeStrategyGroupDao employeeStrategyGroupDao;

	@Autowired
	EmployeeStrategyGroupTransactionDao employeeStrategyGroupTransactionDao;

	private static final Logger logger = LoggerFactory.getLogger(EmployeeBenefitGroupServiceImpl.class);

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void insertNewEmployeeStrategyGroups(Map<String, Set<StrategyGroupDetails>> employeeMap) {
		try {
			for (Map.Entry<String, Set<StrategyGroupDetails>> entry : employeeMap.entrySet()) {
				for (StrategyGroupDetails strategyGroupDetails : entry.getValue()) {
					EmployeeStrategyGroup employeeStrategyGroup = new EmployeeStrategyGroup();
					employeeStrategyGroup.setEmplId(entry.getKey());
					employeeStrategyGroup.setStrategyGroupId(strategyGroupDetails.getStrategyGroupId());
					employeeStrategyGroupDao.saveAndFlush(employeeStrategyGroup);
				}
			}
		} catch (NoResultException ex) {
			CommonUtils.logExceptions(ex, logger, null, null);
		}
	}

	@Override
	public void loadStrategyEmployeeData(Company company, Map<String, Long> benefitProgramStrategyGroupId,
			long strategyId) {
		Set<Employee> strategyEmployeeData = employeeDataDao.getEmployeeGroupDetailsByStrategy(strategyId);
		for (Employee employee : strategyEmployeeData) {
			if (null != benefitProgramStrategyGroupId.get(employee.getBenefitProgram())) {
				EmployeeStrategyGroup employeeStrategyGroup = new EmployeeStrategyGroup();
				employeeStrategyGroup.setEmplId(employee.getEmplId());
				employeeStrategyGroup
						.setStrategyGroupId(benefitProgramStrategyGroupId.get(employee.getBenefitProgram()));
				employeeStrategyGroupDao.saveAndFlush(employeeStrategyGroup);
			} else {
				logger.error("Missing strategy group for the following benefit program {}",
						employee.getBenefitProgram());
				BSSApplicationError error = new BSSApplicationError(
						"Missing Strategy group for the following benefit program {}" + employee.getBenefitProgram());
				throw new BSSApplicationException(error);
			}
			if (employee.isBenefitProgramUpdated()) {
				if (null != benefitProgramStrategyGroupId.get(employee.getUpdatedBenefitProgram())) {
					EmployeeStrategyGroupTransaction txn = new EmployeeStrategyGroupTransaction();
					txn.setEmplid(employee.getEmplId());
					txn.setStrategyGroupId(benefitProgramStrategyGroupId.get(employee.getUpdatedBenefitProgram()));
					txn.setCreateDate(new Date());
					employeeStrategyGroupTransactionDao.saveAndFlush(txn);
				} else {
					logger.error("Missing Strategy Group for the following benefit program {}",
							employee.getUpdatedBenefitProgram());
					BSSApplicationError error = new BSSApplicationError(
							"Missing strategy group for the following benefit program {}"
									+ employee.getUpdatedBenefitProgram());
					throw new BSSApplicationException(error);
				}
			}
		}
	}
}
