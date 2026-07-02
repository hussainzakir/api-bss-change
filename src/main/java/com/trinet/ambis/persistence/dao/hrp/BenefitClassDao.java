package com.trinet.ambis.persistence.dao.hrp;

import java.util.Map;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;

public interface BenefitClassDao {

	String getEligClass( Company company, BenefitGroup group );
	Map<String,String> getBenProgramBenClassMappings( Company company );
}
