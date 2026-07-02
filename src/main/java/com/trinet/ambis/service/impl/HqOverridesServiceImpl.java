package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.ExceptionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.HQExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.HqOverrideDao;
import com.trinet.ambis.persistence.model.HQException;
import com.trinet.ambis.persistence.model.HQExceptionsId;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.HqOverridesService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.CompanyHQData;
import com.trinet.ambis.service.model.HqOverridesDto;
import com.trinet.ambis.util.CommonUtils;

@Service
public class HqOverridesServiceImpl implements HqOverridesService {

	@Autowired
	HQExceptionDao hqExceptionDao;
	
	@Autowired
	HqOverrideDao hqOverrideDao;
	
	@Autowired
	RealmPlanYearService realmPlanYearService;

	@Override
	public List<HqOverridesDto> getHqOverridesDetails(String companyCode, String quarter) {
		return setNextYearPlan(hqOverrideDao.getHqOverridesDetails(companyCode, quarter));
	}

	private List<HqOverridesDto> setNextYearPlan(List<HqOverridesDto> hqOverridesDtos) {
		Set<Long> realmIds = hqOverridesDtos.stream().map(HqOverridesDto::getNextRealmYearId)
				.collect(Collectors.toSet());
		List<RealmPlanYear> realmPlanYears = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(realmIds)) {
			realmPlanYears = realmPlanYearService.getRealmPlanYearByIds(realmIds);
		}
		Map<Long, RealmPlanYear> realmPlanYearIdMap = realmPlanYears.stream()
				.collect(Collectors.toMap(RealmPlanYear::getId, Function.identity()));

		for (HqOverridesDto hqOverridesDto : hqOverridesDtos) {
			if (hqOverridesDto.getNextRealmYearId() != null && hqOverridesDto.getNextRealmYearId() != 0
					&& realmPlanYearIdMap.get(hqOverridesDto.getNextRealmYearId()) != null) {
				hqOverridesDto.setNextYearPlanYearEnd(setDateValue(realmPlanYearIdMap.get(hqOverridesDto.getNextRealmYearId()).getPlanYearEnd()));
				hqOverridesDto.setNextYearPlanYearStart(
						setDateValue(realmPlanYearIdMap.get(hqOverridesDto.getNextRealmYearId()).getPlanYearStart()));
			}

		}

		return hqOverridesDtos;
	}

	private String setDateValue(Date date) {
		return (date == null) ? ""
				: CommonUtils.formatDateToString(date, BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY);

	}

	@Override
	public HqOverridesDto createHqOverridesDetails(HqOverridesDto hqOverridesDto) {
		ExceptionServiceHelper.validateHQOverridesRequestData(hqOverridesDto);
		HQException hqException = new HQException();
		HQExceptionsId hqExceptionsId = new HQExceptionsId();
		hqExceptionsId.setCompany(hqOverridesDto.getCompanyCode());
		hqExceptionsId.setRealmYrId(hqOverridesDto.getRealmYearId());
		hqException.setId(hqExceptionsId);
		hqException.setHqState(hqOverridesDto.getOverrideHqState());
		hqException.setPostalCode(hqOverridesDto.getOverrideHqZip());
		hqExceptionDao.save(hqException);
		return hqOverridesDto;
	}

	@Override
	public void deleteHqOverride(String companyCode, Integer realmYearId) {
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(companyCode);
		id.setRealmYrId(realmYearId);
		HQException hqException = new HQException();
		hqException.setId(id);
		hqExceptionDao.delete(hqException);
	}

	@Override
	public List<CompanyHQData> getCompanyPlanYearData(String code) {
		return hqOverrideDao.getHqPlanYearData(code);
	}

}
