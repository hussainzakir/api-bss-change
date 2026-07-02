
package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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

import com.trinet.ambis.persistence.dao.hrp.StrategyFundingModelDao;
import com.trinet.ambis.persistence.model.StrategyFundingModel;
import com.trinet.ambis.service.impl.StrategyFundingModelServiceImpl;

/**
 * @author rvutukuri
 *
 */
@RunWith(JUnit4.class)
public class StrategyFundingModelServiceImplTest {

	@InjectMocks
	private StrategyFundingModelServiceImpl strategyFundingModelService;

	@Mock
	StrategyFundingModelDao strategyFundingModelDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createUpdateFundingTest() {
		StrategyFundingModel strategyFundingModel = new StrategyFundingModel();
		when(strategyFundingModelDao.saveAndFlush(strategyFundingModel)).thenReturn(strategyFundingModel);
		strategyFundingModelService.createUpdateFunding(strategyFundingModel);
		verify(strategyFundingModelDao, times(1)).saveAndFlush(strategyFundingModel);
	}

	@Test
	public void getStrategyFundingModelByStrategyIdAndGroupId() {
		List<StrategyFundingModel> strategyFundingModelList = new ArrayList<>();
		when(strategyFundingModelDao.findByStrategyIdAndGroupId(27470, 26829)).thenReturn(strategyFundingModelList);
		strategyFundingModelService.getStrategyFundingModelByStrategyIdAndGroupId(27470, 26829);
		verify(strategyFundingModelDao, times(1)).findByStrategyIdAndGroupId(27470, 26829);
	}

	@Test
	public void saveAll() {
		List<StrategyFundingModel> strategyFundingModelList = new ArrayList<>();
		when(strategyFundingModelDao.saveAll(strategyFundingModelList)).thenReturn(strategyFundingModelList);
		List<StrategyFundingModel> actualResult = strategyFundingModelService.saveAll(strategyFundingModelList);
		verify(strategyFundingModelDao, times(1)).saveAll(strategyFundingModelList);
		assertEquals(strategyFundingModelList, actualResult);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteAll() {
		List<StrategyFundingModel> strategyFundingModelList = new ArrayList<>();
		doNothing().when(strategyFundingModelDao).deleteAllInBatch(strategyFundingModelList);
		strategyFundingModelService.deleteAll(strategyFundingModelList);
		verify(strategyFundingModelDao, times(1)).deleteAllInBatch(any(List.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteStrategyFundingModelByStrategyIdAndGroupId() {
		List<StrategyFundingModel> strategyFundingModelList = new ArrayList<>();
		when(strategyFundingModelDao.findByStrategyIdAndGroupId(27470, 26829)).thenReturn(strategyFundingModelList);
		doNothing().when(strategyFundingModelDao).deleteAllInBatch(strategyFundingModelList);
		strategyFundingModelService.deleteStrategyFundingModelByStrategyIdAndGroupId(27470, 26829);
		verify(strategyFundingModelDao, times(1)).findByStrategyIdAndGroupId(27470, 26829);
		verify(strategyFundingModelDao, times(1)).deleteAllInBatch(any(List.class));
	}
}
