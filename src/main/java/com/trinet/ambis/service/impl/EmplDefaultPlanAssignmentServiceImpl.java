package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.trinet.ambis.persistence.dao.hrp.EmplDefaultPlanAssignmentDataDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.EmplDefaultPlanAssignmentDao;
import com.trinet.ambis.persistence.model.EmplDefaultPlanAssignment;
import com.trinet.ambis.persistence.model.EmplDefaultPlanAssignmentId;
import com.trinet.ambis.service.EmplDefaultPlanAssignmentService;
import com.trinet.ambis.service.dto.EmplDefaultPlanAssignmentDto;
import com.trinet.ambis.util.CommonUtils;

import lombok.AllArgsConstructor;

/**
 * @author schaudhari
 *
 */
@Service
@AllArgsConstructor
public class EmplDefaultPlanAssignmentServiceImpl implements EmplDefaultPlanAssignmentService {

	private EmplDefaultPlanAssignmentDao emplDefaultPlanAssignmentDao;

	private EmplDefaultPlanAssignmentDataDao emplDefaultPlanAssignmentDataDao;

	@Override
	@Transactional
	public Map<String, Map<String, String>> findAllBy(long companyId, int potfolioId) {
		List<EmplDefaultPlanAssignment> results = emplDefaultPlanAssignmentDao.findBy(companyId, potfolioId);
		return results.stream()
				.collect(Collectors.groupingBy(result -> result.getEmplDefaultPlanAssignmentId().getEmplId(),
						Collectors.toMap(result -> result.getEmplDefaultPlanAssignmentId().getPlanType(),
								result -> result.getEmplDefaultPlanAssignmentId().getBenefitPlanId())));
	}

	@Override
	@Transactional
	public void saveAll(List<EmplDefaultPlanAssignmentDto> dtos) {
		List<EmplDefaultPlanAssignment> entities = dtos.stream()
				.map(dto -> EmplDefaultPlanAssignment.builder()
						.emplDefaultPlanAssignmentId(EmplDefaultPlanAssignmentId.builder().companyId(dto.getCompanyId())
								.emplId(dto.getEmplId()).portfolioId(dto.getPortfolioId()).planType(dto.getPlanType())
								.benefitPlanId(dto.getBenefitPlanId()).coverageCode(dto.getCoverageCode()).build())
						.build())
				.collect(Collectors.toList());

		emplDefaultPlanAssignmentDataDao.saveEmplDefaultPlanAssignmentData(entities);
	}

	@Override
	public void deleteEmplDefaultPlanAssignment(List<String> employeeIds, String companyCode) {
		Optional.ofNullable(employeeIds).filter(
				list -> list.stream().anyMatch(employeeId -> employeeId != null && !employeeId.trim().isEmpty()))
				.orElseThrow(() -> new IllegalArgumentException(
						"Employee IDs list cannot be null or contain only blank/empty values"));

		Collection<List<String>> bucketedList = CommonUtils.getBucketedList(new ArrayList<>(employeeIds),
				BSSApplicationConstants.QUERY_IN_CLAUSE_PARTITION_SIZE);
		for (List<String> bucket : bucketedList) {
			emplDefaultPlanAssignmentDao.deleteEmplDefaultPlanAssignment(bucket, companyCode);
		}
	}

	@Override
	@Transactional
	public void deleteEmplDefaultPlanAssignments(Set<String> employeeIds) {
		emplDefaultPlanAssignmentDao.deleteEmplDefaultPlanAssignment(employeeIds);
		emplDefaultPlanAssignmentDao.flush();
	}
	
	@Override
	@Transactional
	public void deleteEmplDefaultPlanAssignments(long companyId) {
		emplDefaultPlanAssignmentDao.deleteEmplDefaultPlanAssignment(companyId);
		emplDefaultPlanAssignmentDao.flush();
	}
}
