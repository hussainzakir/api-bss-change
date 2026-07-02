package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.model.HQException;
import com.trinet.ambis.persistence.model.HQExceptionsId;
import com.trinet.ambis.persistence.model.MinimumFundingException;
import com.trinet.ambis.service.model.ExceptionDto;
import com.trinet.ambis.service.model.HqOverridesDto;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.Utils;
import com.trinet.security.util.SecurityUtils;

/**
 * @author schaudhari
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class ExceptionServiceHelperTest extends ServiceUnitTest {

    private MockedStatic<SecurityUtils> securityUtilsMockedStatic;
    private MockedStatic<BenExchngEnums> benExchngEnumsMockedStatic;

    @Before
    public void setUp() {
        securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class);
        benExchngEnumsMockedStatic = Mockito.mockStatic(BenExchngEnums.class);
    }

    @After
    public void tearDown() {
        if (securityUtilsMockedStatic != null) {
            securityUtilsMockedStatic.close();
            securityUtilsMockedStatic = null;
        }
        if (benExchngEnumsMockedStatic != null)
        benExchngEnumsMockedStatic.close();
    }

	public static final String ERROR_MESSAGE_INVALID_COMPANY_CODE = "Invalid company code";
	public static final String ERROR_MESSAGE_INVALID_SERVICE_ORDER_NUMBER = "Invalid service order number";
	public static final String ERROR_MESSAGE_INVALID_HQ_STATE = "Invalid state";
	public static final String ERROR_MESSAGE_INVALID_HQ_POSTAL_CODE = "Invalid postal code";
	private static final String CODE = "5R9";
	
	public @Rule
    ExpectedException expectedException = ExpectedException.none();

	// startDt after endDt and date doesn't overlap
	@Test
	public void validateStartAndEndDtsTest1() {
		Date startDt = Utils.convertStringToDate("01-Jan-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("31-Dec-2021", Constants.DATE_FORMAT);
		ExceptionDto dto = prepareExceptionDto(1111L, startDt, endDt);

		Date existingStartDt = Utils.convertStringToDate("01-Jan-2020", Constants.DATE_FORMAT);
		Date existingEndDt = Utils.convertStringToDate("31-Dec-2021", Constants.DATE_FORMAT);

		Date existingStartDt1 = Utils.convertStringToDate("01-Jan-2019", Constants.DATE_FORMAT);
		Date existingEndDt1 = Utils.convertStringToDate("31-Dec-2019", Constants.DATE_FORMAT);
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(prepareMinFundException(1111L, existingStartDt, existingEndDt));
		existingEntities.add(prepareMinFundException(2222L, existingStartDt1, existingEndDt1));

		ExceptionServiceHelper.validateStartAndEndDts(dto, existingEntities);
	}

	// startDt after endDt are same
	@Test
	public void validateStartAndEndDtsTest2() {
		Date startDt = Utils.convertStringToDate("01-Jan-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("01-Jan-2020", Constants.DATE_FORMAT);
		ExceptionDto dto = prepareExceptionDto(1111L, startDt, endDt);

		Date existingStartDt = Utils.convertStringToDate("01-Jan-2021", Constants.DATE_FORMAT);
		Date existingEndDt = Utils.convertStringToDate("31-Dec-2021", Constants.DATE_FORMAT);
		MinimumFundingException existingEntity = prepareMinFundException(1111L, existingStartDt, existingEndDt);
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		ExceptionServiceHelper.validateStartAndEndDts(dto, existingEntities);

	}

	// startDt before endDt
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDtsTest3() {
		Date startDt = Utils.convertStringToDate("01-Jan-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("31-Dec-2019", Constants.DATE_FORMAT);
		ExceptionDto dto = prepareExceptionDto(1111L, startDt, endDt);

		Date existingStartDt = Utils.convertStringToDate("01-Jan-2021", Constants.DATE_FORMAT);
		Date existingEndDt = Utils.convertStringToDate("31-Dec-2021", Constants.DATE_FORMAT);
		MinimumFundingException existingEntity = prepareMinFundException(1111L, existingStartDt, existingEndDt);
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		ExceptionServiceHelper.validateStartAndEndDts(dto, existingEntities);

	}

	// startDt and endDt overlaps
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDtsTest4() {
		Date startDt = Utils.convertStringToDate("01-Jan-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("31-Dec-2020", Constants.DATE_FORMAT);
		ExceptionDto dto = prepareExceptionDto(1111L, startDt, endDt);

		Date existingStartDt = Utils.convertStringToDate("01-Mar-2020", Constants.DATE_FORMAT);
		Date existingEndDt = Utils.convertStringToDate("31-Dec-2021", Constants.DATE_FORMAT);
		MinimumFundingException existingEntity = prepareMinFundException(2222L, existingStartDt, existingEndDt);
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		ExceptionServiceHelper.validateStartAndEndDts(dto, existingEntities);

	}

	// startDt and endDt overlaps
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDtsTest5() {
		Date startDt = Utils.convertStringToDate("01-Jan-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("31-Dec-2020", Constants.DATE_FORMAT);
		ExceptionDto dto = prepareExceptionDto(1111L, startDt, endDt);

		Date existingStartDt = Utils.convertStringToDate("01-Mar-2019", Constants.DATE_FORMAT);
		Date existingEndDt = Utils.convertStringToDate("28-Feb-2020", Constants.DATE_FORMAT);
		MinimumFundingException existingEntity = prepareMinFundException(2222L, existingStartDt, existingEndDt);
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		ExceptionServiceHelper.validateStartAndEndDts(dto, existingEntities);

	}

    @Test(expected = InvocationTargetException.class)
    public void privateConstructorTest()
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Constructor<?> constructor = ExceptionServiceHelper.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            // Assert the cause is IllegalStateException
            assertTrue(e.getCause() instanceof IllegalStateException);
            throw e;
        }
    }

	@Test
	public void validateCompanyCodeTrueTest() {
		String companyCode = "G48";
		// given
		when(SecurityUtils.isValidCompany(companyCode)).thenReturn(Boolean.TRUE);
		// when
		ExceptionServiceHelper.validateCompanyCode(companyCode);
		// then
		verify(SecurityUtils.class);
		SecurityUtils.isValidCompany(companyCode);
	}

	@Test
	public void validateCompanyCodeFalseTest() {
		String companyCode = "G48";
		// given
		when(SecurityUtils.isValidCompany(companyCode)).thenReturn(Boolean.FALSE);
		// when
		Exception thrownException = null;
		try {
			ExceptionServiceHelper.validateCompanyCode(companyCode);
		} catch (Exception e) {
			thrownException = e;
		}
		// then
		verify(SecurityUtils.class, times(1));
		SecurityUtils.isValidCompany(companyCode);
		assertNotNull(thrownException);
		assertEquals("Invalid company code", thrownException.getMessage());
	}

	@Test
	public void validateOeQuarterTrueTest() {
		String oeQuarter = "8Y";
		// given
		when(BenExchngEnums.isValidQuarter(oeQuarter)).thenReturn(Boolean.TRUE);
		// when
		ExceptionServiceHelper.validateOeQuarter(oeQuarter);
		// then
		verify(BenExchngEnums.class);
		BenExchngEnums.isValidQuarter(oeQuarter);
	}

	@Test
	public void validateOeQuarterFalseTest() {
		String oeQuarter = "8Y";
		// given
		when(BenExchngEnums.isValidQuarter(oeQuarter)).thenReturn(Boolean.FALSE);
		// when
		Exception thrownException = null;
		try {
			ExceptionServiceHelper.validateOeQuarter(oeQuarter);
		} catch (Exception e) {
			thrownException = e;
		}
		// then
		verify(BenExchngEnums.class, times(1));
		BenExchngEnums.isValidQuarter(oeQuarter);
		assertNotNull(thrownException);
		assertEquals("Invalid quarter", thrownException.getMessage());
	}

	private MinimumFundingException prepareMinFundException(long id, Date startDt, Date endDt) {
		MinimumFundingException mfe = new MinimumFundingException();
		mfe.setId(id);
		mfe.setStartDate(startDt);
		mfe.setEndDate(endDt);
		return mfe;
	}

	private ExceptionDto prepareExceptionDto(long id, Date startDt, Date endDt) {
		ExceptionDto dto = new ExceptionDto();
		dto.setId(id);
		dto.setStartDate(startDt);
		dto.setEndDate(endDt);
		return dto;
	}
	
	// invalid company code
	@Test
	public void validateMidYearFundingInvalidCompanyTest() {
		String companyCode = "GSU%";
		String serviceOrderNumber = "12345";
		// given
		when(SecurityUtils.isValidCompany(companyCode)).thenReturn(Boolean.FALSE);
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_COMPANY_CODE);
		ExceptionServiceHelper.validateMidYearFundingRequestData(companyCode, serviceOrderNumber);
	}
	
	// invalid service order number
	@Test
	public void validateMidYearFundingInvalidServiceOrderNumberTest() { 
		String companyCode = "GSU";
		String serviceOrderNumber = "123%45";
		// given
		when(SecurityUtils.isValidCompany(companyCode)).thenReturn(Boolean.TRUE);
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_SERVICE_ORDER_NUMBER);
		ExceptionServiceHelper.validateMidYearFundingRequestData(companyCode, serviceOrderNumber);
	}
	
	// invalid company code and service order number
	@Test
	public void validateMidYearFundingInvalidDetailsTest() {
		String companyCode = "GSU%";
		String serviceOrderNumber = "1234%5";
		// given
		when(SecurityUtils.isValidCompany(companyCode)).thenReturn(Boolean.FALSE);
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_COMPANY_CODE);
		ExceptionServiceHelper.validateMidYearFundingRequestData(companyCode, serviceOrderNumber);
	}

	// valid company code and service order number
	@Test
	public void validateMidYearFundingValidTest() {
		String companyCode = "GSU";
		String serviceOrderNumber = "12345";
		// given
		when(SecurityUtils.isValidCompany(companyCode)).thenReturn(Boolean.TRUE);
		ExceptionServiceHelper.validateMidYearFundingRequestData(companyCode, serviceOrderNumber);
	}
	
	@Test
	public void validateHQOverrideDetailsWithInvalidHQState() {
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(CODE);
		id.setRealmYrId(43L);
		HQException hqException = new HQException();
		hqException.setId(id);
		HqOverridesDto hqOverridesDto = prepareCompanyHqDat();
		// given
		when(SecurityUtils.isValidCompany(hqOverridesDto.getCompanyCode())).thenReturn(Boolean.TRUE);
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_HQ_STATE);
		ExceptionServiceHelper.validateHQOverridesRequestData(hqOverridesDto);
	}
	
	@Test
	public void validateHQOverrideDetailsWithInvalidHQZip() {
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(CODE);
		id.setRealmYrId(43L);
		HQException hqException = new HQException();
		hqException.setId(id);
		HqOverridesDto hqOverridesDto = prepareCompanyHqDat();
		hqOverridesDto.setOverrideHqState("DE");
		hqOverridesDto.setOverrideHqZip("99");
		// given
		when(SecurityUtils.isValidCompany(hqOverridesDto.getCompanyCode())).thenReturn(Boolean.TRUE);
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_HQ_POSTAL_CODE);
		ExceptionServiceHelper.validateHQOverridesRequestData(hqOverridesDto);
	}
	
	@Test
	public void validateHQOverrideDetailsWithInvalidCompany() {
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(CODE);
		id.setRealmYrId(43L);
		HQException hqException = new HQException();
		hqException.setId(id);
		HqOverridesDto hqOverridesDto = prepareCompanyHqDat();
		hqOverridesDto.setOverrideHqState("DE");
		hqOverridesDto.setOverrideHqZip("99");
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_COMPANY_CODE);
		// given
		when(SecurityUtils.isValidCompany(hqOverridesDto.getCompanyCode())).thenReturn(Boolean.FALSE);
		ExceptionServiceHelper.validateHQOverridesRequestData(hqOverridesDto);
	}
	
	@Test
	public void validateValidHQOverrideDetails() {
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(CODE);
		id.setRealmYrId(43L);
		HQException hqException = new HQException();
		hqException.setId(id);
		HqOverridesDto hqOverridesDto = prepareCompanyHqDat();
		hqOverridesDto.setOverrideHqState("DE");
		hqOverridesDto.setOverrideHqZip("99999");
		// given
		when(SecurityUtils.isValidCompany(hqOverridesDto.getCompanyCode())).thenReturn(Boolean.TRUE);
		Exception thrownException = null;
		try {
		     ExceptionServiceHelper.validateHQOverridesRequestData(hqOverridesDto);
		} catch (Exception e) {
			thrownException = e;
		}
		assertNull(thrownException);
	}
	
	private HqOverridesDto prepareCompanyHqDat() {
		HqOverridesDto hqOverridesDto=new HqOverridesDto();
		hqOverridesDto.setCompanyCode(CODE);
		hqOverridesDto.setRealmYearId(12L);
		hqOverridesDto.setNextRealmYearId(43L);
		hqOverridesDto.setOverrideHqState("NA");
		hqOverridesDto.setOverrideHqZip("123-22");
		return hqOverridesDto;
	}
}
