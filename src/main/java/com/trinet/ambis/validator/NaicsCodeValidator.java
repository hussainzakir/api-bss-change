package com.trinet.ambis.validator;

import com.trinet.ambis.persistence.dao.hrp.NaicsBandCodeRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Validator for NAICS codes
 *
 * @author echavarria
 */
@Component
public class NaicsCodeValidator {

    private static final Logger logger = LoggerFactory.getLogger(NaicsCodeValidator.class);

    @Autowired
    private NaicsBandCodeRepository naicsBandCodeRepository;

    /**
     * Validates NAICS code by checking if it exists in the XBSS_NAICS_BAND_CODE table
     *
     * @param naicsCode the NAICS code to validate
     * @return true if valid (exists in the table), false otherwise
     */
    public boolean isValid(String naicsCode) {
        if (StringUtils.isBlank(naicsCode)) {
            return false;
        }

        // Check if the NAICS code exists in the database for the current date
        boolean exists = naicsBandCodeRepository.findActiveByNaicsCodeAndDate(naicsCode, new Date()).isPresent();

        if (!exists) {
            logger.error("NAICS code '{}' not found in XBSS_NAICS_BAND_CODE table. Please verify if this code should be added.", naicsCode);
        }

        return exists;
    }

    /**
     * Validates and throws exception if invalid
     *
     * @param naicsCode the NAICS code to validate
     * @throws IllegalArgumentException if NAICS code is invalid
     */
    public void validate(String naicsCode) {
        if (!isValid(naicsCode)) {
            throw new IllegalArgumentException(
                "Invalid NAICS code. Code not found in XBSS_NAICS_BAND_CODE table: " + naicsCode);
        }
    }
}

