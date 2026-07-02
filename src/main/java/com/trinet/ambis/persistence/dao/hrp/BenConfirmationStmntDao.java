package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.service.model.BenConfirmationStatement;

@Repository
public interface BenConfirmationStmntDao {

	List<BenConfirmationStatement> getBenefitConfirmationStatementsBy(String companyCode);

}
