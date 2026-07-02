package com.trinet.ambis.service;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.template.model.XbssTemplate;

@Service
public interface TemplateService {

    public XbssTemplate saveTemplate(XbssTemplate xbssTemplate);

//    public XbssTemplate findByIndTypeAndStateAndPkgType(String IndType, String State, String PkgType);

}
