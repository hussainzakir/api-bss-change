package com.trinet.ambis.persistence.dao.hrp;

import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.EmplDefaultPlanAssignment;
import com.trinet.ambis.service.dto.EmplDefaultPlanAssignmentDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmplDefaultPlanAssignmentDataDao {

	void saveEmplDefaultPlanAssignmentData(List<EmplDefaultPlanAssignment> emplDefaultPlanAssignments);

}
