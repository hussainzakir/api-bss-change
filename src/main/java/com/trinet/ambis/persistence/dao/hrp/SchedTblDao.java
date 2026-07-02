package com.trinet.ambis.persistence.dao.hrp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.persistence.model.SchedTblId;

@Repository
@Transactional(readOnly = true)
public interface SchedTblDao extends JpaRepository<SchedTbl, SchedTblId> {

	@Query("Select s from SchedTbl s  where s.sched.company = ?1 and s.sched.realmYearId = ?2")
	public SchedTbl getSecheduleDates(String companyCode, Long realmYearId);

}