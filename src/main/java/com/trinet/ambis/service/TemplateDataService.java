package com.trinet.ambis.service;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.NewCompanyOptions;

@Service
public interface TemplateDataService {
	/**
	 * 
	 * @param company
	 * @return
	 */
	NewCompanyOptions getNewCompanyOptions(Company company);
}
