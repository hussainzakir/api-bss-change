package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.service.impl.PsConfigurationServiceImpl;

@RunWith(JUnit4.class)
public class PsConfigurationServiceImplTest {

	@InjectMocks
	PsConfigurationServiceImpl serviceImpl;

	@Mock
	PsDao psDao;

	private static final Date EFF_DT = new Date();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getPsConfigsByDateTest1() {
		Map<String, BigDecimal> hsaMaximumsMap = new HashMap<>();

		when(psDao.getHsaMaximumsByEffDate(EFF_DT)).thenReturn(hsaMaximumsMap);

		Map<String, String> actualResult = serviceImpl.findByEffDate(EFF_DT);

		assertEquals(Collections.emptyMap(), actualResult);

	}

	@Test
	public void getPsConfigsByDateTest2() {
		Map<String, BigDecimal> hsaMaximumsMap = new HashMap<>();
		hsaMaximumsMap.put("HSA_ANNUAL_EMPLOYEE_MAXIMUM", BigDecimal.valueOf(3000));
		hsaMaximumsMap.put("HSA_ANNUAL_FAMILY_MAXIMUM", BigDecimal.valueOf(6000));

		when(psDao.getHsaMaximumsByEffDate(EFF_DT)).thenReturn(hsaMaximumsMap);

		Map<String, String> actualResult = serviceImpl.findByEffDate(EFF_DT);

		assertEquals(2, actualResult.size());
		assertEquals("3000", actualResult.get("HSA_ANNUAL_EMPLOYEE_MAXIMUM"));
		assertEquals("6000", actualResult.get("HSA_ANNUAL_FAMILY_MAXIMUM"));
	}

}