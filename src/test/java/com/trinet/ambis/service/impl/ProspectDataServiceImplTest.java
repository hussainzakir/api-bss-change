package com.trinet.ambis.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.ProspectDataUpdateRequest;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProspectStrategySyncService;
import com.trinet.ambis.validator.NaicsCodeValidator;

/**
 * Unit tests for ProspectDataServiceImpl
 *
 * @author echavarria
 */
@RunWith(MockitoJUnitRunner.class)
public class ProspectDataServiceImplTest {

    @InjectMocks
    private ProspectDataServiceImpl prospectDataService;

    @Mock
    private CompanyDao companyDao;

    @Mock
    private CompanyService companyService;

    @Mock
    private ProspectStrategySyncService prospectStrategySyncService;

    @Mock
    private CacheService cacheService;

    @Mock
    private NaicsCodeValidator naicsCodeValidator;

    private Company company;
    private Company company2;
    private static final String COMPANY_CODE = "TEST123";
    private static final String NAICS_CODE = "541330";

    @Before
    public void setUp() {
        company = new Company();
        company.setId(1L);
        company.setCode(COMPANY_CODE);
        company.setRealmPlanYearId(70L);
        company.setBssNaicsCode(null);

        company2 = new Company();
        company2.setId(2L);
        company2.setCode(COMPANY_CODE);
        company2.setRealmPlanYearId(74L);
        company2.setBssNaicsCode(null);
    }

    @Test
    public void testUpdateProspectData_NoCompaniesFound_ReturnsWithoutError() {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(true)
            .naicsCodeUpdate(false)
            .build();

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then
        verify(prospectStrategySyncService, times(1)).strategySyncOnHQLocationChange(COMPANY_CODE);
        verify(naicsCodeValidator, never()).validate(any());
        verify(cacheService, never()).invalidateStrategyDataCache(any());
    }

    @Test
    public void testUpdateProspectData_LocationUpdateTrue_CallsStrategySyncService() {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(true)
            .naicsCodeUpdate(false)
            .build();

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then
        verify(prospectStrategySyncService, times(1)).strategySyncOnHQLocationChange(COMPANY_CODE);
        verify(naicsCodeValidator, never()).validate(any());
        verify(companyDao, never()).saveAndFlush(any());
        verify(cacheService, never()).invalidateStrategyDataCache(any());
    }

    @Test
    public void testUpdateProspectData_NaicsCodeUpdateTrue_SingleCompany_ValidCode_UpdatesAndInvalidatesCache() {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(true)
            .naicsCode(NAICS_CODE)
            .build();

        when(companyService.getXbssCompaniesByCode(COMPANY_CODE)).thenReturn(List.of(company));

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then
        verify(naicsCodeValidator, times(1)).validate(NAICS_CODE);
        verify(companyDao, times(1)).saveAndFlush(company);
        verify(cacheService, times(1)).invalidateStrategyDataCache(company);
        verify(prospectStrategySyncService, never()).strategySyncOnHQLocationChange(any());
    }

    @Test
    public void testUpdateProspectData_NaicsCodeUpdateTrue_SingleCompany_SameCode_DoesNotUpdate() {
        // Given
        company.setBssNaicsCode(541330);

        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(true)
            .naicsCode(NAICS_CODE)
            .build();

        when(companyService.getXbssCompaniesByCode(COMPANY_CODE)).thenReturn(List.of(company));

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then
        verify(naicsCodeValidator, times(1)).validate(NAICS_CODE);
        verify(companyDao, never()).saveAndFlush(any());
        verify(cacheService, never()).invalidateStrategyDataCache(any());
    }

    @Test
    public void testUpdateProspectData_NaicsCodeUpdateTrue_SingleCompany_DifferentCode_UpdatesAndInvalidatesCache() {
        // Given
        company.setBssNaicsCode(123456);

        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(true)
            .naicsCode(NAICS_CODE)
            .build();

        when(companyService.getXbssCompaniesByCode(COMPANY_CODE)).thenReturn(List.of(company));

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then
        verify(naicsCodeValidator, times(1)).validate(NAICS_CODE);
        verify(companyDao, times(1)).saveAndFlush(company);
        verify(cacheService, times(1)).invalidateStrategyDataCache(company);
    }

