package com.trinet.ambis.persistence.dao.hrp;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.impl.StrategyGroupDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StrategyGroupDataDaoImplTest {

    @InjectMocks
    private StrategyGroupDataDaoImpl dao;

    @Mock private EntityManager em;
    @Mock private Query query;
    @Mock private Company company;
    @Mock private RealmPlanYear realmPlanYear;

	private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;

    @Before
    public void setup() {
	when(em.createNamedQuery("STRATEGY_PORTFOLIO_MISSING_PLANS")).thenReturn(query);
	lenient().when(em.createNamedQuery("STRATEGY_PORTFOLIO_MISSING_PLANS_V2")).thenReturn(query);
	lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
	mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
	mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);
    }

	@After
	public void tearDown() {
		if (mockStaticAppRulesAndConfigsUtils != null) {
			mockStaticAppRulesAndConfigsUtils.close();
			mockStaticAppRulesAndConfigsUtils = null;
		}
	}

    @Test
    public void getStrategyPortfolioMissingPlansTest() {
	long strategyId = 42L;
	ZoneId zone = ZoneId.of("America/Los_Angeles");
	Date planYearEnd = Date.from(LocalDate.of(2025, 12, 31).atStartOfDay(zone).toInstant());
	String oeQuarter = "Q1";
	Long planYearId = 777L;

	when(company.isTexasSitus()).thenReturn(true);
	when(company.getBundleId()).thenReturn(null);
	when(company.getRealmPlanYear()).thenReturn(realmPlanYear);
	when(company.getRealmPlanYearId()).thenReturn(planYearId);
	when(realmPlanYear.getPlanYearEnd()).thenReturn(planYearEnd);
	when(realmPlanYear.getOeQuarter()).thenReturn(oeQuarter);

	List<String> expected = Arrays.asList("PLAN1", "PLAN2");
	when(query.getResultList()).thenReturn(expected);

	List<String> result = dao.getStrategyPortfolioMissingPlans(strategyId, company, null, null);

	assertThat(result, is(expected));

	verify(em).createNamedQuery("STRATEGY_PORTFOLIO_MISSING_PLANS");
	verify(query).setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
	verify(query).setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_TX);
	verify(query).setParameter(BSSQueryConstants.EFF_DT, planYearEnd);
	verify(query).setParameter(BSSQueryConstants.BUNDLE_ID, BSSQueryConstants.ORACLE_NULL);
	verify(query).setParameter(BSSQueryConstants.PLAN_YEAR_ID, planYearId);
	verify(query).setParameter("oeQuarter", oeQuarter);

	@SuppressWarnings("unchecked")
	ArgumentCaptor<Set<String>> portfoliosCaptor = ArgumentCaptor.forClass(Set.class);
	@SuppressWarnings("unchecked")
	ArgumentCaptor<Set<String>> oorCaptor = ArgumentCaptor.forClass(Set.class);

	verify(query).setParameter(eq("portfolios"), portfoliosCaptor.capture());
	verify(query).setParameter(eq("outOfRegionPlans"), oorCaptor.capture());

	assertThat(portfoliosCaptor.getValue(),
		contains(equalTo(BSSQueryConstants.PORTFOLIOS_DUMMY)));
	assertThat(oorCaptor.getValue(),
		contains(equalTo(BSSQueryConstants.PLAN_TO_EXCLUDE)));
    }

	@Test
	public void getStrategyPortfolioMissingPlans_whenBundleV2Enabled_usesV2Query() {
		mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);

		long strategyId = 42L;
		ZoneId zone = ZoneId.of("America/Los_Angeles");
		Date planYearEnd = Date.from(LocalDate.of(2025, 12, 31).atStartOfDay(zone).toInstant());
		String oeQuarter = "Q1";
		Long planYearId = 777L;

		when(company.isTexasSitus()).thenReturn(true);
		when(company.getBundleId()).thenReturn(null);
		when(company.getRealmPlanYear()).thenReturn(realmPlanYear);
		when(company.getRealmPlanYearId()).thenReturn(planYearId);
		when(realmPlanYear.getPlanYearEnd()).thenReturn(planYearEnd);
		when(realmPlanYear.getOeQuarter()).thenReturn(oeQuarter);

		List<String> expected = Arrays.asList("PLAN1", "PLAN2");
		when(query.getResultList()).thenReturn(expected);

		List<String> result = dao.getStrategyPortfolioMissingPlans(strategyId, company, null, null);

		assertThat(result, is(expected));

		verify(em).createNamedQuery("STRATEGY_PORTFOLIO_MISSING_PLANS_V2");
		verify(query).setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		verify(query).setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_TX);
		verify(query).setParameter(BSSQueryConstants.EFF_DT, planYearEnd);
		verify(query).setParameter(BSSQueryConstants.BUNDLE_ID, BSSQueryConstants.ORACLE_NULL);
		verify(query).setParameter(BSSQueryConstants.PLAN_YEAR_ID, planYearId);
		verify(query).setParameter("oeQuarter", oeQuarter);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<String>> portfoliosCaptor = ArgumentCaptor.forClass(Set.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<String>> oorCaptor = ArgumentCaptor.forClass(Set.class);

		verify(query).setParameter(eq("portfolios"), portfoliosCaptor.capture());
		verify(query).setParameter(eq("outOfRegionPlans"), oorCaptor.capture());

		assertThat(portfoliosCaptor.getValue(), contains(equalTo(BSSQueryConstants.PORTFOLIOS_DUMMY)));
		assertThat(oorCaptor.getValue(), contains(equalTo(BSSQueryConstants.PLAN_TO_EXCLUDE)));
	}

	@Test
	public void getStrategyPortfolioMissingPlans_whenBundleV2Enabled_floridaSitus_usesV2Query() {
		mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);

		long strategyId = 10L;
		ZoneId zone = ZoneId.of("America/Los_Angeles");
		Date planYearEnd = Date.from(LocalDate.of(2025, 12, 31).atStartOfDay(zone).toInstant());
		String oeQuarter = "Q2";
		Long planYearId = 888L;

		when(company.isTexasSitus()).thenReturn(false);
		when(company.getBundleId()).thenReturn(null);
		when(company.getRealmPlanYear()).thenReturn(realmPlanYear);
		when(company.getRealmPlanYearId()).thenReturn(planYearId);
		when(realmPlanYear.getPlanYearEnd()).thenReturn(planYearEnd);
		when(realmPlanYear.getOeQuarter()).thenReturn(oeQuarter);

		List<String> expected = Arrays.asList("PLAN3");
		when(query.getResultList()).thenReturn(expected);

		List<String> result = dao.getStrategyPortfolioMissingPlans(strategyId, company, null, null);

		assertThat(result, is(expected));
		verify(em).createNamedQuery("STRATEGY_PORTFOLIO_MISSING_PLANS_V2");
		verify(query).setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_FL);
	}

	@Test
	public void getStrategyPortfolioMissingPlans_whenBundleV2Disabled_usesV1Query() {

		long strategyId = 55L;
		ZoneId zone = ZoneId.of("America/Los_Angeles");
		Date planYearEnd = Date.from(LocalDate.of(2025, 12, 31).atStartOfDay(zone).toInstant());
		String oeQuarter = "Q3";
		Long planYearId = 999L;

		when(company.isTexasSitus()).thenReturn(true);
		when(company.getBundleId()).thenReturn(null);
		when(company.getRealmPlanYear()).thenReturn(realmPlanYear);
		when(company.getRealmPlanYearId()).thenReturn(planYearId);
		when(realmPlanYear.getPlanYearEnd()).thenReturn(planYearEnd);
		when(realmPlanYear.getOeQuarter()).thenReturn(oeQuarter);

		List<String> expected = Arrays.asList("PLAN5", "PLAN6");
		when(query.getResultList()).thenReturn(expected);

		List<String> result = dao.getStrategyPortfolioMissingPlans(strategyId, company, null, null);

		assertThat(result, is(expected));
		verify(em).createNamedQuery("STRATEGY_PORTFOLIO_MISSING_PLANS");
		verify(query).setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		verify(query).setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_TX);
	}
}
