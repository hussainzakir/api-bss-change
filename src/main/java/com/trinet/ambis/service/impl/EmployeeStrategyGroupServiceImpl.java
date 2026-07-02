package com.trinet.ambis.service.impl;

import java.util.*;
import org.springframework.stereotype.Service;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupDao;
import com.trinet.ambis.service.EmployeeStrategyGroupService;
import com.trinet.ambis.util.CommonUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeStrategyGroupServiceImpl implements EmployeeStrategyGroupService {

	private final EmployeeStrategyGroupDao employeeStrategyGroupDao;

	@Override
	public void deleteEmployeeStrategyGroups(List<String> employeeIds, String companyCode) {
		Optional.ofNullable(employeeIds).filter(
				list -> list.stream().anyMatch(employeeId -> employeeId != null && !employeeId.trim().isEmpty()))
				.orElseThrow(() -> new IllegalArgumentException(
						"Employee IDs list cannot be null or contain only blank/empty values"));
		Collection<List<String>> bucketedList = CommonUtils.getBucketedList(new ArrayList<>(employeeIds),
				BSSApplicationConstants.QUERY_IN_CLAUSE_PARTITION_SIZE);
		for (List<String> bucket : bucketedList) {
			employeeStrategyGroupDao.deleteEmployeeStrategyGroups(bucket, companyCode);
		}
	}
	
	@Override
	public void updateEmployeesToDefaultStrategyGroup(List<Long> k1StrategyGroupIds, Long defaultStrategyGroupId) {
		employeeStrategyGroupDao.updateEmployeesToDefaultStrategyGroup(k1StrategyGroupIds, defaultStrategyGroupId);
	}
}
