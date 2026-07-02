/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.HeadCountData;

/**
 * @author rvutukuri
 *
 */
@Service
public interface BenefitGroupHeadCountService {
	/**
	 * 
	 * @param company
	 * @param headCountList
	 */
	void updateGroupHeadCount(Company company, List<HeadCountData> headCountList);

}
