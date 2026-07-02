package com.trinet.ambis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.TemplateDao;
import com.trinet.ambis.persistence.template.model.XbssTemplate;
import com.trinet.ambis.service.TemplateService;

@Service
public class TemplateServiceImpl implements TemplateService {

	@Autowired
	TemplateDao templateDao;

	public XbssTemplate saveTemplate(XbssTemplate xbssTemplate) {
		return templateDao.saveAndFlush(xbssTemplate);
	}

	/**
	 * @param templateDao the templateDao to set
	 */
	public void setTemplateDao(TemplateDao templateDao) {
		this.templateDao = templateDao;
	}
}
