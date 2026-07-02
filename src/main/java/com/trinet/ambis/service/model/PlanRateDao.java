package com.trinet.ambis.service.model;

import java.util.Date;
import java.util.Map;

public interface PlanRateDao {

	Map<String, String> getPortfolios(Date effectiveDate);
	
}
