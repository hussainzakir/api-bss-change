package com.trinet.ambis.persistence.dao.ps;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BenefitPlanDataDao {

	Map<String, List<String>> getMedicalAutoSelectedPlansByRegion(Set<String> plans, long relamYearId);
}
