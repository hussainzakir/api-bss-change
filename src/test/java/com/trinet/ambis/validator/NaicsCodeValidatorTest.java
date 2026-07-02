package com.trinet.ambis.validator;

import com.trinet.ambis.persistence.dao.hrp.NaicsBandCodeRepository;
import com.trinet.ambis.persistence.model.NaicsBandCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for NaicsCodeValidator
 *
 * @author echavarria
 */
@RunWith(MockitoJUnitRunner.class)
public class NaicsCodeValidatorTest {

    @Mock
    private NaicsBandCodeRepository naicsBandCodeRepository;

    @InjectMocks
    private NaicsCodeValidator validator;

    private NaicsBandCode mockNaicsBandCode;

    @Before
    public void setUp() {
        mockNaicsBandCode = NaicsBandCode.builder().build();
    }

    @Test
    public void testIsValid_ValidCodeExistsInDatabase() {
        when(naicsBandCodeRepository.findActiveByNaicsCodeAndDate(eq("541330"), any(Date.class)))
                .thenReturn(Optional.of(mockNaicsBandCode));
        assertTrue(validator.isValid("541330"));
    }

    @Test
    public void testIsValid_ValidCodeNotInDatabase() {
        when(naicsBandCodeRepository.findActiveByNaicsCodeAndDate(eq("999999"), any(Date.class)))
                .thenReturn(Optional.empty());
        assertFalse(validator.isValid("999999"));
    }

    @Test
    public void testIsValid_EmptyString() {
        assertFalse(validator.isValid(""));
    }

    @Test
    public void testIsValid_Null() {
        assertFalse(validator.isValid(null));
    }

    @Test
    public void testIsValid_Whitespace() {
        assertFalse(validator.isValid("   "));
    }

    @Test
    public void testValidate_ValidCodeInDatabase_NoException() {
        when(naicsBandCodeRepository.findActiveByNaicsCodeAndDate(eq("541330"), any(Date.class)))
                .thenReturn(Optional.of(mockNaicsBandCode));
        // Should not throw exception
        validator.validate("541330");
    }

    @Test
    public void testValidate_CodeNotInDatabase_ThrowsException() {
        when(naicsBandCodeRepository.findActiveByNaicsCodeAndDate(eq("999999"), any(Date.class)))
                .thenReturn(Optional.empty());
        try {
            validator.validate("999999");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid NAICS code"));
            assertTrue(e.getMessage().contains("XBSS_NAICS_BAND_CODE"));
        }
    }

    @Test
    public void testValidate_NullCode_ThrowsException() {
        try {
            validator.validate(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid NAICS code"));
        }
    }

    @Test
    public void testValidate_EmptyCode_ThrowsException() {
        try {
            validator.validate("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid NAICS code"));
        }
    }
}

