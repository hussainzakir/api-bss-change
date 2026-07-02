package com.trinet.ambis.persistence.dao.test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.dao.hrp.impl.StrategyFundingDetailDataDaoImpl;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class StrategyFundingDetailDataDaoImplTest {

	@InjectMocks
	StrategyFundingDetailDataDaoImpl strategyFundingDetailDataDao;

	@Mock
	EntityManager em;

	@Mock
	private Query mockedQuery;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(em.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void updateStrategyFundingFlatMaxTest() {

		long strategyId = 111;
		long groupId = 222;
		String planType = PlanTypesEnum.MEDICAL.getCode();
		String coverageLevel = CoverageCodesEnums.COV_EMPLOYEE.getId();
		BigDecimal contribution = BigDecimal.valueOf(200);

		strategyFundingDetailDataDao.updateStrategyFundingDetail(strategyId, groupId, planType, coverageLevel,
				contribution);

		verify(mockedQuery, times(1)).executeUpdate();
	}

}