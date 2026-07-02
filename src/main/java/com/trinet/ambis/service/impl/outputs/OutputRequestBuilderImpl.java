package com.trinet.ambis.service.impl.outputs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.JsonConverterUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.BSSReportDetails;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputData;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.VariableInstructions;
import com.trinet.ambis.service.OutputRequestBuilder;
import com.trinet.ambis.service.outputs.OutputReportDataService;

import lombok.extern.log4j.Log4j2;

/**
 * @author rterle
 *
 */
@Service
@Log4j2
public class OutputRequestBuilderImpl implements OutputRequestBuilder {

	@Value("${bssContent}")
	private String bssContent;

	@Value("${cmsUrl}")
	private String cmsUrl;

	@Value("${template}")
	private String template;

	@Value("${templateEngine}")
	private String templateEngine;

	@Value("${mainTemplate}")
	private String mainTemplate;

	@Value("${mainTemplatePhase2}")
	private String mainTemplatePhase2;

	@Autowired
	private OutputReportDataService outputReportDataService;

	@Override
	public BSSReportDetails prepareBssReportRequest(OutputRequest prospectRequest, Company company,
			HttpServletRequest httpRequest) {
		BSSReportDetails bssReportDetails = new BSSReportDetails();
		bssReportDetails.setCmsType(bssContent);
		bssReportDetails.setCmsUrl(cmsUrl);
		bssReportDetails.setTemplate(template);
		bssReportDetails.setTemplateEngine(templateEngine);
        if (AppRulesAndConfigsUtils.isBssOutputPhase2Enabled()) {
          bssReportDetails.setMainTemplate(mainTemplatePhase2);

        } else {
          bssReportDetails.setMainTemplate(mainTemplate);
        }
		prospectRequest.setBenefitTypes(getOrderedBenefitPlanTypes().apply(prospectRequest.getBenefitTypes()));
		OutputData reportData = outputReportDataService.getData(prospectRequest, company, httpRequest);
		bssReportDetails.setData(reportData);
		bssReportDetails.setVariableInstructions(readVariableInstructions().get());
		log.info("Bss report request for {}: {}", company.getCode(), JsonConverterUtils.convertObjectToJson(bssReportDetails));
		return bssReportDetails;
	}

	private Function<List<String>, List<String>> getOrderedBenefitPlanTypes() {
		return benefitPlanTypes -> {
			List<String> orderedBenefitPlanTypes = new LinkedList<>(ProspectConstants.OUTPUTS_PLAN_TYPE_ORDER);
			orderedBenefitPlanTypes.retainAll(benefitPlanTypes);
			return orderedBenefitPlanTypes;
		};

	}
	
	private static Supplier<VariableInstructions> readVariableInstructions() {
        return () -> {
            String filePath = AppRulesAndConfigsUtils.isBssOutputPhase2Enabled()
                    ? "/Outputs-templates/Templates/newVariableInstructions.json"
                    : "/Outputs-templates/Templates/variableInstructions.json";
            ClassPathResource staticDataResource = new ClassPathResource(filePath);
            try {
                String dto = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                mapper.setVisibility(
                        VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
                return mapper.readValue(dto, VariableInstructions.class);
            } catch (IOException e) {
                throw new BSSApplicationException(
    					new BSSApplicationError("Exception while preparing the variable instructions, Error : " + e.getMessage()));
            }
        };
    }

}
