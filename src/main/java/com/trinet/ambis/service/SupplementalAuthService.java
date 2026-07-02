package com.trinet.ambis.service;

import com.trinet.ambis.service.model.SupplementalLtdAuthReponse;

public interface SupplementalAuthService {

	SupplementalLtdAuthReponse getExecSuppLtdAuthResponse(final String companyCode);

	SupplementalLtdAuthReponse saveExecSuppLtdAuthResponse(final String companyCode, final char answer);
}
