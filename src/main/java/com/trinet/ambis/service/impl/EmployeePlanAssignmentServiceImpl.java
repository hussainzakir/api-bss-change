package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.util.CommonUtils;

import jakarta.transaction.Transactional;

@Service
public class EmployeePlanAssignmentServiceImpl implements EmployeePlanAssignmentService {

	@Autowired
	private EePlanAssignmentDao eePlanAssignmentDao;

	@Autowired
	private EePlanAssignmentDataDao eePlanAssignmentDataDao;

	@Override
	public List<EePlanAssignment> getEmployeePlanAssigmentBy(List<Long> strategyIds) {
		return eePlanAssignmentDao.findByEePlanAssignmentPKStrategyIdIn(strategyIds);
	}

	@Override
	public void deleteEmployeePlanAssignment(List<String> employeeIds, String companyCode) {
		Optional.ofNullable(employeeIds).filter(
				list -> list.stream().anyMatch(employeeId -> employeeId != null && !employeeId.trim().isEmpty()))
				.orElseThrow(() -> new IllegalArgumentException(
						"Employee IDs list cannot be null or contain only blank/empty values"));
		Collection<List<String>> bucketedList = CommonUtils.getBucketedList(new ArrayList<>(employeeIds),
				BSSApplicationConstants.QUERY_IN_CLAUSE_PARTITION_SIZE);
		for (List<String> bucket : bucketedList) {
			eePlanAssignmentDao.deleteEePlanAssignment(bucket, companyCode);
		}
	}

	@Override
	public void deleteEmployeePlanAssignmentForBenTypes(Set<String> employeeIds, Long strategyId, Set<String> benTypes) {
		eePlanAssignmentDao.deleteEePlanAssignmentBy(employeeIds, strategyId, benTypes);
	}

	@Override
	public void copyEePlanAssignmentsFor(long sourceStrategyId, long targetStrategyId) {
		eePlanAssignmentDao.copyEePlanAssignmentsFor(sourceStrategyId, targetStrategyId);
	}

	@Override
	@Transactional
	public void updateEePlanAssignmentCvgCode(Set<String> emplIds) {
		eePlanAssignmentDao.updateEePlanAssignmentCvgCode(emplIds);
	}

	@Override
	public void copyEePlanAssignmentsFor(long sourceStrategyId, long targetStrategyId, String benType) {
		eePlanAssignmentDao.copyEePlanAssignmentsFor(sourceStrategyId, targetStrategyId, benType);
	}

	@Override
	public void saveEePlanAssignments(List<EePlanAssignment> eePlanAssignments) {
		eePlanAssignmentDataDao.saveEmployeePlanAssignments(eePlanAssignments);
	}
}
