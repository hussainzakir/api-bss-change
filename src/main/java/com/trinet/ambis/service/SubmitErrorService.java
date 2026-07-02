package com.trinet.ambis.service;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.SubmitError;

@Service
public interface SubmitErrorService {

	/**
	 * Save SubmitError in db
	 * 
	 * @param SubmitError
	 * @return submitError
	 */
	public SubmitError save(SubmitError submitError);

}
