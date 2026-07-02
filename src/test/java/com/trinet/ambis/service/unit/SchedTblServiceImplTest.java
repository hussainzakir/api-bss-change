/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trinet.ambis.service.BrokerNotificationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.helper.ExceptionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupTransactionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDao;
import com.trinet.ambis.persistence.dao.hrp.SchedTblDao;
import com.trinet.ambis.persistence.dao.hrp.SchedTblDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.SchedMidYearFunding;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.persistence.model.SchedTblId;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.impl.SchedTblServiceImpl;
import com.trinet.ambis.service.model.SchedMidYearFundingDto;
import com.trinet.ambis.service.model.SchedTblAdminDto;
import com.trinet.ambis.service.model.SchedTblDto;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.Utils;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author rvutukuri
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SchedTblServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	SchedTblServiceImpl schedTblServiceImpl;

	@Mock
	SchedTblDao schedTblDao;
	
	@Mock
	SchedTblDataDao schedTblDataDao;

	@Mock
	RealmPlanYearDao realmPlanYearDao;

	@Mock
	RealmPlanYearService realmPlanYearService;
	
	@Mock
	SchedMidYearFundingDao schedMidYearFundingDao;
	
	@Mock
	EmployeeDao employeeDao;
	
	@Mock
	PersonService personService;
	
	@Mock
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Mock
	EmployeeStrategyGroupTransactionDao employeeStrategyGroupTransactionDao;
	@Mock
	BrokerNotificationService brokerNotificationService;

	private static final String COMPANY_CODE = "G48";
	private static final String DEFAULT_COMPANY_CODE = "DEFAULT";
	private static final String QUARTER = "8Y";
	private static final long REALM_YEAR_ID = 3L;

	private MockedStatic<ExceptionServiceHelper> mockStaticExceptionServiceHelper;

	@Before
	public void setUp() {
		if (mockStaticExceptionServiceHelper == null) {
			mockStaticExceptionServiceHelper = Mockito.mockStatic(ExceptionServiceHelper.class);
		}
	}

    @After
    public void tearDown() {
        if (mockStaticExceptionServiceHelper != null)
            mockStaticExceptionServiceHelper.close();
    }

	@Test
	public void getScheduleDatesTest1() {
		SchedTbl st = schedTblServiceImpl.getScheduleDates(COMPANY_CODE, QUARTER, REALM_YEAR_ID);
		assertNull(st);
	}

	@Test
	public void getScheduleDatesTest2() {
		RealmPlanYear rp = new RealmPlanYear();
		rp.setId(3l);
		Mockito.when(realmPlanYearDao.getMaxRealmPlanYearByQuarter(QUARTER)).thenReturn(rp);
		SchedTbl st = schedTblServiceImpl.getScheduleDates(COMPANY_CODE, QUARTER, null);
		assertNull(st);
	}

	@Test
	public void getScheduleDatesTest3() {
		RealmPlanYear rp = new RealmPlanYear();
		rp.setId(3l);
		Mockito.when(schedTblDao.getSecheduleDates(COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
		Mockito.when(realmPlanYearDao.getMaxRealmPlanYearByQuarter("8Y")).thenReturn(rp);
		SchedTbl st = schedTblServiceImpl.getScheduleDates("G48", "8Y", null);
		assertNull(st);
	}

	@Test
	public void getCalcuatedScheduleDatesTest1() {
		RealmPlanYear rp = new RealmPlanYear();
		rp.setId(REALM_YEAR_ID);
		Mockito.when(realmPlanYearDao.getMaxRealmPlanYearByQuarter(QUARTER)).thenReturn(rp);
		Mockito.when(schedTblDao.getSecheduleDates(COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
		Mockito.when(schedTblDao.getSecheduleDates(Constants.DEFAULT_COMPANY_CODE, REALM_YEAR_ID))
				.thenReturn(null);

		SchedTbl st = schedTblServiceImpl.getCalcuatedScheduleDates(COMPANY_CODE, QUARTER, null);
		assertNull(st);
	}

	@Test
	public void getCalcuatedScheduleDatesTest2() {
		SchedTbl clientSchedTbl = prepareSchedTbl(Utils.convertStringToDate("15-JAN-2021", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-FEB-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-APR-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-JAN-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-APR-2020", Constants.DATE_FORMAT));
		SchedTbl defaultSchedTbl = prepareSchedTbl(Utils.convertStringToDate("01-JAN-2021", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-FEB-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-JAN-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-APR-2020", Constants.DATE_FORMAT));
		Mockito.when(schedTblDao.getSecheduleDates(COMPANY_CODE, REALM_YEAR_ID))
				.thenReturn(clientSchedTbl);
		Mockito.when(schedTblDao.getSecheduleDates(Constants.DEFAULT_COMPANY_CODE, REALM_YEAR_ID))
				.thenReturn(defaultSchedTbl);

		SchedTbl st = schedTblServiceImpl.getCalcuatedScheduleDates(COMPANY_CODE, QUARTER, REALM_YEAR_ID);
		assertEquals(clientSchedTbl.getExtensionEndDate(), st.getExtensionEndDate());
	}

	@Test
	public void getCalcuatedScheduleDatesTest3() {
		SchedTbl clientSchedTbl = prepareSchedTbl(Utils.convertStringToDate("15-JAN-2021", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-FEB-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-JAN-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-APR-2020", Constants.DATE_FORMAT));
		SchedTbl defaultSchedTbl = prepareSchedTbl(Utils.convertStringToDate("01-JAN-2021", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-FEB-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-APR-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-JAN-2020", Constants.DATE_FORMAT),
				Utils.convertStringToDate("01-APR-2020", Constants.DATE_FORMAT));
		Mockito.when(schedTblDao.getSecheduleDates(COMPANY_CODE, REALM_YEAR_ID))
				.thenReturn(clientSchedTbl);
		Mockito.when(schedTblDao.getSecheduleDates(Constants.DEFAULT_COMPANY_CODE, REALM_YEAR_ID))
				.thenReturn(defaultSchedTbl);

		SchedTbl st = schedTblServiceImpl.getCalcuatedScheduleDates(COMPANY_CODE, QUARTER, REALM_YEAR_ID);
		assertEquals(defaultSchedTbl.getExtensionEndDate(), st.getExtensionEndDate());
	}

	@Test
	public void createUpdateScheduleDatesWithBrokerNotificationEnabledTest() {
		// Given
		HttpServletRequest request = new MockHttpServletRequest();
		Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
		SchedTblDto schedTblDto = prepareSchedTblDto(COMPANY_CODE, REALM_YEAR_ID, "Q1", internalOpenDate);
		SchedTbl expectedSchedTbl = prepareExistingSchedTbl(COMPANY_CODE, REALM_YEAR_ID, internalOpenDate);
		String lastUpdatedBy = "testUser";
		MockedStatic<com.trinet.ambis.util.AppRulesAndConfigsUtils> mockStatic = Mockito.mockStatic(com.trinet.ambis.util.AppRulesAndConfigsUtils.class);
		mockStatic.when(com.trinet.ambis.util.AppRulesAndConfigsUtils::isBrokerNotificationEnabled).thenReturn(true);
		doNothing().when(brokerNotificationService).validateAndSendBrokerNotification(request, schedTblDto);
		when(schedTblDao.saveAndFlush(any(SchedTbl.class))).thenReturn(expectedSchedTbl);
		// When
		SchedTbl result = schedTblServiceImpl.createUpdateScheduleDates(request, schedTblDto, lastUpdatedBy);
		// Then
		verify(brokerNotificationService, times(1)).validateAndSendBrokerNotification(request, schedTblDto);
		verify(schedTblDao, times(1)).saveAndFlush(any(SchedTbl.class));
		assertNotNull(result);
		assertEquals(lastUpdatedBy, schedTblDto.getLastUpdatedBy());

		mockStatic.close();
	}

	@Test
	public void createUpdateScheduleDatesWithBrokerNotificationDisabledTest() {
		// Given
		HttpServletRequest request = new MockHttpServletRequest();
		Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
		SchedTblDto schedTblDto = prepareSchedTblDto(COMPANY_CODE, REALM_YEAR_ID, "Q1", internalOpenDate);
		SchedTbl expectedSchedTbl = prepareExistingSchedTbl(COMPANY_CODE, REALM_YEAR_ID, internalOpenDate);
		String lastUpdatedBy = "testUser";
		MockedStatic<com.trinet.ambis.util.AppRulesAndConfigsUtils> mockStatic = Mockito.mockStatic(com.trinet.ambis.util.AppRulesAndConfigsUtils.class);
		mockStatic.when(com.trinet.ambis.util.AppRulesAndConfigsUtils::isBrokerNotificationEnabled).thenReturn(false);
		when(schedTblDao.saveAndFlush(any(SchedTbl.class))).thenReturn(expectedSchedTbl);
		// When
		SchedTbl result = schedTblServiceImpl.createUpdateScheduleDates(request, schedTblDto, lastUpdatedBy);
		// Then
		verify(brokerNotificationService, never()).validateAndSendBrokerNotification(any(), any());
		verify(schedTblDao, times(1)).saveAndFlush(any(SchedTbl.class));
		assertNotNull(result);
		assertEquals(lastUpdatedBy, schedTblDto.getLastUpdatedBy());
		mockStatic.close();
	}

	@Test
	public void createUpdateScheduleDatesWithNullLastUpdatedByTest() {
		// Given
		HttpServletRequest request = new MockHttpServletRequest();
		Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
		SchedTblDto schedTblDto = prepareSchedTblDto(COMPANY_CODE, REALM_YEAR_ID, "Q1", internalOpenDate);
		SchedTbl expectedSchedTbl = prepareExistingSchedTbl(COMPANY_CODE, REALM_YEAR_ID, internalOpenDate);
		MockedStatic<com.trinet.ambis.util.AppRulesAndConfigsUtils> mockStatic = Mockito.mockStatic(com.trinet.ambis.util.AppRulesAndConfigsUtils.class);
		mockStatic.when(com.trinet.ambis.util.AppRulesAndConfigsUtils::isBrokerNotificationEnabled).thenReturn(false);
		when(schedTblDao.saveAndFlush(any(SchedTbl.class))).thenReturn(expectedSchedTbl);
		// When
		SchedTbl result = schedTblServiceImpl.createUpdateScheduleDates(request, schedTblDto, null);
		// Then
		verify(schedTblDao, times(1)).saveAndFlush(any(SchedTbl.class));
		assertNotNull(result);
		assertNull(schedTblDto.getLastUpdatedBy());
		mockStatic.close();
	}

	@Test
	public void createUpdateScheduleDatesDefaultCompanyTest() {
		// Given
		HttpServletRequest request = new MockHttpServletRequest();
		Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
		SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "Q1", internalOpenDate);
		SchedTbl expectedSchedTbl = prepareExistingSchedTbl(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, internalOpenDate);
		String lastUpdatedBy = "adminUser";
		MockedStatic<com.trinet.ambis.util.AppRulesAndConfigsUtils> mockStatic = Mockito.mockStatic(com.trinet.ambis.util.AppRulesAndConfigsUtils.class);
		mockStatic.when(com.trinet.ambis.util.AppRulesAndConfigsUtils::isBrokerNotificationEnabled).thenReturn(true);
		doNothing().when(brokerNotificationService).validateAndSendBrokerNotification(request, schedTblDto);
		when(schedTblDao.saveAndFlush(any(SchedTbl.class))).thenReturn(expectedSchedTbl);
		// When
		SchedTbl result = schedTblServiceImpl.createUpdateScheduleDates(request, schedTblDto, lastUpdatedBy);
		// Then
		verify(brokerNotificationService, times(1)).validateAndSendBrokerNotification(request, schedTblDto);
		verify(schedTblDao, times(1)).saveAndFlush(any(SchedTbl.class));
		assertNotNull(result);
		assertEquals(lastUpdatedBy, schedTblDto.getLastUpdatedBy());
		mockStatic.close();
	}

	@Test
	public void createUpdateMidYearDetailsTest() {
		SchedMidYearFundingDto smyfDto = new SchedMidYearFundingDto();
		SchedMidYearFunding smyfEntity = new SchedMidYearFunding();
		smyfDto.setActive(true);
		smyfDto.setId(1);
		smyfDto.setServiceOrderNumber("05707785");
		SchedMidYearFunding smyf1 = new SchedMidYearFunding();
		smyf1.setActive(true);
		List<SchedMidYearFunding> schedMidYearFundingList = new ArrayList<SchedMidYearFunding>();
		schedMidYearFundingList.add(smyfEntity);
		schedMidYearFundingList.add(smyf1);
		SchedMidYearFunding smyf3 = new SchedMidYearFunding();
		smyf3.setActive(true);
		smyf3.setId(1);
		Company company = new Company();
		company.setId(1234);
		company.setRenewalCompany(true);
		company.setRealmPlanYearId(REALM_YEAR_ID);
		company.setCode(COMPANY_CODE);
		smyfDto.setCompanyCode(company.getCode());
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();
		List<SchedMidYearFunding> schedMidYearFundingList1 = new ArrayList<SchedMidYearFunding>();
		schedMidYearFundingList1.add(smyfEntity);
		schedMidYearFundingList1.add(smyf1);
		schedMidYearFundingList1.add(smyf3);
		
		List<Employee> employees = new ArrayList<>();
		doNothing().when(employeeStrategyGroupTransactionDao).deleteByCompanyAndYear(company.getCode(), company.getRealmPlanYearId());
		Mockito.when(schedMidYearFundingDao.findByCompanyCode(company.getCode())).thenReturn(schedMidYearFundingList);
		Mockito.when(schedMidYearFundingDao.saveAll(schedMidYearFundingList)).thenReturn(schedMidYearFundingList1);
		Mockito.when(realmPlanYearService.getRealmForCompanyId(Mockito.anyLong())).thenReturn(realmPlanYear);
		Mockito.when(schedMidYearFundingDao.findByCompanyCodePlanYearEndDate(company.getCode())).thenReturn(schedMidYearFundingList1);
		schedMidYearFundingDao.saveAll(schedMidYearFundingList);
		List<SchedMidYearFundingDto> savedList = schedTblServiceImpl.createUpdateMidYearDetails(smyfDto, company,
				false);
		assertEquals(3, savedList.size());
	}	

	@Test
	public void createUpdateMidYearDetailsTest2() {
		SchedMidYearFundingDto smyfDto = new SchedMidYearFundingDto();
		SchedMidYearFunding smyfEntity = new SchedMidYearFunding();
		smyfDto.setActive(true);
		smyfDto.setId(1);
		SchedMidYearFunding smyf1 = new SchedMidYearFunding();
		smyfDto.setActive(false);
		smyfDto.setServiceOrderNumber("05707785");
		List<SchedMidYearFunding> schedMidYearFundingList = new ArrayList<SchedMidYearFunding>();
		schedMidYearFundingList.add(smyfEntity);
		schedMidYearFundingList.add(smyf1);
		SchedMidYearFunding smyf3 = new SchedMidYearFunding();
		smyf3.setActive(true);
		smyf3.setId(1);
		Company company = new Company();
		company.setId(1234);
		company.setCode(COMPANY_CODE);
		smyfDto.setCompanyCode(company.getCode());
		List<SchedMidYearFunding> schedMidYearFundingList1 = new ArrayList<SchedMidYearFunding>();
		schedMidYearFundingList1.add(smyfEntity);
		schedMidYearFundingList1.add(smyf1);
		schedMidYearFundingList1.add(smyf3);
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();
		
		Mockito.when(schedMidYearFundingDao.findByCompanyCode(company.getCode())).thenReturn(schedMidYearFundingList);
		Mockito.when(schedMidYearFundingDao.saveAll(schedMidYearFundingList)).thenReturn(schedMidYearFundingList1);
		schedMidYearFundingDao.saveAll(schedMidYearFundingList);
		Mockito.when(realmPlanYearService.getRealmForCompanyId(Mockito.anyLong())).thenReturn(realmPlanYear);
		Mockito.when(schedMidYearFundingDao.findByCompanyCodePlanYearEndDate(company.getCode())).thenReturn(schedMidYearFundingList1);

		List<SchedMidYearFundingDto> savedList = schedTblServiceImpl.createUpdateMidYearDetails(smyfDto, company,
				false);
		assertEquals(3, savedList.size());
	}

	@Test
	public void createUpdateMidYearDetailsTest3() {
		SchedMidYearFundingDto smyfDto = new SchedMidYearFundingDto();
		SchedMidYearFunding smyfEntity = new SchedMidYearFunding();
		smyfDto.setActive(true);
		smyfDto.setId(1);
		smyfDto.setServiceOrderNumber("05707785");
		SchedMidYearFunding smyf1 = new SchedMidYearFunding();
		smyf1.setId(1);
		smyf1.setActive(false);
		List<SchedMidYearFunding> schedMidYearFundingList = new ArrayList<SchedMidYearFunding>();
		schedMidYearFundingList.add(smyfEntity);
		schedMidYearFundingList.add(smyf1);
		SchedMidYearFunding smyf3 = new SchedMidYearFunding();
		smyf3.setActive(true);
		smyf3.setId(1);
		Company company = new Company();
		company.setId(1234);
		company.setCode(COMPANY_CODE);
		smyfDto.setCompanyCode(company.getCode());
		List<SchedMidYearFunding> schedMidYearFundingList1 = new ArrayList<SchedMidYearFunding>();
		schedMidYearFundingList1.add(smyfEntity);
		schedMidYearFundingList1.add(smyf1);
		schedMidYearFundingList1.add(smyf3);
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();
		
		Mockito.when(schedMidYearFundingDao.findByCompanyCode(company.getCode())).thenReturn(schedMidYearFundingList);
		Mockito.when(schedMidYearFundingDao.saveAll(schedMidYearFundingList)).thenReturn(schedMidYearFundingList1);
		schedMidYearFundingDao.saveAll(schedMidYearFundingList);
		Mockito.when(realmPlanYearService.getRealmForCompanyId(Mockito.anyLong())).thenReturn(realmPlanYear);
		Mockito.when(schedMidYearFundingDao.findByCompanyCodePlanYearEndDate(company.getCode())).thenReturn(schedMidYearFundingList1);

		List<SchedMidYearFundingDto> savedList = schedTblServiceImpl.createUpdateMidYearDetails(smyfDto, company, true);
		assertEquals(3, savedList.size());
	}

	@Test
	public void getMidYearDetails() {
		SchedMidYearFunding smyf = new SchedMidYearFunding();
		smyf.setActive(true);
		SchedMidYearFunding smyf1 = new SchedMidYearFunding();
		smyf.setActive(false);
		List<SchedMidYearFunding> schedMidYearFundingList = new ArrayList<SchedMidYearFunding>();
		schedMidYearFundingList.add(smyf);
		schedMidYearFundingList.add(smyf1);
		Company company = new Company();
		company.setId(1234);
		company.setCode(COMPANY_CODE);
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();
		
		Mockito.when(schedMidYearFundingDao.findByCompanyCodePlanYearEndDate(company.getCode())).thenReturn(schedMidYearFundingList);
		Mockito.when(realmPlanYearService.getRealmForCompanyId(Mockito.anyLong())).thenReturn(realmPlanYear);

		List<SchedMidYearFundingDto> getList = schedTblServiceImpl.getMidYearDetails(company.getCode());
		assertEquals(2, getList.size());
	}

	@Test
	public void getSchedTblAdminDates() {
		List<SchedTblAdminDto> expectedResultList = new ArrayList<>();
		SchedTblAdminDto schedTblAdminDto = new SchedTblAdminDto();
		expectedResultList.add(schedTblAdminDto);
		when(schedTblDataDao.getSchedTblAdminDates(COMPANY_CODE, QUARTER)).thenReturn(expectedResultList);
		List<SchedTblAdminDto> actualResultList = schedTblServiceImpl.getSchedTblAdminDates(COMPANY_CODE, QUARTER);
		assertEquals(1, actualResultList.size());
		assertEquals(expectedResultList, actualResultList);
	}
	
	@Test
	public void validateRequestDefaultCompanyTest() {
		// given
		SchedTblId schedTblId = new SchedTblId();
		schedTblId.setCompany("DEFAULT");
		SchedTblDto schedTblDto = new SchedTblDto();
		schedTblDto.setSched(schedTblId);
		schedTblDto.setOeQuarter("8Y");
		// when
		schedTblServiceImpl.validateRequest(schedTblDto);
		// then
		verify(ExceptionServiceHelper.class, times(0));
		ExceptionServiceHelper.validateCompanyCode(schedTblId.getCompany());
		verify(ExceptionServiceHelper.class, times(1));
		ExceptionServiceHelper.validateOeQuarter(schedTblDto.getOeQuarter());
	}
	
	@Test
	public void validateRequestNonDefaultCompanyTest() {
		// given
		SchedTblId schedTblId = new SchedTblId();
		schedTblId.setCompany("G48");
		SchedTblDto schedTblDto = new SchedTblDto();
		schedTblDto.setSched(schedTblId);
		schedTblDto.setOeQuarter("8Y");
		// when
		schedTblServiceImpl.validateRequest(schedTblDto);
		// then
		verify(ExceptionServiceHelper.class, times(1));
		ExceptionServiceHelper.validateCompanyCode(schedTblId.getCompany());
		verify(ExceptionServiceHelper.class, times(1));
		ExceptionServiceHelper.validateOeQuarter(schedTblDto.getOeQuarter());
	}
	
	private RealmPlanYear prepareRealmPlanYear() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearStart(new Date());
		realmPlanYear.setPlanYearEnd(new Date());
		return realmPlanYear;
	}

	private SchedTbl prepareSchedTbl(Date openDate, Date closeDate, Date extensionEndDate, Date internalOpenDate,
			Date internalCloseDate) {
		SchedTbl schedTbl = new SchedTbl();
		schedTbl.setOpenDate(openDate);
		schedTbl.setCloseDate(closeDate);
		schedTbl.setExtensionEndDate(extensionEndDate);
		schedTbl.setInternalOpenDate(internalOpenDate);
		schedTbl.setInternalCloseDate(internalCloseDate);
		return schedTbl;
	}

	private SchedTblDto prepareSchedTblDto(String companyCode, Long realmYearId, String quarter, Date internalOpenDate) {
		SchedTblDto schedTblDto = new SchedTblDto();
		SchedTblId schedTblId = new SchedTblId();
		schedTblId.setCompany(companyCode);
		schedTblId.setRealmYearId(realmYearId);
		schedTblDto.setSched(schedTblId);
		schedTblDto.setOeQuarter(quarter);
		schedTblDto.setInternalOpenDate(internalOpenDate);
		return schedTblDto;
	}

	private SchedTbl prepareExistingSchedTbl(String companyCode, Long realmYearId, Date internalOpenDate) {
		SchedTbl schedTbl = new SchedTbl();
		SchedTblId schedTblId = new SchedTblId();
		schedTblId.setCompany(companyCode);
		schedTblId.setRealmYearId(realmYearId);
		schedTbl.setSched(schedTblId);
		schedTbl.setInternalOpenDate(internalOpenDate);
		return schedTbl;
	}

}
