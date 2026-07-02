package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.helper.ExceptionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenOfferExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.BenefitOfferException;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ExceptionAttributeService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.model.BenOfferExceptionDto;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.ExceptionAttributeDto;
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
public class BenefitOfferExceptionServiceImpl implements BenefitOfferExceptionService {

	@Autowired
	PsCompanyDao psCompanyDao;

	@Autowired
	PsDao psDao;

	@Autowired
	BenOfferExceptionDao benOfferExceptionDao;

	@Autowired
	CompanyService companyService;

	@Autowired
	StrategyService strategyService;

	@Autowired
	RealmDataDao realmDataDao;

	@Autowired
	ExceptionAttributeService exceptionAttributeService;
	
	@Override
	public Map<String, Boolean> findApplicableBy(Company company) {
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		Set<BenefitOfferException> benOfferExceptions = benOfferExceptionDao.findApplicableBy(company.getCode(),
				company.getQuater(), company.getRealm().getId(), planStartDate);
		Map<String, Boolean> result = new HashMap<>();
		for (BenefitOfferException benOfferException : benOfferExceptions) {
			if (PlanTypesEnum.DISABILITY.getCode().equals(benOfferException.getPlanType())) {
				result.put(PlanTypesEnum.STD.getCode(), !benOfferException.isOffered());
				result.put(PlanTypesEnum.LTD.getCode(), !benOfferException.isOffered());
			} else {
				result.put(benOfferException.getPlanType(), !benOfferException.isOffered());
			}
		}
		return result;
	}

	@Override
	public List<BenOfferExceptionDto> findAllActive() {
		List<BenOfferExceptionDto> result = null;
		Set<BenefitOfferException> benOfferExceptions = benOfferExceptionDao.findByActive(true);
		result = setAdditionalInfo(benOfferExceptions);
		return result;
	}

	@Override
	@Transactional
	public BenOfferExceptionDto save(BenOfferExceptionDto dto) {
		Company company = psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode());

		dto.setQuarter(company.getQuater());
		dto.setRealmId(company.getRealm().getId());

		validateStartAndEndDts(dto);
		validateBenOfferExceptionData(dto);

		BenefitOfferException entity = CommonUtils.createNewObjectUsing(dto, BenefitOfferException.class);

		entity.setId(0);
		entity.setActive(true);
		entity.setCreatedById(BSSSecurityUtils.getAuthenticatedPersonId());
		entity.setCreateTime(new Date());
		entity.setLastUpdatedById(null);

