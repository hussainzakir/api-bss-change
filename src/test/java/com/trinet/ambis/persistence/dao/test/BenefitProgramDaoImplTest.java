package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.ps.impl.BenefitProgramDaoImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.PlanPackage;

@RunWith(JUnit4.class)
public class BenefitProgramDaoImplTest {

	@InjectMocks
	BenefitProgramDaoImpl benProgramDaoImpl;

	@Mock
	EntityManager entityManager;

	@Mock
	private Query mockedQuery;

	Company comp = null;
	BenefitGroup group = null;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		comp = new Company();
		comp.setCode("G48");
		comp.setPlanStartDate("2018/10/02");
		group = new BenefitGroup();
		when(entityManager.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void isBenProgInPS() {

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.ONE);
		boolean actualResult = benProgramDaoImpl.isBenProgInPS(comp, group);
		assertEquals(true, actualResult);		

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.ZERO);
		actualResult = benProgramDaoImpl.isBenProgInPS(comp, group);
		assertEquals(false, actualResult);

		verify(mockedQuery, times(2)).getSingleResult();
	}
	
	@Test
	public void deleteBenDefnPgm() {

		benProgramDaoImpl.deleteBenDefnPgm(comp, group);
	}

	@Test
	public void insertBenDefnPgm() {
		String cloneProgram = "CloneProg";
		comp.setCode("G48");
		group.setName("UPP");
		
		/*
		 * When funding type is BSUPP and not K1
		 */
		group.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		group.setBenefitOffers(prepareBenefitOfferList());
		when(mockedQuery.executeUpdate()).thenReturn(1);
		benProgramDaoImpl.insertBenDefnPgm(comp, group, cloneProgram);
		
		/*
		 * When funding type is not BSUPP and K1
		 */
		group.setType(BSSApplicationConstants.K1_GROUP_TYPE);
		group.getBenefitOffers().get(0).getPlanPackage().setFundingType(BSSApplicationConstants.BFPCT);
		when(mockedQuery.executeUpdate()).thenReturn(1);
		benProgramDaoImpl.insertBenDefnPgm(comp, group, cloneProgram);

		verify(mockedQuery, times(2)).executeUpdate();
	}

	@Test
	public void deleteBenDefnPlan() {
		Set<String> planTypes = new HashSet<String>();
		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.deleteBenDefnPlan(comp, group, planTypes);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void insertBenDefnPlan() {
		Set<String> planTypes = new HashSet<String>();
		String cloneProgram = "cloneProg";

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.insertBenDefnPlan(comp, group, cloneProgram, planTypes);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteBenDefnOptn() {
		Set<String> planTypes = new HashSet<String>();

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.deleteBenDefnOptn(comp, group, planTypes);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void insertBenDefnOptn() {
		Set<String> planTypes = new HashSet<String>();
		String cloneProgram = "cloneProg";

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.insertBenDefnOptn(comp, group, cloneProgram, planTypes);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteBenDefnCost() {
		Set<String> planTypes = new HashSet<String>();

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.deleteBenDefnCost(comp, group, planTypes);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void insertBenDefnCost() {
		Set<String> planTypes = new HashSet<String>();
		String cloneProgram = "cloneProg";

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.insertBenDefnCost(comp, group, cloneProgram, planTypes);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteAllBenDefnPlan() {

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.deleteAllBenDefnPlan(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}
	
	@Test
	public void insertAllBenDefnPlan() {
		String cloneProgram = "cloneProg";
		List<String> planTypeList = Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE, BSSApplicationConstants.DENTAL_PLAN_TYPE);
		when(mockedQuery.getResultList()).thenReturn(planTypeList);
		when(mockedQuery.executeUpdate()).thenReturn(1);
		
		benProgramDaoImpl.insertAllBenDefnPlan(comp, group, cloneProgram);
		verify(mockedQuery, times(1)).getResultList();
		verify(mockedQuery, times(1)).executeUpdate();		
	}

	@Test
	public void deleteAllBenDefnOptn() {

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.deleteAllBenDefnOptn(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}
	
	@Test
	public void insertAllBenDefnOptn() {
		String cloneProgram = "cloneProg";
		List<String> planTypeList = Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE, BSSApplicationConstants.DENTAL_PLAN_TYPE);
		when(mockedQuery.getResultList()).thenReturn(planTypeList);
		when(mockedQuery.executeUpdate()).thenReturn(1);
		
		benProgramDaoImpl.insertAllBenDefnOptn(comp, group, cloneProgram);
		verify(mockedQuery, times(1)).getResultList();
		verify(mockedQuery, times(1)).executeUpdate();		
	}

	@Test
	public void deleteAllBenDefnCost() {

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.deleteAllBenDefnCost(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void insertAllBenDefnCost() {
		String cloneProgram = "cloneProg";
		List<String> planTypeList = Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE, BSSApplicationConstants.DENTAL_PLAN_TYPE);
		when(mockedQuery.getResultList()).thenReturn(planTypeList);
		when(mockedQuery.executeUpdate()).thenReturn(1);
		
		benProgramDaoImpl.insertAllBenDefnCost(comp, group, cloneProgram);
		verify(mockedQuery, times(1)).getResultList();
		verify(mockedQuery, times(1)).executeUpdate();		
	}

	@Test
	public void deleteFuturePgm() {

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.deleteFuturePgm(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteFuturePlan() {

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.deleteFuturePlan(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteFutureOptn() {

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.deleteFutureOptn(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteFutureCost() {

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benProgramDaoImpl.deleteFutureCost(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}
	
	private List<BenefitOffer> prepareBenefitOfferList() {
		List<BenefitOffer> benefitOfferList = new ArrayList<>();
		BenefitOffer benefitOffer = new BenefitOffer();
		
		BenefitOfferSummary benefitOfferSummary = new BenefitOfferSummary();
		benefitOfferSummary.setType(BSSApplicationConstants.MEDICAL);
		benefitOfferSummary.setGroupId(1L);
		benefitOfferSummary.setDescription("BENEFIT_OFFER_SUMMARY");
		benefitOfferSummary.setHeadcount(10);
		benefitOfferSummary.setWaiverHeadcount(5);
		benefitOfferSummary.setEstimatedTotalCost(BigDecimal.valueOf(1500));
		benefitOfferSummary.setCurrentYearTotalCost(BigDecimal.valueOf(1000));
		benefitOfferSummary.setBsuppExcessAmount(BigDecimal.valueOf(500));
		benefitOfferSummary.setMinFunding(null);
		benefitOffer.setSummary(benefitOfferSummary);

		PlanPackage planPackage = new PlanPackage();
		planPackage.setId(1);
		planPackage.setTemplateId(1);
		planPackage.setFundingModelId(1);
		planPackage.setName("PLAN_PACKAGE_NAME");
		planPackage.setCustomized(true);
		planPackage.setEmployeePaid(true);
		planPackage.setFundingBasePlan("FUNDING_BASE_PLAN");
		planPackage.setWaiverAllowance(BigDecimal.valueOf(500));
		planPackage.setCompanyContributionPercent(BigDecimal.valueOf(80));
		planPackage.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		planPackage.setStrategyId(1L);
		planPackage.setFundingType(BSSApplicationConstants.BSUPP);
		planPackage.setBsuppExcessOption(BigDecimal.ONE);
		planPackage.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		planPackage.setCompanyId(1L);
		planPackage.setHeadCountPlans(new ArrayList<>());
		planPackage.setBsuppSelectedVolPlanTypes(new ArrayList<>());
		benefitOffer.setPlanPackage(planPackage);
		
		benefitOfferList.add(benefitOffer);
		return benefitOfferList;
	}

	
}