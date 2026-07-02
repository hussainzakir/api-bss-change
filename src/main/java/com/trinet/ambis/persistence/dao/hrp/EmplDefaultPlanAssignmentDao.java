package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trinet.ambis.persistence.model.EmplDefaultPlanAssignment;
import com.trinet.ambis.persistence.model.EmplDefaultPlanAssignmentId;

/**
 * @author schaudhari
 *
 */
@Repository
public interface EmplDefaultPlanAssignmentDao
		extends JpaRepository<EmplDefaultPlanAssignment, EmplDefaultPlanAssignmentId> {

	/**
	 * This method returns the all EmplDefaultPlanAssignment for given companyId and
	 * portfolioId
	 * 
	 * @param companyId
	 * @param portfolioId
	 * @return List<EmplDefaultPlanAssignment>
	 */
	@Query("SELECT edpa FROM EmplDefaultPlanAssignment edpa WHERE edpa.emplDefaultPlanAssignmentId.companyId = :companyId"
			+ " AND (edpa.emplDefaultPlanAssignmentId.portfolioId = :portfolioId AND edpa.emplDefaultPlanAssignmentId.planType = '10'"
			+ " OR edpa.emplDefaultPlanAssignmentId.planType != '10') ")
	List<EmplDefaultPlanAssignment> findBy(@Param("companyId") long companyId, @Param("portfolioId") int portfolioId);

	@Modifying
	@Query(value = "delete from EmplDefaultPlanAssignment edpa where edpa.emplDefaultPlanAssignmentId.companyId in "
			+ "(select cmny.id from Company cmny where cmny.code = :companyCode) and  edpa.emplDefaultPlanAssignmentId.emplId in (:employeeIds)")
	void deleteEmplDefaultPlanAssignment(@Param("employeeIds") List<String> employeeIds,
			@Param("companyCode") String companyCode);

	@Modifying
	@Query(value = "delete from EmplDefaultPlanAssignment edpa where edpa.emplDefaultPlanAssignmentId.emplId in (:employeeIds)")
	void deleteEmplDefaultPlanAssignment(@Param("employeeIds") Set<String> employeeIds);
	
	/**
	 * This method delete employee default plan assignments for given companyId
	 * 
	 * @param companyId
	 * @return
	 */
	@Modifying
	@Query("delete from EmplDefaultPlanAssignment edpa WHERE edpa.emplDefaultPlanAssignmentId.companyId = :companyId")
	void deleteEmplDefaultPlanAssignment(@Param("companyId") long companyId);

}