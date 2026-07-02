package com.trinet.ambis.service.unit;

import static org.mockito.Mockito.when;

import com.trinet.ambis.util.BSSSecurityUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.BenOfferExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.service.ExceptionAttributeService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.impl.BenefitOfferExceptionServiceImpl;
import com.trinet.ambis.service.model.AttributeDto;
import com.trinet.ambis.service.model.AttributeValueDto;
import com.trinet.ambis.service.model.BenOfferExceptionDto;
import com.trinet.ambis.service.model.ExceptionAttributeDto;
import com.trinet.ambis.service.model.ProductQuarters;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.Utils;

@RunWith(MockitoJUnitRunner.class)
public class BenefitOfferExceptionServiceImpl1Test extends ServiceUnitTest {

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
	@Mock
	ExceptionAttributeService exceptionAttributeService;

	@Mock
	RealmDataDao realmDataDao;

	public @Rule
	ExpectedException expectedException = ExpectedException.none();

	private static final String COMPANY_CODE = "G48";
	private static final String APPROVER_ID1 = "00002222236";
	private static final String LOGGED_IN_PERSON = "00002222237";
	private static final long PASS_REALM_ID = 3L;
	private static final String Q1_QUATER = "Q1";
	private static final String PLAN_START_DT = "01-JAN-2020";
	private static final String ERROR_MESSAGE_INVALID_COMPANY_CODE = "Invalid company code";
	private static final String ERROR_MESSAGE_INVALID_PLAN_TYPE = "Invalid plan type";
	private static final String ERROR_MESSAGE_INVALID_ORIGINATION = "Invalid origin dept";
	private static final String ERROR_MESSAGE_INVALID_APPROVER_ID = "Invalid approver id";
	private static final String ERROR_MESSAGE_INVALID_QUARTER = "Invalid quarter";
    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMock;

    @Before
    public void setUp() throws Exception {
        bssSecurityUtilsMock = org.mockito.Mockito.mockStatic(BSSSecurityUtils.class);
        bssSecurityUtilsMock.when(BSSSecurityUtils::getAuthenticatedPersonId)
                .thenReturn(LOGGED_IN_PERSON);
    }

    @After
    public void tearDown() {
        bssSecurityUtilsMock.close();
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
		dto.setOriginDept("Sales");
		dto.setApproverId(APPROVER_ID1);
		dto.setQuarter("8Y");
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

	@Test
	public void validateRequestDataForInvalidPlanType() throws Exception {
		// given
		BenOfferExceptionDto dto = prepareExceptionDto(0L, "10");
		// when
		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_PLAN_TYPE);
		dto.setPlanType(dto.getPlanType().concat("$"));
		benOfferExceptionService.save(dto);
	}

	@Test
	public void validateRequestDataForInvalidOrgDept() throws Exception {
		// given
		BenOfferExceptionDto dto = prepareExceptionDto(0L, "10");
		// when
		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_ORIGINATION);
		dto.setOriginDept(dto.getOriginDept().concat("$"));
		benOfferExceptionService.save(dto);
	}

	@Test
	public void validateRequestDataForInvalidApproverId() throws Exception {
		// given
		BenOfferExceptionDto dto = prepareExceptionDto(0L, "10");
		// when
		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_APPROVER_ID);
		dto.setApproverId(dto.getApproverId().concat("$"));
		benOfferExceptionService.save(dto);
	}

	@Test
	public void validateRequestDataForInvalidQuarter() throws Exception {
		// given
		BenOfferExceptionDto dto = prepareExceptionDto(0L, "10");
		// when
		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_QUARTER);
		dto.setQuarter(dto.getQuarter().concat("$"));
		benOfferExceptionService.save(dto);
	}

	@Test
	public void validateUpdateRequestDataForInvalidCompanyCode() throws Exception {
		// given
		BenOfferExceptionDto dto = prepareExceptionDto(1234L, "DI");
		// when
//		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_COMPANY_CODE);
		dto.setCompanyCode(dto.getCompanyCode().concat("$"));
		benOfferExceptionService.update(dto);
	}

	@Test
	public void validateUpdateRequestDataForInvalidPlanType() throws Exception {
		// given
		BenOfferExceptionDto dto = prepareExceptionDto(1234L, "DI");
		// when
//		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_PLAN_TYPE);
		dto.setPlanType(dto.getPlanType().concat("$"));
		benOfferExceptionService.update(dto);
	}

	@Test
	public void validateUpdateRequestDataForInvalidOrgDept() throws Exception {
		// given
		BenOfferExceptionDto dto = prepareExceptionDto(1234L, "10");
		// when
//		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_ORIGINATION);
		dto.setOriginDept(dto.getOriginDept().concat("$"));
		benOfferExceptionService.update(dto);
	}

	@Test
	public void validateUpdateRequestDataForInvalidQuarter() throws Exception {
		// given
		BenOfferExceptionDto dto = prepareExceptionDto(1234L, "10");
		// when
//		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_QUARTER);
		dto.setQuarter(dto.getQuarter().concat("$"));
		benOfferExceptionService.update(dto);
	}

	@Test
	public void validateUpdateRequestDataForInvalidApproverId() throws Exception {
		// given
		BenOfferExceptionDto dto = prepareExceptionDto(1234L, "10");
		// when
//		when(psCompanyDao.getBasicCompanyDetails(dto.getCompanyCode())).thenReturn(prepareCompany());
		when(exceptionAttributeService.findAllExceptionAttributes()).thenReturn(prepareExceptionAttributes());
		when(realmDataDao.getAllProductQuarters()).thenReturn(prepareProducts());
		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_APPROVER_ID);
		dto.setApproverId(dto.getApproverId().concat("$"));
		benOfferExceptionService.update(dto);
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
		benOfferAttributeDtos.add(prepareAttributeDto(2L, "APPROVERS", "00002222236", "Cori D Haitham"));
		benOfferAttributeDtos.add(prepareAttributeDto(4L, "EXCEPTION VALUE TYPE", "Not Offered", "Not Offered"));
		benOfferAttributeDtos.add(prepareAttributeDto(3L, "ORIGINATION DEPT", "Sales", "Sales"));
		benOfferExceptionAttributeDto.setAttributes(benOfferAttributeDtos);

		exceptionAttributeDtos.add(minFundExceptionAttributeDto);
		exceptionAttributeDtos.add(benOfferExceptionAttributeDto);

		return exceptionAttributeDtos;
	}

	private AttributeDto prepareAttributeDto(long attributeId, String name, String attributeValue,
			String attributeValueNames) {
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

	private Map<String, ProductQuarters> prepareProducts() {
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