package com.trinet.ambis.persistence.dao.ps;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.service.model.SupplementalLtdAuthReponse;

@Repository
public interface SupplementalAuthDao {

	public SupplementalLtdAuthReponse getExecSuppLtdAuthResponse(String companyCode);

	public void saveExecSuppLtdAuthResponse(String companyCode, SupplementalLtdAuthReponse supplementalLtdAuthReponse);
}
