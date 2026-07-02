package com.trinet.ambis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.SubmitErrorDao;
import com.trinet.ambis.persistence.model.SubmitError;
import com.trinet.ambis.service.SubmitErrorService;

@Service
public class SubmitErrorServiceImpl implements SubmitErrorService {

	@Autowired
	SubmitErrorDao submitErrorDao;

	@Override
	public SubmitError save(SubmitError submitError) {
		return submitErrorDao.save(submitError);
	}

}
