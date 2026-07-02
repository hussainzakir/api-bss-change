package com.trinet.ambis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;

@Service
public interface BenefitClassService {
	/**
	 * Generate a class code for this benefit group.  If one is already assigned to this group,
	 * that value is returned.
	 * @param company
	 * @param group
	 * @return the value already assigned to this group or a new value generated for this group
	 * @throws Exception when a new class code could not be generated because there are no new values available.
	 * The current scheme only allows for 36 different benefit class codes.
	 */
	String generateClassCode( Company company, BenefitGroup group );

	/**
	 * Generate class codes for each benefit group in a collection.
	 * @param company
	 * @param groups
	 * @return the List of BenefitGroup objects after updating the benefit class (ELIG_CONFIG1) values
	 * @throws Exception when a new class code could not be generated because there are no new values available.
	 * The current scheme only allows for 36 different benefit class codes.
	 */
	List<BenefitGroup> generateAllClassCodes( Company company, List<BenefitGroup> groups );

}
