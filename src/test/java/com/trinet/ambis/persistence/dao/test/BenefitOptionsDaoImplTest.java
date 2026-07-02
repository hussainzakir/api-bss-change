package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.exception.BSSApplicationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.ps.impl.BenefitOptionsDaoImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class BenefitOptionsDaoImplTest {
	
	@InjectMocks
	BenefitOptionsDaoImpl benOptionsDaoImpl;
	
	@Mock
	EntityManager entityManager;
	
	@Mock
	Query mockedQuery;
	
	private static final String LIFE_BAND_CODE = "LB01";
	private static final String DIS_BAND_CODE  = "DB01";

	Company comp = null;
	Company companyWithoutRiskType = null;
	BenefitGroup group = null;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		comp = new Company();
		comp.setCode("G48");
		comp.setPlanStartDate("2018/10/02");
		comp.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		BandCodes bandCodes = new BandCodes();
		bandCodes.setLifeBandCode(LIFE_BAND_CODE);
		bandCodes.setDisBandCode(DIS_BAND_CODE);
		comp.setBandCodes(bandCodes);
		companyWithoutRiskType = new Company();
		group = new BenefitGroup();
		when(entityManager.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void insertOptnEfdt() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.executeUpdate()).thenReturn(1);
		mockedQuery.setParameter(BSSQueryConstants.COMPANY, "G48");
		mockedQuery.setParameter(BSSQueryConstants.EFF_DATE_STR, "2018/04/12");

		// following line counts setEntityManager in code-coverage statistics
		benOptionsDaoImpl.setEntityManager( entityManager );
		int result = benOptionsDaoImpl.insertOptnEfdt(comp);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, result);
	}

	@Test
	public void insertOptn2() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOptn2(comp, group, LIFE_BAND_CODE, DIS_BAND_CODE);

		verify(mockedQuery, times(1)).executeUpdate();
		verify(mockedQuery).setParameter(BSSQueryConstants.RISK_TYPE, "DIFFERENTIALS");
		verify(mockedQuery).setParameter(BSSQueryConstants.LIFE_BAND_CODE, LIFE_BAND_CODE);
		verify(mockedQuery).setParameter(BSSQueryConstants.DIS_BAND_CODE, DIS_BAND_CODE);
		assertEquals(1, result);
	}

	@Test
	public void insertOptn2_usingCompanyWithoutRiskType() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOptn2(companyWithoutRiskType, group, null, null);

		verify(mockedQuery, times(1)).executeUpdate();
		verify(mockedQuery).setParameter(BSSQueryConstants.RISK_TYPE, "BANDS");
		verify(mockedQuery).setParameter(BSSQueryConstants.LIFE_BAND_CODE, (Object) null);
		verify(mockedQuery).setParameter(BSSQueryConstants.DIS_BAND_CODE, (Object) null);
		assertEquals(1, result);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn2_differentialsWithMissingLifeBandCode() {
		benOptionsDaoImpl.insertOptn2(comp, group, null, DIS_BAND_CODE);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn2_differentialsWithMissingDisBandCode() {
		benOptionsDaoImpl.insertOptn2(comp, group, LIFE_BAND_CODE, null);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn2_differentialsWithBlankLifeBandCode() {
		benOptionsDaoImpl.insertOptn2(comp, group, "   ", DIS_BAND_CODE);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn2_differentialsWithBlankDisBandCode() {
		benOptionsDaoImpl.insertOptn2(comp, group, LIFE_BAND_CODE, "\t");
	}


	@Test
	public void testPopulateHsaParameters() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.PF_CLIENT)).thenReturn("9XYZ");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("02-OCT-2018");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("000EF2");
		
		when(mockedQuery.getParameterValue(BSSQueryConstants.ER_HSA_LVL)).thenReturn("7");
		when(mockedQuery.getParameterValue(BSSQueryConstants.HSA_CNTB_FRQ)).thenReturn("M");
		when(mockedQuery.getParameterValue(BSSQueryConstants.ER_HSA_MON_EE)).thenReturn("20");
		when(mockedQuery.getParameterValue(BSSQueryConstants.ER_HSA_MON_FAM)).thenReturn("30");
		when(mockedQuery.getParameterValue(BSSQueryConstants.HSA_FRNT_FRQ)).thenReturn("Q");
		when(mockedQuery.getParameterValue(BSSQueryConstants.ER_HSA_FRT_EE)).thenReturn("201");
		when(mockedQuery.getParameterValue(BSSQueryConstants.ER_HSA_FRT_FAM)).thenReturn("301");
		
		when(mockedQuery.getParameterValue(BSSQueryConstants.HSA_FRT_PAYOUT)).thenReturn("301");
		when(mockedQuery.getParameterValue(BSSQueryConstants.HSA_FRTMNTH_Q1)).thenReturn("301");
		when(mockedQuery.getParameterValue(BSSQueryConstants.HSA_FRTMNTH_Q2)).thenReturn("301");
		when(mockedQuery.getParameterValue(BSSQueryConstants.HSA_FRTMNTH_Q3)).thenReturn("301");
		when(mockedQuery.getParameterValue(BSSQueryConstants.HSA_FRTMNTH_Q4)).thenReturn("301");

		StrategyHsaFundingDto hsa = null;
		int result = benOptionsDaoImpl.populateHsaParameters(comp, group, hsa );

		hsa = setupHsaFundingQ();
		result += benOptionsDaoImpl.populateHsaParameters(comp, group, hsa );
		
		hsa = setupHsaFundingA();
		result += benOptionsDaoImpl.populateHsaParameters(comp, group, hsa );

		verify(mockedQuery, times(3)).executeUpdate();
		assertEquals(3, result);
	}

	@Test
	public void insertOpt2A() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOpt2A(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, result);
	}

	@Test
	public void insertOpt2AFromBenProg() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOpt2AFromBenProg(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, result);
	}

	@Test
	public void insertOpt2ASkeleton() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.PF_CLIENT)).thenReturn("0044");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOpt2ASkeleton(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, result);
	}

	@Test
	public void setDefaultBenProg() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.setDefaultBenProg(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, result);
	}

	@Test
	public void insertOptn3() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOptn3(comp, group, LIFE_BAND_CODE, DIS_BAND_CODE);

		verify(mockedQuery, times(1)).executeUpdate();
		verify(mockedQuery).setParameter(BSSQueryConstants.RISK_TYPE, "DIFFERENTIALS");
		verify(mockedQuery).setParameter(BSSQueryConstants.LIFE_BAND_CODE, LIFE_BAND_CODE);
		verify(mockedQuery).setParameter(BSSQueryConstants.DIS_BAND_CODE, DIS_BAND_CODE);
		assertEquals(1, result);
	}

	@Test
	public void insertOptn3_usingCompanyWithoutRiskType() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOptn3(companyWithoutRiskType, group, null, null);

		verify(mockedQuery, times(1)).executeUpdate();
		verify(mockedQuery).setParameter(BSSQueryConstants.RISK_TYPE, "BANDS");
		verify(mockedQuery).setParameter(BSSQueryConstants.LIFE_BAND_CODE, (Object) null);
		verify(mockedQuery).setParameter(BSSQueryConstants.DIS_BAND_CODE, (Object) null);
		assertEquals(1, result);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3_differentialsWithMissingLifeBandCode() {
		benOptionsDaoImpl.insertOptn3(comp, group, null, DIS_BAND_CODE);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3_differentialsWithMissingDisBandCode() {
		benOptionsDaoImpl.insertOptn3(comp, group, LIFE_BAND_CODE, null);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3_differentialsWithBlankLifeBandCode() {
		benOptionsDaoImpl.insertOptn3(comp, group, "   ", DIS_BAND_CODE);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3_differentialsWithBlankDisBandCode() {
		benOptionsDaoImpl.insertOptn3(comp, group, LIFE_BAND_CODE, "\t");
	}

	@Test
	public void insertOptn3FromBenProg() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOptn3FromBenProg(comp, group, LIFE_BAND_CODE, DIS_BAND_CODE);

		verify(mockedQuery, times(1)).executeUpdate();
		verify(mockedQuery).setParameter(BSSQueryConstants.RISK_TYPE, "DIFFERENTIALS");
		verify(mockedQuery).setParameter(BSSQueryConstants.LIFE_BAND_CODE, LIFE_BAND_CODE);
		verify(mockedQuery).setParameter(BSSQueryConstants.DIS_BAND_CODE, DIS_BAND_CODE);
		assertEquals(1, result);
	}

	@Test
	public void insertOptn3FromBenProg_usingCompanyWithoutRiskType() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOptn3FromBenProg(companyWithoutRiskType, group, null, null);

		verify(mockedQuery, times(1)).executeUpdate();
		verify(mockedQuery).setParameter(BSSQueryConstants.RISK_TYPE, "BANDS");
		verify(mockedQuery).setParameter(BSSQueryConstants.LIFE_BAND_CODE, (Object) null);
		verify(mockedQuery).setParameter(BSSQueryConstants.DIS_BAND_CODE, (Object) null);
		assertEquals(1, result);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3FromBenProg_differentialsWithMissingBandCodes() {
		benOptionsDaoImpl.insertOptn3FromBenProg(comp, group, null, null);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3FromBenProg_differentialsWithMissingDisBandCode() {
		benOptionsDaoImpl.insertOptn3FromBenProg(comp, group, LIFE_BAND_CODE, null);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3FromBenProg_differentialsWithBlankLifeBandCode() {
		benOptionsDaoImpl.insertOptn3FromBenProg(comp, group, "", DIS_BAND_CODE);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3FromBenProg_differentialsWithBlankDisBandCode() {
		benOptionsDaoImpl.insertOptn3FromBenProg(comp, group, LIFE_BAND_CODE, " ");
	}

	@Test
	public void insertOptn3FromClone() {
		String cloneProgram = "CloneProg";
		String cloneCompany = "CloneComp";
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");
		when(mockedQuery.getParameterValue(BSSQueryConstants.CLONE_COMPANY)).thenReturn("CloneComp");
		when(mockedQuery.getParameterValue(BSSQueryConstants.CLONE_PROGRAM)).thenReturn("CloneProg");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOptn3FromClone(comp, group, cloneProgram, cloneCompany, LIFE_BAND_CODE, DIS_BAND_CODE);

		verify(mockedQuery, times(1)).executeUpdate();
		verify(mockedQuery).setParameter(BSSQueryConstants.RISK_TYPE, "DIFFERENTIALS");
		verify(mockedQuery).setParameter(BSSQueryConstants.LIFE_BAND_CODE, LIFE_BAND_CODE);
		verify(mockedQuery).setParameter(BSSQueryConstants.DIS_BAND_CODE, DIS_BAND_CODE);
		assertEquals(1, result);
	}

	@Test
	public void insertOptn3FromClone_usingCompanyWithoutRiskType() {
		String cloneProgram = "CloneProg";
		String cloneCompany = "CloneComp";
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");
		when(mockedQuery.getParameterValue(BSSQueryConstants.CLONE_COMPANY)).thenReturn("CloneComp");
		when(mockedQuery.getParameterValue(BSSQueryConstants.CLONE_PROGRAM)).thenReturn("CloneProg");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.insertOptn3FromClone(companyWithoutRiskType, group, cloneProgram, cloneCompany, null, null);

		verify(mockedQuery, times(1)).executeUpdate();
		verify(mockedQuery).setParameter(BSSQueryConstants.RISK_TYPE, "BANDS");
		verify(mockedQuery).setParameter(BSSQueryConstants.LIFE_BAND_CODE, (Object) null);
		verify(mockedQuery).setParameter(BSSQueryConstants.DIS_BAND_CODE, (Object) null);
		assertEquals(1, result);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3FromClone_differentialsWithMissingBandCodes() {
		benOptionsDaoImpl.insertOptn3FromClone(comp, group, "CloneProg", "CloneComp", null, null);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3FromClone_differentialsWithMissingDisBandCode() {
		benOptionsDaoImpl.insertOptn3FromClone(comp, group, "CloneProg", "CloneComp", LIFE_BAND_CODE, null);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3FromClone_differentialsWithBlankLifeBandCode() {
		benOptionsDaoImpl.insertOptn3FromClone(comp, group, "CloneProg", "CloneComp", " ", DIS_BAND_CODE);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertOptn3FromClone_differentialsWithBlankDisBandCode() {
		benOptionsDaoImpl.insertOptn3FromClone(comp, group, "CloneProg", "CloneComp", LIFE_BAND_CODE, "\n");
	}

	@Test
	public void resetOptn3Bands() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");
		when(mockedQuery.getParameterValue(BSSQueryConstants.REALM_YEAR_ID)).thenReturn(10);

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.resetOptn3Bands(comp, group, LIFE_BAND_CODE, DIS_BAND_CODE);

		verify(mockedQuery, times(1)).executeUpdate();
		verify(mockedQuery).setParameter(BSSQueryConstants.RISK_TYPE, "DIFFERENTIALS");
		verify(mockedQuery).setParameter(BSSQueryConstants.LIFE_BAND_CODE, LIFE_BAND_CODE);
		verify(mockedQuery).setParameter(BSSQueryConstants.DIS_BAND_CODE, DIS_BAND_CODE);
		assertEquals(1, result);
	}

	@Test
	public void resetOptn3Bands_nullRiskType() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");
		when(mockedQuery.getParameterValue(BSSQueryConstants.REALM_YEAR_ID)).thenReturn(10);

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int result = benOptionsDaoImpl.resetOptn3Bands(companyWithoutRiskType, group, null, null);

		verify(mockedQuery, times(1)).executeUpdate();
		verify(mockedQuery).setParameter(BSSQueryConstants.RISK_TYPE, "BANDS");
		verify(mockedQuery).setParameter(BSSQueryConstants.LIFE_BAND_CODE, (Object) null);
		verify(mockedQuery).setParameter(BSSQueryConstants.DIS_BAND_CODE, (Object) null);
		assertEquals(1, result);
	}

	@Test(expected = BSSApplicationException.class)
	public void resetOptn3Bands_differentialsWithMissingLifeBandCode() {
		benOptionsDaoImpl.resetOptn3Bands(comp, group, null, DIS_BAND_CODE);
	}

	@Test(expected = BSSApplicationException.class)
	public void resetOptn3Bands_differentialsWithMissingDisBandCode() {
		benOptionsDaoImpl.resetOptn3Bands(comp, group, LIFE_BAND_CODE, null);
	}

	@Test(expected = BSSApplicationException.class)
	public void resetOptn3Bands_differentialsWithBlankLifeBandCode() {
		benOptionsDaoImpl.resetOptn3Bands(comp, group, "", DIS_BAND_CODE);
	}

	@Test(expected = BSSApplicationException.class)
	public void resetOptn3Bands_differentialsWithBlankDisBandCode() {
		benOptionsDaoImpl.resetOptn3Bands(comp, group, LIFE_BAND_CODE, "   ");
	}

	@Test
	public void deleteFutureEfdt() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.PF_CLIENT)).thenReturn("pfClient");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benOptionsDaoImpl.deleteFutureEfdt(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteFutureOptn2() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.PF_CLIENT)).thenReturn("pfClient");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benOptionsDaoImpl.deleteFutureOptn2(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteFutureOpt2A() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.PF_CLIENT)).thenReturn("pfClient");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benOptionsDaoImpl.deleteFutureOpt2A(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteFutureOptn3() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("G48");
		when(mockedQuery.getParameterValue(BSSQueryConstants.EFF_DATE_STR)).thenReturn("2018/10/02");
		when(mockedQuery.getParameterValue(BSSQueryConstants.PF_CLIENT)).thenReturn("pfClient");
		when(mockedQuery.getParameterValue(BSSQueryConstants.BENEFIT_PROGRAM)).thenReturn("EF2");

		when(mockedQuery.executeUpdate()).thenReturn(1);

		benOptionsDaoImpl.deleteFutureOptn3(comp, group);

		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void getCloneCompany() {
		when(mockedQuery.getSingleResult()).thenReturn("G48");

		String result = benOptionsDaoImpl.getCloneCompany(comp);

		verify(mockedQuery, times(1)).getSingleResult();
		assertEquals("G48", result);
	}

	@Test(expected=BSSApplicationException.class)
	public void getCloneCompanyFail() {
		when(mockedQuery.getSingleResult()).thenThrow( new NoResultException("Mock NRE") );
		benOptionsDaoImpl.getCloneCompany(comp);
	}


	private StrategyHsaFundingDto setupHsaFundingA() {
		StrategyHsaFundingDto hsa = new StrategyHsaFundingDto();

		hsa.setStrategyId( 32165L );
		hsa.setOptionId( 6 );

		hsa.setLumpSumFrequency( "A" );
		hsa.setAnnualMonth( 6 );
		hsa.setAnnualEeAmount( new BigDecimal( "1001" ) );
		hsa.setAnnualFamilyAmount( new BigDecimal( "2001" ) );

		hsa.setQ1Month( null );
		hsa.setQ2Month( null );
		hsa.setQ3Month( null );
		hsa.setQ4Month( null );
		hsa.setQuarterlyEeAmount( null );
		hsa.setQuarterlyFamilyAmount( null );

		hsa.setContributionFrequency( null );
		hsa.setMonthlyEeAmount( null );
		hsa.setMonthlyFamilyAmount( null );

		return hsa;
	}

	private StrategyHsaFundingDto setupHsaFundingQ() {
		StrategyHsaFundingDto hsa = new StrategyHsaFundingDto();

		hsa.setStrategyId( 32165L );
		hsa.setOptionId( 7 );

		hsa.setLumpSumFrequency( "Q" );
		hsa.setAnnualMonth( null );
		hsa.setAnnualEeAmount( null );
		hsa.setAnnualFamilyAmount( null );

		hsa.setQ1Month( 2 );
		hsa.setQ2Month( 5 );
		hsa.setQ3Month( 8 );
		hsa.setQ4Month( 11 );
		hsa.setQuarterlyEeAmount( new BigDecimal( "201" ) );
		hsa.setQuarterlyFamilyAmount( new BigDecimal( "301" ) );

		hsa.setContributionFrequency( "M" );
		hsa.setMonthlyEeAmount( new BigDecimal( "20" ) );
		hsa.setMonthlyFamilyAmount( new BigDecimal( "30" ) );

		return hsa;
	}
}

