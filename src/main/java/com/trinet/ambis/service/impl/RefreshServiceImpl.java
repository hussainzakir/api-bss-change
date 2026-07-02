/**
 * 
 */
package com.trinet.ambis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.service.RefreshService;

/**
 * @author kpamulapati
 *
 */
@Service
public class RefreshServiceImpl implements RefreshService {
	
	@Autowired
	HrpDao hrpDao;
	

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void refreshPlanView() {
		hrpDao.refreshPlanView();
	}

}
