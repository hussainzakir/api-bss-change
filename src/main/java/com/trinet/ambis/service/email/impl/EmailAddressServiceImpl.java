package com.trinet.ambis.service.email.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.email.EmailAddressService;

@Service
public class EmailAddressServiceImpl implements EmailAddressService {
	@Autowired
	private HrpDao hrpDao;

	@Autowired
	private PsDao psDao;

	@Override
	public Set<String> getConfirmationStatementClientRecipients(Company company, String userId) {
		Set<String> emails = getAdminEmails(company.getCode());
		String emplEmailAddress = getEmployeeEmail(company.getCode(), userId);
		if (StringUtils.isNotEmpty(emplEmailAddress)) {
			emails.add(emplEmailAddress);
		}
		return emails;
	}

	@Override
	public Set<String> getConfirmationStatementNonClientRecipients(Company company) {
		Set<String> addresses = new HashSet<>();
		String implementationTeamEmail = BSSMessageConfig.getProperty("ImplementationTeamEmail");
		String customerSetupSiteEmail = BSSMessageConfig.getProperty("CustomerSetupSiteEmail");
		String cssEmail = BSSMessageConfig.getProperty("CssEmail");
		String tmtEmail = BSSMessageConfig.getProperty("TmtEmail");
		String ambroseImplementationEmail = BSSMessageConfig.getProperty("AmbroseImplementationEmail");
		String fundingConfirmEmail = BSSMessageConfig.getProperty("FundingConfirmationEmail");
		if (!company.isRenewalCompany()) {
			addresses.addAll(psDao.getNewClientAddresses(company));
			addresses.add(implementationTeamEmail);
			addresses.add(customerSetupSiteEmail);
			addresses.add(tmtEmail);
		} else {
			addresses.addAll(psDao.getAssignmentAddresses(company));
			addresses.add(ambroseImplementationEmail);
			addresses.add(cssEmail);
			addresses.add(tmtEmail);
		}
		addresses.add(fundingConfirmEmail);
		return addresses;
	}
	
	public String getEmployeeEmail(String companyCode, String employeeId) {
		return hrpDao.getEmplEmail(companyCode, employeeId);
	}

	private Set<String> getAdminEmails(String companyCode) {
		if (CommonServiceHelper.isTriNetCompany(companyCode)) {
			return hrpDao.getRoleEmails(companyCode, "BEN_CORP_AD");
		} else {
			return hrpDao.getBDMEmails(companyCode);
		}
	}

}
