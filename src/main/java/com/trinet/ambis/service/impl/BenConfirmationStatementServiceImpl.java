package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.SubmitServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenConfirmationStmntDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenConfirmationStatementService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.DocManagementService;
import com.trinet.ambis.service.model.BenConfirmationStatement;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.common.AppConfig;
import com.trinet.common.DateUtils;

@Service
public class BenConfirmationStatementServiceImpl implements BenConfirmationStatementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BenConfirmationStatementServiceImpl.class);

	@Autowired
	private BenConfirmationStmntDao benConfirmationStmntDao;

	@Autowired
	private DocManagementService docManagementService;
	
	@Autowired
	private CompanyService companyService;

	private static final String BEN_MID_YEAR_SUBMIT = "Mid-Year Submit";
	private static final String ANNUAL_RENEWAL = "Annual Renewal";
	private static final String FIRST_YEAR = "First Year";
	private static final String MID_YEAR_FUNDING = "Mid-Year Funding";
	private static final int MAX_STATEMENTS_COUNT = 5;

	@Override
	public List<BenConfirmationStatement> getBenConfirmationStatementsBy(String companyCode) {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), null);
		Date benefitStartDate = CommonUtils.formatStringToDate(company.getBenefitStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);

		List<BenConfirmationStatement> benConfirmationStatements = benConfirmationStmntDao
				.getBenefitConfirmationStatementsBy(companyCode);
		List<BenConfirmationStatement> finalConfirmationStmts = new ArrayList<>(benConfirmationStatements.size());
		Map<String, String> confirmationIdsUrls = docManagementService.retrieveConfirmationStatementUrls(companyCode);
		long prevRealmYrId = 0;
		Date prevEffectiveDt = null;
		for (BenConfirmationStatement benConfirmationStatement : benConfirmationStatements) {
			if (benConfirmationStatement.getRealmYrId() == prevRealmYrId) {
				benConfirmationStatement.setStatementStartDate(benConfirmationStatement.getEffectiveDate());
				benConfirmationStatement.setStatementEndDate(DateUtils.addDays(prevEffectiveDt, -1));
			} else {
				benConfirmationStatement.setStatementStartDate(benConfirmationStatement.getEffectiveDate());
				benConfirmationStatement.setStatementEndDate(benConfirmationStatement.getPlanYrEndDate());
			}
			benConfirmationStatement.setSubmitType(getSubmitType(benConfirmationStatement, benefitStartDate));
			String url = confirmationIdsUrls.get(SubmitServiceHelper
					.generateConfirmationStmtPdfName(benConfirmationStatement.getConfirmationNumber()));
			if (!StringUtils.isEmpty(url)) {
				url = AppConfig.getPlatformURL() + url;
			}
			benConfirmationStatement.setUrl(url);
			finalConfirmationStmts.add(benConfirmationStatement);
			prevRealmYrId = benConfirmationStatement.getRealmYrId();
			prevEffectiveDt = benConfirmationStatement.getEffectiveDate();
			if(finalConfirmationStmts.size() == MAX_STATEMENTS_COUNT) {
				break;
			}
		}
		return finalConfirmationStmts;
	}

	private String getSubmitType(BenConfirmationStatement benConfirmationStatement, Date benefitStartDate) {
		String submitType = null;
		if (benConfirmationStatement.getSubmitUser().equalsIgnoreCase(BEN_MID_YEAR_SUBMIT)) {
			submitType = MID_YEAR_FUNDING;
		} else if (DateUtils.compareDate(benConfirmationStatement.getPlanYrStartDate(), benefitStartDate) <= 0) {
			submitType = FIRST_YEAR;
		} else {
			submitType = ANNUAL_RENEWAL;
		}
		return submitType;
	}

}
