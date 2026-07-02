package com.trinet.ambis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trinet.ambis.service.model.ExceptionAttributeDto;

@Service
public interface ExceptionAttributeService {
	
	/**
	 *getting all attributes for exceptions
	 * @return
	 */
	List<ExceptionAttributeDto> findAllExceptionAttributes();
}
