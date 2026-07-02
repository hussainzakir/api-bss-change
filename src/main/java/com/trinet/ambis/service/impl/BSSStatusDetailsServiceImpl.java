package com.trinet.ambis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.BSSStatusDetailsDao;
import com.trinet.ambis.persistence.model.BSSStatusDetailsDto;
import com.trinet.ambis.service.BSSStatusDetailsService;

@Service
public class BSSStatusDetailsServiceImpl implements BSSStatusDetailsService {

	@Autowired
	BSSStatusDetailsDao bssStatusDetailsDao;

	@Override
	public BSSStatusDetailsDto getBssStatusDetail(String companyCode) {
		return bssStatusDetailsDao.getSubmitedStatus(companyCode);
	}

}
