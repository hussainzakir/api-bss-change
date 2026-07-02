package com.trinet.ambis.service.impl.outputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.OutputBenefitsTypeEnums;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.BSSReportDetails;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixFilters;
import com.trinet.ambis.service.outputs.OutputService;
import com.trinet.ambis.service.prospect.dto.BenTypeOfferRes;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.RestApiClient;
import com.trinet.ambis.util.Utils;

@Service
public class OutputServiceImpl implements OutputService {

	@Autowired
	private OutputRequestBuilderImpl outputRequestBuiler;

	@Autowired
	private RestApiClient restApiClient;

	@Autowired
	private StrategyDataDao strategyDataDao;
	
	@Value("${docGenFetchApiUri}")
	private String docGenUrl;

	@Override
	public byte[] generateReport(OutputRequest request, Company company, HttpServletRequest httpRequest) {
		return callDocGenAPI(request, company, httpRequest);
	}

	@Override
	public List<BenTypeOfferRes> getPlanTypeOfferedDetails(List<Long> strategyIds, List<String> planTypes) {
		List<BenTypeOfferRes> results = new ArrayList<>();
		MultiKeyMap benTypeOfferedByStrategyAndGroup = strategyDataDao.getStrategyProgramPlantypeOfferings(strategyIds, planTypes);
		MapIterator it = benTypeOfferedByStrategyAndGroup.mapIterator();
		while (it.hasNext()) {
			long strategyId = (long) ((MultiKey) it.next()).getKey(0);
			String benType = (String) it.getValue();
			String genericBenType = Utils.getGenericPlanTypeCode(benType);
			Optional<BenTypeOfferRes> benTypeRes = results.stream()
					.filter(result -> Long.compare(result.getStrategyId(), strategyId) == 0).findFirst();

			if (!benTypeRes.isPresent()) {
				Set<String> benOfferTypes = new HashSet<>();
				benOfferTypes.add(genericBenType);
				results.add(BenTypeOfferRes.builder().strategyId(strategyId).offerTypes(benOfferTypes).build());
			} else {
				benTypeRes.get().getOfferTypes().add(genericBenType);
			}
		}
		return results;
	}
	
	@Override
	public byte[] generateEmployeeCostAndPlanComparisonReport(Company company, long strategyId, HttpServletRequest httpRequest) {
		OutputRequest ouputRequest = new OutputRequest();
		ouputRequest.setTemplateNames(
				Arrays.asList(ProspectConstants.EMPLOYEE_COST_COMPARISON, ProspectConstants.PLAN_COMPARISON));
		return getReport(company, strategyId, ouputRequest, httpRequest);
	}
	
	@Override
	public byte[] generatePlanAppendixReport(Company company, long strategyId, HttpServletRequest httpRequest) {
		OutputRequest ouputRequest = new OutputRequest();
		ouputRequest.setTemplateNames(Arrays.asList(ProspectConstants.PLAN_APPENDIX));
		return getReport(company, strategyId, ouputRequest, httpRequest);
	}
	
	private byte[] getReport(Company company, long strategyId, OutputRequest ouputRequest, HttpServletRequest httpRequest) {
		ouputRequest.setTnStrategyId(String.valueOf(strategyId));
		ouputRequest.setBenefitTypes(Arrays.asList(OutputBenefitsTypeEnums.MEDICAL.getName(),
				OutputBenefitsTypeEnums.DENTAL.getName(), OutputBenefitsTypeEnums.VISION.getName(),
				OutputBenefitsTypeEnums.LIFE.getName(), OutputBenefitsTypeEnums.DISABILITY.getName()));
		ouputRequest.setPlanAppendixFilters(new PlanAppendixFilters());
		ouputRequest.getPlanAppendixFilters().setIncludeOnlyEeLocationPlans(true);
		return callDocGenAPI(ouputRequest, company, httpRequest);
	}

	private byte[] callDocGenAPI(OutputRequest request, Company company, HttpServletRequest httpRequest) {
		String authCompanyCode = BSSSecurityUtils.getAuthenticatedCompanyCode(httpRequest);
		String authEmplId = BSSSecurityUtils.getAuthenticatedEmplId(httpRequest);
		BSSReportDetails reportDetails = outputRequestBuiler.prepareBssReportRequest(request, company, httpRequest);
        String url = docGenUrl + authCompanyCode + "/" + authEmplId + "/generate-download";
		return restApiClient.getReturnResponse(httpRequest, reportDetails, url, HttpMethod.POST);
	}

}