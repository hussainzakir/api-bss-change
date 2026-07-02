package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.service.model.HeadCountData;

/**
 * 
 * @author akaparaboyna
 *
 */
@Service
public interface HeadCountDistributionService {	
	Map<String, Map<String, Integer>> planHeadCountDistribution(Company company, List<PlanSelection> plans, HeadCountData headCountData);
}
