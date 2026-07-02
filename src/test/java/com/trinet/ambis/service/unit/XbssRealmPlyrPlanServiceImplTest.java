package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.impl.RealmPlyrPlanServiceImpl;

@RunWith(JUnit4.class)
public class XbssRealmPlyrPlanServiceImplTest {

	@InjectMocks
	RealmPlyrPlanServiceImpl realmPlyrPlanService;

	@Mock
	XbssRealmPlyrPlanDao xbssRealmPlyrPlanDao;

	private static final String BENEFIT_PLAN = "30";
	private static final String PLAN_TYPE = "G48";
	private static final BigDecimal REALM_YR_ID = new BigDecimal(10);

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findByBenefitPlanAndPlanTypeAndRealmYearId() {
		List<XbssRealmPlyrPlan> plyrPlans = new ArrayList<>();
		XbssRealmPlyrPlan plyrPlan = new XbssRealmPlyrPlan();
		plyrPlan.setId(1234);
		plyrPlans.add(plyrPlan);

		ArgumentCaptor<String> benefitPlanArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> planTypeArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<BigDecimal> realmPlYrIdArgCaptor = ArgumentCaptor.forClass(BigDecimal.class);

		when(xbssRealmPlyrPlanDao.findByBenefitPlanAndPlanTypeAndRealmYearId(benefitPlanArgCaptor.capture(),
				planTypeArgCaptor.capture(), realmPlYrIdArgCaptor.capture())).thenReturn(plyrPlans);

		List<XbssRealmPlyrPlan> result = realmPlyrPlanService.findByBenefitPlanAndPlanTypeAndRealmYearId(BENEFIT_PLAN,
				PLAN_TYPE, REALM_YR_ID);

		verify(xbssRealmPlyrPlanDao, times(1)).findByBenefitPlanAndPlanTypeAndRealmYearId(any(String.class),
				any(String.class), any(BigDecimal.class));
		assertEquals(1, result.size());
		assertEquals(1234, result.get(0).getId());
	}

	@Test
	public void save() {
		XbssRealmPlyrPlan plyrPlan = new XbssRealmPlyrPlan();
		plyrPlan.setId(1234);
		plyrPlan.setRealmYearId( BigDecimal.valueOf( 99L ));
		plyrPlan.setPlanType("10");
		plyrPlan.setBenefitPlan("BENPLN");
		plyrPlan.setPortfolioId( BigDecimal.valueOf( 55L ));
		plyrPlan.setSitus( "AB" );
		plyrPlan.setBandLocator("Z");
		

		when(xbssRealmPlyrPlanDao.save(plyrPlan)).thenReturn(plyrPlan);

		XbssRealmPlyrPlan result = realmPlyrPlanService.save(plyrPlan);

		verify(xbssRealmPlyrPlanDao, times(1)).save(result);
		assertEquals(1234, result.getId());
		assertEquals(99L, result.getRealmYearId().longValue() );
		assertEquals("10", result.getPlanType() );
		assertEquals("BENPLN", result.getBenefitPlan() );
		assertEquals(55L, result.getPortfolioId().longValue());
		assertEquals("AB", result.getSitus() );
		assertEquals("Z", result.getBandLocator() );
	}

	@Test
	public void getMapForRealmPlanYearTest() {
		when(xbssRealmPlyrPlanDao.findByRealmYearId( Mockito.any( BigDecimal.class ))).thenReturn( preparePlyrData() );
		List<XbssRealmPlyrPlan> serviceListResult = realmPlyrPlanService.getForRealmPlanYear( 999L );
		assertEquals( 28, serviceListResult.size() );

		Map<String,XbssRealmPlyrPlan> serviceMapResult = realmPlyrPlanService.getMapForRealmPlanYear( 999L );
		assertEquals( 28, serviceMapResult.size() );
		assertEquals( "K", serviceMapResult.get( "TS4S6N" ).getBandLocator() );
	}
	
