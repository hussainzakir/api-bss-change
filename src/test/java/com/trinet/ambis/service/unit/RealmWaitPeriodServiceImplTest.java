package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.hrp.RealmWaitPeriodDao;
import com.trinet.ambis.service.impl.RealmWaitPeriodServiceImpl;
import com.trinet.ambis.service.model.WaitPeriod;

@RunWith(JUnit4.class)
public class RealmWaitPeriodServiceImplTest {

	@InjectMocks
	RealmWaitPeriodServiceImpl realmWaitPeriodService;

	@Mock
	RealmWaitPeriodDao realmWaitPeriodDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getWaitPeriodsByRelamPlanYear() {
		Long planYearId = new Long(10);
		ArgumentCaptor<Long> plyrIdCaptor = ArgumentCaptor.forClass(Long.class);

		when(realmWaitPeriodDao.getWaitPeriodsByRealmPlanYear(plyrIdCaptor.capture()))
				.thenReturn(new ArrayList<WaitPeriod>());

		realmWaitPeriodService.getWaitPeriodsByRelamPlanYear(planYearId);

		verify(realmWaitPeriodDao, times(1)).getWaitPeriodsByRealmPlanYear(any(Long.class));
		assertEquals(planYearId, plyrIdCaptor.getValue());
	}

	@Test
	public void getWaitPeriodDescr() {
		String waitPeriod= "FDOH";
		String waitPeriodDescr = "1st of month on/after DOH";
		when( realmWaitPeriodDao.getWaitPeriodDescriptions() ).thenReturn( generateWaitPeriodMap() );

		Map<String,String> map = realmWaitPeriodService.getWaitPeriodDescr();

		verify(realmWaitPeriodDao, times(1)).getWaitPeriodDescriptions();
		assertEquals( 7, map.size() );
		assertEquals( waitPeriodDescr, map.get( waitPeriod ) );
	}


	public static Map<String,String> generateWaitPeriodMap() {
		Map<String,String> waitPdMap = new HashMap<>();
		List<Object[]> results = generateWaitPeriodData();
		for( Object[] r : results ) {
			waitPdMap.put( (String) r[0], (String) r[1] );
		}
		return waitPdMap;
	}

	public static List<Object[]> generateWaitPeriodData() {
		List<Object[]> result = new ArrayList<>();
		result.add( createWaitPeriodRow( "EX90", "Exactly 90 days" ) );
		result.add( createWaitPeriodRow( "F30D", "1st of month on/after 1 service month" ) );
		result.add( createWaitPeriodRow( "F60D", "1st of month on/after 2 service months (90 day max)" ) );
		result.add( createWaitPeriodRow( "F90D", "1st of month following 3 month" ) );
		result.add( createWaitPeriodRow( "FDOH", "1st of month on/after DOH" ) );
		result.add( createWaitPeriodRow( "NONE", "Date of Hire (DOH)" ) );
		result.add( createWaitPeriodRow( "OTHR", "Other" ) );
		return result;
	}

	private static Object[] createWaitPeriodRow( String waitPeriod, String descr ) {
		Object[] row = { waitPeriod, descr };
		return row;
	}
}