package com.trinet.ambis.service.impl;

import com.trinet.ambis.helper.ExceptionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.LifeDisabilityBandOverrideDao;
import com.trinet.ambis.persistence.model.LifeDisabilityBandOverride;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.LifeDisabilityBandOverrideService;
import com.trinet.ambis.service.dto.LifeDisabilityBandOverrideDto;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LifeDisabilityBandOverrideServiceImpl implements LifeDisabilityBandOverrideService {

    private final LifeDisabilityBandOverrideDao lifeDisabilityBandOverrideDao;
    private final CompanyService companyService;

    @Transactional
    @Override
    public void createOverride(LifeDisabilityBandOverrideDto dto) {
        validateCompanyCode(dto.getCompanyCode());
        validateStartAndEndDates(dto);
        lifeDisabilityBandOverrideDao.save(mapDtoToEntity(dto));
    }

    private void validateCompanyCode(String companyCode) {
        if (companyService.getLatestCompany(companyCode) == null) {
            ExceptionServiceHelper.throwException(String.format("Invalid company code: %s", companyCode), "BSS_INVALID_COMPANY_CODE");
        }
    }

    private LifeDisabilityBandOverride mapDtoToEntity(LifeDisabilityBandOverrideDto dto) {
        LifeDisabilityBandOverride entity = CommonUtils.createNewObjectUsing(dto, LifeDisabilityBandOverride.class);
        entity.setCreatedBy(BSSSecurityUtils.getAuthenticatedPersonId());
        entity.setCreateTime(new Date());
        entity.setLastUpdatedBy(null);
        entity.setActive(true);
        return entity;
    }


    private void validateStartAndEndDates(LifeDisabilityBandOverrideDto dto) {
        Set<LifeDisabilityBandOverride> existingEntities = lifeDisabilityBandOverrideDao.findByCompanyCodeAndActive(dto.getCompanyCode(), true);
        ExceptionServiceHelper.validateStartAndEndDts(dto, existingEntities);
    }

}
