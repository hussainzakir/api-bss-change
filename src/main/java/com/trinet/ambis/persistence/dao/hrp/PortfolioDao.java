package com.trinet.ambis.persistence.dao.hrp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.template.model.XbssPortfolio;

@Repository
@Transactional(readOnly = true)
public interface PortfolioDao extends JpaRepository<XbssPortfolio, Long> {

}
