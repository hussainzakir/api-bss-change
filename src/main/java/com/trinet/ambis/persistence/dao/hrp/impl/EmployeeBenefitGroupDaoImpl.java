package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.EmployeeBenefitGroup;
import com.trinet.ambis.service.model.EmployeeCensusStrategyGroupDetails;
import com.trinet.ambis.service.model.EmployeeStrategyGroupDetails;
import com.trinet.ambis.service.model.StrategyGroupDetails;
import com.trinet.ambis.util.DaoUtils;

/**
 * @author mpulipaka
 */
public  class EmployeeBenefitGroupDaoImpl implements EmployeeBenefitGroupDao {

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager hrpEntityManager;
	
	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager entityManager;

	private static final Logger logger = LoggerFactory.getLogger(EmployeeBenefitGroupDaoImpl.class);
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void updateEmployees(long groupId, List<String> listOfEmployeeIds) {
		 try {
			 //Updated the employees benefit group after assign to group call
             Query query = hrpEntityManager.createNamedQuery("updateEmployeesBenefitGroup");
             query.setParameter(BSSQueryConstants.GROUP_ID, groupId);
             query.setParameter("listOfEmployeeIds", listOfEmployeeIds);
             int num = DaoUtils.executeUpdate(query, "updateEmployeesBenefitGroup");
             logger.info(" Number of employee rows updated {}", num);
           } 
    	 catch (NoResultException ex) {
             logger.error("NoResultException in updateEmployees: ", ex);
         }
	}
	

