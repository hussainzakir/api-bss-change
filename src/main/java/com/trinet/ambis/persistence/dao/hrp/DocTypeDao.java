package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.DocumentType;

@Repository
@Transactional(readOnly = true)
public interface DocTypeDao  extends JpaRepository<DocumentType, Long>{

	List<DocumentType> findByDocTypeName(String docTypeName);
}
