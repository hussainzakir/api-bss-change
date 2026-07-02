package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.TemplatePortfolioPlanDao;
import com.trinet.ambis.persistence.template.model.XbssTemplate;
import com.trinet.ambis.persistence.template.model.XbssTemplatePortfolioPlan;
import com.trinet.ambis.service.TemplatePortfolioPlanService;

@Service
public class TemplatePortfolioPlanServiceImpl implements TemplatePortfolioPlanService {

    @Autowired
    TemplatePortfolioPlanDao templatePortfolioPlanDao;

    @Override
    public List<XbssTemplatePortfolioPlan> findByXbssTemplate(XbssTemplate xbssTemplate) {
        return templatePortfolioPlanDao.findByXbssTemplate(xbssTemplate);
    }

	/**
	 * @param templatePortfolioPlanDao the templatePortfolioPlanDao to set
	 */
	public void setTemplatePortfolioPlanDao(TemplatePortfolioPlanDao templatePortfolioPlanDao) {
		this.templatePortfolioPlanDao = templatePortfolioPlanDao;
	}
}
