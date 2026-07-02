package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.ExceptionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.MinFundExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.MinimumFundingException;
import com.trinet.ambis.service.ExceptionAttributeService;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.model.ExceptionAttributeDto;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.service.model.ProductQuarters;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.Utils;

/**
 * @author schaudhari
 *
 */
@Service
public class MinFundExceptionServiceImpl implements MinFundExceptionService {

	@Autowired
	MinFundExceptionDao minFundExceptionDao;

	@Autowired
	PsCompanyDao psCompanyDao;

	@Autowired
	PsDao psDao;
	
	@Autowired
	RealmDataDao realmDataDao;

	@Autowired
	ExceptionAttributeService exceptionAttributeService;

	@Override
	public MinFundExceptionDto findBy(long id) {
		MinFundExceptionDto result = null;
		MinimumFundingException minFundException = minFundExceptionDao.findById(id);
		Set<MinimumFundingException> minFundExceptions = new HashSet<>(Arrays.asList(minFundException));
		result = setAdditionalInfo(minFundExceptions).get(0);
		return result;
	}

	@Override
	public List<MinFundExceptionDto> findAllActive() {
		List<MinFundExceptionDto> result = null;
		Set<MinimumFundingException> minFundExceptions = minFundExceptionDao.findByActive(true);
		result = setAdditionalInfo(minFundExceptions);
		return result;
	}

	@Override
	@Transactional
	public MinFundExceptionDto update(MinFundExceptionDto dto) {
		validateRequest(dto);
		validateMinFundExceptionData(dto);

		MinimumFundingException existingEntity = minFundExceptionDao.findById(dto.getId());
		MinimumFundingException newEntity = CommonUtils.createNewObjectUsing(existingEntity,
				MinimumFundingException.class);

		newEntity.setId(0);
		newEntity.setCreateTime(new Date());
		newEntity.setLastUpdatedById(BSSSecurityUtils.getAuthenticatedPersonId());
		newEntity.setApproverId(dto.getApproverId());
		newEntity.setStartDate(dto.getStartDate());
		newEntity.setEndDate(dto.getEndDate());
		newEntity.setPlanType(dto.getPlanType());
		newEntity.setMinFundType(dto.getMinFundType());
		newEntity.setMinFundValue(dto.getMinFundValue());

		minFundExceptionDao.save(newEntity);
		existingEntity.setActive(false);
		return findBy(newEntity.getId());
	}

	@Override
	public MinFundExceptionDto save(MinFundExceptionDto dto) {
		Company company = psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode());

		dto.setQuarter(company.getQuater());
		dto.setRealmId(company.getRealm().getId());

		validateRequest(dto);
		validateMinFundExceptionData(dto);

		MinimumFundingException entity = CommonUtils.createNewObjectUsing(dto, MinimumFundingException.class);

		entity.setId(0);
		entity.setActive(true);
		entity.setCreatedById(BSSSecurityUtils.getAuthenticatedPersonId());
		entity.setCreateTime(new Date());
		entity.setLastUpdatedById(null);

		minFundExceptionDao.save(entity);
		return findBy(entity.getId());
	}

	@Override
	public Set<MinFundExceptionDto> findActiveByCompanyCodeAndQuarter(Company company) {
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		Set<MinimumFundingException> entities = minFundExceptionDao.findActiveBy(company.getCode(),
				company.getQuater(), planStartDate);
		Set<MinFundExceptionDto> dtos = new HashSet<>(entities.size());
		for (MinimumFundingException exception : entities) {
			dtos.add(CommonUtils.createNewObjectUsing(exception, MinFundExceptionDto.class));
		}
		return dtos;
	}

	private void validateRequest(MinFundExceptionDto dto) {
		validateMinFundValue(dto);
		Set<MinimumFundingException> existingEntities = minFundExceptionDao.findActiveBy(dto.getCompanyCode(),
				dto.getQuarter(), dto.getRealmId(), dto.getPlanType());
		ExceptionServiceHelper.validateStartAndEndDts(dto, existingEntities);
	}

	private List<MinFundExceptionDto> setAdditionalInfo(Set<MinimumFundingException> minFundExceptions) {
		List<MinFundExceptionDto> minFundExceptionDtos = new ArrayList<>(minFundExceptions.size());
		if (CollectionUtils.isNotEmpty(minFundExceptions)) {
			Map<String, String> emplNames = getEmployeeNames(minFundExceptions);
			Map<String, String> compNames = getCompaniesNames(minFundExceptions);

			for (MinimumFundingException minFundException : minFundExceptions) {
				MinFundExceptionDto minFundExceptionDto = CommonUtils.createNewObjectUsing(minFundException,
						MinFundExceptionDto.class);
				minFundExceptionDto.setApproverName(emplNames.get(minFundExceptionDto.getApproverId()));
				minFundExceptionDto.setCreatedByName(emplNames.get(minFundExceptionDto.getCreatedById()));
				minFundExceptionDto.setLastUpdatedByName(emplNames.get(minFundExceptionDto.getLastUpdatedById()));
				minFundExceptionDto.setCompanyName(compNames.get(minFundExceptionDto.getCompanyCode()));
				minFundExceptionDto.setExchange(BenExchngEnums.getById(minFundException.getRealmId()).getBenExchng());
				minFundExceptionDtos.add(minFundExceptionDto);
			}
		}
		return minFundExceptionDtos;
	}

	private Map<String, String> getEmployeeNames(Set<MinimumFundingException> minFundExceptions) {
		Set<String> emplIds = new HashSet<>();
		for (MinimumFundingException minimumFundingException : minFundExceptions) {
			emplIds.add(minimumFundingException.getApproverId());
			emplIds.add(minimumFundingException.getCreatedById());
			if (null != minimumFundingException.getLastUpdatedById()) {
				emplIds.add(minimumFundingException.getLastUpdatedById());
			}
		}

		return psDao.getEmployeesFullName(emplIds);
	}

	private Map<String, String> getCompaniesNames(Set<MinimumFundingException> minFundExceptions) {
		Set<String> companyCodes = new HashSet<>();
		for (MinimumFundingException minimumFundingException : minFundExceptions) {
			companyCodes.add(minimumFundingException.getCompanyCode().toUpperCase());
		}

		return psCompanyDao.findCompaniesNames(companyCodes);
	}

	private void validateMinFundValue(MinFundExceptionDto dto) {
		if ("NONE".equals(dto.getMinFundType())) {
			dto.setMinFundValue(BigDecimal.ZERO);
		}
	}

	private void validateMinFundExceptionData(MinFundExceptionDto dto)  {
		List<String> products = new ArrayList<>();
		List<ExceptionAttributeDto> exceptionAttributes = exceptionAttributeService.findAllExceptionAttributes();
		
		List<ProductQuarters> productQuarters = new ArrayList<>();
		Map<String, ProductQuarters> productQuartersMap = realmDataDao.getAllProductQuarters();
		productQuarters.addAll(productQuartersMap.values());

		for (ProductQuarters productQuarter : productQuarters) {
			products.addAll(productQuarter.getQuarters());
		}

		ExceptionServiceHelper.validateRequestData(dto, exceptionAttributes, products);
    }
}
