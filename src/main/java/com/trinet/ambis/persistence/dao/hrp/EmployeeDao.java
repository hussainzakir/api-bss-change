package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.Employee;

/**
 * @author mpulipaka
 */
@Repository
@Transactional(readOnly = true)
public interface EmployeeDao extends JpaRepository<Employee, Long> {

	public List<Employee> findByCompanyAndRealmYearId(String companyCode, long realmYearId);

}
