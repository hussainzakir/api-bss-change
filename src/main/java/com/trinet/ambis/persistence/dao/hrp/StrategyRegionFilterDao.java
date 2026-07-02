/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.StrategyRegion;

/**
 * @author schaudhari
 *
 */

@Repository
@Transactional(readOnly = true)
public interface StrategyRegionFilterDao extends JpaRepository<StrategyRegion, Long> {

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	int deleteByIdIn(final @Param("ids") Set<Long> ids);
}