package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.hrp.DeselectionExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearRuleDao;
import com.trinet.ambis.persistence.model.RealmPlanYearRule;
import com.trinet.ambis.service.impl.RealmPlanYearRuleServiceImpl;

/**
 * @author hliddle
 *
 */
@RunWith(JUnit4.class)
public class RealmPlanYearRuleServiceImplTest {

	@InjectMocks
	private RealmPlanYearRuleServiceImpl realmPlanYearRuleService;

	@Mock
	RealmPlanYearRuleDao realmPlanYearRuleDao;

	@Mock
	DeselectionExceptionDao deselectionExceptionDao;


	private final long REALM_YEAR_ID = 50;
	private final String COMPANY_CODE = "3MP";
	private final Date EFF_DATE = new Date();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findByRealmPlanYearId() {
		long realmPlanYearId = 1;
		List<RealmPlanYearRule> realmPlanYearRuleList = new ArrayList<>();
		when(realmPlanYearRuleDao.findByIdRealmPlanYearId(realmPlanYearId)).thenReturn(realmPlanYearRuleList);
		realmPlanYearRuleService.findByRealmPlanYearId(realmPlanYearId);
		verify(realmPlanYearRuleDao, times(1)).findByIdRealmPlanYearId(realmPlanYearId);
	}


	@Test
	public void findPickChooseWithExceptionsZeroTest() {
		// test when both flags are zero (false)
		when( deselectionExceptionDao.getPickChooseWithException( REALM_YEAR_ID, COMPANY_CODE, EFF_DATE ) )
				.thenReturn( makeDaoResult( "PICK_CHOOSE_FLAG", 0, 0 ) );
		assertFalse( realmPlanYearRuleService.findPickChooseWithExceptions( REALM_YEAR_ID, COMPANY_CODE, EFF_DATE ) );
	}

	@Test
	public void findPickChooseWithExceptionsOneTest() {
		// test when flags are zero and one (false and true)
		when( deselectionExceptionDao.getPickChooseWithException( REALM_YEAR_ID, COMPANY_CODE, EFF_DATE ) )
				.thenReturn( makeDaoResult( "PICK_CHOOSE_FLAG", 0, 1 ) );
		assertTrue( realmPlanYearRuleService.findPickChooseWithExceptions( REALM_YEAR_ID, COMPANY_CODE, EFF_DATE ) );
	}

	@Test
	public void findPickChooseWithExceptionsTwoTest() {
		// test when flags are one and zero (true and false)
		when( deselectionExceptionDao.getPickChooseWithException( REALM_YEAR_ID, COMPANY_CODE, EFF_DATE ) )
				.thenReturn( makeDaoResult( "PICK_CHOOSE_FLAG", 1, 0 ) );
		assertTrue( realmPlanYearRuleService.findPickChooseWithExceptions( REALM_YEAR_ID, COMPANY_CODE, EFF_DATE ) );
	}

	@Test
	public void findPickChooseWithExceptionsThreeTest() {
		// test when both flags are one (true)
		when( deselectionExceptionDao.getPickChooseWithException( REALM_YEAR_ID, COMPANY_CODE, EFF_DATE ) )
				.thenReturn( makeDaoResult( "PICK_CHOOSE_FLAG", 1, 1 ) );
		assertTrue( realmPlanYearRuleService.findPickChooseWithExceptions( REALM_YEAR_ID, COMPANY_CODE, EFF_DATE ) );
	}


	private List<Object[]> makeDaoResult( String key, int value, int exception ) {
		List<Object[]> result = new ArrayList<>();
		result.add( new Object[] { key, new BigDecimal( value ), String.valueOf( exception ).charAt(0) } );
		return result;
	}
}
