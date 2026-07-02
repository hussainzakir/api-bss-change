package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.service.model.BenConfirmationStatement;

public interface BenConfirmationStatementService {

	public List<BenConfirmationStatement> getBenConfirmationStatementsBy(String companyCode);
}