		benOfferExceptionDao.save(entity);
		applyExceptionToExistingStrategies(dto);
		dto = findBy(entity.getId());
		return dto;
	}

	@Override
	@Transactional
	public BenOfferExceptionDto update(BenOfferExceptionDto dto) {
		validateStartAndEndDts(dto);
		validateBenOfferExceptionData(dto);
		BenefitOfferException existingEntity = benOfferExceptionDao.findById(dto.getId());
		BenefitOfferException newEntity = CommonUtils.createNewObjectUsing(existingEntity, BenefitOfferException.class);

		newEntity.setId(0);
		newEntity.setCreateTime(new Date());
		newEntity.setLastUpdatedById(BSSSecurityUtils.getAuthenticatedPersonId());
		newEntity.setApproverId(dto.getApproverId());
		newEntity.setStartDate(dto.getStartDate());
		newEntity.setEndDate(dto.getEndDate());
		newEntity.setPlanType(dto.getPlanType());
		newEntity.setOriginDept(dto.getOriginDept());
		newEntity.setOffered(dto.isOffered());
		benOfferExceptionDao.save(newEntity);
		applyExceptionToExistingStrategies(dto);
		existingEntity.setActive(false);
		return findBy(newEntity.getId());
	}

	@Override
	public BenOfferExceptionDto findBy(long id) {
		BenOfferExceptionDto result = null;
		BenefitOfferException benOfferException = benOfferExceptionDao.findById(id);
		Set<BenefitOfferException> benOfferExceptions = new HashSet<>(Arrays.asList(benOfferException));
		result = setAdditionalInfo(benOfferExceptions).get(0);
		return result;
	}

	@Override
	public void applyException(Company company, Map<String, ?> benOffers) {
		Map<String, Boolean> benOfferExceptions = findApplicableBy(company);
		benOfferExceptions.forEach((offerTypeCode, exception) -> {
			if (exception) {
				removeOfferTypeFromBenOffers(benOffers, offerTypeCode);
			}
		});
	}

	@Override
	public void applyException(Company company, List<BenefitOffer> benOffers) {
		Map<String, Boolean> benOfferExceptions = findApplicableBy(company);
		benOfferExceptions.forEach((offerTypeCode, exception) -> {
			if (exception) {
				removeOfferTypeFromBenOffers(benOffers, offerTypeCode);
			}
		});
	}

	private void removeOfferTypeFromBenOffers(Map<String, ?> data, String offerTypeCode) {
		PlanTypesEnum planTypeEnum = PlanTypesEnum.planType(offerTypeCode);
		switch (planTypeEnum) {
		case STD:
		case LTD:
			// Key can be code or name so remove using both.
			data.remove(PlanTypesEnum.STD.getCode());
			data.remove(PlanTypesEnum.LTD.getCode());
			data.remove(PlanTypesEnum.STD.getName());
			break;
		case DENTAL:
		case DENTAL_VOLUNTARY:
			data.remove(PlanTypesEnum.DENTAL.getCode());
			data.remove(PlanTypesEnum.DENTAL_VOLUNTARY.getCode());
			data.remove(PlanTypesEnum.DENTAL.getName());
			data.remove(PlanTypesEnum.DENTAL_VOLUNTARY.getName());
			break;
		case VISION:
		case VISION_VOLUNTARY:
			data.remove(PlanTypesEnum.VISION.getCode());
			data.remove(PlanTypesEnum.VISION_VOLUNTARY.getCode());
			data.remove(PlanTypesEnum.VISION.getName());
			data.remove(PlanTypesEnum.VISION_VOLUNTARY.getName());
			break;
		default:
			data.remove(offerTypeCode);
			data.remove(PlanTypesEnum.getName(offerTypeCode));
		}
	}

	private void removeOfferTypeFromBenOffers(List<BenefitOffer> benOffers, String offerTypeCode) {
		Predicate<BenefitOffer> isExceptionPresent = benOffer -> (offerTypeCode.equals(benOffer.getSummary().getType())
				|| PlanTypesEnum.getName(offerTypeCode).equals(benOffer.getSummary().getType()));
		benOffers.removeIf(isExceptionPresent);
	}

	private List<BenOfferExceptionDto> setAdditionalInfo(Set<BenefitOfferException> benOfferExceptions) {
		List<BenOfferExceptionDto> benOfferExceptionDtos = new ArrayList<>(benOfferExceptions.size());
		if (CollectionUtils.isNotEmpty(benOfferExceptions)) {
			Map<String, String> emplNames = getEmployeeNames(benOfferExceptions);
			Map<String, String> compNames = getCompaniesNames(benOfferExceptions);

			for (BenefitOfferException benOfferException : benOfferExceptions) {
				BenOfferExceptionDto benOfferExceptionDto = CommonUtils.createNewObjectUsing(benOfferException,
						BenOfferExceptionDto.class);
				benOfferExceptionDto.setApproverName(emplNames.get(benOfferExceptionDto.getApproverId()));
				benOfferExceptionDto.setCreatedByName(emplNames.get(benOfferExceptionDto.getCreatedById()));
				benOfferExceptionDto.setLastUpdatedByName(emplNames.get(benOfferExceptionDto.getLastUpdatedById()));
				benOfferExceptionDto.setCompanyName(compNames.get(benOfferExceptionDto.getCompanyCode()));
				benOfferExceptionDto.setOriginationDeptName(benOfferExceptionDto.getOriginDept());
				benOfferExceptionDto.setExchange(BenExchngEnums.getById(benOfferException.getRealmId()).getBenExchng());
				benOfferExceptionDtos.add(benOfferExceptionDto);
			}
		}
		return benOfferExceptionDtos;
	}

	private Map<String, String> getEmployeeNames(Set<BenefitOfferException> benOfferExceptions) {
		Set<String> emplIds = new HashSet<>();
		for (BenefitOfferException benOfferException : benOfferExceptions) {
			emplIds.add(benOfferException.getApproverId());
			emplIds.add(benOfferException.getCreatedById());
			if (null != benOfferException.getLastUpdatedById()) {
				emplIds.add(benOfferException.getLastUpdatedById());
			}
		}

		return psDao.getEmployeesFullName(emplIds);
	}

	private Map<String, String> getCompaniesNames(Set<BenefitOfferException> benOfferExceptions) {
		Set<String> companyCodes = new HashSet<>();
		for (BenefitOfferException benOfferException : benOfferExceptions) {
			companyCodes.add(benOfferException.getCompanyCode().toUpperCase());
		}
		return psCompanyDao.findCompaniesNames(companyCodes);
	}

	private void validateStartAndEndDts(BenOfferExceptionDto dto) {
		Set<BenefitOfferException> existingEntities = benOfferExceptionDao.findActiveBy(dto.getCompanyCode(),
				dto.getQuarter(), dto.getRealmId(), dto.getPlanType());
		ExceptionServiceHelper.validateStartAndEndDts(dto, existingEntities);
	}

	private void applyExceptionToExistingStrategies(final BenOfferExceptionDto dto) {
		if (!dto.isOffered()) {
			Set<String> planTypeCodes = new HashSet<>();
			if (PlanTypesEnum.DISABILITY.getCode().equals(dto.getPlanType())) {
				planTypeCodes.add(PlanTypesEnum.STD.getCode());
				planTypeCodes.add(PlanTypesEnum.LTD.getCode());
			} else if (PlanTypesEnum.DENTAL.getCode().equals(dto.getPlanType())) {
				planTypeCodes.add(PlanTypesEnum.DENTAL.getCode());
				planTypeCodes.add(PlanTypesEnum.DENTAL_VOLUNTARY.getCode());
			} else if (PlanTypesEnum.VISION.getCode().equals(dto.getPlanType())) {
				planTypeCodes.add(PlanTypesEnum.VISION.getCode());
				planTypeCodes.add(PlanTypesEnum.VISION_VOLUNTARY.getCode());
			} else {
				planTypeCodes.add(dto.getPlanType());
			}
			strategyService.syncStrategiesForBenOfferException(dto, planTypeCodes);
		}
	}
	
	private void validateBenOfferExceptionData(BenOfferExceptionDto dto)  {
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