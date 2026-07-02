package com.trinet.ambis.service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;


@Service
public interface BenefitsPlanViewService {

	public CompletableFuture<List<BenefitPlanCompare>> getBenefitPlanAttributes(Set<String> planIds,
			Date effDate, String template, HttpServletRequest httpRequest);
}
