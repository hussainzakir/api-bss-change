package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.trinet.ambis.helper.ExceptionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenOfferExceptionDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.BenefitOfferException;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.impl.BenefitOfferExceptionServiceImpl;
import com.trinet.ambis.service.model.BenOfferExceptionDto;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.Utils;

@RunWith(MockitoJUnitRunner.class)
public class BenefitOfferExceptionServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	BenefitOfferExceptionServiceImpl benOfferExceptionService;

	@Mock
	StrategyService strategyService;

	@Mock
	BenOfferExceptionDao benOfferExceptionDao;

	@Mock
	PsCompanyDao psCompanyDao;

	@Mock
	PsDao psDao;

	private static final long MIN_FUND_EXCEPTION_ID1 = 1111L;
	private static final long MIN_FUND_EXCEPTION_ID2 = 2222L;
	private static final long MIN_FUND_EXCEPTION_ID3 = 3333L;
	private static final long NEWLY_CREATED_BEN_OFFER_EXCEPTION_ID = 3333L;
	private static final String COMPANY_CODE = "G48";
	private static final String COMPANY_CODE2 = "SCL";
	private static final String COMPANY_NAME1 = "Company1 Inc.";
	private static final String COMPANY_NAME2 = "Company2 Inc.";
	private static final String APPROVER_ID1 = "00002222236";
	private static final String APPROVER_ID2 = "00002222233";
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
	private static final String PLAN_START_DT = "01-JAN-2020";


	@Test
	public void findApplicableBy() {
		Company company = prepareCompany();
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);

		when(benOfferExceptionDao.findApplicableBy(company.getCode(), company.getQuater(), company.getRealm().getId(),
				planStartDate)).thenReturn(prepareBenOfferExceptions());

		Map<String, Boolean> actualResult = benOfferExceptionService.findApplicableBy(company);

		assertEquals(3, actualResult.size());
		assertEquals(true, actualResult.get("10"));
		assertEquals(true, actualResult.get("30"));
		assertEquals(true, actualResult.get("31"));
	}

	@Test
	public void findAllActive() {
		Set<String> empIds = new HashSet<>();
		empIds.addAll(Arrays.asList(APPROVER_ID1, APPROVER_ID2, CREATED_BY_ID1));
		Set<String> companyCodes = new HashSet<>();
		companyCodes.addAll(Arrays.asList(COMPANY_CODE));

		when(benOfferExceptionDao.findByActive(true)).thenReturn(prepareBenOfferExceptions());
		when(psDao.getEmployeesFullName(empIds)).thenReturn(prepareEmpNames());
		when(psCompanyDao.findCompaniesNames(companyCodes)).thenReturn(prepareCompanyNames());

		List<BenOfferExceptionDto> actualResult = benOfferExceptionService.findAllActive();

		assertEquals(2, actualResult.size());
		assertEquals(COMPANY_NAME1, actualResult.get(0).getCompanyName());
		assertEquals(COMPANY_NAME1, actualResult.get(1).getCompanyName());
	}

//	@Test
	public void save() {
		Date startDate = Utils.convertStringToDate("01-JAN-2020", Constants.DATE_FORMAT);
		Date endDate = Utils.convertStringToDate("31-DEC-2021", Constants.DATE_FORMAT);
		Set<String> planTypeCodes = new HashSet<>();
		planTypeCodes.add("10");
		Set<String> empIds = new HashSet<>();
		empIds.addAll(Arrays.asList(APPROVER_ID1, CREATED_BY_ID1));
		Set<String> companyCodes = new HashSet<>();
		companyCodes.addAll(Arrays.asList(COMPANY_CODE));
		BenOfferExceptionDto dto = prepareExceptionDto(0L, "10");

		when(psCompanyDao.getBasicCompanyDetails(COMPANY_CODE)).thenReturn(prepareCompany());
		when(benOfferExceptionDao.findActiveBy(COMPANY_CODE, Q1_QUATER, PASS_REALM_ID, "10"))
				.thenReturn(new HashSet<>());
		doNothing().when(strategyService).syncStrategiesForBenOfferException(dto, planTypeCodes);
		when(benOfferExceptionDao.findById(0L)).thenReturn(
				prepareBenOfferException(COMPANY_CODE, "10", false, APPROVER_ID1, CREATED_BY_ID1, startDate, endDate));
		when(psDao.getEmployeesFullName(empIds)).thenReturn(prepareEmpNames());
		when(psCompanyDao.findCompaniesNames(companyCodes)).thenReturn(prepareCompanyNames());

		BenOfferExceptionDto actualResult = benOfferExceptionService.save(dto);

		verify(benOfferExceptionDao, times(1)).save(any(BenefitOfferException.class));
		verify(strategyService, times(1)).syncStrategiesForBenOfferException(dto, planTypeCodes);
		ExceptionServiceHelper.validateStartAndEndDts(dto, new HashSet<>());
		assertEquals(COMPANY_NAME1, actualResult.getCompanyName());
		assertEquals(APPROVER_NAME1, actualResult.getApproverName());
		assertEquals(CREATED_BY_NAME1, actualResult.getCreatedByName());
	}

