package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.MinimumFundingException;

/**
 * @author schaudhari
 *
 */
@Repository
@Transactional(readOnly = true)
public interface MinFundExceptionDao extends JpaRepository<MinimumFundingException, Long> {

	/**
	 * This method returns all active MinimumFundingException for given companyCode
	 * and quarter.
	 * 
	 * 
	 * @param companyCode
	 * @param quarter
	 * @return
	 */
	@Query("SELECT mfe FROM MinimumFundingException mfe WHERE mfe.companyCode = ?1 AND mfe.quarter = ?2 AND TRUNC(?3) BETWEEN mfe.startDate AND mfe.endDate AND mfe.active = 1")
	Set<MinimumFundingException> findActiveBy(String companyCode, String quarter, Date planStartDate);

	/**
	 * This method returns all MinimumFundingException for given status.
	 * 
	 * @param active
	 * @return
	 */
	Set<MinimumFundingException> findByActive(boolean active);

	/**
	 * This method returns all active MinimumFundingException for given companyCode,
	 * quarter, realmId and planType
	 * 
	 * @param companyCode
	 * @param quarter
	 * @param realmId
	 * @param planType
	 * @return
	 */
	@Query("SELECT mfe FROM MinimumFundingException mfe WHERE mfe.companyCode = ?1 AND mfe.quarter = ?2 AND mfe.realmId = ?3 AND mfe.planType = ?4 AND mfe.active = 1")
	Set<MinimumFundingException> findActiveBy(String companyCode, String quarter, long realmId, String planType);

	MinimumFundingException findById(long id);
}