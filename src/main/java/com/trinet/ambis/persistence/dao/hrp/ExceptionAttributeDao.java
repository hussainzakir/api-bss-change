package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.ExceptionTypeAttribute;


@Repository
@Transactional(readOnly = true)
public interface ExceptionAttributeDao extends JpaRepository<ExceptionTypeAttribute, Long> {
	
	/**
	 * This method returns all ExceptionAttributes for given companyCode
	 * 
	 * @return
	 */
	
	List<ExceptionTypeAttribute> findAll();


}