//	@Test
	public void update() {
		Set<String> empIds = new HashSet<>();
		empIds.addAll(Arrays.asList(APPROVER_ID1, CREATED_BY_ID1));
		Set<String> companyCodes = new HashSet<>();
		companyCodes.addAll(Arrays.asList(COMPANY_CODE));
		Date startDate = Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT);
		Date endDate = Utils.convertStringToDate("29-MAY-2021", Constants.DATE_FORMAT);
		BenOfferExceptionDto dto = prepareExceptionDto(1234L, "DI");
		Set<String> planTypeCodes = new HashSet<>();
		planTypeCodes.add("30");
		planTypeCodes.add("31");
		BenefitOfferException updatedEntity = new BenefitOfferException();
		updatedEntity.setId(NEWLY_CREATED_BEN_OFFER_EXCEPTION_ID);
		updatedEntity.setApproverId(APPROVER_ID1);
		updatedEntity.setCreatedById(CREATED_BY_ID1);
		updatedEntity.setCompanyCode(COMPANY_CODE);
		updatedEntity.setRealmId(PASS_REALM_ID);
		BenefitOfferException existingEntity = prepareBenOfferException(COMPANY_CODE, "11", false, APPROVER_ID2,
				CREATED_BY_ID1, startDate, endDate);

		ArgumentCaptor<BenefitOfferException> entityCaptor = ArgumentCaptor.forClass(BenefitOfferException.class);

		when(benOfferExceptionDao.findById(1234L)).thenReturn(existingEntity);
		doAnswer(new Answer<BenefitOfferException>() {
			@Override
			public BenefitOfferException answer(InvocationOnMock invocation) throws Throwable {
				entityCaptor.getValue().setId(NEWLY_CREATED_BEN_OFFER_EXCEPTION_ID);
				return entityCaptor.getValue();
			}

		}).when(benOfferExceptionDao).save(entityCaptor.capture());
		when(benOfferExceptionDao.findById(NEWLY_CREATED_BEN_OFFER_EXCEPTION_ID)).thenReturn(updatedEntity);
		when(psDao.getEmployeesFullName(empIds)).thenReturn(prepareEmpNames());
		when(psCompanyDao.findCompaniesNames(companyCodes)).thenReturn(prepareCompanyNames());

		benOfferExceptionService.update(dto);

		assertEquals(dto.getApproverId(), entityCaptor.getValue().getApproverId());
		assertEquals(dto.getPlanType(), entityCaptor.getValue().getPlanType());
		assertEquals(dto.getStartDate(), entityCaptor.getValue().getStartDate());
		assertEquals(dto.getEndDate(), entityCaptor.getValue().getEndDate());
		verify(strategyService, times(1)).syncStrategiesForBenOfferException(dto, planTypeCodes);
		ExceptionServiceHelper.validateStartAndEndDts(dto, new HashSet<>());
	}

	@Test
	public void findBy() {
		Date startDate = Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT);
		Date endDate = Utils.convertStringToDate("29-MAY-2021", Constants.DATE_FORMAT);
		BenefitOfferException existingEntity = prepareBenOfferException(COMPANY_CODE, "11", false, APPROVER_ID2,
				CREATED_BY_ID1, startDate, endDate);
		existingEntity.setId(1234L);
		Set<String> empIds = new HashSet<>();
		empIds.addAll(Arrays.asList(APPROVER_ID2, CREATED_BY_ID1));
		Set<String> companyCodes = new HashSet<>();
		companyCodes.addAll(Arrays.asList(COMPANY_CODE));

		when(benOfferExceptionDao.findById(1234L)).thenReturn(existingEntity);
		when(psDao.getEmployeesFullName(empIds)).thenReturn(prepareEmpNames());
		when(psCompanyDao.findCompaniesNames(companyCodes)).thenReturn(prepareCompanyNames());

		BenOfferExceptionDto actualResult = benOfferExceptionService.findBy(1234L);

		assertEquals(APPROVER_NAME2, actualResult.getApproverName());
		assertEquals(CREATED_BY_NAME1, actualResult.getCreatedByName());
		assertEquals("TriNet III", actualResult.getExchange());
	}

	@Test
	public void applyExceptionTestMedical() {
		Company company = prepareCompany();
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		Map<String, Set<StateBenefitPlan>> planRates = new HashMap<>();
		planRates.put("10", new HashSet<>());
		planRates.put("11", new HashSet<>());
		planRates.put("14", new HashSet<>());
		planRates.put("23", new HashSet<>());
		planRates.put("30", new HashSet<>());
		Set<BenefitOfferException> applicableException = new HashSet<>();

		Date startDt = Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("29-MAY-2021", Constants.DATE_FORMAT);
		applicableException
				.add(prepareBenOfferException(COMPANY_CODE, "10", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));

		when(benOfferExceptionDao.findApplicableBy(company.getCode(), company.getQuater(), company.getRealm().getId(),
				planStartDate)).thenReturn(applicableException);

		benOfferExceptionService.applyException(company, planRates);

		assertEquals(4, planRates.size());
		assertNull(planRates.get("10"));
	}

	@Test
	public void applyExceptionTestDental() {
		Company company = prepareCompany();
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		Map<String, Set<StateBenefitPlan>> planRates = new HashMap<>();
		planRates.put("10", new HashSet<>());
		planRates.put("11", new HashSet<>());
		planRates.put("14", new HashSet<>());
		planRates.put("23", new HashSet<>());
		planRates.put("30", new HashSet<>());
		Set<BenefitOfferException> applicableException = new HashSet<>();

		Date startDt = Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("29-MAY-2021", Constants.DATE_FORMAT);
		applicableException
				.add(prepareBenOfferException(COMPANY_CODE, "11", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));

		when(benOfferExceptionDao.findApplicableBy(company.getCode(), company.getQuater(), company.getRealm().getId(),
				planStartDate)).thenReturn(applicableException);

		benOfferExceptionService.applyException(company, planRates);

		assertEquals(4, planRates.size());
		assertNull(planRates.get("11"));
	}

	@Test
	public void applyExceptionTestVision() {
		Company company = prepareCompany();
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		Map<String, Set<StateBenefitPlan>> planRates = new HashMap<>();
		planRates.put("10", new HashSet<>());
		planRates.put("11", new HashSet<>());
		planRates.put("14", new HashSet<>());
		planRates.put("23", new HashSet<>());
		planRates.put("30", new HashSet<>());
		Set<BenefitOfferException> applicableException = new HashSet<>();

		Date startDt = Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("29-MAY-2021", Constants.DATE_FORMAT);
		applicableException
				.add(prepareBenOfferException(COMPANY_CODE, "14", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));

		when(benOfferExceptionDao.findApplicableBy(company.getCode(), company.getQuater(), company.getRealm().getId(),
				planStartDate)).thenReturn(applicableException);

		benOfferExceptionService.applyException(company, planRates);

		assertEquals(4, planRates.size());
		assertNull(planRates.get("14"));
	}

	@Test
	public void applyExceptionTestCommuter() {
		Company company = prepareCompany();
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		Map<String, Set<StateBenefitPlan>> planRates = new HashMap<>();
		planRates.put("10", new HashSet<>());
		planRates.put("11", new HashSet<>());
		planRates.put("14", new HashSet<>());
		planRates.put("23", new HashSet<>());
		planRates.put("30", new HashSet<>());
		Set<BenefitOfferException> applicableException = new HashSet<>();

		Date startDt = Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("29-MAY-2021", Constants.DATE_FORMAT);
		applicableException
				.add(prepareBenOfferException(COMPANY_CODE, "23", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));

		when(benOfferExceptionDao.findApplicableBy(company.getCode(), company.getQuater(), company.getRealm().getId(),
				planStartDate)).thenReturn(applicableException);

		benOfferExceptionService.applyException(company, planRates);

		assertEquals(4, planRates.size());
		assertNull(planRates.get("23"));
	}

	@Test
	public void applyExceptionTestDisability() {
		Company company = prepareCompany();
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		Map<String, Set<StateBenefitPlan>> planRates = new HashMap<>();
		planRates.put("10", new HashSet<>());
		planRates.put("11", new HashSet<>());
		planRates.put("14", new HashSet<>());
		planRates.put("23", new HashSet<>());
		planRates.put("30", new HashSet<>());
		Set<BenefitOfferException> applicableException = new HashSet<>();

		Date startDt = Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("29-MAY-2021", Constants.DATE_FORMAT);
		applicableException
				.add(prepareBenOfferException(COMPANY_CODE, "DI", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));

		when(benOfferExceptionDao.findApplicableBy(company.getCode(), company.getQuater(), company.getRealm().getId(),
				planStartDate)).thenReturn(applicableException);

		benOfferExceptionService.applyException(company, planRates);

		assertEquals(4, planRates.size());
		assertNull(planRates.get("30"));
	}

	@Test
	public void applyExceptionTestLife() {
		Company company = prepareCompany();
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		Map<String, Set<StateBenefitPlan>> planRates = new HashMap<>();
		planRates.put("10", new HashSet<>());
		planRates.put("11", new HashSet<>());
		planRates.put("14", new HashSet<>());
		planRates.put("23", new HashSet<>());
		planRates.put("30", new HashSet<>());
		planRates.put("A3", new HashSet<>());
		Set<BenefitOfferException> applicableException = new HashSet<>();

		Date startDt = Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("29-MAY-2021", Constants.DATE_FORMAT);
		applicableException
				.add(prepareBenOfferException(COMPANY_CODE, "A3", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));

		when(benOfferExceptionDao.findApplicableBy(company.getCode(), company.getQuater(), company.getRealm().getId(),
				planStartDate)).thenReturn(applicableException);

		benOfferExceptionService.applyException(company, planRates);

		assertEquals(5, planRates.size());
		assertNull(planRates.get("A3"));
	}

	@Test
	public void applyExceptionTestAll() {
		Company company = prepareCompany();
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		Map<String, Set<StateBenefitPlan>> planRates = new HashMap<>();
		planRates.put("10", new HashSet<>());
		planRates.put("1D", new HashSet<>());
		planRates.put("1V", new HashSet<>());
		planRates.put("23", new HashSet<>());
		planRates.put("30", new HashSet<>());
		planRates.put("A3", new HashSet<>());
		Set<BenefitOfferException> applicableExceptions = new HashSet<>();

		Date startDt = Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("29-MAY-2021", Constants.DATE_FORMAT);
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "10", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "11", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "14", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "23", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "30", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "A3", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));

		when(benOfferExceptionDao.findApplicableBy(company.getCode(), company.getQuater(), company.getRealm().getId(),
				planStartDate)).thenReturn(applicableExceptions);

		benOfferExceptionService.applyException(company, planRates);

		assertEquals(0, planRates.size());
	}
	
	@Test
	public void applyException() {
		Company company = prepareCompany();
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		List<BenefitOffer> benoffers = new ArrayList<>();
		benoffers.add(prepareBenOffer("medical"));
		benoffers.add(prepareBenOffer("dental"));
		benoffers.add(prepareBenOffer("vision"));
		benoffers.add(prepareBenOffer("LIFE"));
		benoffers.add(prepareBenOffer("CMTR"));
		benoffers.add(prepareBenOffer("DISABILITY"));
		Set<BenefitOfferException> applicableExceptions = new HashSet<>();
		
		Date startDt = Utils.convertStringToDate("01-MAR-2020", Constants.DATE_FORMAT);
		Date endDt = Utils.convertStringToDate("29-MAY-2021", Constants.DATE_FORMAT);
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "10", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "11", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "14", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "23", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "DI", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));
		applicableExceptions
				.add(prepareBenOfferException(COMPANY_CODE, "A3", false, APPROVER_ID1, CREATED_BY_ID1, startDt, endDt));

		when(benOfferExceptionDao.findApplicableBy(company.getCode(), company.getQuater(), company.getRealm().getId(),
				planStartDate)).thenReturn(applicableExceptions);
		
		benOfferExceptionService.applyException(company, benoffers);
		
		assertEquals(0, benoffers.size());
	}

	private Set<BenefitOfferException> prepareBenOfferExceptions() {
		Date startDate = Utils.convertStringToDate("01-JAN-2020", Constants.DATE_FORMAT);
		Date endDate = Utils.convertStringToDate("31-DEC-2021", Constants.DATE_FORMAT);
		Set<BenefitOfferException> benOfferExceptions = new HashSet<>();
		benOfferExceptions.add(
				prepareBenOfferException(COMPANY_CODE, "10", false, APPROVER_ID1, CREATED_BY_ID1, startDate, endDate));
		benOfferExceptions.add(
				prepareBenOfferException(COMPANY_CODE, "DI", false, APPROVER_ID2, CREATED_BY_ID1, startDate, endDate));

		return benOfferExceptions;
	}

	private BenefitOfferException prepareBenOfferException(String compCode, String planType, boolean offered,
			String approverId, String creatorId, Date startDt, Date endDt) {
		BenefitOfferException benOfferException = new BenefitOfferException();
		benOfferException.setCompanyCode(compCode);
		benOfferException.setPlanType(planType);
		benOfferException.setOffered(offered);
		benOfferException.setQuarter(Q1_QUATER);
		benOfferException.setRealmId(PASS_REALM_ID);
		benOfferException.setActive(true);
		benOfferException.setApproverId(approverId);
		benOfferException.setCreatedById(creatorId);
		benOfferException.setOriginDept("SALES");
		benOfferException.setStartDate(startDt);
		benOfferException.setEndDate(endDt);
		return benOfferException;
	}

	private Map<String, String> prepareEmpNames() {
		Map<String, String> names = new HashMap<String, String>();
		names.put(APPROVER_ID1, APPROVER_NAME1);
		names.put(APPROVER_ID2, APPROVER_NAME2);
		names.put(CREATED_BY_ID1, CREATED_BY_NAME1);
		return names;
	}

	private Map<String, String> prepareCompanyNames() {
		Map<String, String> names = new HashMap<String, String>();
		names.put(COMPANY_CODE, COMPANY_NAME1);
		return names;
	}

	private BenOfferExceptionDto prepareExceptionDto(long id, String planType) {
		Date startDate = Utils.convertStringToDate("01-JAN-2020", Constants.DATE_FORMAT);
		Date endDate = Utils.convertStringToDate("31-DEC-2021", Constants.DATE_FORMAT);

		BenOfferExceptionDto dto = new BenOfferExceptionDto();
		dto.setId(id);
		dto.setStartDate(startDate);
		dto.setEndDate(endDate);
		dto.setCompanyCode(COMPANY_CODE);
		dto.setPlanType(planType);
		dto.setOriginDept("SALES");
		dto.setApproverId(APPROVER_ID1);
		dto.setOffered(false);
		return dto;
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setQuater(Q1_QUATER);
		Realm realm = new Realm();
		realm.setId(PASS_REALM_ID);
		company.setRealm(realm);
		company.setPlanStartDate(PLAN_START_DT);
		return company;
	}

	private BenefitOffer prepareBenOffer(String planType) {
		BenefitOffer benOffer = new BenefitOffer();
		BenefitOfferSummary summary = new BenefitOfferSummary();
		summary.setType(planType);
		benOffer.setSummary(summary );
		return benOffer;
	}

}