package com.trinet.ambis.service.impl.planofferings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.trinet.ambis.enums.OutputBenefitsTypeEnums;
import com.trinet.ambis.rest.controllers.dto.outputs.VariableInstructions;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsReportDetails;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.PlanOfferingsRequestBuilder;
import com.trinet.ambis.service.planofferings.PlanOfferingsReportDataService;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author smaguluri
 *
 */


@Service
@Log4j2
public class PlanOfferingsRequestBuilderImpl implements PlanOfferingsRequestBuilder{
	
	@Value("${bssContent}")
	private String bssContent;

	@Value("${cmsUrl}")
	private String cmsUrl;

	@Value("${template}")
	private String template;

	@Value("${templateEngine}")
	private String templateEngine;

	@Value("${planOfferingsMainTemplate}")
	private String mainTemplate;
	
	@Autowired
	private PlanOfferingsReportDataService planOfferingsReportDataService;

	@Override
	public PlanOfferingsReportDetails buildPlanOfferingsReportRequest(PlanOfferingsRequest planOfferingsRequest,
			HttpServletRequest httpRequest) {
		transformToBSSBenefitTypeCode(planOfferingsRequest);
		PlanOfferingsReportDetails reportDetails = new PlanOfferingsReportDetails();
		reportDetails.setCmsType(bssContent);
		reportDetails.setCmsUrl(cmsUrl);
		reportDetails.setTemplate(template);
		reportDetails.setTemplateEngine(templateEngine);
		reportDetails.setMainTemplate(mainTemplate);
		reportDetails.setData(
				planOfferingsReportDataService.preparePlanOfferingsData(planOfferingsRequest, httpRequest));
		reportDetails.setVariableInstructions(readPlanOfferingsRequest("/Outputs-templates/Templates/planofferingsvariableinstructons.json", new TypeReference<VariableInstructions>(){}).get());
		log.info("Plan Offerings report request {}", reportDetails);
		return reportDetails;
	}
	

	/**
	 * This method is to Transform to BSS BenefitType codes
	 * 
	 * @param planOfferingsRequest
	 */
	private void transformToBSSBenefitTypeCode(PlanOfferingsRequest planOfferingsRequest) {
		List<String> bSSBenefitTypeCodes = planOfferingsRequest.getBenefitTypes().stream()
				.map(OutputBenefitsTypeEnums::getBenTypeCodeByName).collect(Collectors.toList());
		planOfferingsRequest.setBenefitTypes(bSSBenefitTypeCodes);
	}
	
	public static <T> Supplier<T> readPlanOfferingsRequest(String filePath, TypeReference<T> valueType) {
		return () -> {
			ClassPathResource staticDataResource = new ClassPathResource(filePath);
			try {
				String dto = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
				mapper.setVisibility(
						VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
				return mapper.readValue(dto, valueType);
			} catch (IOException e) {
				log.error("Error while created plan comparison Object...{} ", filePath);
			}
			return null;
		};
	}
}
