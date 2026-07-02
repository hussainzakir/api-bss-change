/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.BenefitGroupStrategy;

/**
 * @author rvutukuri
 *
 */
@Repository
@Transactional(readOnly = true)
public interface BenefitStrategyGroupDao extends JpaRepository<BenefitGroupStrategy, Long> {

	public BenefitGroupStrategy findById(long id);

	public BenefitGroupStrategy findByStrategyIdAndGroupId(long strategyId, long groupId);

	public List<BenefitGroupStrategy> findByStrategyIdAndStatus(long strategyId, String status);

	@Query("SELECT bsg FROM BenefitGroupStrategy bsg,"
			+ " Strategy s,"
			+ " Company c"
			+ " WHERE bsg.strategyId = s.id"
			+ " AND s.companyId = c.id"
			+ " AND c.code = ?1")
	List<BenefitGroupStrategy> findBy(String companyCode);
	
	public BenefitGroupStrategy findByIdAndStrategyId(long id, long strategyId);

}
