package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.BSSApplicationConstants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.PlanDeselectionExceptionDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanDeselectionExceptions;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.rest.controllers.dto.PlanDeselectionExceptionResDto;
import com.trinet.ambis.service.impl.PlanDeselectionExceptionServiceImpl;
import com.trinet.ambis.util.BSSSecurityUtils;

@RunWith(MockitoJUnitRunner.class)
public class PlanDeselectionExceptionServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	PlanDeselectionExceptionServiceImpl planDeselectionExceptionService;

	@Mock
	PlanDeselectionExceptionDao planDeselectionExceptionDao;

	@Mock
	PsCompanyDao psCompanyDao;

	@Mock
	PsDao psDao;

	private static final String EMPLOYEE_ID = "0000000123456";

	private static final long EXCEPTION_ID1 = 1L;
	private static final long EXCEPTION_ID2 = 2L;

	private static final String COMPANY_CODE1 = "TA4";
	private static final String COMPANY_CODE2 = "SYJ";

	private static final String COMPANY_NAME1 = "Company1 Inc.";
	private static final String COMPANY_NAME2 = "Company2 Inc.";

	private static final String COMPANY_QUARTER1 = "SM";
	private static final String COMPANY_QUARTER2 = "SY";

	private static final String APPROVER_ID1 = "00002222233";
	private static final String APPROVER_ID2 = "00002222234";

	private static final String APPROVER_NAME1 = "John Coleman";
	private static final String APPROVER_NAME2 = "Tom Brady";

	private static final String CREATED_BY_ID1 = "00002222235";
	private static final String CREATED_BY_ID2 = "00002222236";

	private static final String CREATED_BY_NAME1 = "Chris Walton";
	private static final String CREATED_BY_NAME2 = "Alex Jou";

	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

	@SuppressWarnings("unchecked")
	private ArgumentCaptor<Set<String>> emplIdsArgCaptor = ArgumentCaptor.forClass(Set.class);

	@SuppressWarnings("unchecked")
	private ArgumentCaptor<Set<String>> compIdsArgCaptor = ArgumentCaptor.forClass(Set.class);

	private ArgumentCaptor<PlanDeselectionExceptions> planDeselectionExceptionsArgCaptor = ArgumentCaptor
			.forClass(PlanDeselectionExceptions.class);

	private ArgumentCaptor<String> companyCodeArgCaptor = ArgumentCaptor.forClass(String.class);

	private ArgumentCaptor<String> quarterArgCaptor = ArgumentCaptor.forClass(String.class);

	private ArgumentCaptor<Boolean> activeArgCaptor = ArgumentCaptor.forClass(Boolean.class);

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

    private MockedStatic<BSSSecurityUtils> bssApplicationConstantsMockedStatic;

    @Before
    public void setUp() {
        bssApplicationConstantsMockedStatic = mockStatic(BSSSecurityUtils.class);
        when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(EMPLOYEE_ID);

    }

    @After
    public void tearDown() {
        bssApplicationConstantsMockedStatic.close();
    }


	@Test
	public void findAllActiveWithRecordsTest() throws ParseException {
		// given
		Set<PlanDeselectionExceptions> expectedResult = prepareEntities();
		when(planDeselectionExceptionDao.findByActive(true)).thenReturn(expectedResult);
		when(psDao.getEmployeesFullName(emplIdsArgCaptor.capture())).thenReturn(prepareEmpNames());
		when(psCompanyDao.findCompaniesNames(compIdsArgCaptor.capture())).thenReturn(prepareCompaniesNames());

		// when
		Set<PlanDeselectionExceptionResDto> actualResult = planDeselectionExceptionService.findAllActive();

		// then
		assertEquals(2, actualResult.size());

		PlanDeselectionExceptions entity1 = getEntityByExcpetionId(expectedResult, EXCEPTION_ID1);
		PlanDeselectionExceptionResDto dto1 = getDtoByExcpetionId(actualResult, EXCEPTION_ID1);

		assertEquals(entity1.getId(), dto1.getId());
		assertEquals(entity1.getCompanyCode(), dto1.getCompanyCode());
		assertEquals(COMPANY_NAME1, dto1.getCompanyName());
		assertEquals(entity1.getStartDate(), dto1.getStartDate());
		assertEquals(entity1.getEndDate(), dto1.getEndDate());
		assertEquals(entity1.getApproverId(), dto1.getApproverId());
		assertEquals(APPROVER_NAME1, dto1.getApproverName());
		assertEquals(entity1.getCreateTime(), dto1.getCreateTime());
		assertEquals(BenExchngEnums.TRINET_II.getBenExchng(), dto1.getExchange());
		assertEquals(BenExchngEnums.TRINET_II.getId(), dto1.getRealmId());
		assertEquals(entity1.getQuarter(), dto1.getQuarter());

		PlanDeselectionExceptions entity2 = getEntityByExcpetionId(expectedResult, EXCEPTION_ID2);
		PlanDeselectionExceptionResDto dto2 = getDtoByExcpetionId(actualResult, EXCEPTION_ID2);

		assertEquals(entity2.getId(), dto2.getId());
		assertEquals(entity2.getCompanyCode(), dto2.getCompanyCode());
		assertEquals(COMPANY_NAME2, dto2.getCompanyName());
		assertEquals(entity2.getStartDate(), dto2.getStartDate());
		assertEquals(entity2.getEndDate(), dto2.getEndDate());
		assertEquals(entity2.getApproverId(), dto2.getApproverId());
		assertEquals(APPROVER_NAME2, dto2.getApproverName());
		assertEquals(entity2.getCreateTime(), dto2.getCreateTime());
		assertEquals(BenExchngEnums.TRINET_II.getBenExchng(), dto2.getExchange());
		assertEquals(BenExchngEnums.TRINET_II.getId(), dto2.getRealmId());
		assertEquals(entity2.getQuarter(), dto2.getQuarter());

		verify(psDao, times(1)).getEmployeesFullName(emplIdsArgCaptor.capture());
		verify(psCompanyDao, times(1)).findCompaniesNames(compIdsArgCaptor.capture());
		verify(planDeselectionExceptionDao, times(1)).findByActive(true);
	}

	@Test
	public void findAllActiveWithEmptyRecordsTest() {
		// given
		Set<PlanDeselectionExceptions> expectedResult = Collections.emptySet();
		when(planDeselectionExceptionDao.findByActive(true)).thenReturn(expectedResult);

		// when
		Set<PlanDeselectionExceptionResDto> actualResult = planDeselectionExceptionService.findAllActive();

		// then
		assertEquals(0, actualResult.size());

		verify(psDao, times(0)).getEmployeesFullName(emplIdsArgCaptor.capture());
		verify(psCompanyDao, times(0)).findCompaniesNames(compIdsArgCaptor.capture());
		verify(planDeselectionExceptionDao, times(1)).findByActive(true);
	}

	@Test
	public void findByIdWithRecordTest() throws ParseException {
		// given
		PlanDeselectionExceptions expectedResult = prepareEntity1();
		when(planDeselectionExceptionDao.findById(EXCEPTION_ID1)).thenReturn(expectedResult);
		when(psDao.getEmployeesFullName(emplIdsArgCaptor.capture())).thenReturn(prepareEmpNames());
		when(psCompanyDao.findCompaniesNames(compIdsArgCaptor.capture())).thenReturn(prepareCompaniesNames());

		// when
		PlanDeselectionExceptionResDto actualResult = planDeselectionExceptionService.findById(EXCEPTION_ID1);

		// then
		assertNotNull(actualResult);
		assertEquals(expectedResult.getCompanyCode(), actualResult.getCompanyCode());
		assertEquals(COMPANY_NAME1, actualResult.getCompanyName());
		assertEquals(expectedResult.getStartDate(), actualResult.getStartDate());
		assertEquals(expectedResult.getEndDate(), actualResult.getEndDate());
		assertEquals(expectedResult.getApproverId(), actualResult.getApproverId());
		assertEquals(APPROVER_NAME1, actualResult.getApproverName());
		assertEquals(expectedResult.getCreateTime(), actualResult.getCreateTime());
		assertEquals(BenExchngEnums.TRINET_II.getBenExchng(), actualResult.getExchange());
		assertEquals(BenExchngEnums.TRINET_II.getId(), actualResult.getRealmId());
		assertEquals(expectedResult.getQuarter(), actualResult.getQuarter());

		verify(psDao, times(1)).getEmployeesFullName(emplIdsArgCaptor.capture());
		verify(psCompanyDao, times(1)).findCompaniesNames(compIdsArgCaptor.capture());
		verify(planDeselectionExceptionDao, times(1)).findById(EXCEPTION_ID1);
	}

	@Test
	public void createExceptionForTrinetIITest() throws ParseException {
		// given
		PlanDeselectionExceptionResDto planDeselectionExceptionResDto = prepareDto1();
		Company company = prepareCompany1();
		PlanDeselectionExceptions planDeselectionExceptions = prepareEntity1();
		when(psDao.getEmployeesFullName(emplIdsArgCaptor.capture())).thenReturn(prepareEmpNames());
		when(psCompanyDao.findCompaniesNames(compIdsArgCaptor.capture())).thenReturn(prepareCompaniesNames());
		when(psCompanyDao.getBasicCompanyDetails(planDeselectionExceptionResDto.getCompanyCode())).thenReturn(company);
		when(planDeselectionExceptionDao.findByActiveAndCompanyCodeAndQuarter(activeArgCaptor.capture(),
				companyCodeArgCaptor.capture(), quarterArgCaptor.capture())).thenReturn(Collections.emptySet());
		when(planDeselectionExceptionDao.save(planDeselectionExceptionsArgCaptor.capture()))
				.thenReturn(planDeselectionExceptions);

		// when
		PlanDeselectionExceptionResDto actualResult = planDeselectionExceptionService
				.create(planDeselectionExceptionResDto);

		// then
		assertNotNull(actualResult);
		assertEquals(planDeselectionExceptionResDto.getCompanyCode(), actualResult.getCompanyCode());
		assertEquals(COMPANY_NAME1, actualResult.getCompanyName());
		assertEquals(planDeselectionExceptionResDto.getStartDate(), actualResult.getStartDate());
		assertEquals(planDeselectionExceptionResDto.getEndDate(), actualResult.getEndDate());
		assertEquals(planDeselectionExceptionResDto.getApproverId(), actualResult.getApproverId());
		assertEquals(APPROVER_NAME1, actualResult.getApproverName());
		assertEquals(BenExchngEnums.TRINET_II.getBenExchng(), actualResult.getExchange());
		assertEquals(BenExchngEnums.TRINET_II.getId(), actualResult.getRealmId());
		assertEquals(planDeselectionExceptionResDto.getQuarter(), actualResult.getQuarter());

		verify(psDao, times(1)).getEmployeesFullName(emplIdsArgCaptor.capture());
		verify(psCompanyDao, times(1)).findCompaniesNames(compIdsArgCaptor.capture());
		verify(psCompanyDao, times(1)).getBasicCompanyDetails(planDeselectionExceptionResDto.getCompanyCode());
		verify(planDeselectionExceptionDao, times(1)).findByActiveAndCompanyCodeAndQuarter(activeArgCaptor.capture(),
				companyCodeArgCaptor.capture(), quarterArgCaptor.capture());
		verify(planDeselectionExceptionDao, times(1)).save(planDeselectionExceptionsArgCaptor.capture());
	}

	@Test
	public void createExceptionForTrinetIIDateOverlappingTest() throws ParseException {
		// given
		PlanDeselectionExceptionResDto planDeselectionExceptionResDto = prepareDto1();
		Company company = prepareCompany1();
		PlanDeselectionExceptions planDeselectionExceptions = prepareEntity1();
		when(psCompanyDao.getBasicCompanyDetails(planDeselectionExceptionResDto.getCompanyCode())).thenReturn(company);
		Set<PlanDeselectionExceptions> planDeselectionExceptionsSet = new HashSet<>();
		planDeselectionExceptionsSet.add(planDeselectionExceptions);
		when(planDeselectionExceptionDao.findByActiveAndCompanyCodeAndQuarter(activeArgCaptor.capture(),
				companyCodeArgCaptor.capture(), quarterArgCaptor.capture())).thenReturn(planDeselectionExceptionsSet);

		// when
		try {
			planDeselectionExceptionService.create(planDeselectionExceptionResDto);
		} catch (BSSApplicationException e) {
			// then
//			assertEquals("Start date 2022-09-15 or End date 2022-09-20 can't fall in between existing dates.",
//					e.getMessage());
		}

		verify(psDao, times(0)).getEmployeesFullName(emplIdsArgCaptor.capture());
		verify(psCompanyDao, times(0)).findCompaniesNames(compIdsArgCaptor.capture());
		verify(psCompanyDao, times(1)).getBasicCompanyDetails(planDeselectionExceptionResDto.getCompanyCode());
		verify(planDeselectionExceptionDao, times(1)).findByActiveAndCompanyCodeAndQuarter(activeArgCaptor.capture(),
				companyCodeArgCaptor.capture(), quarterArgCaptor.capture());
		verify(planDeselectionExceptionDao, times(0)).save(planDeselectionExceptionsArgCaptor.capture());
	}

	@Test
	public void createExceptionForNonTrinetIITest() throws ParseException {
		// given
		Company company = prepareNonTrinetCompanyCompany1();
		PlanDeselectionExceptionResDto planDeselectionExceptionResDto = prepareDto1();
		when(psCompanyDao.getBasicCompanyDetails(planDeselectionExceptionResDto.getCompanyCode())).thenReturn(company);

		// when
		try {
			planDeselectionExceptionService.create(planDeselectionExceptionResDto);
		} catch (BSSApplicationException e) {
			// then
			assertEquals("Exceptions are allowed only for TriNet II exchange companies.", e.getMessage());
		}
	}

	@Test
	public void updateTest() throws ParseException {
		// given
		PlanDeselectionExceptionResDto planDeselectionExceptionResDto = prepareUpdateDto1();
		PlanDeselectionExceptions planDeselectionExceptions = prepareUpdateEntity1();
		when(psDao.getEmployeesFullName(emplIdsArgCaptor.capture())).thenReturn(prepareEmpNames());
		when(psCompanyDao.findCompaniesNames(compIdsArgCaptor.capture())).thenReturn(prepareCompaniesNames());
		when(planDeselectionExceptionDao.save(planDeselectionExceptionsArgCaptor.capture()))
				.thenReturn(planDeselectionExceptions);
		when(planDeselectionExceptionDao.findById(EXCEPTION_ID1)).thenReturn(planDeselectionExceptions);

		// when
		PlanDeselectionExceptionResDto actualResult = planDeselectionExceptionService
				.update(planDeselectionExceptionResDto);

		// then
		assertNotNull(actualResult);
		assertEquals(planDeselectionExceptionResDto.getCompanyCode(), actualResult.getCompanyCode());
		assertEquals(COMPANY_NAME1, actualResult.getCompanyName());
		assertEquals(planDeselectionExceptionResDto.getStartDate(), actualResult.getStartDate());
		assertEquals(planDeselectionExceptionResDto.getEndDate(), actualResult.getEndDate());
		assertEquals(planDeselectionExceptionResDto.getApproverId(), actualResult.getApproverId());
		assertEquals(APPROVER_NAME2, actualResult.getApproverName());
		assertEquals(BenExchngEnums.TRINET_II.getBenExchng(), actualResult.getExchange());
		assertEquals(BenExchngEnums.TRINET_II.getId(), actualResult.getRealmId());
		assertEquals(planDeselectionExceptionResDto.getQuarter(), actualResult.getQuarter());

		verify(psDao, times(1)).getEmployeesFullName(emplIdsArgCaptor.capture());
		verify(psCompanyDao, times(1)).findCompaniesNames(compIdsArgCaptor.capture());
		verify(planDeselectionExceptionDao, times(1)).findByActiveAndCompanyCodeAndQuarter(activeArgCaptor.capture(),
				companyCodeArgCaptor.capture(), quarterArgCaptor.capture());
		verify(planDeselectionExceptionDao, times(1)).save(planDeselectionExceptionsArgCaptor.capture());
	}

	private Set<PlanDeselectionExceptions> prepareEntities() throws ParseException {
		Set<PlanDeselectionExceptions> entities = new HashSet<>();
		entities.add(prepareEntity1());
		entities.add(prepareEntity2());
		return entities;
	}

	private PlanDeselectionExceptions prepareEntity1() throws ParseException {
		PlanDeselectionExceptions planDeselectionExceptions1 = new PlanDeselectionExceptions();
		planDeselectionExceptions1.setId(EXCEPTION_ID1);
		planDeselectionExceptions1.setCompanyCode(COMPANY_CODE1);
		planDeselectionExceptions1.setQuarter(COMPANY_QUARTER1);
		planDeselectionExceptions1.setStartDate(simpleDateFormat.parse("15-09-2022"));
		planDeselectionExceptions1.setEndDate(simpleDateFormat.parse("20-09-2022"));
		planDeselectionExceptions1.setApproverId(APPROVER_ID1);
		planDeselectionExceptions1.setCreateTime(simpleDateFormat.parse("14-09-2022"));
		planDeselectionExceptions1.setCreatedById(CREATED_BY_ID1);
		return planDeselectionExceptions1;
	}

	private PlanDeselectionExceptions prepareEntity2() throws ParseException {
		PlanDeselectionExceptions planDeselectionExceptions2 = new PlanDeselectionExceptions();
		planDeselectionExceptions2.setId(EXCEPTION_ID2);
		planDeselectionExceptions2.setCompanyCode(COMPANY_CODE2);
		planDeselectionExceptions2.setQuarter(COMPANY_QUARTER2);
		planDeselectionExceptions2.setStartDate(simpleDateFormat.parse("15-10-2022"));
		planDeselectionExceptions2.setEndDate(simpleDateFormat.parse("20-10-2022"));
		planDeselectionExceptions2.setApproverId(APPROVER_ID2);
		planDeselectionExceptions2.setCreateTime(simpleDateFormat.parse("14-10-2022"));
		planDeselectionExceptions2.setCreatedById(CREATED_BY_ID2);
		return planDeselectionExceptions2;
	}

	private Map<String, String> prepareEmpNames() {
		Map<String, String> emplNames = new HashMap<>();
		emplNames.put(APPROVER_ID1, APPROVER_NAME1);
		emplNames.put(APPROVER_ID2, APPROVER_NAME2);
		emplNames.put(CREATED_BY_ID1, CREATED_BY_NAME1);
		emplNames.put(CREATED_BY_ID2, CREATED_BY_NAME2);
		return emplNames;
	}

	private Map<String, String> prepareCompaniesNames() {
		Map<String, String> compNames = new HashMap<>();
		compNames.put(COMPANY_CODE1, COMPANY_NAME1);
		compNames.put(COMPANY_CODE2, COMPANY_NAME2);
		return compNames;
	}

	private PlanDeselectionExceptionResDto getDtoByExcpetionId(Set<PlanDeselectionExceptionResDto> dtos,
			long exceptionId) {
		return dtos.stream().filter(dto -> dto.getId() == exceptionId).findFirst().orElseThrow();
	}

	private PlanDeselectionExceptions getEntityByExcpetionId(Set<PlanDeselectionExceptions> entities,
			long exceptionId) {
		return entities.stream().filter(entity -> entity.getId() == exceptionId).findFirst().orElseThrow();
	}

	private PlanDeselectionExceptionResDto prepareDto1() throws ParseException {
		PlanDeselectionExceptionResDto planDeselectionExceptionResDt1 = new PlanDeselectionExceptionResDto();
		planDeselectionExceptionResDt1.setCompanyCode(COMPANY_CODE1);
		planDeselectionExceptionResDt1.setStartDate(simpleDateFormat.parse("15-09-2022"));
		planDeselectionExceptionResDt1.setEndDate(simpleDateFormat.parse("20-09-2022"));
		planDeselectionExceptionResDt1.setApproverId(APPROVER_ID1);
		return planDeselectionExceptionResDt1;
	}

	private Company prepareCompany1() throws ParseException {
		Company company1 = new Company();
		company1.setCode(COMPANY_CODE1);
		company1.setQuater(COMPANY_QUARTER1);
		Realm realm = new Realm();
		realm.setId(BenExchngEnums.TRINET_II.getId());
		company1.setRealm(realm);
		return company1;
	}

	private Company prepareNonTrinetCompanyCompany1() throws ParseException {
		Company company1 = new Company();
		company1.setCode(COMPANY_CODE1);
		company1.setQuater(COMPANY_QUARTER1);
		Realm realm = new Realm();
		realm.setId(BenExchngEnums.TRINET_IV.getId());
		company1.setRealm(realm);
		return company1;
	}

	private PlanDeselectionExceptionResDto prepareUpdateDto1() throws ParseException {
		PlanDeselectionExceptionResDto planDeselectionExceptionResDto1 = new PlanDeselectionExceptionResDto();
		planDeselectionExceptionResDto1.setId(EXCEPTION_ID1);
		planDeselectionExceptionResDto1.setCompanyCode(COMPANY_CODE1);
		planDeselectionExceptionResDto1.setStartDate(simpleDateFormat.parse("16-09-2022"));
		planDeselectionExceptionResDto1.setEndDate(simpleDateFormat.parse("21-09-2022"));
		planDeselectionExceptionResDto1.setApproverId(APPROVER_ID2);
		return planDeselectionExceptionResDto1;
	}

	private PlanDeselectionExceptions prepareUpdateEntity1() throws ParseException {
		PlanDeselectionExceptions planDeselectionExceptions1 = new PlanDeselectionExceptions();
		planDeselectionExceptions1.setId(EXCEPTION_ID1);
		planDeselectionExceptions1.setCompanyCode(COMPANY_CODE1);
		planDeselectionExceptions1.setQuarter(COMPANY_QUARTER1);
		planDeselectionExceptions1.setStartDate(simpleDateFormat.parse("16-09-2022"));
		planDeselectionExceptions1.setEndDate(simpleDateFormat.parse("21-09-2022"));
		planDeselectionExceptions1.setApproverId(APPROVER_ID2);
		planDeselectionExceptions1.setCreateTime(simpleDateFormat.parse("14-09-2022"));
		planDeselectionExceptions1.setCreatedById(CREATED_BY_ID1);
		return planDeselectionExceptions1;
	}

}
