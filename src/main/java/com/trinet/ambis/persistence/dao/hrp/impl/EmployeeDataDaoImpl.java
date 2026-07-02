package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.service.EmployeeBenefitGroupService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.EmpBenPlanMapping;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.DaoUtils;

/**
 * @author mpulipaka
 */
/**
 * Following Dao is used to the details of Employees from PeopleSoft (Executes
 * the PSFT queries)
 */

public class EmployeeDataDaoImpl implements EmployeeDataDao {

	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager entityManager;

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	private static final Logger logger = LoggerFactory.getLogger(EmployeeDataDaoImpl.class);
	private static final String EMPLOYEES_CENSUS_DATA_BY_COMPANY = "EMPLOYEES_CENSUS_DATA_BY_COMPANY";
	private static final String XBSS_EMPLOYEE_STRATEGY_GROUP = "XBSS_EMPLOYEE_STRATEGY_GROUP";
	private static final String XBSS_EMPLOYEE_PLAN_MAPPING = "XBSS_EMPLOYEE_PLAN_MAPPING";
	private static final String EMPLOYEE_CENSUS_PLAN_DATA = "EMPLOYEE_CENSUS_PLAN_DATA";
	private static final String EMPLOYEE_PLAN_DATA = "EMPLOYEE_PLAN_DATA";
	private static final String COMPANY_EMPLOYEE_LIST = "COMPANY_EMPLOYEE_LIST";
	private static final String MEDICAL_PLAN_TYPES = "medicalPlanTypes";
	private static final String DENTAL_PLAN_TYPES = "dentalPlanTypes";
	private static final String VISION_PLAN_TYPES = "visionPlanTypes";
	private static final String STRATEGY_ID = "STRATEGY_ID";

	@Autowired
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Autowired
	EmployeeBenefitGroupService employeeBenefitGroupService;

	@Autowired
	PortfolioRuleDao portfolioRuleDao;

	@Autowired
	RealmPlanYearService realmPlanYearService;

	@Autowired
	XbssRealmPlyrPlanDao realmPlyrPlanDao;

