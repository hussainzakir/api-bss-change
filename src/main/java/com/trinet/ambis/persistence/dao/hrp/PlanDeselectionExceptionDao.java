package com.trinet.ambis.persistence.dao.hrp;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trinet.ambis.persistence.model.PlanDeselectionExceptions;

@Repository
public interface PlanDeselectionExceptionDao extends JpaRepository<PlanDeselectionExceptions, Long> {

	public Set<PlanDeselectionExceptions> findByActive(boolean active);

	public Set<PlanDeselectionExceptions> findByActiveAndCompanyCodeAndQuarter(boolean active, String companyCode,
			String quarter);

	PlanDeselectionExceptions findById(long id);
}
