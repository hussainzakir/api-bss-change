package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.template.model.XbssTemplate;
import com.trinet.ambis.persistence.template.model.XbssTemplatePortfolioPlan;

@Repository
@Transactional(readOnly = true)
public interface TemplatePortfolioPlanDao extends JpaRepository<XbssTemplatePortfolioPlan, Long> {

    List<XbssTemplatePortfolioPlan> findByXbssTemplate(XbssTemplate xbssTemplate);
}
