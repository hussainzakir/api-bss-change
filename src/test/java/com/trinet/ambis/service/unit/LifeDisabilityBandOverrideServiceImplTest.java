package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.ExceptionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.LifeDisabilityBandOverrideDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.LifeDisabilityBandOverride;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.dto.LifeDisabilityBandOverrideDto;
import com.trinet.ambis.service.impl.LifeDisabilityBandOverrideServiceImpl;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.security.util.SecurityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LifeDisabilityBandOverrideServiceImplTest {

    @Mock
    private LifeDisabilityBandOverrideDao lifeDisabilityBandOverrideDao;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private LifeDisabilityBandOverrideServiceImpl service;

    private LifeDisabilityBandOverrideDto dto;
    private Company company;

    @Before
    public void setUp() {
        dto = new LifeDisabilityBandOverrideDto();
        dto.setCompanyCode("G48");
        dto.setApproverId("00002340287");
        dto.setStartDate(new Date(System.currentTimeMillis()));
        dto.setEndDate(new Date(System.currentTimeMillis() + 86400000));
        dto.setLifeBand("L1");
        dto.setDisBand("D1");

        Realm realm = new Realm();
        realm.setId(3L);

        company = new Company();
        company.setCode("G48");
        company.setQuater("Q1");
        company.setRealm(realm);
    }

    @Test
    public void createOverride_ValidRequest_SavesEntity() {
        LifeDisabilityBandOverride mapped = new LifeDisabilityBandOverride();
        when(companyService.getLatestCompany(dto.getCompanyCode())).thenReturn(company);
        when(lifeDisabilityBandOverrideDao.findByCompanyCodeAndActive(dto.getCompanyCode(), true))
                .thenReturn(Collections.emptySet());

        try (MockedStatic<SecurityUtils> securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
             MockedStatic<BSSSecurityUtils> bssMock = Mockito.mockStatic(BSSSecurityUtils.class);
             MockedStatic<CommonUtils> commonMock = Mockito.mockStatic(CommonUtils.class)) {

            securityUtilsMock.when(() -> SecurityUtils.isValidCompany(dto.getCompanyCode())).thenReturn(true);
            bssMock.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn("00011122233");
            commonMock.when(() -> CommonUtils.createNewObjectUsing(any(LifeDisabilityBandOverrideDto.class),
                    eq(LifeDisabilityBandOverride.class))).thenReturn(mapped);

            service.createOverride(dto);

            verify(companyService).getLatestCompany(dto.getCompanyCode());
            verify(lifeDisabilityBandOverrideDao).findByCompanyCodeAndActive(dto.getCompanyCode(), true);
            verify(lifeDisabilityBandOverrideDao).save(mapped);
            assertTrue(mapped.isActive());
            assertNotNull(mapped.getCreateTime());
            assertNull(mapped.getLastUpdatedBy());
        }
    }

    @Test(expected = BSSApplicationException.class)
    public void createOverride_InvalidCompanyCode_ThrowsException() {
        try (MockedStatic<ExceptionServiceHelper> exceptionHelperMock = Mockito.mockStatic(ExceptionServiceHelper.class)) {
            when(companyService.getLatestCompany(dto.getCompanyCode())).thenReturn(null);
            exceptionHelperMock.when(() -> ExceptionServiceHelper.throwException(any(), any()))
                    .thenThrow(new BSSApplicationException());
            service.createOverride(dto);
        }
    }

    @Test(expected = BSSApplicationException.class)
    public void createOverride_OverlappingDates_ThrowsException() {
        LifeDisabilityBandOverride existing = new LifeDisabilityBandOverride();
        existing.setId(99L);
        existing.setStartDate(new Date(System.currentTimeMillis() - 86400000));
        existing.setEndDate(new Date(System.currentTimeMillis() + 86400000 * 2));
        Set<LifeDisabilityBandOverride> existingEntities = new HashSet<>();
        existingEntities.add(existing);

        when(companyService.getLatestCompany(dto.getCompanyCode())).thenReturn(company);
        when(lifeDisabilityBandOverrideDao.findByCompanyCodeAndActive(dto.getCompanyCode(), true))
                .thenReturn(existingEntities);

        try (MockedStatic<ExceptionServiceHelper> exceptionHelperMock = Mockito.mockStatic(ExceptionServiceHelper.class)) {
            exceptionHelperMock.when(() -> ExceptionServiceHelper.validateStartAndEndDts(any(), any()))
                    .thenThrow(new BSSApplicationException());
            service.createOverride(dto);
        }
    }
}