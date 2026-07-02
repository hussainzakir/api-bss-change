package com.trinet.ambis.persistence.dao.hrp;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.HQException;

/**
 * @author schaudhari
 *
 */
@Repository
@Transactional(readOnly = true)
public interface HQExceptionDao extends JpaRepository<HQException, Long> {

	/**
	 * This method returns HQException for the given realmPlanYearId and company
	 * @param realmPlanYearId
	 * @param companyCode
	 * @return HQException
	 */
	Optional<HQException> findByIdRealmYrIdAndIdCompany(long realmPlanYearId, String companyCode);

}