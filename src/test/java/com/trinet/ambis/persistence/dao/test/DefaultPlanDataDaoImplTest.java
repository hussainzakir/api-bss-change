/**
 * 
 */
package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSApplicationConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.impl.DefaultPlanDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author schaudhari
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class DefaultPlanDataDaoImplTest {

	@InjectMocks
	DefaultPlanDataDaoImpl dao;

	@Mock
	EntityManager em;

	@Mock
	private Query mockedQuery;

    private MockedStatic<RulesAndConfigsUtils> mockedStaticRulesAndConfigsUtils;

    @Before
    public void setup() {
        mockedStaticRulesAndConfigsUtils = org.mockito.Mockito.mockStatic(RulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        mockedStaticRulesAndConfigsUtils.close();
    }

	@Test
	@Ignore
	public void insertStrategyDefaultAssignments_test() {
		when(em.createNamedQuery("INSERT_PROSP_STRATEGY_DEFAULT_PLAN_ASSIGNMENTS")).thenReturn(mockedQuery);
		
		dao.insertStrategyDefaultAssignmentsBy(Set.of("EMPL1", "EMPL2"), 1111L, Arrays.asList(1L), Arrays.asList(2L), Set.of("10"));

		verify(mockedQuery, times(1)).setParameter("DEN_VIS_BEN_TYPE_CODES", Arrays.asList("11", "1D", "14", "1V"));
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void getRegionalDefaultPlansByPlan_test() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setOeQuarter("Q1");
		realmPlanYear.setPlanYearEnd(new Date());

		Company company = new Company();
		company.setHeadQuatersState("FL");
		company.setRealmPlanYearId(1L);
		company.setRealmPlanYear(realmPlanYear);

		when(RulesAndConfigsUtils.findPickChooseWithExceptions(company)).thenReturn(true);
		when(em.createNamedQuery("REGIONAL_DEFAULT_PLANS_BY_PLAN")).thenReturn(mockedQuery);
		when(mockedQuery.getResultList()).thenReturn(preparePlanData());

		Map<String, Map<String, Long>> actualResults = dao.getRegionalDefaultPlansByPlanType(company);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());
		assertEquals(1, actualResults.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).get("PLAN_1").longValue());
		assertEquals(2, actualResults.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).get("PLAN_2").longValue());
		assertEquals(1, actualResults.get(BSSApplicationConstants.DENTAL_PLAN_TYPE).get("PLAN_3").longValue());

	}

	private List<Object[]> preparePlanData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[1] = "PLAN_1";
		r[2] = new BigDecimal(1);
		results.add(r);

		r = new Object[3];
		r[0] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[1] = "PLAN_2";
		r[2] = new BigDecimal(2);
		results.add(r);

		r = new Object[3];
		r[0] = BSSApplicationConstants.DENTAL_PLAN_TYPE;
		r[1] = "PLAN_3";
		r[2] = new BigDecimal(1);
		results.add(r);
		return results;
	}

}