package com.trinet.ambis.persistence.dao.hrp;

import com.trinet.ambis.persistence.model.EePlanAssignment;

import java.util.List;

public interface EePlanAssignmentDataDao {

	void saveEmployeePlanAssignments(List<EePlanAssignment> eePlanAssignments);

}