package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.BenefitStrategyGroupDao;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.service.impl.StrategyGroupServiceImpl;

/**
 * @author hliddle
 *
 */
@RunWith(JUnit4.class)
public class StrategyGroupServiceImplTest {

	@InjectMocks
	StrategyGroupServiceImpl strategyGroupService;

	@Mock
	private BenefitStrategyGroupDao benefitStrategyGroupDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findById() {
		BenefitGroupStrategy benGrpStrategy = new BenefitGroupStrategy();

		long id = 1111L;
		when(benefitStrategyGroupDao.findById(id)).thenReturn(benGrpStrategy);

		BenefitGroupStrategy actual = strategyGroupService.findById(id);

		assertEquals(benGrpStrategy, actual);
	}

	@Test
	public void findByStrategyIdAndGroupId() {
		BenefitGroupStrategy benGrpStrategy = new BenefitGroupStrategy();

		long strategyId = 1111L;
		long groupId = 2222L;
		when(benefitStrategyGroupDao.findByStrategyIdAndGroupId(strategyId, groupId)).thenReturn(benGrpStrategy);

		BenefitGroupStrategy actual = strategyGroupService.findByStrategyIdAndGroupId(strategyId, groupId);

		assertEquals(benGrpStrategy, actual);
	}

	@Test
	public void findByStrategyIdAndStatus() {
		List<BenefitGroupStrategy> benGrpStrategies = new ArrayList<>();

		long strategyId = 1111L;
		String status = "A";
		when(benefitStrategyGroupDao.findByStrategyIdAndStatus(strategyId, status)).thenReturn(benGrpStrategies);

		List<BenefitGroupStrategy> actual = strategyGroupService.findByStrategyIdAndStatus(strategyId, status);

		assertEquals(benGrpStrategies, actual);
	}


	@Test
	public void getBenefitGroupStrategy() {
		long id = 1111;
		BenefitGroupStrategy benGrpStrategy = new BenefitGroupStrategy();

		when(benefitStrategyGroupDao.findById(id)).thenReturn(benGrpStrategy);

		BenefitGroupStrategy actualResult = strategyGroupService.getBenefitGroupStrategyBy(id);

		verify(benefitStrategyGroupDao, times(1)).findById(id);
		assertEquals(benGrpStrategy, actualResult);
	}

	@Test
	public void getBenefitGroupStrategy1() {
		long strategyId = 1111;
		long groupId = 2222;
		BenefitGroupStrategy benGrpStrategy = new BenefitGroupStrategy();

		when(benefitStrategyGroupDao.findByStrategyIdAndGroupId(strategyId, groupId)).thenReturn(benGrpStrategy);

		BenefitGroupStrategy actualResult = strategyGroupService.getBenefitGroupStrategy(strategyId, groupId);

		verify(benefitStrategyGroupDao, times(1)).findByStrategyIdAndGroupId(strategyId, groupId);
		assertEquals(benGrpStrategy, actualResult);
	}

	@Test
	public void getBenefitGroupStrategy2() {
		long strategyId = 1111;
		String status = "A";
		List<BenefitGroupStrategy> benGrpStrategies = new ArrayList<>();
		when(benefitStrategyGroupDao.findByStrategyIdAndStatus(strategyId, status)).thenReturn(benGrpStrategies);

		List<BenefitGroupStrategy> actualResult = strategyGroupService.getBenefitGroupStrategy(strategyId, status);

		verify(benefitStrategyGroupDao, times(1)).findByStrategyIdAndStatus(strategyId, status);
		assertEquals(benGrpStrategies, actualResult);
	}

	@Test
	public void saveBenefitGroupStrategy() {
		BenefitGroupStrategy benefitGroupStrategy = new BenefitGroupStrategy();
		when(benefitStrategyGroupDao.save(benefitGroupStrategy)).thenReturn(benefitGroupStrategy);

		BenefitGroupStrategy actualResult = strategyGroupService.saveBenefitGroupStrategy(benefitGroupStrategy);

		verify(benefitStrategyGroupDao, times(1)).save(benefitGroupStrategy);
		assertEquals(benefitGroupStrategy, actualResult);
	}

	@Test
	public void saveBenefitGroupStrategies() {
		List<BenefitGroupStrategy> benefitGroupStrategies = new ArrayList<>();
		when(benefitStrategyGroupDao.saveAll(benefitGroupStrategies)).thenReturn(benefitGroupStrategies);

		List<BenefitGroupStrategy> actualResult = strategyGroupService
				.saveBenefitGroupStrategies(benefitGroupStrategies);

		verify(benefitStrategyGroupDao, times(1)).saveAll(benefitGroupStrategies);
		assertEquals(benefitGroupStrategies, actualResult);
	}
	
	@Test
	public void findBy() {
		List<BenefitGroupStrategy> benGrpStrategies = new ArrayList<>();

		String companyCode = "COMPANY_CODE";
		when(benefitStrategyGroupDao.findBy(companyCode)).thenReturn(benGrpStrategies);

		List<BenefitGroupStrategy> actual = strategyGroupService.findBy(companyCode);

		assertEquals(benGrpStrategies, actual);
	}	
	
	@Test
	public void getBenefitGroupStrategyBy() {
		long id = 2222;
		long strategyId = 1111;
		BenefitGroupStrategy benefitGroupStrategy = new BenefitGroupStrategy();

		when(benefitStrategyGroupDao.findByIdAndStrategyId(id, strategyId)).thenReturn(benefitGroupStrategy);

		BenefitGroupStrategy actual = strategyGroupService.getBenefitGroupStrategyBy(id, strategyId);

		assertEquals(benefitGroupStrategy, actual);
	}	
	
	@Test
	public void updateBenefitGroupStrategyStatus() {
		BenefitGroupStrategy bgs = new BenefitGroupStrategy();
		bgs.setId(122);
		strategyGroupService.updateBenefitGroupStrategyStatus(bgs, BSSApplicationConstants.STATUS_DELETED);
		verify(benefitStrategyGroupDao, times(1)).saveAndFlush(any());
	}

}