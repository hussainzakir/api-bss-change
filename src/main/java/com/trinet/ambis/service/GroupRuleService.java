package com.trinet.ambis.service;

import java.util.Date;
import java.util.List;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.GroupRuleDto;

/**
 * @author hliddle
 *
 */
public interface GroupRuleService {

	/**
	 * Returns a List of all {@code GroupRuleDto} for given
	 * effective and expiration date
	 * 
	 * @param effDate
	 * @param expDate
	 * @return {@code List<GroupRuleDto>}
	 */
	List<GroupRuleDto> findByDate(Date date);
	
	List<GroupRuleDto> getApplicableGroups(Company company, boolean onlyMandatory);

}