	@Override
	public Map<String, Employee> getEmployeesByCompany(Company company) {
		Date effDate = company.getRealmPlanYear().getPlanYearEnd();
		try {
			List<Object[]> results = null;
			Query query = em.createNamedQuery(EMPLOYEES_CENSUS_DATA_BY_COMPANY);
			query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
			query.setParameter(BSSQueryConstants.EFF_DATE, effDate);
			query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYear().getId());
			results = DaoUtils.getResultList(query, EMPLOYEES_CENSUS_DATA_BY_COMPANY);
			Map<String, Employee> employees = new HashMap<>();
			for (Object[] result : results) {
				Employee employee = new Employee();
				String employeeId = (String) result[0];
				employee.setEmplId(employeeId);
				employee.setEmplRcd(((BigDecimal) result[1]).longValue());
				employee.setEmplName((String) result[2]);
				employee.setDepartment((String) result[3]);
				employee.setLocation((String) result[4]);
				employee.setJobTitle((String) result[5]);
				employee.setCompany(company.getCode());
				employee.setRealmYearId(company.getRealmPlanYear().getId());
				employee.setEffdt(effDate);
				String psftBenefitProgramName = (String) result[6];
				employee.setBenefitProgram(psftBenefitProgramName);
				employees.put(employeeId, employee);
			}
			return employees;
		} catch (NoResultException ex) {
			CommonUtils.logExceptions(ex, logger, company.getCode(), "");
			return null;
		}
	}

	@Override
	public Set<Employee> getEmployeeGroupDetailsByStrategy(long strategyId) {
		try {
			Query query = em.createNamedQuery(XBSS_EMPLOYEE_STRATEGY_GROUP);
			query.setParameter(STRATEGY_ID, strategyId);
			List<Object[]> results = DaoUtils.getResultList(query, XBSS_EMPLOYEE_STRATEGY_GROUP);
			Set<Employee> employees = new HashSet<>();
			for (Object[] result : results) {
				Employee employee = new Employee();
				employee.setEmplId((String) result[0]);
				employee.setEmplRcd(((BigDecimal) result[1]).longValue());
				employee.setStrategyGroupId(((BigDecimal) result[2]).longValue());
				employee.setBenefitProgram((String) result[3]);
				employee.setUpdatedBenefitProgram((String) result[4]);
				employee.setBenefitGroupName((String) result[5]);
				employee.setBenefitGroupId(((BigDecimal) result[6]).longValue());
				employee.setBenefitProgramUpdated(Boolean.valueOf((String) result[7]));
				employees.add(employee);
			}
			return employees;
		} catch (NoResultException ex) {
			CommonUtils.logExceptions(ex, logger, "", "");
			return new HashSet<>();
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public MultiKeyMap getEmpPlanMapping(String companyCode, long realmYearId) {
		java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
		MultiKeyMap empBenPlanMappingMap = MultiKeyMap.decorate(new LinkedMap());

		Query query = buildQuery(companyCode, realmYearId, currentDate);
		List<Object[]> results = DaoUtils.getResultList(query, XBSS_EMPLOYEE_PLAN_MAPPING);
		for (Object[] empPlanMapping : results) {
			String emplId = empPlanMapping[0].toString();
			String planType = empPlanMapping[1].toString();

			String[] planDetails = { safeTrim(empPlanMapping[2]), safeTrim(empPlanMapping[3]),
					safeTrim(empPlanMapping[4]), safeTrim(empPlanMapping[5]) };

			EmpBenPlanMapping existingMapping = (EmpBenPlanMapping) empBenPlanMappingMap.get(emplId, planType);

			if (existingMapping != null) {
				updateMapping(existingMapping, planDetails);
			} else {
				EmpBenPlanMapping newMapping = createNewMapping(planType, planDetails);
				empBenPlanMappingMap.put(emplId, planType, newMapping);
			}
		}
		return empBenPlanMappingMap;
	}

	private Query buildQuery(String companyCode, long realmYearId, java.sql.Date currentDate) {
		Query query = em.createNamedQuery(XBSS_EMPLOYEE_PLAN_MAPPING);
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		query.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmYearId);
		query.setParameter(BSSQueryConstants.EFF_DATE, currentDate);
		query.setParameter(MEDICAL_PLAN_TYPES, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		query.setParameter(DENTAL_PLAN_TYPES, BSSApplicationConstants.DENTAL_PLAN_TYPES);
		query.setParameter(VISION_PLAN_TYPES, BSSApplicationConstants.VISION_PLAN_TYPES);
		return query;
	}

	private String safeTrim(Object obj) {
		return obj == null ? StringUtils.EMPTY : obj.toString().trim();
	}

	private void updateMapping(EmpBenPlanMapping existingMapping, String[] planDetails) {
        existingMapping.getAltBenPlans().add(planDetails[2]);
	}

	private EmpBenPlanMapping createNewMapping(String planType, String[] planDetails) {
		return new EmpBenPlanMapping(planType, planDetails[0], planDetails[1], new ArrayList<>(Arrays.asList(planDetails[2])), planDetails[3]);
	}

	@Override
	public List<Object[]> getEmployeeCensusStrategyPlanData(Company company, Long currentStrategyId, Date effDate) {

		Query query = em.createNamedQuery(EMPLOYEE_CENSUS_PLAN_DATA);
		query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYear().getId());
		query.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		return DaoUtils.getResultList(query, EMPLOYEE_CENSUS_PLAN_DATA);
	}

	@Override
	public List<Object[]> getEmployeeStrategyPlanData(Company company, Long currentStrategyId) {

		Query query = entityManager.createNamedQuery(EMPLOYEE_PLAN_DATA);
		query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
		query.setParameter(BSSQueryConstants.EFF_DATE, company.getRealmPlanYear().getPlanYearEnd());
		query.setParameter(BSSQueryConstants.PRIMARY_PLAN_TYPES, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
		return DaoUtils.getResultList(query, EMPLOYEE_PLAN_DATA);
	}

	/**
	 * Mirror plan logic is not required. Just return empty map
	 */
	public MultiKeyMap getMirrorPlanEnrolledEmployees(Company company) {
		return new MultiKeyMap();
	}

	public void setEntityManager(EntityManager em) {
		this.entityManager = em;
	}

	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	public void setHrpEntityManager(EntityManager em) {
		this.em = em;
	}

	public EntityManager getHrpEntityManager() {
		return this.em;
	}

	@Override
	public List<String> getEmployeesBy(String companyCode) {
		Query query = em.createNamedQuery(COMPANY_EMPLOYEE_LIST);
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		List<Object[]> results = DaoUtils.getResultList(query, COMPANY_EMPLOYEE_LIST);
		List<String> empData = new ArrayList<>();
		for (Object[] result : results) {
			empData.add((String) result[0]);
		}
		return empData;
	}
}
