package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.hrp.StrategyGroupHeadCountDao;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;
import com.trinet.ambis.service.impl.StrategyGroupHeadCountServiceImpl;

/**
 * @author hliddle
 *
 */
@RunWith(JUnit4.class)
public class StrategyGroupHeadCountServiceImplTest {

	@InjectMocks
	StrategyGroupHeadCountServiceImpl strategyGroupHeadCountService;

	@Mock
	StrategyGroupHeadCountDao strategyGroupHeadCountDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}


	@Test
	public void saveStrategyGroupHeadCount() {
		StrategyGroupHeadCount sghc = new StrategyGroupHeadCount();
		when(strategyGroupHeadCountDao.save(sghc)).thenReturn(sghc);

		StrategyGroupHeadCount actualResult = strategyGroupHeadCountService.saveStrategyGroupHeadCount(sghc);

		verify(strategyGroupHeadCountDao, times(1)).save(sghc);
		assertEquals(sghc, actualResult);
	}

	@Test
	public void findStrategyGroupHeadCountBy() {
		long strategyGroupId = 1111;
		List<StrategyGroupHeadCount> sghcs = new ArrayList<>();

		when(strategyGroupHeadCountDao.findByIdStrategyGroupId(strategyGroupId)).thenReturn(sghcs);

		List<StrategyGroupHeadCount> actualResult = strategyGroupHeadCountService
				.findStrategyGroupHeadCountBy(strategyGroupId);

		verify(strategyGroupHeadCountDao, times(1)).findByIdStrategyGroupId(strategyGroupId);
		assertEquals(sghcs, actualResult);
	}

}