	@Override
	public Map<String, EmployeeBenefitGroup> getBenefitProgramDetails(Company company) {
		
        //Key is benefit program name from PSFT ( eg: VP5) 
		Map<String, EmployeeBenefitGroup> mapOfEmployees = new HashMap<>();
		try {
			 Query query = hrpEntityManager.createNamedQuery("getBenefitProgramDetails");
             query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
             query.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYear().getId());

     		 List<Object[]> results = DaoUtils.getResultList(query, "getBenefitProgramDetails");
             
             for(Object[] result : results ) {	 
              if( result[0]!=null )
               {
            	 EmployeeBenefitGroup  employeeBenefitGroup = new EmployeeBenefitGroup();  
            	 employeeBenefitGroup.setBenefitProgram((String) result[0]);
            	 if(result[1]!=null) {
                	 employeeBenefitGroup.setBenefitGroupId(((BigDecimal) result[1]).longValue());
            	 }
            	 if(result[2]!=null) {
            		 employeeBenefitGroup.setBenefitGroupName((String) result[2]);
            	 }
            	 if(result[3]!=null) {
            		 employeeBenefitGroup.setEligConfig1((String) result[3]);
            	 }
            	
              	 mapOfEmployees.put((String) result[0], employeeBenefitGroup);
               }
              }
           } 
    	 catch (NoResultException ex) {
             logger.error("NoResultException in getBenefitProgramDetails: ", ex);
         }
		return mapOfEmployees;
   }

	@Override
	public Map<String, Set<StrategyGroupDetails>> getStrategyGroupDetailsForCompany(Company company) {

		Map<String, Set<StrategyGroupDetails>> mapOfStrategyGroups = new HashMap<>();
		try {
			Query query = hrpEntityManager.createNamedQuery("STRATEGY_GROUP_DETAILS_FOR_COMPANY");
			query.setParameter("COMPANY_ID", company.getId());

    		List<Object[]> results = DaoUtils.getResultList(query, "STRATEGY_GROUP_DETAILS_FOR_COMPANY");

			for (Object[] result : results) {
				if (result[0] != null) {
					
					StrategyGroupDetails details = new StrategyGroupDetails();
					
					long strategyGroupId = ((BigDecimal) result[0]).longValue();
					String benefitProgram = (String) result[3];
					
					details.setStrategyGroupId(strategyGroupId);
					details.setStrategyId(((BigDecimal) result[1]).longValue());
					details.setGroupId(((BigDecimal) result[2]).longValue());
					details.setBenefitProgram(benefitProgram);
					details.setGroupName((String) result[4]);
					details.setGroupType((String) result[5]);
					details.setStatus((String) result[6]);
					details.setDefaultGroup(((Character) result[7]).charValue() == '1');
					details.setHeadcount(((BigDecimal) result[8]).longValue());
					details.setWaitingPeriod((String) result[9]);
					details.setEligConfig1((String) result[10]);
					
					if (mapOfStrategyGroups.containsKey(benefitProgram)) {
						mapOfStrategyGroups.get(benefitProgram).add(details);
					}
					else {
						Set<StrategyGroupDetails> strategyGroups = new HashSet<>();
						strategyGroups.add(details);
						mapOfStrategyGroups.put(benefitProgram, strategyGroups);
					}

				}
			}
		} catch (NoResultException ex) {
            logger.error("NoResultException in getStrategyGroupDetailsForCompany: ", ex);
		}
		return mapOfStrategyGroups;
	}
	
		
	
	
	@Override
	public Map<String, EmployeeBenefitGroup> getEmployeeBenefitGroup(Company company) {
       
		Map<String, EmployeeBenefitGroup> mapOfEmployees = new HashMap<>();
		try {
			 Query query = hrpEntityManager.createNamedQuery("getEmployeeSelectionAndBenefitProgram");
             query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
             query.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYear().getId());

     		 List<Object[]> results = DaoUtils.getResultList(query, "getEmployeeSelectionAndBenefitProgram");
             for(Object[] result : results ) {
               if( result[0]!=null )
               {
            	 EmployeeBenefitGroup  employeeBenefitGroup = new EmployeeBenefitGroup();  
            	 employeeBenefitGroup.setBenefitProgram((String) result[1]);
            	 employeeBenefitGroup.setUpdatedBenefitProgram((String) result[2]);
            	 if(result[3]!=null) {
                	 employeeBenefitGroup.setBenefitGroupId(((BigDecimal) result[3]).longValue());
            	 }
            	 if(result[4]!=null) {
            		 employeeBenefitGroup.setBenefitGroupName((String) result[4]);
            	 }
            	 if(result[5]!=null) {
            		 employeeBenefitGroup.setEligConfig1((String) result[5]);
            	 }
            	
              	 mapOfEmployees.put((String) result[0], employeeBenefitGroup);
               }
              }
           } 
    	 catch (NoResultException ex) {
             logger.error("NoResultException in getEmployeeBenefitGroup: ", ex);
         }
		return mapOfEmployees;
   }
	
	@Override
	public Map<String, EmployeeStrategyGroupDetails> getEmployeeDetailsByStrategy(long strategyId) {
		Map<String, EmployeeStrategyGroupDetails> mapOfEmployees = new HashMap<>();
		try {
			Query query = hrpEntityManager.createNamedQuery("EMPLOYEE_DETAILS_BY_STRATEGY");
			query.setParameter("STRATEGY_ID", strategyId);

			List<Object[]> results = DaoUtils.getResultList(query, "EMPLOYEE_DETAILS_BY_STRATEGY");
			for (Object[] result : results) {
				if (result[0] != null) {
					EmployeeStrategyGroupDetails employeeStrategyGroup = new EmployeeStrategyGroupDetails();

					employeeStrategyGroup.setEmplId((String) result[0]);
					employeeStrategyGroup.setEmplRcd(((BigDecimal) result[1]).longValue());
					employeeStrategyGroup.setFutureStrategyGroupId(((BigDecimal) result[2]).longValue());
					employeeStrategyGroup.setStrategyId(((BigDecimal) result[3]).longValue());
					employeeStrategyGroup.setCurrentBenefitProgram((String) result[4]);
					employeeStrategyGroup.setCurrentEligConfig1((String) result[5]);
					employeeStrategyGroup.setFutureBenefitProgram((String) result[6]);
					employeeStrategyGroup.setFutureGroupName((String) result[7]);
					employeeStrategyGroup.setFutureGroupId(((BigDecimal) result[8]).longValue());
					employeeStrategyGroup.setFutureEligConfig1((String) result[9]);
					mapOfEmployees.put((String) result[0], employeeStrategyGroup);
				}
			}
		} catch (NoResultException ex) {
			logger.error("NoResultException in getEmployeeStrategyGroupDetails: ", ex);
		}
		return mapOfEmployees;
	}
	
	@Override
	public List<String> getEmployeeDetailsByStrategyAndGroup(long strategyId, long groupId) {
		Query query = hrpEntityManager.createNamedQuery("EMPLOYEES_BY_STRATEGY_GROUP");
		query.setParameter("STRATEGY_ID", strategyId);
		query.setParameter("GROUP_ID", groupId);
		return DaoUtils.getResultStringList(query, "EMPLOYEES_BY_STRATEGY_GROUP");
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteEmployeeStrategyGroups(Set<String> employeeIds) {
		try {
			Query query = hrpEntityManager.createNamedQuery("DELETE_EMPLOYEE_STRATEGY_GROUP");
			query.setParameter("LISTOFEMPLIDS", employeeIds);
			DaoUtils.executeUpdate(query, "DELETE_EMPLOYEE_STRATEGY_GROUP");
		} catch (NoResultException ex) {
            logger.error("NoResultException in deleteEmployeeStrategyGroups: ", ex);
		}
	}	

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteEmployeeStrategyGroups(Company company) {
		try {
			Query query = hrpEntityManager.createNamedQuery("DELETE_EMPLOYEE_STRATEGY_GROUPS_BY_COMPANY");
			query.setParameter("company", company.getCode());
			query.setParameter("realmYearId", company.getRealmPlanYearId());
			DaoUtils.executeUpdate(query, "DELETE_EMPLOYEE_STRATEGY_GROUPS_BY_COMPANY");
		} catch (NoResultException ex) {
			logger.error("NoResultException in deleteEmployeeStrategyGroups: ", ex);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteEmployees(Set<String> employeeIds) {
		try {
			deleteEmployeeStrategyGroups(employeeIds);
			Query query = hrpEntityManager.createNamedQuery("DELETE_EMPLOYEE");
			query.setParameter("LISTOFEMPLIDS", employeeIds);
			DaoUtils.executeUpdate(query, "DELETE_EMPLOYEE");
		} catch (NoResultException ex) {
            logger.error("NoResultException in deleteEmployees: ", ex);
		}
	}	
	
	@Override
	public Map<String, List<EmployeeCensusStrategyGroupDetails>> getEmployeeStrategyGroupDetails(String companyCode) {
		Query query = hrpEntityManager.createNamedQuery("EMPLOYEE_STRATEGY_GROUP_DATA_LIST");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		try {
			List<Object[]> employeeStrategyGroupResult = DaoUtils.getResultList(query,
					"EMPLOYEE_STRATEGY_GROUP_DATA_LIST");
			return getEmployeeToStrategyGroupMapping(employeeStrategyGroupResult);
		} catch (NoResultException ex) {
			logger.error("NoResultException in getEmployeeStrategyGroupDetails: ", ex);
		}
		return new HashMap<>();
	}
	
	@Override
	public List<EmployeeCensusStrategyGroupDetails> getStartegyGroupByCompanyAndStrategy(String companyCode) {
		Query query = hrpEntityManager.createNamedQuery("STRATEGY_GROUP_DATA_LIST");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		try {
			List<Object[]> strategyGroupResult = DaoUtils.getResultList(query, "STRATEGY_GROUP_DATA_LIST");
			List<EmployeeCensusStrategyGroupDetails> list = new ArrayList<>();
			for (Object[] result : strategyGroupResult) {
				EmployeeCensusStrategyGroupDetails strategy = new EmployeeCensusStrategyGroupDetails();
				strategy.setStrategyGroupId(((BigDecimal) result[0]).longValue());
				strategy.setStrategyId(((BigDecimal) result[1]).longValue());
				strategy.setGroupType((String) result[2]);
                strategy.setGroupDesc((String) result[3]);
				list.add(strategy);					
			}
			return list;

		} catch (NoResultException ex) {
			logger.error("NoResultException in getStartegyGroupByCompanyAndStrategy: ", ex);
		}

		return new ArrayList<>();
	}
	
	

	/**
	 * @param hrpEntityManager the hrpEntityManager to set
	 */
	public void setHrpEntityManager(EntityManager hrpEntityManager) {
		this.hrpEntityManager = hrpEntityManager;
	}


	/**
	 * @param entityManager the entityManager to set
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	private Map<String, List<EmployeeCensusStrategyGroupDetails>> getEmployeeToStrategyGroupMapping(
			List<Object[]> employeeStrategyGroupResult) {
		Map<String, List<EmployeeCensusStrategyGroupDetails>> empToStrategyGroupDtls = new HashMap<>();
		for (Object[] result : employeeStrategyGroupResult) {
			String employeeId = (String) result[0];
			EmployeeCensusStrategyGroupDetails employee = new EmployeeCensusStrategyGroupDetails();
			employee.setEmplId(employeeId);
			employee.setStrategyId(((BigDecimal) result[1]).longValue());
			employee.setStrategyGroupId(((BigDecimal) result[2]).longValue());
			employee.setGroupType((String) result[3]);
			employee.setGroupDesc((String) result[4]);
			employee.setBenefitProgram((String) result[5]);

			List<EmployeeCensusStrategyGroupDetails> empGroupDetails;
			if (empToStrategyGroupDtls.containsKey(employeeId)) {
				empGroupDetails = empToStrategyGroupDtls.get(employeeId);
			} else {
				empGroupDetails = new ArrayList<>();
			}
			empGroupDetails.add(employee);
			empToStrategyGroupDtls.put(employeeId, empGroupDetails);
		}
		return empToStrategyGroupDtls;
	}
	
}
