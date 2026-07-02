package com.trinet.ambis.persistence.dao.hrp;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.EmployeeStrategyGroupTransaction;


@Repository
@Transactional
public interface EmployeeStrategyGroupTransactionDao extends JpaRepository<EmployeeStrategyGroupTransaction, Long> {

	/**
	 * Deletes transactions from the table by company and YearId.  This is needed when activating
	 * a mid-year funding change.
	 * 
	 * @param company
	 * @param realmYearId
	 */
	@Modifying
	@Query("Delete from EmployeeStrategyGroupTransaction e "
		+  " Where e.strategyGroupId in ( "
		+  "       Select sg.id "
		+  "         from Company c "
		+  "            , Strategy s "
		+  "            , BenefitGroupStrategy sg "
		+  "        Where c.code = ?1 "
		+  "          and c.realmPlanYearId = ?2 "
		+  "          and s.companyId = c.id "
		+  "          and sg.strategyId = s.id )")
	void deleteByCompanyAndYear(String company, long realmYearId);

	/**
	 * Deletes transactions from the table.  This is needed when the associated STRATEGY is being deleted.
	 * 
	 * @param strategyIds a Set of strategy IDs to remove
	 */
	@Modifying
	@Query("Delete from EmployeeStrategyGroupTransaction e "
		+  " Where e.strategyGroupId in ( "
		+  "       Select sg.id "
		+  "         from Strategy s "
		+  "            , BenefitGroupStrategy sg "
		+  "        Where s.id in ( ?1 ) "
		+  "          and sg.strategyId = s.id )")
	void deleteByStrategyIds(Set<Long> strategyIds);
}
