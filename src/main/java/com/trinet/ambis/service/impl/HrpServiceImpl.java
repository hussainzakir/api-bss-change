package com.trinet.ambis.service.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.service.HrpService;
import com.trinet.security.util.SecurityUtils;

@Service
public class HrpServiceImpl implements HrpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(HrpServiceImpl.class);
	
	@Autowired
	private HrpDao hrpDao;
	
	@Override
	public boolean hasAccessToGatewayApp(String appKey) {
		Set<String> appRoles = hrpDao.getGatewayAppAccessibleRolesBy(appKey);
		List<String> userRoles = SecurityUtils.getAuthorizedUserRoles();
		LOGGER.info("Gateway appId : {} appRoles : {}", appKey, appRoles);
		LOGGER.info("UserRoles : ", userRoles);
		if (CollectionUtils.isNotEmpty(appRoles) && CollectionUtils.isNotEmpty(userRoles))
			for (String userRole : userRoles) {
				if (appRoles.contains(userRole)) {
					return true;
				}
			}
		return false;
	}

}
