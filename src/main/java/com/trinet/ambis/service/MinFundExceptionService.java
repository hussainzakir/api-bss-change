package com.trinet.ambis.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.MinFundExceptionDto;

@Service
public interface MinFundExceptionService {

	public static final String FLAT = "FLT";
	public static final String PERCENT = "PCT";

	MinFundExceptionDto findBy(long id);

	List<MinFundExceptionDto> findAllActive();

	MinFundExceptionDto update(MinFundExceptionDto object);

	MinFundExceptionDto save(MinFundExceptionDto object);

	Set<MinFundExceptionDto> findActiveByCompanyCodeAndQuarter(Company company);
}
