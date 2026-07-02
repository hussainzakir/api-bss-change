package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import com.trinet.ambis.persistence.dao.ps.impl.PsSubmitDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.impl.PsSubmitDataServiceImpl;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySummary;

@RunWith(MockitoJUnitRunner.class)
public class PsSubmitDataServiceImplTest {

	@InjectMocks
	@Qualifier("realPsSubmitDataService")
	PsSubmitDataServiceImpl psSubmitDataServiceImpl;

	@Mock
	PsSubmitDataDaoImpl psSubmitDataDao;
	
	@Ignore
	@Test
	public void submitData() {
		String userId = "121231231";
		String peodId = "AL";
		String quarter = "IV";
		Long relamYearId = (long) 10;
		Date payrollCutOffDate = new Date();

		ArgumentCaptor<List> companyCaptor = ArgumentCaptor.forClass(ArrayList.class);
		ArgumentCaptor<Boolean> isSingleClientCaptor = ArgumentCaptor.forClass(Boolean.class);
		
		doNothing().when(psSubmitDataDao).createEntityManager();
		
		doNothing().when(psSubmitDataDao).defaultSubmit(companyCaptor.capture(), isSingleClientCaptor.capture(), anyString(), any(long.class));

		psSubmitDataServiceImpl.defaultSubmit(new ArrayList<>(), true, userId, relamYearId);

		verify(psSubmitDataDao, times(1)).createEntityManager();
		verify(psSubmitDataDao, times(1)).defaultSubmit(any(List.class), any(Boolean.class), any(String.class), any(long.class));
//		assertEquals(peodId, peoidCaptor.getValue());
//		assertEquals(quarter, quarterDataCaptor.getValue());
//		assertEquals(relamYearId, realmYrIdCaptor.getValue());
//		assertEquals(payrollCutOffDate, payrollCutOffCaptor.getValue());
	}

	@Ignore
	@Test
	public void defaultSubmit() {
		Company company = new Company();
		company.setCode("ABC");
		StrategyData strategy = new StrategyData();
		StrategySummary strategySummary = new StrategySummary();
		strategySummary.setId((long) 1111);
		strategy.setStrategySummary(strategySummary);
		String userId = "testuser";
		boolean emailFlag = true;
		String uniqueTrxId = "randomid";
		boolean resubmitFlag = false;

		ArgumentCaptor<Company> compCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<StrategyData> strategyDataCaptor = ArgumentCaptor.forClass(StrategyData.class);
		ArgumentCaptor<String> useridCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Boolean> emailFlgCaptor = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<String> uniqueTrxIdCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Boolean> resubmitFlagCaptor = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Boolean> isMultiClientSubmit = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Map> bdmCount = ArgumentCaptor.forClass(Map.class);
		
		doNothing().when(psSubmitDataDao).createEntityManager();
		doNothing().when(psSubmitDataDao).submitData(compCaptor.capture(), strategyDataCaptor.capture(),
				useridCaptor.capture(), emailFlgCaptor.capture(), uniqueTrxIdCaptor.capture(),
				resubmitFlagCaptor.capture(), isMultiClientSubmit.capture(), bdmCount.capture());

		psSubmitDataServiceImpl.submitData(company, strategy, userId, emailFlag, uniqueTrxId, resubmitFlag);

		verify(psSubmitDataDao, times(1)).createEntityManager();
		verify(psSubmitDataDao, times(1)).submitData(any(Company.class), any(StrategyData.class), any(String.class),
				any(Boolean.class), any(String.class), any(Boolean.class), any(Boolean.class), nullable(Map.class));
		assertEquals("ABC", compCaptor.getValue().getCode());
		assertEquals(1111, strategyDataCaptor.getValue().getStrategySummary().getId().intValue());
		assertEquals(userId, useridCaptor.getValue());
		assertEquals(emailFlag, emailFlgCaptor.getValue());
		assertEquals(uniqueTrxId, uniqueTrxIdCaptor.getValue());

	}

}