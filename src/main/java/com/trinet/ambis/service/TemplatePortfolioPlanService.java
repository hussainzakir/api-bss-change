package com.trinet.ambis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.template.model.XbssTemplate;
import com.trinet.ambis.persistence.template.model.XbssTemplatePortfolioPlan;

@Service
public interface TemplatePortfolioPlanService {

    List<XbssTemplatePortfolioPlan> findByXbssTemplate(XbssTemplate xbssTemplate);

}
