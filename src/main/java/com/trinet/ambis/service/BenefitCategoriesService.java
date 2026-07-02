/**
 * 
 */
package com.trinet.ambis.service;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitsCategories;

/**
 * @author rvutukuri
 *
 */
@Service
public interface BenefitCategoriesService {
	/**
	 * 
	 * @param company
	 * @return
	 */
	BenefitsCategories constructBenefitsCategories(Company company);

}
