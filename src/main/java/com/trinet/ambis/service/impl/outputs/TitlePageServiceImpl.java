package com.trinet.ambis.service.impl.outputs;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.TitlePageData;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.outputs.TitlePageService;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author pallu
 *
 * Get Title Page Data
 */
@Service
public class TitlePageServiceImpl implements TitlePageService {

    @Autowired
    private PersonService personService;

    @Override
	public TitlePageData getTitlePageData(OutputRequest outputRequest, Company company) {
		TitlePageData titlePageData = new TitlePageData();

		titlePageData.setExchange(
				Objects.requireNonNull(BenExchngEnums.getByBenExchange(company.getRealm().getBenExchange()),
						"Exchange cannot be Null").getBenExchng());
		titlePageData.setZipcode(company.getZipCode());
		titlePageData.setExpirationDate(CommonUtils.formatDate(company.getExpiryDate(),
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD, BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY));
		titlePageData.setPlanYearStartDate((company.getRealmPlanYear().getPlanYearStart()).toString());
		titlePageData.setPlanYearEndDate((company.getRealmPlanYear().getPlanYearEnd()).toString());
		titlePageData.setEmployeeRegions(company.getEmployeeRegions());
		titlePageData.setProposalId(company.getProposalId());
		titlePageData.setEffectiveDate(CommonUtils.formatDate(company.getBenefitStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY, BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY));
		
		boolean isPlanAppendixFiltersExist = outputRequest.getTemplateNames().stream().anyMatch(templateName -> templateName.equals(ProspectConstants.PLAN_APPENDIX))
				&& outputRequest.getPlanAppendixFilters()!= null ;
		
		if(isPlanAppendixFiltersExist) {
			titlePageData.setRegions(outputRequest.getPlanAppendixFilters().getRegions());
			titlePageData.setAdditionalZipCodes(outputRequest.getPlanAppendixFilters().getZipCodes());
			titlePageData.setGroupName(outputRequest.getPlanAppendixFilters().getGroupName());
		}
		return titlePageData;
	}
}
