package com.trinet.ambis.service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;


@Service
public interface BplService {

	/**
	 * Get all the BPLPlanAttributes for given planIds and effective date
	 * 
	 * @param planIds
	 * @param effDate
	 * @param template
	 * @param httpRequest
	 * @return
	 */
	public CompletableFuture<List<BenefitPlanCompare>> getBPLAttributes(Set<String> planIds,
			Date effDate, String template, HttpServletRequest httpRequest);
}
