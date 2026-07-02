package com.trinet.ambis.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.DeselectionExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearRuleDao;
import com.trinet.ambis.persistence.model.RealmPlanYearRule;
import com.trinet.ambis.service.RealmPlanYearRuleService;

@Service
public class RealmPlanYearRuleServiceImpl implements RealmPlanYearRuleService {

	@Autowired
	RealmPlanYearRuleDao realmPlanYearRuleDao;

	@Autowired
	DeselectionExceptionDao deselectionExceptionDao;

	@Override
	public List<RealmPlanYearRule> findByRealmPlanYearId(long realmPlanYearId) {
		return realmPlanYearRuleDao.findByIdRealmPlanYearId(realmPlanYearId);
	}


	@Override
	public boolean findPickChooseWithExceptions( long realmYearId, String companyCode, Date effdt ) {
		List<Object[]> list = deselectionExceptionDao.getPickChooseWithException( realmYearId, companyCode, effdt );
		String value = null;
		String exception = null;
		for( Object[] row : list ) {
			value = row[1].toString();
			exception = row[2].toString();
		}

		return "1".equals(exception) || "1".equals(value);
	}

}