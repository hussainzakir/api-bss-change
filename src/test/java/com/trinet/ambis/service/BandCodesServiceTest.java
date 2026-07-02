package com.trinet.ambis.service;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

import java.util.*;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.NaicsBandCodeRepository;
import com.trinet.ambis.persistence.model.NaicsBandCode;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.BandCodesRepository;
import com.trinet.ambis.persistence.model.BandCodes;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.CarrierBand;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.CarrierBand.CarrierBandDetails;
import com.trinet.ambis.service.impl.BandCodesServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.CommonUtils;

@RunWith(MockitoJUnitRunner.class)
public class BandCodesServiceTest extends ServiceUnitTest {

	@InjectMocks
	private BandCodesServiceImpl bandCodesService;

	@Mock
	private BandCodesRepository bandCodesRepository;

	@Captor
	ArgumentCaptor<List<BandCodes>> bandCodesCaptor;

    @Mock
    private NaicsBandCodeRepository naicsBandCodeRepository;

    private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMock;

    @Before
    public void setUp() {
        appRulesAndConfigsUtilsMock = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        appRulesAndConfigsUtilsMock.close();
    }

	/**
	 * given exchange bands and company id</br>
	 * when saveAll repository called </br>
	 * then save exchange bands</br>
	 **/
	@Test
	public void saveTest() {
		// given
		// data
		long companyId = 123;
		CarrierBand carrierBand = buildCarrierBandData();
		// method mocks
		when(bandCodesRepository.saveAll(bandCodesCaptor.capture())).thenReturn(bandCodesCaptor.capture());
		// when
		bandCodesService.save(carrierBand, companyId);
		// then
		// assertions
		List<BandCodes> actualResult = bandCodesCaptor.getValue();
		assertNotNull(actualResult);
		assertEquals(2, actualResult.size());
		BandCodes bandCodes1 = actualResult.get(0);
		assertEquals("AETNA", bandCodes1.getBandCodesUK().getBandCodeType());
		assertEquals(123, bandCodes1.getBandCodesUK().getCompanyId());
		assertEquals("2023-04-01",
				CommonUtils.formatDateToString(bandCodes1.getBandCodesUK().getEffectiveDt(), "yyyy-MM-dd"));
		assertEquals("20A", bandCodes1.getBandCodeVal());
		BandCodes bandCodes2 = actualResult.get(1);
		assertEquals("KAISER", bandCodes2.getBandCodesUK().getBandCodeType());
		assertEquals(123, bandCodes2.getBandCodesUK().getCompanyId());
		assertEquals("2023-04-01",
				CommonUtils.formatDateToString(bandCodes1.getBandCodesUK().getEffectiveDt(), "yyyy-MM-dd"));
		assertEquals("12A", bandCodes2.getBandCodeVal());
		// verify
		verify(bandCodesRepository, times(1)).saveAll(bandCodesCaptor.getValue());
	}

	/**
	 * given list of company id</br>
	 * when delete is called </br>
	 * then repository method is called</br>
	 **/
	@Test
	public void deleteCompanyBandsTest() {
		// given
		// data
		List<Long> companyIds = Arrays.asList( 101L, 102L );

		// when
		bandCodesService.deleteCompanyBands(companyIds);

		// then
		// assertions
		verify(bandCodesRepository, times(1)).deleteWhereCompanyIdIn(companyIds);
	}

    /**
     * GIVEN a NAICS code, effective date, band code type "LIFE", and exchange TNIV
     * WHEN getBandCodeByType is called
     * THEN it should return the LIFE band code
     */
    @Test
    public void testGetBandCodeByType_Life() {
        // GIVEN
        String naicsCode = "1234";
        Date effectiveDate = new Date();
        String bandCodeType = "LIFE";
        BenExchngEnums exchange = BenExchngEnums.TRINET_IV;
        Map<String, String> bandCodeMap = new HashMap<>();
        bandCodeMap.put(BSSApplicationConstants.LIFE, "4");
        bandCodeMap.put(BSSApplicationConstants.DISABILITY, "1");
        when(AppRulesAndConfigsUtils.getTNIVLifeAndDisabilityBandCode()).thenReturn(bandCodeMap);
        // WHEN
        String result = bandCodesService.getBandCodeByType(naicsCode, effectiveDate, bandCodeType, exchange);

        // THEN
        assertEquals("4", result); // TNIV returns "4" for LIFE

    }

