package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.service.PsConfigurationService;

@Service
public class PsConfigurationServiceImpl implements PsConfigurationService {

	@Autowired
	PsDao psDao;

	@Override
	public Map<String, String> findByEffDate(Date effDate) {
		Map<String, String> configsMap = new HashMap<>();

		Map<String, BigDecimal> hsaMaximumMap = psDao.getHsaMaximumsByEffDate(effDate);
		for (Map.Entry<String, BigDecimal> entry : hsaMaximumMap.entrySet()) {
			configsMap.put(entry.getKey(), entry.getValue().toString());
		}

		return configsMap;
	}
}