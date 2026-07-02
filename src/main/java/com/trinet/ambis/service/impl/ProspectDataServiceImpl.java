package com.trinet.ambis.service.impl;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.ProspectDataUpdateRequest;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProspectDataService;
import com.trinet.ambis.service.ProspectStrategySyncService;
import com.trinet.ambis.validator.NaicsCodeValidator;

/**
 * Implementation of ProspectDataService
 *
 * @author echavarria
 */
@Service
@Transactional
public class ProspectDataServiceImpl implements ProspectDataService {

    private static final Logger logger = LoggerFactory.getLogger(ProspectDataServiceImpl.class);

    @Autowired
    private CompanyDao companyDao;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ProspectStrategySyncService prospectStrategySyncService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private NaicsCodeValidator naicsCodeValidator;

    @Override
    public void updateProspectData(String companyCode, ProspectDataUpdateRequest request) {
        logger.info("Processing prospect data update for company: {}, locationUpdate: {}, naicsCodeUpdate: {}",
            companyCode, request.isLocationUpdate(), request.isNaicsCodeUpdate());

        // Handle location update
        if (request.isLocationUpdate()) {
            logger.info("Processing location update for company: {}", companyCode);
            prospectStrategySyncService.strategySyncOnHQLocationChange(companyCode);
        }

        // Handle NAICS code update
        if (request.isNaicsCodeUpdate()) {
            handleNaicsCodeUpdate(companyCode, request.getNaicsCode());
        }
    }

    /**
     * Handles NAICS code update for all company records across realm years
     */
    private void handleNaicsCodeUpdate(String companyCode, String newNaicsCode) {
        logger.info("Processing NAICS code update for company: {}, new NAICS: {}",
            companyCode, newNaicsCode);

        // Validate NAICS code format 2-6 digits, numeric
        naicsCodeValidator.validate(newNaicsCode);

        Integer newNaicsCodeInt = Integer.valueOf(newNaicsCode);

        // Fetch all BSS company records for this company code across all realm years
        List<Company> companies = companyService.getXbssCompaniesByCode(companyCode);

        if (companies == null || companies.isEmpty()) {
            logger.info("No BSS companies found for company code: {}", companyCode);
            return;
        }

        // Update each company record if NAICS code differs
        for (Company company : companies) {
            if (!Objects.equals(company.getBssNaicsCode(), newNaicsCodeInt)) {
                logger.info("NAICS code changed for company: {} (realm year: {}). Old: {}, New: {}",
                    company.getCode(), company.getRealmPlanYearId(), company.getBssNaicsCode(), newNaicsCodeInt);

                // Update NAICS code
                company.setBssNaicsCode(newNaicsCodeInt);
                companyDao.saveAndFlush(company);

                logger.info("NAICS code updated in database for company: {} (realm year: {})",
                    company.getCode(), company.getRealmPlanYearId());

                // Invalidate strategy cache
                cacheService.invalidateStrategyDataCache(company);

                logger.info("Strategy data cache invalidated for company: {} (realm year: {})",
                    company.getCode(), company.getRealmPlanYearId());
            } else {
                logger.info("NAICS code unchanged for company: {} (realm year: {}). No update needed.",
                    company.getCode(), company.getRealmPlanYearId());
            }
        }
    }
}

