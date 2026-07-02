package com.trinet.ambis.persistence.dao.hrp.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.persistence.dao.hrp.PersonDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.util.DaoUtils;

public class PersonDaoImpl implements PersonDao {

	@PersistenceContext(unitName = "bis-hrp")
    EntityManager em;
	
	private static final Logger logger = LoggerFactory.getLogger(PersonDaoImpl.class);

	private static final String FIRST_LAST_NAME = "getFirstAndLastName";
	private static final String CS_AUTH_MAIL = "getCSAuthEmail";
	private static final String COMP_HRM_MAIL = "getHrmEmailForCompany";
	
	public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public EntityManager getEntityManager() {
        return this.em;
    }
	
	@Override
	public String getFirstandLastName(String userId) {
		logger.info("Inside getFirstandLastName(String userId) method of PersonDaoImpl");
		String employeeName = null;
		try{
			Query query = em.createNamedQuery(FIRST_LAST_NAME);
			query.setParameter("userPersonId", userId);

			List<Object[]> results = DaoUtils.getResultList(query, FIRST_LAST_NAME);
			if(!results.isEmpty()){
				if(results.size() > 1){
					logger.error("!!!!! Multiple entries found for the employee {}", userId);
					return null;
				}
				Object[] record = results.get(0);
				if(record.length == 2){
				logger.info("{} {} just logged in...", record[0], record[1]);
				employeeName = record[0]+" "+record[1];
				}
			}
		}catch(Exception exc){
			logger.error("Exception {} encountered in getFirstandLastName(String userId) method of PersonDaoImpl", exc);
		}
		return employeeName;
	}

	/**
	 * Method that returns the Email Address of the designated employee who is responsible for CustomerSetup at new client.
	 *
	 * @param loggedInUserId
	 * @return
	 */
	@Override
	public String getCSAuthEmail(String loggedInUserId) {
		logger.info("Inside getCSAuthEmail(String loggedInUserId) method of PersonDaoImpl");
		String csAuthEmail = null;
		try {
			Query query = em.createNamedQuery(CS_AUTH_MAIL);
			query.setParameter("personId", loggedInUserId);
			List<String> results = DaoUtils.getResultStringList(query, CS_AUTH_MAIL);
			if (!results.isEmpty()) {
				if (results.size() > 1) {
					logger.error("!!!!! Multiple entries found for the employee {}", loggedInUserId);
					return null;
				}
				csAuthEmail = results.get(0);
			}
		} catch (Exception exc) {
			logger.error("Exception {} encountered in getCSAuthEmail(String loggedInUserId) method of PersonDaoImpl", exc);
		}
		return csAuthEmail;
	}

	/**
	 * Method that returns Email Address of HR Manager assigned to the new client.
	 *
	 * @param companyId
	 * @return
	 */
	@Override
	public String getCompanyHrmEmail(String companyId) {
		logger.info("Inside getCompanyHrmEmail(String companyId) method of PersonDaoImpl");
		String companyHrmEmail = null;
		try {
			Query query = em.createNamedQuery(COMP_HRM_MAIL);
			query.setParameter("companyId", companyId);
			List<String> results = DaoUtils.getResultStringList(query, COMP_HRM_MAIL);
			if (!results.isEmpty()) {
				if (results.size() > 1) {
					logger.error("!!!!! Multiple entries found for the company {}", companyId);
					return null;
				}
				companyHrmEmail = results.get(0);
			}
		} catch (Exception exc) {
			logger.error("Exception {} encountered in getCompanyHrmEmail(String companyId) method of PersonDaoImpl", exc);
		}
		return companyHrmEmail;
	}

}
