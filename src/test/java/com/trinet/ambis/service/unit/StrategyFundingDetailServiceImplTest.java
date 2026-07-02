package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDetailDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDetailDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingFlatMaxDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingFlatMaxDataDao;
import com.trinet.ambis.persistence.model.StrategyFundingDetail;
import com.trinet.ambis.persistence.model.StrategyFundingFlatMax;
import com.trinet.ambis.service.impl.StrategyFundingDetailServiceImpl;

/**
 * @author akaparaboyna
 *
 */
@RunWith(JUnit4.class)
public class StrategyFundingDetailServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	private StrategyFundingDetailServiceImpl sfdService;

	@Mock
	StrategyFundingDetailDao strategyFundingDetailDao;

	@Mock
	StrategyFundingFlatMaxDao strategyFundingFlatMaxDao;

	@Mock
	StrategyFundingDetailDataDao strategyFundingDetailDataDao;
	
	@Mock
	StrategyFundingFlatMaxDataDao strategyFundingFlatMaxDataDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getStrategyFundingDetailByStrategyFundingIdTest() {
		List<StrategyFundingDetail> list = strategyFundingDetailDao.findBySfDetailIdStrategyFundingId(1055);
		List<StrategyFundingDetail> list1 = sfdService.getStrategyFundingDetailByStrategyFundingId(1055);
		assertEquals(list.size(), list1.size());
	}

	@Test
	public void getStrategyFundingFlatMaxByStrategyFundingIdTest() {
		List<StrategyFundingFlatMax> list = strategyFundingFlatMaxDao.findBySfDetailIdStrategyFundingId(1055);
		List<StrategyFundingFlatMax> list1 = sfdService.getStrategyFundingFlatMaxByStrategyFundingId(1055);
		assertEquals(list.size(), list1.size());
	}

	@Test
	public void updateStrategyFundingDetailTest() {
		long strategyId = 111;
		long groupId = 222;
		String planType = PlanTypesEnum.MEDICAL.getCode();
		String coverageLevel = CoverageCodesEnums.COV_EMPLOYEE.getId();
		BigDecimal contribution = BigDecimal.valueOf(200);

		doNothing().when(strategyFundingDetailDataDao).updateStrategyFundingDetail(strategyId, groupId, planType,
				coverageLevel, contribution);
		sfdService.updateStrategyFundingDetail(strategyId, groupId, planType, coverageLevel, contribution);

		verify(strategyFundingDetailDataDao, times(1)).updateStrategyFundingDetail(strategyId, groupId, planType,
				coverageLevel, contribution);

	}

	@Test
	public void updateStrategyFundingFlatMaxTest() {
		long strategyId = 111;
		long groupId = 222;
		String planType = PlanTypesEnum.MEDICAL.getCode();
		String coverageLevel = CoverageCodesEnums.COV_EMPLOYEE.getId();
		BigDecimal contribution = BigDecimal.valueOf(200);

		doNothing().when(strategyFundingFlatMaxDataDao).updateStrategyFundingFlatMax(strategyId, groupId, planType,
				coverageLevel, contribution);
		sfdService.updateStrategyFundingFlatMax(strategyId, groupId, planType, coverageLevel, contribution);

		verify(strategyFundingFlatMaxDataDao, times(1)).updateStrategyFundingFlatMax(strategyId, groupId, planType,
				coverageLevel, contribution);

	}
}
