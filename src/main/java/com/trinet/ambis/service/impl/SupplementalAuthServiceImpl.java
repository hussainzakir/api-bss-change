package com.trinet.ambis.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.dao.ps.SupplementalAuthDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.SupplementalAuthService;
import com.trinet.ambis.service.email.EmailAddressService;
import com.trinet.ambis.service.model.SupplementalLtdAuthReponse;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.common.DateUtils;

@Service
public class SupplementalAuthServiceImpl implements SupplementalAuthService {

	@Autowired
	private PsDao psDao;

	@Autowired
	private EmailAddressService emailAddressService;

	@Autowired
	private SupplementalAuthDao supplementalAuthDao;

	@Autowired
	private CompanyService companyService;

	@Override
	public SupplementalLtdAuthReponse getExecSuppLtdAuthResponse(final String companyCode) {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), null);
		SupplementalLtdAuthReponse response = supplementalAuthDao.getExecSuppLtdAuthResponse(companyCode);
		if (company.isRenewalOpen()) {
			response = setBannerAndPopupDisplayFlag(company, response);
			if (response.getAuthUserId() != null) {
				setAuthPersonDetails(companyCode, response);
			}
			setLoggedInPersonDetails(companyCode, response);
		}
		if (response == null) {
			response = SupplementalLtdAuthReponse.builder().build();
		}
		return response;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public SupplementalLtdAuthReponse saveExecSuppLtdAuthResponse(final String companyCode, final char answer) {
		String authUserId = BSSSecurityUtils.getAuthenticatedPersonId();
		SupplementalLtdAuthReponse supplementalLtdAuthReponse = SupplementalLtdAuthReponse.builder().answer(answer)
				.authUserId(authUserId).build();
		supplementalAuthDao.saveExecSuppLtdAuthResponse(companyCode, supplementalLtdAuthReponse);
		return getExecSuppLtdAuthResponse(companyCode);
	}

	private SupplementalLtdAuthReponse setBannerAndPopupDisplayFlag(Company company,
			SupplementalLtdAuthReponse response) {
		if (response == null) {
			response = SupplementalLtdAuthReponse.builder().displayPopup(true).displayBanners(true).build();
		} else {
			Date closeDt = company.getSchedTbl().getExtensionEndDate().after(company.getSchedTbl().getCloseDate())
					? company.getSchedTbl().getExtensionEndDate()
					: company.getSchedTbl().getCloseDate();
			if (DateUtils.isDateWithinRange(response.getAuthDate(), company.getSchedTbl().getOpenDate(), closeDt)) {
				response.setDisplayBanners(true);
			}
		}
		return response;
	}

	private void setAuthPersonDetails(final String companyCode, SupplementalLtdAuthReponse response) {
		String authFirstName = psDao.getEmployeeFirstName(response.getAuthUserId());
		String authLastName = psDao.getEmployeeLastName(response.getAuthUserId());
		String authEmail = emailAddressService.getEmployeeEmail(companyCode, response.getAuthUserId());
		response.setAuthFirstName(authFirstName);
		response.setAuthLastName(authLastName);
		response.setAuthEmail(authEmail);
	}

	private void setLoggedInPersonDetails(final String companyCode, SupplementalLtdAuthReponse response) {
		String loggedInEmplId = BSSSecurityUtils.getAuthenticatedPersonId();
		String firstName = psDao.getEmployeeFirstName(loggedInEmplId);
		String lastName = psDao.getEmployeeLastName(loggedInEmplId);
		String email = emailAddressService.getEmployeeEmail(companyCode, loggedInEmplId);
		response.setEmail(email);
		response.setUserId(loggedInEmplId);
		response.setLastName(lastName);
		response.setFirstName(firstName);
	}

}
