/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.Contribution;

/**
 * @author kpamulapati
 *
 */

@Repository
@Transactional(readOnly = true)
public interface ContributionDao extends JpaRepository<Contribution, Long>{	
	public Contribution findByPlanSelectionIdAndCoverageLevel(long planSelectionId,String coverageLevel);
	List<Contribution> findByPlanSelectionId(long planSelectionId);
	public Contribution findById( long id );
	
	/**
	 * Returns a List of all Contributions associated with the given planSelectionIds
	 * 
	 * @param planSelectionIds
	 * @return
	 */
	List<Contribution> findByPlanSelectionIdIn(List<Long> planSelectionIds);
}


