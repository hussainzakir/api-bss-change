package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.BenefitOfferException;

@Repository
@Transactional(readOnly = true)
public interface BenOfferExceptionDao extends JpaRepository<BenefitOfferException, Long> {

	/**
	 * This method returns all applicable BenefitOfferException if given rpyStartDt falls between
	 * exception start and end date for companyCode, quarter and realmId.
	 * 
	 * @param companyCode
	 * @param quarter
	 * @param realmId
	 * @param rpyStartDt
	 * @return
	 */
	@Query("SELECT boe FROM BenefitOfferException boe WHERE boe.companyCode = ?1 AND boe.quarter = ?2 AND boe.realmId = ?3 AND ?4 BETWEEN boe.startDate AND boe.endDate AND boe.active = 1")
	Set<BenefitOfferException> findApplicableBy(String companyCode, String quarter, long realmId, Date rpyStartDt);

	/**
	 * This method returns all BenefitOfferException for given status.
	 * 
	 * @param active
	 * @return
	 */
	Set<BenefitOfferException> findByActive(boolean active);

	/**
	 * This method returns all active BenefitOfferException for given companyCode,
	 * quarter, realmId and planType
	 * 
	 * @param companyCode
	 * @param quarter
	 * @param realmId
	 * @param planType
	 * @return
	 */
	@Query("SELECT boe FROM BenefitOfferException boe WHERE boe.companyCode = ?1 AND boe.quarter = ?2 AND boe.realmId = ?3 AND boe.planType = ?4 AND boe.active = 1")
	Set<BenefitOfferException> findActiveBy(String companyCode, String quarter, long realmId, String planType);
	
	BenefitOfferException findById(long id);
}
