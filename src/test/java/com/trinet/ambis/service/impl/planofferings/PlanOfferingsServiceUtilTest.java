package com.trinet.ambis.service.impl.planofferings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.test.config.TestHelper;

@RunWith(MockitoJUnitRunner.class)
public class PlanOfferingsServiceUtilTest extends ServiceUnitTest {

	@InjectMocks
	private PlanOfferingsServiceUtil planOfferingsServiceUtil;

	@Mock
	private RealmPlanYearDao realmPlanYearDaoMock;

	@Captor
	ArgumentCaptor<String> oeQuarterCaptor;

	@Captor
	ArgumentCaptor<Date> planYearStartCaptor;

	/**
	 * given PlanOfferingsRequest <br>
	 * when method is called <br>
	 * then build the company object
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void buildCompanyTest1() {
		// given
		// data
		PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();
		// method mocks
		when(realmPlanYearDaoMock.findByOeQuarterAndPlanYearStart(oeQuarterCaptor.capture(),
				planYearStartCaptor.capture())).thenReturn(realmPlanYear);
		// when
		Company actualResult = planOfferingsServiceUtil.buildCompany(planOfferingsRequest);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(BSSApplicationConstants.DUMMY, actualResult.getCode());
		assertEquals(74L, actualResult.getRealmPlanYear().getId());
		assertEquals(realmPlanYear.getPlanYearStart(), actualResult.getRealmPlanYear().getPlanYearStart());
		assertEquals("TriNet III", actualResult.getRealm().getBenExchange());
		assertEquals("95124", actualResult.getZipCode());
		assertEquals("Q4", actualResult.getQuater());
		assertEquals(74, actualResult.getRealmPlanYearId());
		assertEquals("01-Oct-2024", actualResult.getPlanStartDate());
		assertEquals("CA", actualResult.getHeadQuatersState());

		// verify
		verify(realmPlanYearDaoMock, times(1)).findByOeQuarterAndPlanYearStart(oeQuarterCaptor.getValue(),
				planYearStartCaptor.getValue());
	}

	private PlanOfferingsRequest preparePlanOfferingsRequest() {
		return TestHelper.readPlanComparisonRequest("/PlanOfferingsServiceUtilTest/buildCompanyTest1.json",
				new TypeReference<PlanOfferingsRequest>() {
				}).get();
	}

	private RealmPlanYear prepareRealmPlanYear() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(74L);
		realmPlanYear.setPlanYearStart(java.sql.Date.valueOf("2024-10-01"));
		return realmPlanYear;
	}

}
