package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.BenefitGroup;

/**
 * @author mpulipaka
 *
 */

@Repository
@Transactional(readOnly = true)
public interface BenefitGroupDao extends JpaRepository<BenefitGroup, Long> {
	@Transactional
	public List<BenefitGroup> findByCompanyIdAndStatus(long companyId, String status);

	@Transactional
	public List<BenefitGroup> findByCompanyId(long companyId);

	public BenefitGroup findById( long id );

	public BenefitGroup findByCompanyIdAndId(long companyId, long id);

	@Query("select  XG from BenefitGroup XG JOIN FETCH XG.benefitGroupStrategy bgs where bgs.strategyId=?1 AND bgs.status=?2 and bgs.groupId =XG.id")
	public List<BenefitGroup> getBenefitGroupsByStrategyId(long strategyId, String status);

	@Query("select  XG from BenefitGroup XG JOIN FETCH XG.benefitGroupStrategy bgs where bgs.strategyId=?1 AND bgs.groupId=?2 AND bgs.status=?3")
	public BenefitGroup getBenefitGroupsByStrategyIdAndGroupId(long strategyId, long groupId, String status);
	
	@Query("select  XG from BenefitGroup XG JOIN FETCH XG.benefitGroupStrategy bgs where bgs.strategyId=?1 AND bgs.status in(?2) and bgs.groupId =XG.id")
	public List<BenefitGroup> getBenefitGroupsByStrategyId(long strategyId, List<String> status);

}
