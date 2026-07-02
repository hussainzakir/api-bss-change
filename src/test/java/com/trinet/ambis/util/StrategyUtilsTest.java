package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.BenefitPlanRate;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class StrategyUtilsTest {

	@Mock
	PsCompanyDao psCompanyDao;

	String medicalPlan = "MEDPLAN";
	String dentalPlan = "DENTALPLAN";
	String visionPlan = "VISIONPLAN";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findBandCode() {

		Company company = prepareCompany();
		String actualResult;
		String benefitPlan = null;
		Map<String, XbssRealmPlyrPlan> plyrMap = this.preparePlyrMapping();

		// BOGUS - Test with bogus plan (null condition)
		benefitPlan = "BOGUS";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals("N", actualResult);

		// Bogus band locator
		benefitPlan = "TS4NUL";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals("N", actualResult);

		// AETNA - Test with HMO plan and HMO band code is not blank
		benefitPlan = "TS4S6K";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getAetnaHmoBandCode(), actualResult);

		// AETNA - Test with HMO plan and HMO band code is blank
		company.getBandCodes().setAetnaHmoBandCode("");
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getAetnaBandCode(), actualResult);

		// AETNA - Test with PPO plan and PPO band code is not blank
		benefitPlan = "TS4S6L";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getAetnaPpoBandCode(), actualResult);

		// AETNA - Test with PPO plan and PPO band code is blank
		company.getBandCodes().setAetnaPpoBandCode("");
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getAetnaBandCode(), actualResult);

		// AETNA - Test with POS plan (neither hmo or ppo)
		benefitPlan = "TS13HK";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getAetnaBandCode(), actualResult);

		// AETNA - Test with blank Aetna band code
		company.getBandCodes().setAetnaBandCode("");
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals("", actualResult);

		// UNC plan
		benefitPlan = "TS1EKW";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getUhcBandCode(), actualResult);

		// Kaiser plans
		benefitPlan = "TS13HH";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getKaiserBandCode(), actualResult);

		benefitPlan = "TS1EKY";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getKaiMidAtlBandCode(), actualResult);

		benefitPlan = "TS3GIB";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getKaiHawaiiBandCode(), actualResult);

		benefitPlan = "TS13HI";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getKaisCoBandCode(), actualResult);

		benefitPlan = "TS1EKS";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getKaisNwBandCode(), actualResult);

		// Blue Shield/Blue Cross plans
		benefitPlan = "TS13HJ";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getBsOfCaBandCode(), actualResult);

		benefitPlan = "TS1EKU";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getBcbsBandCode(), actualResult);

		benefitPlan = "TS1EKV";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getBcbsNcBandCode(), actualResult);

		benefitPlan = "TS1EKX";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getBcOfIdBandCode(), actualResult);

		benefitPlan = "TS1EL0";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getBcbsMNBandCode(), actualResult);

		// Tufts plan
		benefitPlan = "TS1998";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getTuftsBandCode(), actualResult);

		// Harvard Pilgrim plan
		benefitPlan = "TS4S6H";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getHarvardBandCode(), actualResult);

		// Empire plan
		benefitPlan = "TS4S6M";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getEmpireNYBand(), actualResult);

		// unbanded plan
		benefitPlan = "TS0TF8";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals("N", actualResult);

		// UNKNOWN vendor
		benefitPlan = "TS11LH";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals("N", actualResult);
		
		// UNKNOWN vendor
		benefitPlan = "TS4S6X";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals("200", actualResult);

		// Life plan
		benefitPlan = "TS0SRO";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getLifeBandCode(), actualResult);

		// LTD plan
		benefitPlan = "TS0SRS";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals(company.getBandCodes().getDisBandCode(), actualResult);

		// OTHER (Dental) plan type
		benefitPlan = "TS2J1T";
		actualResult = StrategyUtils.findBandCode(company, benefitPlan, plyrMap);
		assertEquals("N", actualResult);
	}

	@Test
	public void getPlanCost() {

		Map<String, List<BenefitPlanRate>> planRateMap;
		Map<String, BigDecimal> actualResult;
		String benefitPlan = medicalPlan;
		String benefitPlanN = "MEDPLAN-N";

		/*
		 * Test with null planRateMap
		 */
		planRateMap = new HashMap<String, List<BenefitPlanRate>>();
		actualResult = StrategyUtils.getPlanCost(planRateMap.get(benefitPlan));
		assertTrue(actualResult.isEmpty());

		/*
		 * Test with empty planRateMap
		 */
		planRateMap = new HashMap<String, List<BenefitPlanRate>>();
		planRateMap.put(benefitPlan, new ArrayList<BenefitPlanRate>());
		actualResult = StrategyUtils.getPlanCost(planRateMap.get(benefitPlan));
		assertTrue(actualResult.isEmpty());

		/*
		 * Test with populated planRateMap
		 */
		planRateMap = preparePlanRateMap();
		actualResult = StrategyUtils.getPlanCost(planRateMap.get(benefitPlan));
		assertEquals(1, actualResult.size());
		assertEquals(BigDecimal.valueOf(1000), actualResult.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));

		/*
		 * Test with populated planRateMap - bandCode of N
		 */
		actualResult = StrategyUtils.getPlanCost(planRateMap.get(benefitPlanN));
		assertEquals(1, actualResult.size());
		assertEquals(BigDecimal.valueOf(5000), actualResult.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
	}

    @Test
    public void getPlanCostTest2() {
        BenefitPlanRate planRate1 = new BenefitPlanRate();
        planRate1.setBenefitPlan(medicalPlan);
        planRate1.setBandCode("10");
        planRate1.setCoverageCode("1");
        planRate1.setEmployerCost(BigDecimal.valueOf(1000));

        BenefitPlanRate planRate2 = new BenefitPlanRate();
        planRate2.setBenefitPlan(medicalPlan);
        planRate2.setBandCode("10");
        planRate2.setCoverageCode("2");
        planRate2.setEmployerCost(BigDecimal.valueOf(2000));

        BenefitPlanRate planRate3 = new BenefitPlanRate();
        planRate3.setBenefitPlan(dentalPlan);
        planRate3.setBandCode("40");
        planRate3.setCoverageCode("1");
        planRate3.setEmployerCost(BigDecimal.valueOf(3000));

        BenefitPlanRate planRate4 = new BenefitPlanRate();
        planRate4.setBenefitPlan(dentalPlan);
        planRate4.setBandCode("50");
        planRate4.setCoverageCode("2");
        planRate4.setEmployerCost(BigDecimal.valueOf(4000));

        /*
         * Test with Null or empty rates
         */
        assertTrue(StrategyUtils.getPlanCost(null).isEmpty());
        assertTrue(StrategyUtils.getPlanCost(Collections.emptyList()).isEmpty());

        /*
         * Test with populated planRateMap
         */
        Map<String, BigDecimal> actualResult = StrategyUtils.getPlanCost(Arrays.asList(planRate1, planRate2));
        assertEquals(2, actualResult.size());
        assertEquals(BigDecimal.valueOf(1000), actualResult.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
        assertEquals(BigDecimal.valueOf(2000), actualResult.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));

        /*
         * Test with Inconsistent planIds
         */
        try {
            StrategyUtils.getPlanCost(Arrays.asList(planRate1, planRate3));
        } catch (IllegalArgumentException e) {
            assertEquals("All rates must have the same planId.", e.getMessage());
        }

        /*
         * Test with Inconsistent bandcode
         */
        try {
            StrategyUtils.getPlanCost(Arrays.asList(planRate3, planRate4));
        } catch (IllegalArgumentException e) {
            assertEquals("All rates must have the same bandCode.", e.getMessage());
        }
    }

	@Test
	public void getRegion() {
		String actualResult;
		String industry;

		/*
		 * Test Ambrose Set
		 */
		industry = "financialServices";
		actualResult = StrategyUtils.getRegion("CA", industry);
		assertEquals("California", actualResult);

		actualResult = StrategyUtils.getRegion("NY", industry);
		assertEquals("NewYork", actualResult);

		actualResult = StrategyUtils.getRegion("NV", industry);
		assertEquals("Nevada", actualResult);

		actualResult = StrategyUtils.getRegion("PR", industry);
		assertEquals("PuertoRico", actualResult);

		actualResult = StrategyUtils.getRegion("HI", industry);
		assertEquals("Hawaii", actualResult);

		actualResult = StrategyUtils.getRegion("DC", industry);
		assertEquals("Others", actualResult);

		/*
		 * Test SOI Set
		 */
		industry = "agricultureTransportationOther";
		actualResult = StrategyUtils.getRegion("AZ", industry);
		assertEquals("Arizona", actualResult);

		actualResult = StrategyUtils.getRegion("LA", industry);
		assertEquals("Others", actualResult);

		/*
		 * Test SOI No Med Set
		 */
		industry = "Exchange11industry";
		actualResult = StrategyUtils.getRegion("NY", industry);
		assertEquals("New York", actualResult);

		actualResult = StrategyUtils.getRegion("LA", industry);
		assertEquals("Others", actualResult);

		/*
		 * Test Passport Set
		 */
		industry = "ManufWhlSlRetWhtr";
		actualResult = StrategyUtils.getRegion("NY", industry);
		assertEquals("New York", actualResult);

		/*
		 * Test with fake industry
		 */
		industry = "00";
		actualResult = StrategyUtils.getRegion("CA", industry);
		assertEquals("California", actualResult);
	}

	@Test
	public void getRegionThrowException() {

		String industry;

		/*
		 * Test Ambrose Set with exception
		 */
		industry = "financialServices";
		StrategyUtils.getRegion("00", industry);
	}

	@Test
	public void getAllBenefitPlans() {

	}

	/*
	 * 
	 * Setup methods
	 * 
	 */
	private Company prepareCompany() {
		Company company = new Company();

		BandCodes bandCodes = new BandCodes();
		bandCodes.setAetnaBandCode("10");
		bandCodes.setAetnaHmoBandCode("20");
		bandCodes.setAetnaPpoBandCode("30");
		bandCodes.setUhcBandCode("40");
		bandCodes.setKaiserBandCode("50");
		bandCodes.setKaiMidAtlBandCode("60");
		bandCodes.setKaiHawaiiBandCode("70");
		bandCodes.setKaisCoBandCode("80");
		bandCodes.setBsOfCaBandCode("90");
		bandCodes.setTuftsBandCode("100");
		bandCodes.setBcbsBandCode("110");
		bandCodes.setBcbsNcBandCode("120");
		bandCodes.setBcOfIdBandCode("130");
		bandCodes.setKaisNwBandCode("140");
		bandCodes.setBcbsMNBandCode("150");
		bandCodes.setLifeBandCode("160");
		bandCodes.setDisBandCode("170");
		bandCodes.setEmpireNYBand("180");
		bandCodes.setHarvardBandCode("190");
		bandCodes.setHighmarkBandCode("200");
		company.setBandCodes(bandCodes);
		return company;
	}

	private Map<String, List<BenefitPlanRate>> preparePlanRateMap() {
		String benefitPlan;
		Map<String, List<BenefitPlanRate>> planRateMap = new HashMap<String, List<BenefitPlanRate>>();

		// Plan with regular band code
		benefitPlan = medicalPlan;
		List<BenefitPlanRate> planRateList = new ArrayList<BenefitPlanRate>();
		BenefitPlanRate planRate = new BenefitPlanRate();
		planRate.setBenefitPlan(benefitPlan);
		planRate.setBandCode("10");
		planRate.setCoverageCode("1");
		planRate.setEmployerCost(BigDecimal.valueOf(1000));
		planRateList.add(planRate);
		planRateMap.put(benefitPlan, planRateList);

		// Plan with N band code
		planRateList = new ArrayList<BenefitPlanRate>();
		benefitPlan = "MEDPLAN-N";
		planRate = new BenefitPlanRate();
		planRate.setBenefitPlan(benefitPlan);
		planRate.setBandCode("N");
		planRate.setCoverageCode("1");
		planRate.setEmployerCost(BigDecimal.valueOf(5000));
		planRateList.add(planRate);
		planRateMap.put(benefitPlan, planRateList);

		return planRateMap;
	}

	private Map<String, XbssRealmPlyrPlan> preparePlyrMapping() {
		Map<String, XbssRealmPlyrPlan> map = new HashMap<>();
		this.makePlyrObj(map, 19401, 24, "10", "TS0TF8", 9, "", "");
		this.makePlyrObj(map, 19402, 24, "10", "TS11LH", 9, "", "1");
		this.makePlyrObj(map, 19403, 24, "10", "TS13HF", 2, "", "2");
		this.makePlyrObj(map, 19404, 24, "10", "TS13HG", 2, "", "3");
		this.makePlyrObj(map, 19405, 24, "10", "TS13HH", 2, "", "4");
		this.makePlyrObj(map, 19406, 24, "10", "TS13HI", 2, "", "5");
		this.makePlyrObj(map, 19407, 24, "10", "TS13HJ", 2, "", "6");
		this.makePlyrObj(map, 19408, 24, "10", "TS13HK", 2, "", "7");
		this.makePlyrObj(map, 19409, 24, "10", "TS1998", 9, "", "8");
		this.makePlyrObj(map, 19410, 24, "10", "TS1EKS", 9, "", "9");
		this.makePlyrObj(map, 19411, 24, "10", "TS1EKU", 9, "", "A");
		this.makePlyrObj(map, 19412, 24, "10", "TS1EKV", 9, "", "B");
		this.makePlyrObj(map, 19413, 24, "10", "TS1EKW", 9, "", "C");
		this.makePlyrObj(map, 19414, 24, "10", "TS1EKX", 9, "", "D");
		this.makePlyrObj(map, 19415, 24, "10", "TS1EKY", 9, "", "E");
		this.makePlyrObj(map, 19416, 24, "10", "TS1EL0", 9, "", "F");
		this.makePlyrObj(map, 19417, 24, "10", "TS3GIB", 9, "", "G");
		this.makePlyrObj(map, 19418, 24, "10", "TS4S6K", 9, "", "H");
		this.makePlyrObj(map, 19419, 24, "10", "TS4S6L", 9, "", "I");
		this.makePlyrObj(map, 19420, 24, "10", "TS4S6M", 9, "", "J");
		this.makePlyrObj(map, 19420, 24, "10", "TS4S6N", 9, "", "K");
		this.makePlyrObj(map, 19420, 24, "10", "TS4S6H", 9, "", "L");
		this.makePlyrObj(map, 19420, 24, "10", "TS4S6X", 9, "", "M");
		this.makePlyrObj(map, 19420, 24, "10", "TS4NUL", 9, "", null);
		this.makePlyrObj(map, 19436, 24, "11", "TS2J1T", 16, "", "");
		this.makePlyrObj(map, 19471, 24, "14", "TS4S84", 15, "", "");
		this.makePlyrObj(map, 19472, 24, "1D", "TS0TFY", 3, "", "");
		this.makePlyrObj(map, 19498, 24, "1V", "TS4S8W", 15, "", "");
		this.makePlyrObj(map, 19499, 24, "23", "TS0SRO", 3, "", "");
		this.makePlyrObj(map, 19507, 24, "30", "TS0SRS", 3, "", "");
		this.makePlyrObj(map, 19511, 24, "31", "TS2J41", 3, "", "");
		return map;
	}

	private void makePlyrObj(Map<String, XbssRealmPlyrPlan> plyrMap, long id, long rpyId, String planType,
			String benefitPlan, long portId, String situs, String locator) {
		XbssRealmPlyrPlan plyr = new XbssRealmPlyrPlan();
		plyr.setId(id);
		plyr.setRealmYearId(BigDecimal.valueOf(rpyId));
		plyr.setPlanType(planType);
		plyr.setBenefitPlan(benefitPlan);
		plyr.setPortfolioId(BigDecimal.valueOf(portId));
		plyr.setBandLocator(locator);

		plyrMap.put(plyr.getBenefitPlan(), plyr);
	}
}
