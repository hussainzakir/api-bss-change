package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.MinFundExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.MinimumFundingException;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.service.ExceptionAttributeService;
import com.trinet.ambis.service.impl.MinFundExceptionServiceImpl;
import com.trinet.ambis.service.model.AttributeDto;
import com.trinet.ambis.service.model.AttributeValueDto;
import com.trinet.ambis.service.model.ExceptionAttributeDto;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.service.model.ProductQuarters;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.Utils;
import com.trinet.common.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class MinFundExceptionServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	MinFundExceptionServiceImpl minFundExceptionService;

	@Mock
	MinFundExceptionDao minFundExceptionDao;

	@Mock
	PsCompanyDao psCompanyDao;

	@Mock
	PsDao psDao;
	
	@Mock
	ExceptionAttributeService exceptionAttributeService;
	
	@Mock
	RealmDataDao realmDataDao;
	
	public @Rule
	ExpectedException expectedException = ExpectedException.none();


	private static final long MIN_FUND_EXCEPTION_ID1 = 1111L;
	private static final long MIN_FUND_EXCEPTION_ID2 = 2222L;
	private static final long MIN_FUND_EXCEPTION_ID3 = 3333L;
	private static final long NEWLY_CREATED_MIN_FUND_EXCEPTION_ID = 3333L;
	private static final String COMPANY_CODE1 = "G48";
	private static final String COMPANY_CODE2 = "SCL";
	private static final String COMPANY_NAME1 = "Company1 Inc.";
	private static final String COMPANY_NAME2 = "Company2 Inc.";
	private static final String APPROVER_ID1 = "00002222233";
	private static final String APPROVER_ID2 = "00002222236";
	private static final String CREATED_BY_ID1 = "00002222234";
	private static final String UPDATED_BY_ID1 = "00002222235";
	private static final String LOGGED_IN_PERSON = "00002222237";
	private static final String LOGGED_IN_PERSON_NAME = "Jim Watson";
	private static final String APPROVER_NAME1 = "John Coleman";
	private static final String APPROVER_NAME2 = "Tom Brady";
	private static final String CREATED_BY_NAME1 = "Chris Walton";
	private static final String UPDATED_BY_NAME1 = "Alex Jou";
	private static final String PASS_EXCHANGE = "PAS";
	private static final long PASS_REALM_ID = 3L;
	private static final String Q1_QUATER = "Q1";
	
	private static final String ERROR_MESSAGE_INVALID_COMPANY_CODE = "Invalid company code";
	private static final String ERROR_MESSAGE_INVALID_PLAN_TYPE = "Invalid plan type";
	private static final String ERROR_MESSAGE_INVALID_FUND_TYPE = "Invalid Fund Type";
	private static final String ERROR_MESSAGE_INVALID_APPROVER_ID = "Invalid approver id";

	@Test
	@SuppressWarnings("unchecked")
	public void findBy() throws Exception {
		long id = MIN_FUND_EXCEPTION_ID3;

		ArgumentCaptor<Set> emplIdsArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Set> compIdsArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(minFundExceptionDao.findById(id)).thenReturn(prepareMinFundExcetionEntity());
		when(psDao.getEmployeesFullName(emplIdsArgCaptor.capture())).thenReturn(prepareEmpNames());
		when(psCompanyDao.findCompaniesNames(compIdsArgCaptor.capture())).thenReturn(prepareCompaniesNames());

		MinFundExceptionDto actualResult = minFundExceptionService.findBy(id);

		assertEquals(id, actualResult.getId());
		assertEquals(APPROVER_NAME1, actualResult.getApproverName());
		assertEquals(CREATED_BY_NAME1, actualResult.getCreatedByName());
		assertEquals(null, actualResult.getLastUpdatedByName());
		assertTrue(emplIdsArgCaptor.getValue().containsAll(Arrays.asList(APPROVER_ID1, CREATED_BY_ID1)));
		assertTrue(compIdsArgCaptor.getValue().containsAll(Arrays.asList(COMPANY_CODE1)));
		assertEquals(COMPANY_NAME1, actualResult.getCompanyName());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findAllActive() throws Exception {
		ArgumentCaptor<Set> emplIdsArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Set> compIdsArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(minFundExceptionDao.findByActive(true)).thenReturn(prepareMinFundExcetions());
		when(psDao.getEmployeesFullName(emplIdsArgCaptor.capture())).thenReturn(prepareEmpNames());
		when(psCompanyDao.findCompaniesNames(compIdsArgCaptor.capture())).thenReturn(prepareCompaniesNames());

		List<MinFundExceptionDto> actualResult = minFundExceptionService.findAllActive();

		assertEquals(2, actualResult.size());
		if (Long.valueOf(MIN_FUND_EXCEPTION_ID1).equals(actualResult.get(0).getId())) {
			assertEquals(APPROVER_NAME1, actualResult.get(0).getApproverName());
			assertEquals(CREATED_BY_NAME1, actualResult.get(0).getCreatedByName());
			assertEquals(null, actualResult.get(0).getLastUpdatedByName());
			assertEquals(null, actualResult.get(0).getLastUpdatedByName());
			assertEquals(COMPANY_NAME1, actualResult.get(0).getCompanyName());
		}
		if (Long.valueOf(MIN_FUND_EXCEPTION_ID2).equals(actualResult.get(1).getId())) {
			assertEquals(APPROVER_NAME2, actualResult.get(1).getApproverName());
			assertEquals(CREATED_BY_NAME1, actualResult.get(1).getCreatedByName());
			assertEquals(UPDATED_BY_NAME1, actualResult.get(1).getLastUpdatedByName());
			assertEquals(COMPANY_NAME2, actualResult.get(1).getCompanyName());
		}
		assertTrue(emplIdsArgCaptor.getValue()
				.containsAll(Arrays.asList(APPROVER_ID1, APPROVER_ID2, CREATED_BY_ID1, UPDATED_BY_ID1)));
		assertTrue(compIdsArgCaptor.getValue().containsAll(Arrays.asList(COMPANY_CODE1, COMPANY_CODE2)));
	}

	@Test
	@Ignore
	@SuppressWarnings("unchecked")
	public void update() throws Exception {
		MinimumFundingException existingEntity = prepareMinFundExcetionEntity();
		ArgumentCaptor<MinimumFundingException> entityCaptor = ArgumentCaptor.forClass(MinimumFundingException.class);

		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(LOGGED_IN_PERSON);
		when(minFundExceptionDao.findById(MIN_FUND_EXCEPTION_ID1)).thenReturn(existingEntity);
		doAnswer(new Answer<MinimumFundingException>() {

			@Override
			public MinimumFundingException answer(InvocationOnMock invocation) throws Throwable {
				entityCaptor.getValue().setId(NEWLY_CREATED_MIN_FUND_EXCEPTION_ID);
				return entityCaptor.getValue();
			}

		}).when(minFundExceptionDao).save(entityCaptor.capture());

		when(minFundExceptionDao.findById(NEWLY_CREATED_MIN_FUND_EXCEPTION_ID)).thenReturn(existingEntity);
		when(psDao.getEmployeesFullName(anySet())).thenReturn(prepareEmpNames());

		MinFundExceptionDto dto = prepareMinFundExceptionDTO();
		dto.setMinFundType("FLT");
		MinFundExceptionDto actualResult = minFundExceptionService.update(dto);

		verify(minFundExceptionDao, times(1)).save(any(MinimumFundingException.class));
		assertEquals(NEWLY_CREATED_MIN_FUND_EXCEPTION_ID, entityCaptor.getValue().getId());
		assertEquals(LOGGED_IN_PERSON, entityCaptor.getValue().getLastUpdatedById());
		assertEquals(APPROVER_ID1, entityCaptor.getValue().getApproverId());
		assertEquals(APPROVER_NAME1, actualResult.getApproverName());
		assertEquals(BigDecimal.valueOf(500.50), entityCaptor.getValue().getMinFundValue());
		assertFalse(existingEntity.isActive());
		assertEquals(PASS_EXCHANGE, actualResult.getExchange());
		verify(minFundExceptionDao, times(1)).findById(NEWLY_CREATED_MIN_FUND_EXCEPTION_ID);

	}

	@Test
	@Ignore
	public void save() throws Exception {
		Company company = prepareCompany();
		ArgumentCaptor<MinimumFundingException> entityCaptor = ArgumentCaptor.forClass(MinimumFundingException.class);

		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(LOGGED_IN_PERSON);
		when(psCompanyDao.getBasicCompanyDetails(COMPANY_CODE1)).thenReturn(company);

		doAnswer(new Answer<MinimumFundingException>() {

			@Override
			public MinimumFundingException answer(InvocationOnMock invocation) throws Throwable {
				entityCaptor.getValue().setId(NEWLY_CREATED_MIN_FUND_EXCEPTION_ID);
				return entityCaptor.getValue();
			}

		}).when(minFundExceptionDao).save(entityCaptor.capture());

		when(minFundExceptionDao.findById(NEWLY_CREATED_MIN_FUND_EXCEPTION_ID))
				.thenReturn(prepareMinFundExcetionEntity());
		when(psDao.getEmployeesFullName(anySet())).thenReturn(prepareEmpNames());

		MinFundExceptionDto actualResult = minFundExceptionService.save(prepareMinFundExceptionDTO());

		verify(minFundExceptionDao, times(1)).save(any(MinimumFundingException.class));
		assertEquals(NEWLY_CREATED_MIN_FUND_EXCEPTION_ID, entityCaptor.getValue().getId());
		assertEquals(LOGGED_IN_PERSON, entityCaptor.getValue().getCreatedById());
		assertEquals(APPROVER_ID1, entityCaptor.getValue().getApproverId());
		assertEquals(CREATED_BY_NAME1, actualResult.getCreatedByName());
		assertEquals(APPROVER_NAME1, actualResult.getApproverName());
		assertNull(entityCaptor.getValue().getLastUpdatedById());
		assertTrue(entityCaptor.getValue().isActive());
		assertEquals(PASS_REALM_ID, entityCaptor.getValue().getRealmId());
		assertEquals(Q1_QUATER, entityCaptor.getValue().getQuarter());
		assertEquals(PASS_EXCHANGE, actualResult.getExchange());
		verify(minFundExceptionDao, times(1)).findById(NEWLY_CREATED_MIN_FUND_EXCEPTION_ID);
	}

	@Test
	public void findActiveByCompanyCodeAndQuarter() throws Exception {
		Company company = prepareCompany();
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		
		when(minFundExceptionDao.findActiveBy(COMPANY_CODE1, "Q1", planStartDate))
				.thenReturn(prepareMinFundExcetions().stream().collect(Collectors.toSet()));

		Set<MinFundExceptionDto> actualResult = minFundExceptionService.findActiveByCompanyCodeAndQuarter(company);

		assertEquals(2, actualResult.size());
	}

	// Start date before end date
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDate_test1() throws Exception {
		Company company = prepareCompany();
		MinimumFundingException existingEntity = prepareMinFundExcetionEntity();
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		MinFundExceptionDto dto = prepareMinFundExceptionDTO();
		dto.setStartDate(DateUtils.getDateFromString("2020-01-02"));
		dto.setEndDate(DateUtils.getDateFromString("2020-01-01"));

		when(psCompanyDao.getBasicCompanyDetails(COMPANY_CODE1)).thenReturn(company);
		when(minFundExceptionDao.findActiveBy(company.getCode(), company.getQuater(), company.getRealm().getId(), "10"))
				.thenReturn(existingEntities);

		minFundExceptionService.save(dto);
	}

	// Start start date equals end date.
	// new start date is between existing start and end date.
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDate_test2() throws Exception {
		Company company = prepareCompany();
		MinimumFundingException existingEntity = prepareMinFundExcetionEntity();
		existingEntity.setStartDate(DateUtils.getDateFromString("2020-01-01"));
		existingEntity.setEndDate(DateUtils.getDateFromString("2020-01-03"));
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		MinFundExceptionDto dto = prepareMinFundExceptionDTO();
		dto.setStartDate(DateUtils.getDateFromString("2020-01-02"));
		dto.setEndDate(DateUtils.getDateFromString("2020-01-04"));

		when(psCompanyDao.getBasicCompanyDetails(COMPANY_CODE1)).thenReturn(company);
		when(minFundExceptionDao.findActiveBy(company.getCode(), company.getQuater(), company.getRealm().getId(), "10"))
				.thenReturn(existingEntities);

		minFundExceptionService.save(dto);
	}

	// new start date is equal to existing start date.
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDate_test3() throws Exception {
		Company company = prepareCompany();
		MinimumFundingException existingEntity = prepareMinFundExcetionEntity();
		existingEntity.setStartDate(DateUtils.getDateFromString("2020-01-01"));
		existingEntity.setEndDate(DateUtils.getDateFromString("2020-01-03"));
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		MinFundExceptionDto dto = prepareMinFundExceptionDTO();
		dto.setStartDate(DateUtils.getDateFromString("2020-01-01"));
		dto.setEndDate(DateUtils.getDateFromString("2020-01-04"));

		when(psCompanyDao.getBasicCompanyDetails(COMPANY_CODE1)).thenReturn(company);
		when(minFundExceptionDao.findActiveBy(company.getCode(), company.getQuater(), company.getRealm().getId(), "10"))
				.thenReturn(existingEntities);

		minFundExceptionService.save(dto);
	}

	// new start date is equal to existing end date.
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDate_test4() throws Exception {
		Company company = prepareCompany();
		MinimumFundingException existingEntity = prepareMinFundExcetionEntity();
		existingEntity.setStartDate(DateUtils.getDateFromString("2020-01-01"));
		existingEntity.setEndDate(DateUtils.getDateFromString("2020-01-03"));
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		MinFundExceptionDto dto = prepareMinFundExceptionDTO();
		dto.setStartDate(DateUtils.getDateFromString("2020-01-03"));
		dto.setEndDate(DateUtils.getDateFromString("2020-01-04"));

		when(psCompanyDao.getBasicCompanyDetails(COMPANY_CODE1)).thenReturn(company);
		when(minFundExceptionDao.findActiveBy(company.getCode(), company.getQuater(), company.getRealm().getId(), "10"))
				.thenReturn(existingEntities);

		minFundExceptionService.save(dto);
	}

	// new end date is between existing start and end dt
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDate_test5() throws Exception {
		Company company = prepareCompany();
		MinimumFundingException existingEntity = prepareMinFundExcetionEntity();
		existingEntity.setStartDate(DateUtils.getDateFromString("2020-01-01"));
		existingEntity.setEndDate(DateUtils.getDateFromString("2020-01-03"));
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		MinFundExceptionDto dto = prepareMinFundExceptionDTO();
		dto.setStartDate(DateUtils.getDateFromString("2019-12-28"));
		dto.setEndDate(DateUtils.getDateFromString("2020-01-02"));

		when(psCompanyDao.getBasicCompanyDetails(COMPANY_CODE1)).thenReturn(company);
		when(minFundExceptionDao.findActiveBy(company.getCode(), company.getQuater(), company.getRealm().getId(), "10"))
				.thenReturn(existingEntities);

		minFundExceptionService.save(dto);
	}

	// new end date is equals to existing start dt
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDate_test6() throws Exception {
		Company company = prepareCompany();
		MinimumFundingException existingEntity = prepareMinFundExcetionEntity();
		existingEntity.setStartDate(DateUtils.getDateFromString("2020-01-01"));
		existingEntity.setEndDate(DateUtils.getDateFromString("2020-01-03"));
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		MinFundExceptionDto dto = prepareMinFundExceptionDTO();
		dto.setStartDate(DateUtils.getDateFromString("2019-12-28"));
		dto.setEndDate(DateUtils.getDateFromString("2020-01-01"));

		when(psCompanyDao.getBasicCompanyDetails(COMPANY_CODE1)).thenReturn(company);
		when(minFundExceptionDao.findActiveBy(company.getCode(), company.getQuater(), company.getRealm().getId(), "10"))
				.thenReturn(existingEntities);

		minFundExceptionService.save(dto);
	}

	// new end date is equals to existing end dt
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDate_test7() throws Exception {
		Company company = prepareCompany();
		MinimumFundingException existingEntity = prepareMinFundExcetionEntity();
		existingEntity.setStartDate(DateUtils.getDateFromString("2020-01-01"));
		existingEntity.setEndDate(DateUtils.getDateFromString("2020-01-03"));
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		MinFundExceptionDto dto = prepareMinFundExceptionDTO();
		dto.setStartDate(DateUtils.getDateFromString("2019-12-28"));
		dto.setEndDate(DateUtils.getDateFromString("2020-01-03"));

		when(psCompanyDao.getBasicCompanyDetails(COMPANY_CODE1)).thenReturn(company);
		when(minFundExceptionDao.findActiveBy(company.getCode(), company.getQuater(), company.getRealm().getId(), "10"))
				.thenReturn(existingEntities);

		minFundExceptionService.save(dto);
	}

	// new start and end date is between existing start and end date
	@Test(expected = BSSApplicationException.class)
	public void validateStartAndEndDate_test8() throws Exception {
		Company company = prepareCompany();
		MinimumFundingException existingEntity = prepareMinFundExcetionEntity();
		existingEntity.setStartDate(DateUtils.getDateFromString("2020-01-01"));
		existingEntity.setEndDate(DateUtils.getDateFromString("2020-01-04"));
		Set<MinimumFundingException> existingEntities = new HashSet<>();
		existingEntities.add(existingEntity);

		MinFundExceptionDto dto = prepareMinFundExceptionDTO();
		dto.setStartDate(DateUtils.getDateFromString("2020-01-02"));
		dto.setEndDate(DateUtils.getDateFromString("2020-01-03"));

		when(psCompanyDao.getBasicCompanyDetails(COMPANY_CODE1)).thenReturn(company);
		when(minFundExceptionDao.findActiveBy(company.getCode(), company.getQuater(), company.getRealm().getId(), "10"))
				.thenReturn(existingEntities);

		minFundExceptionService.save(dto);
	}

	private MinFundExceptionDto prepareMinFundExceptionDTO() {
		MinFundExceptionDto dto = new MinFundExceptionDto();
		dto.setId(MIN_FUND_EXCEPTION_ID1);
		dto.setApproverId(APPROVER_ID1);
		dto.setCompanyCode(COMPANY_CODE1);
		dto.setStartDate(new Date());
		dto.setEndDate(new Date());
		dto.setMinFundValue(BigDecimal.valueOf(500.50));
		dto.setPlanType("10");
		return dto;
	}

	private MinimumFundingException prepareMinFundExcetionEntity() {
		MinimumFundingException minFundException = new MinimumFundingException();
		minFundException.setId(MIN_FUND_EXCEPTION_ID3);
		minFundException.setApproverId(APPROVER_ID1);
		minFundException.setLastUpdatedById(null);
		minFundException.setCreatedById(CREATED_BY_ID1);
		minFundException.setMinFundValue(BigDecimal.valueOf(500));
		minFundException.setCompanyCode(COMPANY_CODE1);
		minFundException.setStartDate(new Date());
		minFundException.setEndDate(DateUtils.addDays(new Date(), 31));
		minFundException.setRealmId(PASS_REALM_ID);
		return minFundException;
	}

	private Map<String, String> prepareEmpNames() {
		Map<String, String> emplNames = new HashMap<>();
		emplNames.put(APPROVER_ID1, APPROVER_NAME1);
		emplNames.put(APPROVER_ID2, APPROVER_NAME2);
		emplNames.put(UPDATED_BY_ID1, UPDATED_BY_NAME1);
		emplNames.put(CREATED_BY_ID1, CREATED_BY_NAME1);
		emplNames.put(LOGGED_IN_PERSON, LOGGED_IN_PERSON_NAME);
		return emplNames;
	}

	private Map<String, String> prepareCompaniesNames() {
		Map<String, String> compNames = new HashMap<>();
		compNames.put(COMPANY_CODE1, COMPANY_NAME1);
		compNames.put(COMPANY_CODE2, COMPANY_NAME2);
		return compNames;
	}

	private Set<MinimumFundingException> prepareMinFundExcetions() {
		MinimumFundingException minFundException1 = new MinimumFundingException();
		minFundException1.setId(MIN_FUND_EXCEPTION_ID1);
		minFundException1.setPlanType("10");
		minFundException1.setApproverId(APPROVER_ID1);
		minFundException1.setLastUpdatedById(null);
		minFundException1.setCreatedById(CREATED_BY_ID1);
		minFundException1.setMinFundValue(BigDecimal.valueOf(500));
		minFundException1.setCompanyCode(COMPANY_CODE1);
		minFundException1.setRealmId(PASS_REALM_ID);
		MinimumFundingException minFundException2 = new MinimumFundingException();
		minFundException2.setId(MIN_FUND_EXCEPTION_ID2);
		minFundException1.setPlanType("11");
		minFundException2.setApproverId(APPROVER_ID2);
		minFundException2.setCreatedById(CREATED_BY_ID1);
		minFundException2.setLastUpdatedById(UPDATED_BY_ID1);
		minFundException2.setMinFundValue(BigDecimal.valueOf(500));
		minFundException2.setCompanyCode(COMPANY_CODE2);
		minFundException2.setRealmId(PASS_REALM_ID);
		return new HashSet<>(Arrays.asList(minFundException1, minFundException2));
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode(COMPANY_CODE1);
		company.setQuater(Q1_QUATER);
		Realm realm = new Realm();
		realm.setId(PASS_REALM_ID);
		realm.setBenExchange(PASS_EXCHANGE);
		company.setRealm(realm);
		company.setPlanStartDate("01-JAN-2020");
		return company;
	}
	
	@Test
	public void validateRequestDataForInvalidPlanType()throws Exception  {
		// given
		MinFundExceptionDto dto = prepareInvalidMinFundExceptionDTO();
		// when
		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_PLAN_TYPE);
		dto.setPlanType(dto.getPlanType().concat("$"));
		minFundExceptionService.save(dto);
	}
	
	@Test
	public void validateRequestDataForInvalidFundType()throws Exception  {
		// given
		MinFundExceptionDto dto = prepareInvalidMinFundExceptionDTO();
		// when
		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_FUND_TYPE);
		dto.setMinFundType(dto.getMinFundType().concat("$"));
		minFundExceptionService.save(dto);
	}

	@Test
	public void validateRequestDataForInvalidApproverId()throws Exception  {
		// given
		MinFundExceptionDto dto = prepareInvalidMinFundExceptionDTO();
		// when
		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_APPROVER_ID);
		dto.setApproverId(dto.getApproverId().concat("$"));
		minFundExceptionService.save(dto);
	}

	@Test
	public void validateUpdateRequestDataForInvalidCompanyCode()throws Exception  {
		// given
		MinFundExceptionDto dto = prepareInvalidMinFundExceptionDTO();
		// when
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_COMPANY_CODE);
		dto.setCompanyCode(dto.getCompanyCode().concat("$"));
		minFundExceptionService.update(dto);
	}
	
	@Test
	public void validateUpdateRequestDataForInvalidPlanType()throws Exception  {
		// given
		MinFundExceptionDto dto = prepareInvalidMinFundExceptionDTO();
		// when
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_PLAN_TYPE);
		dto.setPlanType(dto.getPlanType().concat("$"));
		minFundExceptionService.update(dto);
	}

	@Test
	public void validateUpdateRequestDataForInvalidFundType()throws Exception  {
		// given
		MinFundExceptionDto dto = prepareInvalidMinFundExceptionDTO();
		// when
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_FUND_TYPE);
		dto.setMinFundType(dto.getMinFundType().concat("$"));
		minFundExceptionService.update(dto);
	}

	@Test
	public void validateUpdateRequestDataForInvalidApproverId()throws Exception  {
		// given
		MinFundExceptionDto dto = prepareInvalidMinFundExceptionDTO();
		// when
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_APPROVER_ID);
		dto.setApproverId(dto.getApproverId().concat("$"));
		minFundExceptionService.update(dto);
	}
	
	private MinFundExceptionDto prepareInvalidMinFundExceptionDTO() {
		MinFundExceptionDto dto = new MinFundExceptionDto();
		dto.setId(MIN_FUND_EXCEPTION_ID1);
		dto.setApproverId(APPROVER_ID1);
		dto.setCompanyCode(COMPANY_CODE1);
		dto.setMinFundType("PCT");
		dto.setStartDate(new Date());
		dto.setEndDate(new Date());
		dto.setMinFundValue(BigDecimal.valueOf(500.50));
		dto.setPlanType("10");
		return dto;
	}
	
	private List<ExceptionAttributeDto> prepareExceptionAttributes() {
		List<ExceptionAttributeDto> exceptionAttributeDtos = new ArrayList<>();
		ExceptionAttributeDto minFundExceptionAttributeDto = new ExceptionAttributeDto();
		minFundExceptionAttributeDto.setExceptionId(1L);
		minFundExceptionAttributeDto.setExceptionName("MIN FUNDING EXCEPTION");
		List<AttributeDto> minFundAttributeDtos = new ArrayList<>();
		minFundAttributeDtos.add(prepareAttributeDto(1L, "PLAN TYPE", "10", "medical"));
		minFundAttributeDtos.add(prepareAttributeDto(2L, "APPROVERS", "00002340287", "Cori D Haitham"));
		minFundAttributeDtos.add(prepareAttributeDto(4L, "EXCEPTION VALUE TYPE", "PCT", "PCT"));
		minFundExceptionAttributeDto.setAttributes(minFundAttributeDtos);

		ExceptionAttributeDto benOfferExceptionAttributeDto = new ExceptionAttributeDto();
		benOfferExceptionAttributeDto.setExceptionId(2L);
		benOfferExceptionAttributeDto.setExceptionName("BENEFIT OFFER EXCEPTION");
		
		List<AttributeDto> benOfferAttributeDtos = new ArrayList<>();
		
		benOfferAttributeDtos.add(prepareAttributeDto(1L, "PLAN TYPE", "10", "medical"));
		benOfferAttributeDtos.add(prepareAttributeDto(2L, "APPROVERS", "00002340287", "Cori D Haitham"));
		benOfferAttributeDtos.add(prepareAttributeDto(4L, "EXCEPTION VALUE TYPE", "Not Offered", "Not Offered"));
		benOfferAttributeDtos.add(prepareAttributeDto(3L, "ORIGINATION DEPT", "Sales", "Sales"));
		benOfferExceptionAttributeDto.setAttributes(benOfferAttributeDtos);
		
		exceptionAttributeDtos.add(minFundExceptionAttributeDto);
		exceptionAttributeDtos.add(benOfferExceptionAttributeDto);
		
		return exceptionAttributeDtos;
	}

	private AttributeDto prepareAttributeDto(long attributeId, String name, String attributeValue, String attributeValueNames) {
		AttributeDto attributeDto = new AttributeDto();
		attributeDto.setAttributeId(attributeId);
		attributeDto.setAttributeName(name);
		attributeDto.setValues(prepareAttribteValues(attributeValue, attributeValueNames));
		return attributeDto;
	}
	
	private List<AttributeValueDto> prepareAttribteValues(String value, String name) {
		List<AttributeValueDto> attributeValueDtos = new ArrayList<>();
		AttributeValueDto attributeValueDto = new AttributeValueDto();
		attributeValueDto.setAttributeValue(value);
		attributeValueDto.setName(name);
		attributeValueDtos.add(attributeValueDto);
		return attributeValueDtos;	
	}
	
	private Map<String, ProductQuarters>  prepareProducts() {
		Map<String, ProductQuarters> productQuarters = new HashMap<>();
		String product = "AMB/TriNet IV";
		String quarter = "8Y";
		List<String> quarters = new ArrayList<>();
		quarters.add(quarter);
		ProductQuarters productQuarter = new ProductQuarters();
		productQuarter.setProduct(product);
		productQuarter.setQuarters(quarters);
		productQuarters.put(product, productQuarter);
		return productQuarters;
	}


}