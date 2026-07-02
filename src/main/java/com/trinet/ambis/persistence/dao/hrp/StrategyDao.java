/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.Strategy;

/**
 * @author kpamulapati
 *
 */

@Repository
@Transactional(readOnly = true)
public interface StrategyDao extends JpaRepository<Strategy, Long> {
	/**
	 * 
	 * @param companyId
	 * @return
	 */
	List<Strategy> findByCompanyId(long companyId);
	
	/**
	 * 
	 * @param companyId
	 * @param submitted
	 * @return
	 */
	List<Strategy> findByCompanyIdAndStatus(long companyId, String status);

	/**
	 * 
	 * @param companyId
	 * @param submitted
	 * @return
	 */
	List<Strategy> findByCompanyIdAndSubmitted(long companyId, boolean submitted);
	
	/**
	 * 
	 * @param strategyId
	 * @param companyId
	 * @return
	 */
	Strategy findByIdAndCompanyIdAndStatus(long strategyId, long companyId, String status);

	/**
	 * 
	 * @param strategyId
	 * @param companyId
	 * @return
	 */
	Strategy findByIdAndCompanyId(long strategyId, long companyId);

	@Query("select s from Strategy s where s.companyId in (Select c.id from Company c where c.code = :code) and s.submitted = 1 and submitDate is not null")
	List<Strategy> findSubmittedStrategiesByCompanyCode(@Param("code") String code);

	/**
	 * This method will update the strategy to submitted status and will populate
	 * submitted, submitDate, type and defaultSubmit data.
	 * 
	 * @param strategyId
	 * @param isDefaultSubmit
	 * @param submitDate
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	@Modifying
	@Query("UPDATE Strategy s SET s.submitted = 1, s.submitDate = :submitDate, s.defaultSubmit = :isDefaultSubmit where s.id = :strategyId")
	int updateToSubmitted(@Param("strategyId") long strategyId, @Param("isDefaultSubmit") boolean isDefaultSubmit,
			@Param("submitDate") Date submitDate);

	@Query("select s from Strategy s where s.companyId in (Select c.id from Company c where c.code = :code)")
	List<Strategy> findBy(@Param("code") String code);
	
	/**
	 * This method updates the stratey's name in XBSS_STRATEGY
	 * 
	 * @param strategyId
	 * @param strategyName
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	@Modifying
	@Query("UPDATE Strategy s SET s.name = :strategyName WHERE s.id = :strategyId")
	int updateStrategyName(@Param("strategyId") long strategyId, @Param("strategyName") String strategyName);
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	@Modifying
	@Query("UPDATE Strategy s SET s.totalBudget = :totalBudget, s.budgetFactor = :budgetFactor WHERE s.id = :strategyId")
	int updateStrategyBudget(@Param("strategyId") long strategyId, @Param("totalBudget") BigDecimal totalBudget,
			@Param("budgetFactor") int budgetFactor);
	
	Strategy findById(long id);
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	@Modifying
	@Query("UPDATE Strategy s SET s.status = :status WHERE s.id IN (:strategyIds)")
	int updateStrategiesStatus(@Param("strategyIds") List<Long> strategyIds, @Param("status") String status);
	

}