    @Test
    public void testUpdateProspectData_NaicsCodeUpdateTrue_MultipleCompanies_AllUpdated() {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(true)
            .naicsCode(NAICS_CODE)
            .build();

        when(companyService.getXbssCompaniesByCode(COMPANY_CODE))
            .thenReturn(List.of(company, company2));

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then - Both companies should be updated
        verify(naicsCodeValidator, times(1)).validate(NAICS_CODE);
        verify(companyDao, times(2)).saveAndFlush(any(Company.class));
        verify(cacheService, times(2)).invalidateStrategyDataCache(any(Company.class));
        verify(companyService, times(1)).getXbssCompaniesByCode(COMPANY_CODE);
    }

    @Test
    public void testUpdateProspectData_NaicsCodeUpdateTrue_MultipleCompanies_MixedUpdates() {
        // Given - company1 has different NAICS, company2 has same NAICS
        company.setBssNaicsCode(123456);
        company2.setBssNaicsCode(541330); // Same as new code

        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(true)
            .naicsCode(NAICS_CODE)
            .build();

        when(companyService.getXbssCompaniesByCode(COMPANY_CODE))
            .thenReturn(List.of(company, company2));

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then - Only company1 should be updated
        verify(naicsCodeValidator, times(1)).validate(NAICS_CODE);
        verify(companyDao, times(1)).saveAndFlush(company);
        verify(companyDao, never()).saveAndFlush(company2);
        verify(cacheService, times(1)).invalidateStrategyDataCache(company);
        verify(cacheService, never()).invalidateStrategyDataCache(company2);
    }

    @Test
    public void testUpdateProspectData_BothUpdatesTrue_CallsBothServices() {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(true)
            .naicsCodeUpdate(true)
            .naicsCode(NAICS_CODE)
            .build();

        when(companyService.getXbssCompaniesByCode(COMPANY_CODE)).thenReturn(List.of(company));

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then
        verify(prospectStrategySyncService, times(1)).strategySyncOnHQLocationChange(COMPANY_CODE);
        verify(naicsCodeValidator, times(1)).validate(NAICS_CODE);
        verify(companyDao, times(1)).saveAndFlush(company);
        verify(cacheService, times(1)).invalidateStrategyDataCache(company);
    }

    @Test
    public void testUpdateProspectData_BothUpdatesFalse_DoesNothing() {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(false)
            .build();

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then
        verify(prospectStrategySyncService, never()).strategySyncOnHQLocationChange(any());
        verify(naicsCodeValidator, never()).validate(any());
        verify(companyDao, never()).saveAndFlush(any());
        verify(cacheService, never()).invalidateStrategyDataCache(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateProspectData_InvalidNaicsCode_ThrowsException() {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(true)
            .naicsCode("INVALID")
            .build();

        doThrow(new IllegalArgumentException("Invalid NAICS code")).when(naicsCodeValidator).validate(any());

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then - exception is thrown
    }

    @Test
    public void testUpdateProspectData_NaicsCodeUpdateTrue_MultipleCompanies_WithNullValue() {
        // Given - company1 has null NAICS, company2 has a value
        company.setBssNaicsCode(null);
        company2.setBssNaicsCode(999999);

        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(true)
            .naicsCode(NAICS_CODE)
            .build();

        when(companyService.getXbssCompaniesByCode(COMPANY_CODE))
            .thenReturn(List.of(company, company2));

        // When
        prospectDataService.updateProspectData(COMPANY_CODE, request);

        // Then - Both companies should be updated (null comparison check)
        verify(naicsCodeValidator, times(1)).validate(NAICS_CODE);
        verify(companyDao, times(2)).saveAndFlush(any(Company.class));
        verify(cacheService, times(2)).invalidateStrategyDataCache(any(Company.class));
    }
}

