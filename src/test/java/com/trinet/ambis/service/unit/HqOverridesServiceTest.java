package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.exception.BSSApplicationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.helper.ExceptionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.HQExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.HqOverrideDao;
import com.trinet.ambis.persistence.model.HQException;
import com.trinet.ambis.persistence.model.HQExceptionsId;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.impl.HqOverridesServiceImpl;
import com.trinet.ambis.service.model.CompanyHQData;
import com.trinet.ambis.service.model.HqOverridesDto;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class HqOverridesServiceTest extends ServiceUnitTest {

	@InjectMocks
	HqOverridesServiceImpl hqOverridesService;

	@Mock
	ApplicationContext context;

	@Mock
	HqOverrideDao hqOverrideDao;

	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	HQExceptionDao hqExceptionDao;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	public static final String ERROR_MESSAGE_INVALID_HQ_STATE = "Invalid state";
	public static final String ERROR_MESSAGE_INVALID_HQ_POSTAL_CODE = "Invalid postal code";

    private MockedStatic<ExceptionServiceHelper> exceptionServiceHelperMockedStatic;

    @Before
    public void setUp() {
        exceptionServiceHelperMockedStatic = Mockito.mockStatic(ExceptionServiceHelper.class);
    }

    @After
    public void tearDown() {
        exceptionServiceHelperMockedStatic.close();
    }

	private static final String CODE = "5R9";

	@Test
	public void getHqOverridesDetails() {
		Set<Long> id = new HashSet<Long>();
		id.add(2L);
		when(realmPlanYearService.getRealmPlanYearByIds(id)).thenReturn(preparePlanYear());

		when(hqOverrideDao.getHqOverridesDetails(CODE, "Q2")).thenReturn(prepareCompanyHqDataList());

		List<HqOverridesDto> result = hqOverridesService.getHqOverridesDetails(CODE, "Q2");
		assertEquals(1, result.size());
		assertTrue(result.get(0).getNextYearPlanYearEnd().contains("/"));

	}

	@Test
	public void createHqOverridesDetails() {
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(CODE);
		id.setRealmYrId(43L);
		HQException hqException = new HQException();
		hqException.setId(id);
		HqOverridesDto hqOverridesDto = prepareCompanyHqDat();
		hqOverridesDto.setOverrideHqState("IL");
		hqOverridesDto.setOverrideHqZip("12322");
		HqOverridesDto result = hqOverridesService.createHqOverridesDetails(hqOverridesDto);
		assertEquals(hqOverridesDto, result);
	}

	@Test
	public void deleteHqOverride() {
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(CODE);
		id.setRealmYrId(43L);
		HQException hqException = new HQException();
		hqException.setId(id);
		doNothing().when(hqExceptionDao).delete(hqException);
		hqOverridesService.deleteHqOverride(CODE,43);
		Mockito.verify(hqExceptionDao, Mockito.times(1)).delete((hqException));

	}
	@Test
	public void getCompanyPlanYearData() {
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(CODE);
		id.setRealmYrId(43L);
		HQException hqException = new HQException();
		hqException.setId(id);
		when(hqOverrideDao.getHqPlanYearData(CODE)).thenReturn(prepareCompanyHq());

		List<CompanyHQData> result=hqOverridesService.getCompanyPlanYearData(CODE);
		assertEquals(1, result.size());

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

	private List<RealmPlanYear> preparePlanYear() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		List<RealmPlanYear> realmPlanYears = new ArrayList<>();
		realmPlanYear.setId(2L);
		realmPlanYear.setRealmId(43L);
		realmPlanYear.setOeQuarter("Q2");
		realmPlanYear.setPlanYearStart(new Date());
		realmPlanYear.setPlanYearEnd(new Date());
		realmPlanYears.add(realmPlanYear);
		return realmPlanYears;
	}

	private List<HqOverridesDto> prepareCompanyHqDataList() {
		HqOverridesDto hqOverridesDto = new HqOverridesDto();
		List<HqOverridesDto> listOfHq = new ArrayList<>();
		hqOverridesDto.setCanCopy(true);
		hqOverridesDto.setCompanyCode(CODE);
		hqOverridesDto.setCompanyName("Test");
		hqOverridesDto.setRealmYearId(43L);
		hqOverridesDto.setNextRealmYearId(2L);
		hqOverridesDto.setNextYearPlanYearStart("01-01-2021");
		listOfHq.add(hqOverridesDto);
		return listOfHq;
	}
	
	private List<CompanyHQData> prepareCompanyHq() {
		CompanyHQData companyRealmData = new CompanyHQData();
		companyRealmData.setCode("001");
		companyRealmData.setRealmYearId(41L);
		companyRealmData.setCompanyHq("NA");
		List<CompanyHQData> listOfCompany = new ArrayList<>();
		listOfCompany.add(companyRealmData);
		return listOfCompany;
	}
	
	@Test
	public void createHqOverridesWithInvalidDetails() { 
		Exception exception = null;
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(CODE);
		id.setRealmYrId(43L);
		HQException hqException = new HQException();
		hqException.setId(id);
		HqOverridesDto hqOverridesDto = prepareCompanyHqDat();
		hqOverridesDto.setOverrideHqState("IE");
		try {
			//when
            exceptionServiceHelperMockedStatic
                    .when(() -> ExceptionServiceHelper.validateHQOverridesRequestData(hqOverridesDto))
                    .thenThrow(new BSSApplicationException());
			hqOverridesService.createHqOverridesDetails(hqOverridesDto);
		} catch (Exception e) {
			exception = e;
		}
		// then
		verify(hqExceptionDao, times(0)).save(hqException);
		assertNotNull(exception);
	}
	
	@Test
	public void createHqOverridesWithValidDetails() {
		HqOverridesDto result = null;
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(CODE);
		id.setRealmYrId(12L);
		HQException hqException = new HQException();
		hqException.setId(id);
		HqOverridesDto hqOverridesDto = prepareCompanyHqDat();
		hqException.setHqState(hqOverridesDto.getOverrideHqState());
		hqException.setPostalCode(hqOverridesDto.getOverrideHqZip());
		try {
            exceptionServiceHelperMockedStatic
                    .when(() -> ExceptionServiceHelper.validateHQOverridesRequestData(hqOverridesDto))
                    .thenAnswer(invocation -> null);;
            result = hqOverridesService.createHqOverridesDetails(hqOverridesDto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// then
		verify(hqExceptionDao, times(1)).save(hqException);
		assertEquals(hqOverridesDto, result);
	}
	
}
