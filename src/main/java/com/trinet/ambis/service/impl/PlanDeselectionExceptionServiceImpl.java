package com.trinet.ambis.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.ExceptionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.PlanDeselectionExceptionDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanDeselectionExceptions;
import com.trinet.ambis.rest.controllers.dto.PlanDeselectionExceptionResDto;
import com.trinet.ambis.service.PlanDeselectionExceptionService;
import com.trinet.ambis.util.BSSSecurityUtils;

@Service
public class PlanDeselectionExceptionServiceImpl implements PlanDeselectionExceptionService {

	@Autowired
	PlanDeselectionExceptionDao planDeselectionExceptionDao;

	@Autowired
	PsDao psDao;

	@Autowired
	PsCompanyDao psCompanyDao;

	@Override
	public Set<PlanDeselectionExceptionResDto> findAllActive() {
		Set<PlanDeselectionExceptions> planDeselectionExceptions = planDeselectionExceptionDao.findByActive(true);
		return mapToDtos(planDeselectionExceptions);
	}

	@Override
	public PlanDeselectionExceptionResDto findById(long id) {
		return mapToDto(planDeselectionExceptionDao.findById(id));
	}

	@Override
	public PlanDeselectionExceptionResDto create(PlanDeselectionExceptionResDto planDeselectionExceptionResDto) {
		Company company = psCompanyDao.getBasicCompanyDetails(planDeselectionExceptionResDto.getCompanyCode());
		if (company.getRealm().getId() != BenExchngEnums.TRINET_II.getId()) {
			ExceptionServiceHelper.throwException(
					String.format("Exceptions are allowed only for %s exchange companies.",
							BenExchngEnums.TRINET_II.getBenExchng()),
					BSSErrorResponseCodes.BSS_PLAN_DESELECTION_EXCEPTION_ERROR);
		}
		planDeselectionExceptionResDto.setQuarter(company.getQuater());
		validateRequest(planDeselectionExceptionResDto);
		PlanDeselectionExceptions planDeselectionException = mapToEntity(planDeselectionExceptionResDto);
		return mapToDto(planDeselectionExceptionDao.save(planDeselectionException));
	}

	@Override
	@Transactional
	public PlanDeselectionExceptionResDto update(PlanDeselectionExceptionResDto planDeselectionExceptionResDto) {
		PlanDeselectionExceptions existingPlanDeselectionExceptions = planDeselectionExceptionDao
				.findById(planDeselectionExceptionResDto.getId());
		planDeselectionExceptionResDto.setQuarter(existingPlanDeselectionExceptions.getQuarter());
		validateRequest(planDeselectionExceptionResDto);
		PlanDeselectionExceptions planDeselectionException = mapToEntity(planDeselectionExceptionResDto);
		PlanDeselectionExceptions updatedPlanDeselectionException = planDeselectionExceptionDao
				.save(planDeselectionException);
		existingPlanDeselectionExceptions.setActive(false);
		return mapToDto(updatedPlanDeselectionException);
	}

	private Set<PlanDeselectionExceptionResDto> mapToDtos(Set<PlanDeselectionExceptions> planDeselectionExceptions) {
		if (CollectionUtils.isNotEmpty(planDeselectionExceptions)) {
			Map<String, String> employeeFullNames = getEmployeeFullNames(planDeselectionExceptions);
			Map<String, String> companyNames = getCompanyNames(planDeselectionExceptions);
			return planDeselectionExceptions.stream().map(
					planDeselectionException -> mapToDto(planDeselectionException, employeeFullNames, companyNames))
					.collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	private PlanDeselectionExceptionResDto mapToDto(PlanDeselectionExceptions planDeselectionException) {
		Map<String, String> employeeFullNames = getEmployeeFullNames(Set.of(planDeselectionException));
		Map<String, String> companyNames = getCompanyNames(Set.of(planDeselectionException));
		return mapToDto(planDeselectionException, employeeFullNames, companyNames);
	}

	private Map<String, String> getEmployeeFullNames(Set<PlanDeselectionExceptions> planDeselectionExceptions) {
		return psDao.getEmployeesFullName(planDeselectionExceptions.stream().flatMap(planDeselectionException -> Stream
				.of(planDeselectionException.getApproverId(), planDeselectionException.getCreatedById()))
				.collect(Collectors.toSet()));
	}

	private Map<String, String> getCompanyNames(Set<PlanDeselectionExceptions> planDeselectionExceptions) {
		return psCompanyDao.findCompaniesNames(planDeselectionExceptions.stream()
				.map(planDeselectionException -> planDeselectionException.getCompanyCode().toUpperCase())
				.collect(Collectors.toSet()));
	}

	private PlanDeselectionExceptionResDto mapToDto(PlanDeselectionExceptions planDeselectionException,
			Map<String, String> employeeFullNames, Map<String, String> companyNames) {
		PlanDeselectionExceptionResDto planDeselectionExceptionResDto = new PlanDeselectionExceptionResDto();
		planDeselectionExceptionResDto.setId(planDeselectionException.getId());
		planDeselectionExceptionResDto.setCompanyCode(planDeselectionException.getCompanyCode());
		planDeselectionExceptionResDto.setCompanyName(companyNames.get(planDeselectionException.getCompanyCode()));
		planDeselectionExceptionResDto.setStartDate(planDeselectionException.getStartDate());
		planDeselectionExceptionResDto.setEndDate(planDeselectionException.getEndDate());
		planDeselectionExceptionResDto.setApproverId(planDeselectionException.getApproverId());
		planDeselectionExceptionResDto.setApproverName(employeeFullNames.get(planDeselectionException.getApproverId()));
		planDeselectionExceptionResDto.setCreateTime(planDeselectionException.getCreateTime());
		BenExchngEnums exchange = BenExchngEnums.getByQuarter(planDeselectionException.getQuarter());
		planDeselectionExceptionResDto.setExchange(exchange.getBenExchng());
		planDeselectionExceptionResDto.setRealmId(exchange.getId());
		planDeselectionExceptionResDto.setQuarter(planDeselectionException.getQuarter());
		return planDeselectionExceptionResDto;
	}

	private void validateRequest(PlanDeselectionExceptionResDto planDeselectionExceptionResDto) {
		Set<PlanDeselectionExceptions> existingEntities = planDeselectionExceptionDao
				.findByActiveAndCompanyCodeAndQuarter(true, planDeselectionExceptionResDto.getCompanyCode(),
						planDeselectionExceptionResDto.getQuarter());
		ExceptionServiceHelper.validateStartAndEndDts(planDeselectionExceptionResDto, existingEntities);
	}

	private PlanDeselectionExceptions mapToEntity(PlanDeselectionExceptionResDto planDeselectionExceptionResDto) {
		PlanDeselectionExceptions planDeselectionExceptions = new PlanDeselectionExceptions();
		planDeselectionExceptions.setCompanyCode(planDeselectionExceptionResDto.getCompanyCode());
		planDeselectionExceptions.setQuarter(planDeselectionExceptionResDto.getQuarter());
		planDeselectionExceptions.setStartDate(planDeselectionExceptionResDto.getStartDate());
		planDeselectionExceptions.setEndDate(planDeselectionExceptionResDto.getEndDate());
		planDeselectionExceptions.setActive(true);
		planDeselectionExceptions.setApproverId(planDeselectionExceptionResDto.getApproverId());
		planDeselectionExceptions.setCreateTime(new Date());
		planDeselectionExceptions.setCreatedById(BSSSecurityUtils.getAuthenticatedPersonId());
		return planDeselectionExceptions;
	}

}