    /**
     * GIVEN a NAICS code, effective date, band code type "DIS", and exchange TNIV
     * WHEN getBandCodeByType is called
     * THEN it should return the DIS band code
     */
    @Test
    public void testGetBandCodeByType_LifeTNIV() {
        // GIVEN
        String naicsCode = "1234";
        Date effectiveDate = new Date();
        String bandCodeType = "DISABILITY";
        BenExchngEnums exchange = BenExchngEnums.TRINET_IV;
        Map<String, String> bandCodeMap = new HashMap<>();
        bandCodeMap.put(BSSApplicationConstants.LIFE, "4");
        bandCodeMap.put(BSSApplicationConstants.DISABILITY, "1");
        when(AppRulesAndConfigsUtils.getTNIVLifeAndDisabilityBandCode()).thenReturn(bandCodeMap);
        // WHEN
        String result = bandCodesService.getBandCodeByType(naicsCode, effectiveDate, bandCodeType, exchange);

        // THEN
        assertEquals("1", result); // TNIV returns "1" for DIS
    }

    /**
     * GIVEN a NAICS code, effective date, band code type "DIS", and exchange TNIII
     * WHEN getBandCodeByType is called
     * THEN it should return the DIS band code
     */
    @Test
    public void testGetBandCodeByType_Dis() {
        // GIVEN
        String naicsCode = "5678";
        Date effectiveDate = new Date();
        String bandCodeType = "DISABILITY";
        BenExchngEnums exchange = BenExchngEnums.TRINET_III;
        NaicsBandCode bandCode = new NaicsBandCode();
        bandCode.setDisabilityBandCode("1");

        when(naicsBandCodeRepository.findActiveByNaicsCodeAndDate(naicsCode, effectiveDate))
                .thenReturn(Optional.of(bandCode));

        // WHEN
        String result = bandCodesService.getBandCodeByType(naicsCode, effectiveDate, bandCodeType, exchange);

        // THEN
        assertEquals("1", result);
    }

    /**
     * GIVEN a NAICS code, effective date, unknown band code type, and exchange TNIII
     * WHEN getBandCodeByType is called
     * THEN it should return null
     */
    @Test
    public void testGetBandCodeByType_UnknownType() {
        // GIVEN
        String naicsCode = "9999";
        Date effectiveDate = new Date();
        String bandCodeType = "UNKNOWN";
        BenExchngEnums exchange = BenExchngEnums.TRINET_III;
        NaicsBandCode bandCode = new NaicsBandCode();

        when(naicsBandCodeRepository.findActiveByNaicsCodeAndDate(naicsCode, effectiveDate))
                .thenReturn(Optional.of(bandCode));

        // WHEN
        String result = bandCodesService.getBandCodeByType(naicsCode, effectiveDate, bandCodeType, exchange);

        // THEN
        assertNull(result);
    }

    /**
     * GIVEN a NAICS code and effective date that do not exist, and exchange TNIII
     * WHEN getBandCodeByType is called
     * THEN it should return null
     */
    @Test
    public void testGetBandCodeByType_NotFound() {
        // GIVEN
        String naicsCode = "0000";
        Date effectiveDate = new Date();
        String bandCodeType = "LIFE";
        BenExchngEnums exchange = BenExchngEnums.TRINET_III;

        when(naicsBandCodeRepository.findActiveByNaicsCodeAndDate(naicsCode, effectiveDate))
                .thenReturn(Optional.empty());

        // WHEN
        String result = bandCodesService.getBandCodeByType(naicsCode, effectiveDate, bandCodeType, exchange);

        // THEN
        assertNull(result);
    }


	private CarrierBand buildCarrierBandData() {
		CarrierBandDetails carrierBandDetails1 = CarrierBandDetails.builder().carrier("AETNA").bandCode("20A")
				.buildCarrierBandDetails();
		CarrierBandDetails carrierBandDetails2 = CarrierBandDetails.builder().carrier("KAISER").bandCode("12A")
				.buildCarrierBandDetails();
		List<CarrierBandDetails> carrierBands1 = List.of(carrierBandDetails1, carrierBandDetails2);
		return CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2023-04-01", "yyyy-MM-dd"))
				.carrierBands(carrierBands1).buildCarrierBand();
	}

}
