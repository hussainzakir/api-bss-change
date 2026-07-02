package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.rest.controllers.dto.PlanDeselectionExceptionResDto;
import com.trinet.ambis.service.PlanDeselectionExceptionService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlanDeselectionExceptionControllerTest extends ServiceUnitTest {

	@InjectMocks
	private PlanDeselectionExceptionController planDeselectionExceptionController;

	@Mock
	private PlanDeselectionExceptionService planDeselectionExceptionService;

	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

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

	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLOYEE_ID);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
	}

	@Test
	public void findAllActiveTest() throws Exception {
		// given
		Set<PlanDeselectionExceptionResDto> expectedResult = prepareDtos();
		when(planDeselectionExceptionService.findAllActive()).thenReturn(expectedResult);

		// when
		Set<PlanDeselectionExceptionResDto> actualResult = planDeselectionExceptionController.findAllActive();

		// then
		assertEquals(2, actualResult.size());

		PlanDeselectionExceptionResDto expectedResult1 = getDtoByExcpetionId(expectedResult, EXCEPTION_ID1);
		PlanDeselectionExceptionResDto actualResult1 = getDtoByExcpetionId(actualResult, EXCEPTION_ID1);

		assertEquals(expectedResult1.getId(), actualResult1.getId());
		assertEquals(expectedResult1.getCompanyCode(), actualResult1.getCompanyCode());
		assertEquals(expectedResult1.getCompanyName(), actualResult1.getCompanyName());
		assertEquals(expectedResult1.getStartDate(), actualResult1.getStartDate());
		assertEquals(expectedResult1.getEndDate(), actualResult1.getEndDate());
		assertEquals(expectedResult1.getApproverId(), actualResult1.getApproverId());
		assertEquals(expectedResult1.getApproverName(), actualResult1.getApproverName());
		assertEquals(expectedResult1.getCreateTime(), actualResult1.getCreateTime());
		assertEquals(expectedResult1.getExchange(), actualResult1.getExchange());
		assertEquals(expectedResult1.getRealmId(), actualResult1.getRealmId());
		assertEquals(expectedResult1.getQuarter(), actualResult1.getQuarter());

		PlanDeselectionExceptionResDto expectedResult2 = getDtoByExcpetionId(expectedResult, EXCEPTION_ID1);
		PlanDeselectionExceptionResDto actualResult2 = getDtoByExcpetionId(actualResult, EXCEPTION_ID1);

		assertEquals(expectedResult2.getId(), actualResult2.getId());
		assertEquals(expectedResult2.getCompanyCode(), actualResult2.getCompanyCode());
		assertEquals(expectedResult2.getCompanyName(), actualResult2.getCompanyName());
		assertEquals(expectedResult2.getStartDate(), actualResult2.getStartDate());
		assertEquals(expectedResult2.getEndDate(), actualResult2.getEndDate());
		assertEquals(expectedResult2.getApproverId(), actualResult2.getApproverId());
		assertEquals(expectedResult2.getApproverName(), actualResult2.getApproverName());
		assertEquals(expectedResult2.getCreateTime(), actualResult2.getCreateTime());
		assertEquals(expectedResult2.getExchange(), actualResult2.getExchange());
		assertEquals(expectedResult2.getRealmId(), actualResult2.getRealmId());
		assertEquals(expectedResult2.getQuarter(), actualResult2.getQuarter());

		verify(planDeselectionExceptionService, times(1)).findAllActive();
	}

	@Test
	public void findByIdTest() throws Exception {
		// given
		PlanDeselectionExceptionResDto expectedResult = prepareDto1();
		when(planDeselectionExceptionService.findById(EXCEPTION_ID1)).thenReturn(expectedResult);

		// when
		PlanDeselectionExceptionResDto actualResult = planDeselectionExceptionController.findById(EXCEPTION_ID1);

		// then
		assertNotNull(actualResult);
		assertEquals(expectedResult.getId(), actualResult.getId());
		assertEquals(expectedResult.getCompanyCode(), actualResult.getCompanyCode());
		assertEquals(expectedResult.getCompanyName(), actualResult.getCompanyName());
		assertEquals(expectedResult.getStartDate(), actualResult.getStartDate());
		assertEquals(expectedResult.getEndDate(), actualResult.getEndDate());
		assertEquals(expectedResult.getApproverId(), actualResult.getApproverId());
		assertEquals(expectedResult.getApproverName(), actualResult.getApproverName());
		assertEquals(expectedResult.getCreateTime(), actualResult.getCreateTime());
		assertEquals(expectedResult.getExchange(), actualResult.getExchange());
		assertEquals(expectedResult.getRealmId(), actualResult.getRealmId());
		assertEquals(expectedResult.getQuarter(), actualResult.getQuarter());

		verify(planDeselectionExceptionService, times(1)).findById(EXCEPTION_ID1);
	}

	@Test
	public void createTest() throws ParseException {
		// given
		PlanDeselectionExceptionResDto expectedResult = prepareDto1();
		PlanDeselectionExceptionResDto createReqDto = prepareCreateReqDto1();
		when(planDeselectionExceptionService.create(createReqDto)).thenReturn(expectedResult);

		// when
		PlanDeselectionExceptionResDto actualResult = planDeselectionExceptionController.create(createReqDto);

		// then
		assertNotNull(actualResult);
		assertEquals(expectedResult.getId(), actualResult.getId());
		assertEquals(expectedResult.getCompanyCode(), actualResult.getCompanyCode());
		assertEquals(expectedResult.getCompanyName(), actualResult.getCompanyName());
		assertEquals(expectedResult.getStartDate(), actualResult.getStartDate());
		assertEquals(expectedResult.getEndDate(), actualResult.getEndDate());
		assertEquals(expectedResult.getApproverId(), actualResult.getApproverId());
		assertEquals(expectedResult.getApproverName(), actualResult.getApproverName());
		assertEquals(expectedResult.getCreateTime(), actualResult.getCreateTime());
		assertEquals(expectedResult.getExchange(), actualResult.getExchange());
		assertEquals(expectedResult.getRealmId(), actualResult.getRealmId());
		assertEquals(expectedResult.getQuarter(), actualResult.getQuarter());

		verify(planDeselectionExceptionService, times(1)).create(createReqDto);
	}

	@Test
	public void updateTest() throws ParseException {
		// given
		PlanDeselectionExceptionResDto expectedResult = prepareDto1();
		PlanDeselectionExceptionResDto updateReqDto = prepareUpdateReqDto1();
		when(planDeselectionExceptionService.update(updateReqDto)).thenReturn(expectedResult);

		// when
		PlanDeselectionExceptionResDto actualResult = planDeselectionExceptionController.update(updateReqDto);

		// then
		assertNotNull(actualResult);
		assertEquals(expectedResult.getId(), actualResult.getId());
		assertEquals(expectedResult.getCompanyCode(), actualResult.getCompanyCode());
		assertEquals(expectedResult.getCompanyName(), actualResult.getCompanyName());
		assertEquals(expectedResult.getStartDate(), actualResult.getStartDate());
		assertEquals(expectedResult.getEndDate(), actualResult.getEndDate());
		assertEquals(expectedResult.getApproverId(), actualResult.getApproverId());
		assertEquals(expectedResult.getApproverName(), actualResult.getApproverName());
		assertEquals(expectedResult.getCreateTime(), actualResult.getCreateTime());
		assertEquals(expectedResult.getExchange(), actualResult.getExchange());
		assertEquals(expectedResult.getRealmId(), actualResult.getRealmId());
		assertEquals(expectedResult.getQuarter(), actualResult.getQuarter());

		verify(planDeselectionExceptionService, times(1)).update(updateReqDto);
	}

	private Set<PlanDeselectionExceptionResDto> prepareDtos() throws ParseException {
		Set<PlanDeselectionExceptionResDto> dtos = new HashSet<>();
		dtos.add(prepareDto1());
		dtos.add(prepareDto2());
		return dtos;
	}

	private PlanDeselectionExceptionResDto prepareDto1() throws ParseException {
		PlanDeselectionExceptionResDto planDeselectionExceptionResDto1 = new PlanDeselectionExceptionResDto();
		planDeselectionExceptionResDto1.setId(EXCEPTION_ID1);
		planDeselectionExceptionResDto1.setCompanyCode(COMPANY_CODE1);
		planDeselectionExceptionResDto1.setStartDate(simpleDateFormat.parse("16-09-2022"));
		planDeselectionExceptionResDto1.setEndDate(simpleDateFormat.parse("21-09-2022"));
		planDeselectionExceptionResDto1.setApproverId(APPROVER_ID1);
		planDeselectionExceptionResDto1.setCompanyName(COMPANY_NAME1);
		planDeselectionExceptionResDto1.setQuarter(COMPANY_QUARTER1);
		planDeselectionExceptionResDto1.setRealmId(BenExchngEnums.TRINET_II.getId());
		return planDeselectionExceptionResDto1;
	}

	private PlanDeselectionExceptionResDto prepareDto2() throws ParseException {
		PlanDeselectionExceptionResDto planDeselectionExceptionResDto2 = new PlanDeselectionExceptionResDto();
		planDeselectionExceptionResDto2.setId(EXCEPTION_ID2);
		planDeselectionExceptionResDto2.setCompanyCode(COMPANY_CODE2);
		planDeselectionExceptionResDto2.setStartDate(simpleDateFormat.parse("16-10-2022"));
		planDeselectionExceptionResDto2.setEndDate(simpleDateFormat.parse("21-10-2022"));
		planDeselectionExceptionResDto2.setApproverId(APPROVER_ID2);
		planDeselectionExceptionResDto2.setCompanyName(COMPANY_NAME2);
		planDeselectionExceptionResDto2.setQuarter(COMPANY_QUARTER2);
		planDeselectionExceptionResDto2.setRealmId(BenExchngEnums.TRINET_II.getId());
		return planDeselectionExceptionResDto2;
	}

	private PlanDeselectionExceptionResDto getDtoByExcpetionId(Set<PlanDeselectionExceptionResDto> dtos,
			long exceptionId) {
		return dtos.stream().filter(dto -> dto.getId() == exceptionId).findFirst().orElseThrow();
	}

	private PlanDeselectionExceptionResDto prepareCreateReqDto1() throws ParseException {
		PlanDeselectionExceptionResDto planDeselectionExceptionResDto1 = new PlanDeselectionExceptionResDto();
		planDeselectionExceptionResDto1.setCompanyCode(COMPANY_CODE1);
		planDeselectionExceptionResDto1.setStartDate(simpleDateFormat.parse("16-09-2022"));
		planDeselectionExceptionResDto1.setEndDate(simpleDateFormat.parse("21-09-2022"));
		planDeselectionExceptionResDto1.setApproverId(APPROVER_ID1);
		return planDeselectionExceptionResDto1;
	}

	private PlanDeselectionExceptionResDto prepareUpdateReqDto1() throws ParseException {
		PlanDeselectionExceptionResDto planDeselectionExceptionResDto1 = new PlanDeselectionExceptionResDto();
		planDeselectionExceptionResDto1.setId(EXCEPTION_ID1);
		planDeselectionExceptionResDto1.setCompanyCode(COMPANY_CODE1);
		planDeselectionExceptionResDto1.setStartDate(simpleDateFormat.parse("16-09-2022"));
		planDeselectionExceptionResDto1.setEndDate(simpleDateFormat.parse("21-09-2022"));
		planDeselectionExceptionResDto1.setApproverId(APPROVER_ID1);
		return planDeselectionExceptionResDto1;
	}

}
