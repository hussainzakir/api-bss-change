package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.SubmitStatusDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.impl.SubmitStatusServiceImpl;

@RunWith(JUnit4.class)
public class SubmitStatusServiceImplTest {

	@InjectMocks
	SubmitStatusServiceImpl submitStatusService;

	@Mock
	SubmitStatusDao submitStatusDao;

	private static final String CONFIRMATION_NUMBER = "123456";
	private static final String COMPANY_CODE = "G48";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createUpdateSubmitStatus() {
		SubmitStatus status = SubmitStatus.builder().confirmationNumber(CONFIRMATION_NUMBER).build();
		ArgumentCaptor<SubmitStatus> statusToSave = ArgumentCaptor.forClass(SubmitStatus.class);

		when(submitStatusDao.saveAndFlush(statusToSave.capture())).thenReturn(status);

		submitStatusService.createUpdateSubmitStatus(status);

		verify(submitStatusDao, times(1)).saveAndFlush(any(SubmitStatus.class));
		assertEquals(CONFIRMATION_NUMBER, statusToSave.getValue().getConfirmationNumber());
	}

	@Test
	public void findByConfirmationNumber() {
		SubmitStatus status = SubmitStatus.builder().confirmationNumber(CONFIRMATION_NUMBER).build();

		ArgumentCaptor<String> companyCodeArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> confirmationArgCaptor = ArgumentCaptor.forClass(String.class);

		when(submitStatusDao.findByCompanyAndConfirmationNumber(companyCodeArgCaptor.capture(),
				confirmationArgCaptor.capture())).thenReturn(status);

		submitStatusService.findByConfirmationNumber(COMPANY_CODE, CONFIRMATION_NUMBER);

		verify(submitStatusDao, times(1)).findByCompanyAndConfirmationNumber(any(String.class), any(String.class));
		assertEquals(CONFIRMATION_NUMBER, confirmationArgCaptor.getValue());
		assertEquals(COMPANY_CODE, companyCodeArgCaptor.getValue());

	}

	@Test
	public void findLatestSubmitStatusBy() {
		SubmitStatus status = SubmitStatus.builder().confirmationNumber(CONFIRMATION_NUMBER).build();

		ArgumentCaptor<String> companyCodeArgCaptor = ArgumentCaptor.forClass(String.class);

		when(submitStatusDao.findLatestSubmitStatusBy(companyCodeArgCaptor.capture())).thenReturn(status);

		submitStatusService.findLatestSubmitStatusBy(COMPANY_CODE);

		verify(submitStatusDao, times(1)).findLatestSubmitStatusBy(any(String.class));
		assertEquals(COMPANY_CODE, companyCodeArgCaptor.getValue());
	}

	@Test
	public void updateAndCommit() {
		SubmitStatus submitStatus = SubmitStatus.builder().build();

		submitStatusService.updateAndCommit(submitStatus);

		verify(submitStatusDao, times(1)).saveAndFlush(submitStatus);
	}

	@Test
	public void findByConfirmationNumber_test1() {
		String confirmationNumber = "HHDJASHKDHHJKSADH";
		Set<String> statuses = new HashSet<>(
				Arrays.asList(BSSApplicationConstants.UNPROCESSED, BSSApplicationConstants.PROCESSING));

		submitStatusService.findByConfirmationNumberAndStatus(confirmationNumber, statuses);

		verify(submitStatusDao, times(1)).findByConfirmationNumberAndStatusIn(confirmationNumber, statuses);
	}
	
	@Test
	public void findByStrategyIdAndStatus_test() {
		Company company = new Company();
		company.setRealmPlanYearId(40);
		company.setCode("G48");

		Set<String> statuses = new HashSet<>(
				Arrays.asList(BSSApplicationConstants.UNPROCESSED, BSSApplicationConstants.PROCESSING));

		submitStatusService.findByCompanyAndPlanYearIdAndStatuses(company, statuses);

		verify(submitStatusDao, times(1)).findByCompanyAndRealmYrIdAndStatusIn("G48", 40, statuses);
	}
}