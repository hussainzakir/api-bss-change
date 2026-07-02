/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanCompareConstants;
import com.trinet.ambis.persistence.dao.hrp.QuarterAndPlanYearDto;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.plancompare.dao.hrp.PlanCompareDao;
import com.trinet.ambis.persistence.plancompare.model.PlanYearDetailDto;
import com.trinet.ambis.service.impl.RealmPlanYearServiceImpl;

/**
 * @author rvutukuri
 *
 */
@RunWith(JUnit4.class)
public class RealmPlanYearServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	RealmPlanYearServiceImpl realmPlanYearService;

	@Mock
	RealmPlanYearDao realmPlanYearDao;
	
	@Mock
	PlanCompareDao planCompareDao;

	private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMock;

	private static final long ID = 10;
	private static final long REALM_ID = 3;
	private static final String QUARTER = "AL";
	private static final Date COMPANY_PLAN_START_DATE = new Date();
	private static final RealmPlanYear REALM_PLYR = new RealmPlanYear();
	private static final Date CURRENT_DATE = new Date();

	static {
		REALM_PLYR.setId(ID);
		REALM_PLYR.setRealmId(REALM_ID);
		REALM_PLYR.setOeQuarter(QUARTER);
		REALM_PLYR.setPlanYearEnd(CURRENT_DATE);
	}

	@Before
	public void setUp() {
		rulesAndConfigsUtilsMock = Mockito.mockStatic(RulesAndConfigsUtils.class);
		MockitoAnnotations.initMocks(this);
	}
	@After
	public void tearDown() {
		rulesAndConfigsUtilsMock.close();
	}

	@Test
	public void getRealmPlanYear() {
		when(realmPlanYearDao.findByRealmIdAndOeQuarterAndPlanYearStart(ID, QUARTER, COMPANY_PLAN_START_DATE))
				.thenReturn(REALM_PLYR);

		RealmPlanYear result = realmPlanYearService.getRealmPlanYear(ID, QUARTER, COMPANY_PLAN_START_DATE);

		verify(realmPlanYearDao, times(1)).findByRealmIdAndOeQuarterAndPlanYearStart(ID, QUARTER,
				COMPANY_PLAN_START_DATE);
		assertEquals(REALM_PLYR, result);
	}

	@Test
	public void getMaxRealmPlanYear() {
		when(realmPlanYearDao.getMaxRealmPlanYearByRealmIdAndQuarter(ID, QUARTER)).thenReturn(REALM_PLYR);

		RealmPlanYear result = realmPlanYearService.getMaxRealmPlanYear(ID, QUARTER);

		verify(realmPlanYearDao, times(1)).getMaxRealmPlanYearByRealmIdAndQuarter(ID, QUARTER);
		assertEquals(REALM_PLYR, result);
	}

	@Test
	public void getRealmPlanYearById() {
		when(realmPlanYearDao.findById(ID)).thenReturn(REALM_PLYR);

		RealmPlanYear result = realmPlanYearService.getRealmPlanYearById(ID);

		verify(realmPlanYearDao, times(1)).findById(ID);
		assertEquals(REALM_PLYR, result);
	}

	@Test
	public void getPreviousRealmPlanYear() {
		String code = "G48";

		when(realmPlanYearDao.findPreviousRealmPlanYearByRealmPlanYearId(code, ID)).thenReturn(REALM_PLYR);

		RealmPlanYear result = realmPlanYearService.getPreviousRealmPlanYear(code, ID);

		verify(realmPlanYearDao, times(1)).findPreviousRealmPlanYearByRealmPlanYearId(code, ID);
		assertEquals(REALM_PLYR, result);
	}

	@Test
	public void getPreviousRealmPlanYear1() {
		when(realmPlanYearDao.findPreviousRealmPlanYearByRealmIdAndOeQuarter(REALM_PLYR.getId(),
				REALM_PLYR.getRealmId(), REALM_PLYR.getOeQuarter())).thenReturn(REALM_PLYR);

		RealmPlanYear result = realmPlanYearService.getPreviousRealmPlanYear(REALM_PLYR);

		verify(realmPlanYearDao, times(1)).findPreviousRealmPlanYearByRealmIdAndOeQuarter(ID, 3, QUARTER);
		assertEquals(REALM_PLYR, result);
	}

	@Test
	public void getCurrentRealmPlanYear() {
		when(realmPlanYearDao.getCurrentRealmPlanYear(ID, QUARTER)).thenReturn(REALM_PLYR);

		RealmPlanYear result = realmPlanYearService.getCurrentRealmPlanYear(ID, QUARTER);

		verify(realmPlanYearDao, times(1)).getCurrentRealmPlanYear(ID, QUARTER);
		assertEquals(REALM_PLYR, result);
	}

	@Test
	public void getNextRealmPlanYear() {
		when(realmPlanYearDao.getNextRealmPlanYear(REALM_ID, QUARTER, CURRENT_DATE)).thenReturn(REALM_PLYR);

		RealmPlanYear result = realmPlanYearService.getNextRealmPlanYear(REALM_PLYR);

		verify(realmPlanYearDao, times(1)).getNextRealmPlanYear(REALM_ID, QUARTER, CURRENT_DATE);
		assertEquals(REALM_PLYR, result);
	}

	@Test
	public void getLatestRealmPlanYear() {
		when(realmPlanYearDao.findByLatestRealmIdAndOeQuarterAndPlanYearStart(REALM_ID, QUARTER,
				COMPANY_PLAN_START_DATE)).thenReturn(REALM_PLYR);

		RealmPlanYear result = realmPlanYearService.getLatestRealmPlanYear(REALM_ID, QUARTER, COMPANY_PLAN_START_DATE);

		verify(realmPlanYearDao, times(1)).findByLatestRealmIdAndOeQuarterAndPlanYearStart(REALM_ID, QUARTER,
				COMPANY_PLAN_START_DATE);
		assertEquals(REALM_PLYR, result);
	}

	@Test
	public void getRealmForCompanyId() {
		when(realmPlanYearDao.findByCompanyId(Mockito.anyLong())).thenReturn(REALM_PLYR);

		RealmPlanYear result = realmPlanYearService.getRealmForCompanyId(1);

		verify(realmPlanYearDao, times(1)).findByCompanyId(Mockito.anyLong());
		assertEquals(REALM_PLYR, result);
	}
	
	@Test
	public void getListOfOeQuartersAndPlanYears() {
		List<Object[]> listOfResult=new ArrayList<>();
		prepareData(listOfResult);
		when(realmPlanYearDao.getQuartersAndPlanYearsInfo()).thenReturn(listOfResult);
		List<QuarterAndPlanYearDto> quraterList=realmPlanYearService.getOeQuartersAndPlanYearsInfo();

		assertEquals(1, quraterList.size());

	}

	@Test
	public void getRealmPlanYearBy() {
		when(realmPlanYearDao.findByOeQuarter(COMPANY_PLAN_START_DATE, QUARTER)).thenReturn(REALM_PLYR);

		RealmPlanYear result = realmPlanYearService.findRealmPlanYearBy(COMPANY_PLAN_START_DATE, QUARTER);

		verify(realmPlanYearDao, times(1)).findByOeQuarter(COMPANY_PLAN_START_DATE, QUARTER);
		assertEquals(REALM_PLYR, result);
	}
	
	private void prepareData(List<Object[]> listOfResolt) {
		Object obj[]= {"pass","1","Q4",new Date()};
		listOfResolt.add(obj);
	}
	
	@Test
	public void findCurrentAndFuturePlanYearsBy() {
		Company company = getCompanyDetail().get();
		
		List<PlanYearDetailDto> planYearDetails = new ArrayList<>();
		planYearDetails.add(getCurrentPlanYearDetail().get());
		planYearDetails.add(getFuturePlanYearDetail().get());
		
		String planYearDate = LocalDate.now().format(DateTimeFormatter.ofPattern(BSSApplicationConstants.DATE_FORMAT_DD_MMM_YY));
		
		when(planCompareDao.findPlanYearDetailsBy(company.getQuater(), planYearDate, 0, 1)).thenReturn(planYearDetails);
		List<PlanYearDetailDto> currentAndFuturePlans = realmPlanYearService.findCurrentAndFuturePlanYearsBy(company.getCode(), company.getQuater());
		
		assertEquals(2, currentAndFuturePlans.size());
	}

	@Test
	public void getRenewalRiskTypeForLatestPlanYearInQuarter() {
		String quarter = "Q3";
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(1L);
		RiskTypeEnum riskTypeEnum = RiskTypeEnum.BANDS;

		when(realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter)).thenReturn(rpy);
		when(RulesAndConfigsUtils.getRenewalRiskType(rpy.getId())).thenReturn(riskTypeEnum);

		RiskTypeEnum result = realmPlanYearService.getRenewalRiskTypeForLatestPlanYearInQuarter(quarter);

		verify(realmPlanYearDao, times(1)).getMaxRealmPlanYearByQuarter(quarter);
		rulesAndConfigsUtilsMock.verify(() -> RulesAndConfigsUtils.getRenewalRiskType(rpy.getId()), times(1));
		assertEquals(riskTypeEnum, result);
	}
	
	private Supplier<Company> getCompanyDetail(){
		return () -> {
			Company company = new Company();
			company.setCode("SSO");
			company.setDescription("Test Company Inc");
			company.setQuater("SM");
			company.setTransitionPeriod(Boolean.TRUE);
			company.setRenewalOpen(Boolean.TRUE);
			return company;
		};
	}
	
	private Supplier<PlanYearDetailDto> getCurrentPlanYearDetail(){
		return () -> {
			PlanYearDetailDto plan = new PlanYearDetailDto();
			plan.setPlanYear(PlanCompareConstants.CURRENT.getAction());
			plan.setRealmYearId("56");
			return plan;
		};
	}
	
	private Supplier<PlanYearDetailDto> getFuturePlanYearDetail(){
		return () -> {
			PlanYearDetailDto plan = new PlanYearDetailDto();
			plan.setPlanYear(PlanCompareConstants.FUTURE.getAction());
			plan.setRealmYearId("66");
			return plan;
		};
	}

}
