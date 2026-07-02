package com.trinet.ambis.service;

import java.util.Set;

import com.trinet.ambis.rest.controllers.dto.PlanDeselectionExceptionResDto;

public interface PlanDeselectionExceptionService {

	Set<PlanDeselectionExceptionResDto> findAllActive();

	PlanDeselectionExceptionResDto create(PlanDeselectionExceptionResDto dto);

	PlanDeselectionExceptionResDto update(PlanDeselectionExceptionResDto dto);

	PlanDeselectionExceptionResDto findById(long id);

}