	@Test
	public void getPlanTypePlanMapForRealmPlanYearTest() {
		when(xbssRealmPlyrPlanDao.findByRealmYearIdInAndPlanTypeInOrderByRealmYearId(Mockito.anySet(),
				Mockito.anyList())).thenReturn(preparePlyrData());

		List<String> planTypes = new ArrayList<>();

		Map<String, List<XbssRealmPlyrPlan>> actualResult = realmPlyrPlanService.getPlanTypePlanMapForRealmPlanYear(1,
				planTypes);
		assertEquals(8, actualResult.size());
		assertEquals(21, actualResult.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).size());
		assertEquals(1, actualResult.get(BSSApplicationConstants.DENTAL_PLAN_TYPE).size());
		assertEquals(1, actualResult.get(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE).size());
		assertEquals(1, actualResult.get(BSSApplicationConstants.VISION_PLAN_TYPE).size());
		assertEquals(1, actualResult.get(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE).size());

	}
	
	
	private List<XbssRealmPlyrPlan> preparePlyrData() {
		List<XbssRealmPlyrPlan> mockData = new ArrayList<>();
		mockData.add( this.makePlyrObj( 19401, 24, "10", "TS0TF8", 9, "", "" ));
		mockData.add( this.makePlyrObj( 19402, 24, "10", "TS11LH", 9, "", "1" ));
		mockData.add( this.makePlyrObj( 19403, 24, "10", "TS13HF", 2, "", "2" ));
		mockData.add( this.makePlyrObj( 19404, 24, "10", "TS13HG", 2, "", "3" ));
		mockData.add( this.makePlyrObj( 19405, 24, "10", "TS13HH", 2, "", "4" ));
		mockData.add( this.makePlyrObj( 19406, 24, "10", "TS13HI", 2, "", "5" ));
		mockData.add( this.makePlyrObj( 19407, 24, "10", "TS13HJ", 2, "", "6" ));
		mockData.add( this.makePlyrObj( 19408, 24, "10", "TS13HK", 2, "", "7" ));
		mockData.add( this.makePlyrObj( 19409, 24, "10", "TS1998", 9, "", "8" ));
		mockData.add( this.makePlyrObj( 19410, 24, "10", "TS1EKS", 9, "", "9" ));
		mockData.add( this.makePlyrObj( 19411, 24, "10", "TS1EKU", 9, "", "A" ));
		mockData.add( this.makePlyrObj( 19412, 24, "10", "TS1EKV", 9, "", "B" ));
		mockData.add( this.makePlyrObj( 19413, 24, "10", "TS1EKW", 9, "", "C" ));
		mockData.add( this.makePlyrObj( 19414, 24, "10", "TS1EKX", 9, "", "D" ));
		mockData.add( this.makePlyrObj( 19415, 24, "10", "TS1EKY", 9, "", "E" ));
		mockData.add( this.makePlyrObj( 19416, 24, "10", "TS1EL0", 9, "", "F" ));
		mockData.add( this.makePlyrObj( 19417, 24, "10", "TS3GIB", 9, "", "G" ));
		mockData.add( this.makePlyrObj( 19418, 24, "10", "TS4S6K", 9, "", "H" ));
		mockData.add( this.makePlyrObj( 19419, 24, "10", "TS4S6L", 9, "", "I" ));
		mockData.add( this.makePlyrObj( 19420, 24, "10", "TS4S6M", 9, "", "J" ));
		mockData.add( this.makePlyrObj( 19420, 24, "10", "TS4S6N", 9, "", "K" ));
		mockData.add( this.makePlyrObj( 19436, 24, "11", "TS2J1T", 16, "", "" ));
		mockData.add( this.makePlyrObj( 19471, 24, "14", "TS4S84", 15, "", "" ));
		mockData.add( this.makePlyrObj( 19472, 24, "1D", "TS0TFY", 3, "", "" ));
		mockData.add( this.makePlyrObj( 19498, 24, "1V", "TS4S8W", 15, "", "" ));
		mockData.add( this.makePlyrObj( 19499, 24, "23", "TS0SRO", 3, "", "" ));
		mockData.add( this.makePlyrObj( 19507, 24, "30", "TS0SRS", 3, "", "" ));
		mockData.add( this.makePlyrObj( 19511, 24, "31", "TS2J41", 3, "", "" ));
		return mockData;
	}

	private XbssRealmPlyrPlan makePlyrObj( long id, long rpyId, String planType, String benefitPlan,
			long portId, String situs, String locator ) {
		XbssRealmPlyrPlan plyr = new XbssRealmPlyrPlan();
		plyr.setId( id );
		plyr.setRealmYearId( BigDecimal.valueOf( rpyId ) );
		plyr.setPlanType( planType );
		plyr.setBenefitPlan( benefitPlan );
		plyr.setPortfolioId( BigDecimal.valueOf( portId ) );
		plyr.setBandLocator( locator );
		plyr.setSitus(situs);
		
		return plyr;
	}

}