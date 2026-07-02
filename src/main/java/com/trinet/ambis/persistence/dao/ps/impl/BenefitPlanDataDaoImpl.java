package com.trinet.ambis.persistence.dao.ps.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.ps.BenefitPlanDataDao;
import com.trinet.ambis.util.DaoUtils;

public class BenefitPlanDataDaoImpl implements BenefitPlanDataDao {
	
    @PersistenceContext(unitName = "bis-hrp")
    private EntityManager hrpEntityManager;

    private static final Logger logger = LoggerFactory.getLogger(BenefitPlanDataDaoImpl.class);

    public EntityManager getHrpEntityManager() {
        return this.hrpEntityManager;
    }

    public void setHrpEntityManager(EntityManager em) {
        this.hrpEntityManager = em;
    }

	@Override
	public Map<String, List<String>> getMedicalAutoSelectedPlansByRegion(Set<String> plans, long relamYearId) {
		Query query = hrpEntityManager.createNamedQuery("getMedicalAutoSelectedPlans");
        query.setParameter("planList", plans);
        query.setParameter(BSSQueryConstants.REALM_YEAR_ID, relamYearId);
        logger.info("REALM YEAR ID : {}  PLANS : {}", relamYearId, plans);
        List<Object[]> results = DaoUtils.getResultList(query, "getMedicalAutoSelectedPlans");
        Map<String, List<String>> map = new HashMap<>();

        for (Object[] r : results) {  
        	List<String> list = null;
        	if (map.get((String) r[1]) != null) {
        		list = map.get((String) r[1]);        	   
        	}
        	else {
        		list = new ArrayList<>(); 
        	}
        	list.add((String) r[0]);
    		map.put((String) r[1], list);
        }		
		return map;
	}

}
