package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trinet.ambis.persistence.dao.hrp.impl.ContributionDataDaoImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.hrp.ContributionDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.service.impl.ContributionServiceImpl;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.PlanContribution;


@RunWith(JUnit4.class)
public class ContributionServiceImplTest {

	@InjectMocks
	ContributionServiceImpl contributionService;

	@Mock
	ContributionDao contributionDao;

	@Mock
	ContributionDataDaoImpl contributionDataDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createUpdate() {
		Contribution contribution = new Contribution();
		contribution.setId(111111);
		ArgumentCaptor<Contribution> contributionCaptor = ArgumentCaptor.forClass(Contribution.class);

		when(contributionDao.saveAndFlush(contributionCaptor.capture())).thenReturn(contribution);

		Contribution result = contributionService.createUpdate(contribution);

		verify(contributionDao, times(1)).saveAndFlush(any(Contribution.class));
		assertEquals(contribution, contributionCaptor.getValue());
		assertEquals(111111, result.getId());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void saveAll() {
		List<Contribution> contributionsList = new ArrayList<>();
		Contribution contribution = new Contribution();
		contribution.setId(111111);
		Contribution contribution1 = new Contribution();
		contribution1.setId(222222);
		contributionsList.add(contribution);
		contributionsList.add(contribution1);

		ArgumentCaptor<List> contributionCaptor = ArgumentCaptor.forClass(List.class);

		doNothing().when(contributionDataDao).saveContributionData(contributionCaptor.capture());

		contributionService.saveAll(contributionsList);

		verify(contributionDataDao, times(1)).saveContributionData(any(List.class));
		assertEquals(2, contributionCaptor.getValue().size());
		assertEquals(111111, ((Contribution) contributionCaptor.getValue().get(0)).getId());
		assertEquals(222222, ((Contribution) contributionCaptor.getValue().get(1)).getId());
	}

	@Test
	public void getById() {
		long id = 111111;
		Contribution contribution = new Contribution();
		contribution.setId(id);

		when(contributionDao.findById(anyLong())).thenReturn(contribution);

		Contribution result = contributionService.getById(id);

		verify(contributionDao, times(1)).findById(id);
		assertEquals(id, result.getId());
	}

	@Test
	public void getPlanContributions() {
		List<Long> planSelectionIds = Arrays.asList(new Long(111111), new Long(222222));
		Map<String, List<BenefitPlanRate>> planRates = new HashMap<String, List<BenefitPlanRate>>();
		BenefitPlanRate planRate = new BenefitPlanRate();
		planRate.setPlanType("medical");
		List<BenefitPlanRate> planRatesList = Arrays.asList(planRate);
		planRates.put("key", planRatesList);
		boolean contributionRequired = true;

		Map<Long, List<PlanContribution>> contributions = new HashMap<Long, List<PlanContribution>>();

		when(strategyDataDao.getByPlanSelectionId(planSelectionIds, planRates, contributionRequired))
				.thenReturn(contributions);

		Map<Long, List<PlanContribution>> result = contributionService.getPlanContributions(planSelectionIds, planRates,
				contributionRequired);

		verify(strategyDataDao, times(1)).getByPlanSelectionId(planSelectionIds, planRates, contributionRequired);
		assertEquals(contributions, result);
	}

	@Test
	public void getByPlanSelectionIdAndName() {
		long planSelectionId = 111111;
		String coverageLevel = "employee";
		Contribution contribution = new Contribution();
		contribution.setId(222222);
		ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

		when(contributionDao.findByPlanSelectionIdAndCoverageLevel(idCaptor.capture(), nameCaptor.capture()))
				.thenReturn(contribution);

		Contribution result = contributionService.getByPlanSelectionIdAndName(planSelectionId, coverageLevel);

		verify(contributionDao, times(1)).findByPlanSelectionIdAndCoverageLevel(any(Long.class), any(String.class));
		assertEquals(Long.valueOf(planSelectionId), idCaptor.getValue());
		assertEquals(coverageLevel, nameCaptor.getValue());
		assertEquals(222222, result.getId());
	}

	@Test
	public void getContributions() {
		long planSelectionId = 11111;
		List<Contribution> contributionsList = new ArrayList<Contribution>();
		Contribution contribution = new Contribution();
		contribution.setId(111111);
		Contribution contribution1 = new Contribution();
		contribution1.setId(222222);
		contributionsList.add(contribution);
		contributionsList.add(contribution1);
		ArgumentCaptor<Long> planSelectionIdCaptor = ArgumentCaptor.forClass(Long.class);
		when(contributionDao.findByPlanSelectionId(planSelectionIdCaptor.capture())).thenReturn(contributionsList);

		List<Contribution> result = contributionService.getContributions(planSelectionId);

		verify(contributionDao, times(1)).findByPlanSelectionId(any(Long.class));
		assertEquals(Long.valueOf(planSelectionId), planSelectionIdCaptor.getValue());
		assertEquals(2, result.size());
		assertEquals(111111, result.get(0).getId());
		assertEquals(222222, result.get(1).getId());
	}

	@Test
	public void saveContribution() {
		Contribution contribution = new Contribution();
		contribution.setId(111111);
		ArgumentCaptor<Contribution> contributionCaptor = ArgumentCaptor.forClass(Contribution.class);

		when(contributionDao.saveAndFlush(contributionCaptor.capture())).thenReturn(contribution);

		Contribution result = contributionService.saveContribution(contribution);

		verify(contributionDao, times(1)).saveAndFlush(any(Contribution.class));
		assertEquals(111111, contributionCaptor.getValue().getId());
		assertEquals(111111, result.getId());
	}

	@Test
	public void deleteAll() {
		List<Contribution> contributionsList = new ArrayList<Contribution>();
		Contribution contribution = new Contribution();
		contribution.setId(111111);
		Contribution contribution1 = new Contribution();
		contribution1.setId(222222);
		contributionsList.add(contribution);
		contributionsList.add(contribution1);
		ArgumentCaptor<List> contributionsCaptor = ArgumentCaptor.forClass(List.class);

		doNothing().when(contributionDao).deleteAllInBatch(contributionsCaptor.capture());

		contributionService.deleteAll(contributionsList);

		verify(contributionDao, times(1)).deleteAllInBatch(any(List.class));
		assertEquals(2, contributionsCaptor.getValue().size());
		assertEquals(111111, ((Contribution) contributionsCaptor.getValue().get(0)).getId());
		assertEquals(222222, ((Contribution) contributionsCaptor.getValue().get(1)).getId());
	}

}