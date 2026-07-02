package com.trinet.ambis.helper;

import static com.trinet.ambis.enums.OmsOfferingEnum.OMB_TLD;
import static com.trinet.ambis.enums.OmsOfferingEnum.OM_OD_OV_TLD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.RealmRegionMinFunding;
import com.trinet.ambis.persistence.model.RealmRegionMinFundingPK;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.CompanyBandCodes;
import com.trinet.ambis.service.model.Industry;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.service.model.PlanYearCommonData;
import com.trinet.ambis.service.model.SelectionDate;
import com.trinet.ambis.service.model.UserData;
import com.trinet.ambis.util.Rules;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;

@RunWith(MockitoJUnitRunner.class)
public class CompanyServiceHelperTest {

	@Mock
	RealmPlanYearService realmPlanYearService;
	
	@Mock
	CompanyService companyService;
	
	@Mock
	RealmDataDao realmDataDao;
	
	@Mock
	MandatoryRegionDao mandatoryRegionDao;

    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setUp() {
        rulesAndConfigsUtilsMockedStatic = org.mockito.Mockito.mockStatic(RulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        rulesAndConfigsUtilsMockedStatic.close();
    }

	@Test
	public void getIndustry() {

		Industry actualResult;
		Company company;

		when( RulesAndConfigsUtils.findPickChooseWithExceptions( any( Company.class ) ) ).thenReturn( false );

		/*
		 * Test ACCORD
		 */
		company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(),"NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.HS, actualResult.getIndustryType());

		/*
		 * Test AMBROSE - 51, 52, DEFAULT
		 */
		company = prepareCompany(51,BenExchngEnums.TRINET_IV.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.TM, actualResult.getIndustryType());

		company = prepareCompany(52,BenExchngEnums.TRINET_IV.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.FS, actualResult.getIndustryType());

		company = prepareCompany(0,BenExchngEnums.TRINET_IV.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.BS, actualResult.getIndustryType());

		/*
		 * Test ALP
		 */
		company = prepareCompany(0,BenExchngEnums.TRINET_XI.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.EX, actualResult.getIndustryType());

		/*
		 * Test SOI
		 */
		company = prepareCompany(81,BenExchngEnums.TRINET_II.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.AT, actualResult.getIndustryType());

		company = prepareCompany(23,BenExchngEnums.TRINET_II.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.CN, actualResult.getIndustryType());

		company = prepareCompany(33,BenExchngEnums.TRINET_II.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.DG, actualResult.getIndustryType());

		company = prepareCompany(45,BenExchngEnums.TRINET_II.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.RW, actualResult.getIndustryType());

		company = prepareCompany(56,BenExchngEnums.TRINET_II.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.FB, actualResult.getIndustryType());

		company = prepareCompany(92,BenExchngEnums.TRINET_II.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.EH, actualResult.getIndustryType());

		company = prepareCompany(72,BenExchngEnums.TRINET_II.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.LH, actualResult.getIndustryType());

		company = prepareCompany(0,BenExchngEnums.TRINET_II.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.AT, actualResult.getIndustryType());

		company = prepareCompany(11,BenExchngEnums.TRINET_II.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.AT, actualResult.getIndustryType());

		/*
		 * Test PAS
		 */
		company = prepareCompany(49,BenExchngEnums.TRINET_III.getBenExchng(), "MN");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.MF, actualResult.getIndustryType());

		company = prepareCompany(51,BenExchngEnums.TRINET_III.getBenExchng(), "MN");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.IS, actualResult.getIndustryType());

		company = prepareCompany(53,BenExchngEnums.TRINET_III.getBenExchng(),"MN");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.FR, actualResult.getIndustryType());

		company = prepareCompany(54,BenExchngEnums.TRINET_III.getBenExchng(), "MN");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.PT, actualResult.getIndustryType());

		company = prepareCompany(92,BenExchngEnums.TRINET_III.getBenExchng(), "MN");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.AO, actualResult.getIndustryType());

		company = prepareCompany(0,BenExchngEnums.TRINET_III.getBenExchng(), "MN");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.IS, actualResult.getIndustryType());
		
		
		
		
		company = prepareCompany(11,BenExchngEnums.TRINET_III.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.AM, actualResult.getIndustryType());

		company = prepareCompany(61,BenExchngEnums.TRINET_III.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.EH, actualResult.getIndustryType());

		company = prepareCompany(53,BenExchngEnums.TRINET_III.getBenExchng(),"NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.FP, actualResult.getIndustryType());

		company = prepareCompany(72,BenExchngEnums.TRINET_III.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.LH, actualResult.getIndustryType());

		company = prepareCompany(44,BenExchngEnums.TRINET_III.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.RW, actualResult.getIndustryType());

		company = prepareCompany(81,BenExchngEnums.TRINET_III.getBenExchng(), "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertEquals(IndustryType.TC, actualResult.getIndustryType());

		/*
		 * Test not real realm
		 */
		company = prepareCompany(0,"NONE", "NY");
		actualResult = CompanyServiceHelper.getIndustry(company);
		assertNull(actualResult.getIndustryType());
	}

	@Test
	public void createBssCompany() {

		Company company;
		Company actualResult;

		company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(), "NY");

		/*
		 * Test for renewal company
		 */
		actualResult = CompanyServiceHelper.createBssCompany(company);
		assertEquals(company.getCode(), actualResult.getCode());
		assertNotNull(actualResult.getUpdateTime());
		assertEquals(BigDecimal.ZERO, actualResult.getCurrentYearTotalCost());
		assertEquals(company.getDescription(), actualResult.getDescription());
		assertEquals(company.getName(), actualResult.getName());
		assertEquals(company.getRealmPlanYearId(), actualResult.getRealmPlanYearId());
		assertEquals(company.getActualHeadCount(), actualResult.getActualHeadCount());
		assertEquals(company.getActualHeadCount(), actualResult.getHeadcount());

		/*
		 * Test for new company
		 */
		company.setRenewalCompany(false);
		actualResult = CompanyServiceHelper.createBssCompany(company);
		assertEquals(company.getCode(), actualResult.getCode());
		assertNotNull(actualResult.getUpdateTime());
		assertEquals(BigDecimal.ZERO, actualResult.getCurrentYearTotalCost());
		assertEquals(company.getDescription(), actualResult.getDescription());
		assertEquals(company.getName(), actualResult.getName());
		assertEquals(company.getRealmPlanYearId(), actualResult.getRealmPlanYearId());
		assertEquals(0, actualResult.getActualHeadCount());
		assertEquals(company.getHeadcount(), actualResult.getHeadcount());

	}

	@Test
	public void mapPSCompanyDataToBSSCompany() {
		Company company;
		Company bssCompany;

		company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(), "NY");

		/*
		 * Test company isEligAle true; renewal true
		 */
		bssCompany = new Company();
		CompanyServiceHelper.mapPSCompanyDataToBSSCompany(company, bssCompany);
		assertEquals(company.getBenefitProgram(), bssCompany.getBenefitProgram());
		assertEquals(company.getRealmPlanYear(), bssCompany.getRealmPlanYear());
		assertEquals(company.getRealm(), bssCompany.getRealm());
		assertEquals(company.getBandCodes(), bssCompany.getBandCodes());
		assertEquals(company.getRealmPlanYear().getAleAmount(), bssCompany.getAleAmount());
		assertEquals(company.isEligAle(), bssCompany.isEligAle());
		assertEquals(company.isBMGUser(), bssCompany.isBMGUser());
		assertEquals(company.isCSAUser(), bssCompany.isCSAUser());
		assertEquals(company.isTMTUser(), bssCompany.isTMTUser());
		assertEquals(company.getHeadQuatersCity(), bssCompany.getHeadQuatersCity());
		assertEquals(company.getHeadQuatersState(), bssCompany.getHeadQuatersState());
		assertEquals(company.isPayrollProcessed(), bssCompany.isPayrollProcessed());
		assertEquals(company.getQuater(), bssCompany.getQuater());
		assertEquals(company.getActualHeadCount(), bssCompany.getActualHeadCount());
		assertEquals(company.getIndustry(), bssCompany.getIndustry());
		assertEquals(company.isRenewalOpen(), bssCompany.isRenewalOpen());
		assertEquals(company.isRenewalCompany(), bssCompany.isRenewalCompany());
		assertEquals(company.isMbg(), bssCompany.isMbg());
		assertEquals(company.getPfClient(), bssCompany.getPfClient());
		assertEquals(company.isTransitionPeriod(), bssCompany.isTransitionPeriod());
		assertEquals(Utils.convertDateToString(company.getRealmPlanYear().getPlanYearEnd()),
				bssCompany.getPlanEndDate());
		assertEquals(company.getCompanySetupDate(), bssCompany.getCompanySetupDate());
		assertEquals(company.getSchedTbl(), bssCompany.getSchedTbl());
		assertEquals(company.isTexasSitus(), bssCompany.isTexasSitus());
		assertEquals(company.isK1Company(), bssCompany.isK1Company());
		assertEquals(company.getRegionalMinimumFundings(), bssCompany.getRegionalMinimumFundings());
		assertEquals(company.getExclusiveMedPlan(), bssCompany.getExclusiveMedPlan());
		assertEquals(company.getPlanStartDate(), bssCompany.getLiveDate());
		assertEquals(company.getPlanStartDate(), bssCompany.getPlanStartDate());

		/*
		 * Test the centralize method to determine the prospect converted new client
		 * ProspectConvertedOnboardingClientTrue_whenProspectConvertedAndNotRenewal
		 */
		company.setProspectConvertedClient(true);
		company.setRenewalCompany(false);
		bssCompany = new Company();
		CompanyServiceHelper.mapPSCompanyDataToBSSCompany(company, bssCompany);
		assertTrue(bssCompany.isProspectConvertedOnboardingClient());

		/*
		 * Test the centralize method to determine the prospect converted new client
		 * ProspectConvertedButRenewal_shouldNotSetOnboardingFlag
		 */
		company.setProspectConvertedClient(true);
		company.setRenewalCompany(true);
		bssCompany = new Company();
		CompanyServiceHelper.mapPSCompanyDataToBSSCompany(company, bssCompany);
		assertFalse(bssCompany.isProspectConvertedOnboardingClient());

		/*
		 * Test company isEligAle false; renewal false
		 */
		bssCompany = new Company();
		company.setEligAle(false);
		company.setRenewalCompany(false);
		CompanyServiceHelper.mapPSCompanyDataToBSSCompany(company, bssCompany);

		assertNull(bssCompany.getAleAmount());
		assertEquals(company.getPlanStartDate(), bssCompany.getPlanStartDate());

	}

	@Test
	public void populatePlanYearCommonData() {

		Company company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(), "NY");
		PlanYearCommonData actualResult = CompanyServiceHelper.populatePlanYearCommonData(company);
		assertEquals(company.getRealmPlanYear().getPlanYearStart(), actualResult.getEffectiveDate());
		assertEquals(company.getRealmPlanYear().getPlanYearEnd(), actualResult.getEndDate());
	}

	@Test
	public void populateCurrentPlanYearData() {

		PlanYearCommonData actualResult;
		Company company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(), "NY");
		RealmPlanYear currentRealmPlanYear = null;

		/*
		 * Test renewal true; currentRealmPlanYear from service is null
		 */
		when(realmPlanYearService.getPreviousRealmPlanYear(company.getRealmPlanYear()))
				.thenReturn(currentRealmPlanYear);
		actualResult = CompanyServiceHelper.populateCurrentPlanYearData(company, realmPlanYearService);
		assertEquals(company.getRealmPlanYear().getPlanYearStart(), actualResult.getEffectiveDate());
		assertEquals(company.getRealmPlanYear().getPlanYearEnd(), actualResult.getEndDate());

		/*
		 * Test renewal true; currentRealmPlanYear from service is not null
		 */
		currentRealmPlanYear = new RealmPlanYear();
		currentRealmPlanYear.setPlanYearStart(new Date());
		currentRealmPlanYear.setPlanYearEnd(new Date());
		when(realmPlanYearService.getPreviousRealmPlanYear(company.getRealmPlanYear()))
				.thenReturn(currentRealmPlanYear);
		actualResult = CompanyServiceHelper.populateCurrentPlanYearData(company, realmPlanYearService);
		assertEquals(currentRealmPlanYear.getPlanYearStart(), actualResult.getEffectiveDate());
		assertEquals(currentRealmPlanYear.getPlanYearEnd(), actualResult.getEndDate());

		/*
		 * Test renewal false
		 */
		company.setRenewalCompany(false);
		actualResult = CompanyServiceHelper.populateCurrentPlanYearData(company, realmPlanYearService);
		assertEquals(company.getRealmPlanYear().getPlanYearStart(), actualResult.getEffectiveDate());
		assertEquals(company.getRealmPlanYear().getPlanYearEnd(), actualResult.getEndDate());
	}

	@Test
	public void populateUserCommonData() {
		UserData actualResult;
		Company company = new Company();

		/*
		 * Test isCSAUser true, TMTUser false
		 */
		company.setCSAUser(true);
		company.setTMTUser(false);
		actualResult = CompanyServiceHelper.populateUserCommonData(company);
		assertTrue(actualResult.getCsaUser());
		assertFalse(actualResult.getBmgUser());
		assertFalse(actualResult.isTmtUser());

		/*
		 * Test isCSAUser false, BMGUser true, TMTUser true
		 */
		company.setCSAUser(false);
		company.setBMGUser(true);
		company.setTMTUser(true);
		actualResult = CompanyServiceHelper.populateUserCommonData(company);
		assertFalse(actualResult.getCsaUser());
		assertTrue(actualResult.getBmgUser());
		assertTrue(actualResult.isTmtUser());
	}

	@Test
	public void populateSecheduleTableData() {
		Company company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(), "NY");
		Date minus2Days = new DateTime().minusDays(2).toDate();
		String minus2DaysFormatted = CommonServiceHelper.formatDateToString(minus2Days,
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);

		CompanyServiceHelper.populateScheduleTableData(company, company.getRealmPlanYear());
		assertEquals(minus2DaysFormatted, CommonServiceHelper.formatDateToString(company.getSchedTbl().getOpenDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		assertEquals(minus2DaysFormatted, CommonServiceHelper.formatDateToString(company.getSchedTbl().getCloseDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		assertEquals(minus2DaysFormatted, CommonServiceHelper.formatDateToString(
				company.getSchedTbl().getExtensionEndDate(), BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		assertEquals(company.getRealmPlanYear().getId(), company.getSchedTbl().getSched().getRealmYearId());
		assertEquals(company.getCode(), company.getSchedTbl().getSched().getCompany());
	}

	@Test
	public void constructBlankScheduleDates() {

		SchedTbl schedTbl = new SchedTbl();
		Company company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(), "NY");
		Date minus2Days = new DateTime().minusDays(2).toDate();
		String minus2DaysFormatted = CommonServiceHelper.formatDateToString(minus2Days,
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);

		CompanyServiceHelper.constructBlankScheduleDates(schedTbl, company.getRealmPlanYearId(), company.getCode());
		assertEquals(minus2DaysFormatted, CommonServiceHelper.formatDateToString(schedTbl.getOpenDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		assertEquals(minus2DaysFormatted, CommonServiceHelper.formatDateToString(schedTbl.getCloseDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		assertEquals(minus2DaysFormatted, CommonServiceHelper.formatDateToString(schedTbl.getExtensionEndDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		assertEquals(company.getRealmPlanYearId(), schedTbl.getSched().getRealmYearId());
		assertEquals(company.getCode(), schedTbl.getSched().getCompany());
	}

	@Test
	public void constructSelectionDateTest1() {
		// close date before extension end date
		java.util.Date intOpenDate = java.sql.Date.valueOf( "2023-10-01" );
		java.util.Date intCloseDate = java.sql.Date.valueOf( "2023-10-31" );
		java.util.Date openDate = java.sql.Date.valueOf( "2023-10-07" );
		java.util.Date closeDate = java.sql.Date.valueOf( "2023-10-31" );
		java.util.Date extEndDate = java.sql.Date.valueOf( "2023-11-10" );

		SchedTbl schedTbl = new SchedTbl();
		schedTbl.setInternalOpenDate( intOpenDate );
		schedTbl.setInternalCloseDate( intCloseDate );
		schedTbl.setOpenDate( openDate );
		schedTbl.setCloseDate( closeDate );
		schedTbl.setExtensionEndDate( extEndDate );

		SelectionDate selDate = CompanyServiceHelper.constructSelectionDate( schedTbl );

		assertEquals( intOpenDate, selDate.getInternalOpenDate() );
		assertEquals( intCloseDate, selDate.getInternalCloseDate() );
		assertEquals( openDate, selDate.getExternalOpenDate() );
		assertEquals( extEndDate, selDate.getExternalCloseDate() );
	}

	@Test
	public void constructSelectionDateTest2() {
		// close date after extension end date
		java.util.Date intOpenDate = java.sql.Date.valueOf( "2023-10-01" );
		java.util.Date intCloseDate = java.sql.Date.valueOf( "2023-10-31" );
		java.util.Date openDate = java.sql.Date.valueOf( "2023-10-07" );
		java.util.Date closeDate = java.sql.Date.valueOf( "2023-11-07" );
		java.util.Date extEndDate = java.sql.Date.valueOf( "2023-10-31" );

		SchedTbl schedTbl = new SchedTbl();
		schedTbl.setInternalOpenDate( intOpenDate );
		schedTbl.setInternalCloseDate( intCloseDate );
		schedTbl.setOpenDate( openDate );
		schedTbl.setCloseDate( closeDate );
		schedTbl.setExtensionEndDate( extEndDate );

		SelectionDate selDate = CompanyServiceHelper.constructSelectionDate( schedTbl );

		assertEquals( intOpenDate, selDate.getInternalOpenDate() );
		assertEquals( intCloseDate, selDate.getInternalCloseDate() );
		assertEquals( openDate, selDate.getExternalOpenDate() );
		assertEquals( closeDate, selDate.getExternalCloseDate() );
	}

	@Test
	public void isRenewalOpen() {

		boolean actualResult;
		Company company;
		RealmPlanYear realmPlanYear;
		SchedTbl schedTbl;

		realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearStart(new Date());

		company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(), "NY");
		/*
		 * Test when schedTbl is null
		 */
		schedTbl = null;
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertNull(company.getSchedTbl());

		// schedTbl open and one extension date is null
		schedTbl = new SchedTbl();
		schedTbl.setOpenDate(new DateTime().minusDays(2).toDate());
		schedTbl.setExtensionEndDate(null);
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// schedTbl open and one extension date is null
		schedTbl = new SchedTbl();
		schedTbl.setOpenDate(null);
		schedTbl.setExtensionEndDate(new DateTime().plusDays(2).toDate());
		schedTbl.setCloseDate(new DateTime().plusDays(2).toDate());
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// schedTbl not in Renewal Period
		schedTbl = new SchedTbl();
		schedTbl.setOpenDate(new DateTime().plusDays(1).toDate());
		schedTbl.setExtensionEndDate(new DateTime().plusDays(2).toDate());
		schedTbl.setCloseDate(new DateTime().plusDays(2).toDate());
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// schedTbl In Renewal Period
		schedTbl = new SchedTbl();
		schedTbl.setOpenDate(new DateTime().minusDays(2).toDate());
		schedTbl.setExtensionEndDate(new DateTime().plusDays(2).toDate());
		schedTbl.setCloseDate(new DateTime().plusDays(2).toDate());
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertTrue(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// CSA user and is not Internal Date
		schedTbl = new SchedTbl();
		company.setCSAUser(true);
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// CSA user and is Internal Date
		schedTbl = new SchedTbl();
		schedTbl.setInternalOpenDate(new DateTime().minusDays(2).toDate());
		schedTbl.setInternalCloseDate(new DateTime().plusDays(2).toDate());
		company.setCSAUser(true);
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertTrue(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// BMG user and is Internal Date
		schedTbl = new SchedTbl();
		schedTbl.setInternalOpenDate(new DateTime().minusDays(2).toDate());
		schedTbl.setInternalCloseDate(new DateTime().plusDays(2).toDate());
		company.setBMGUser(true);
		company.setCSAUser(false);
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertTrue(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// BMG user and Internal close date is null
		schedTbl = new SchedTbl();
		schedTbl.setInternalOpenDate(new DateTime().minusDays(2).toDate());
		company.setBMGUser(true);
		company.setCSAUser(false);
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// BMG user and is not Internal Date
		schedTbl = new SchedTbl();
		schedTbl.setInternalOpenDate(new DateTime().minusDays(10).toDate());
		schedTbl.setInternalCloseDate(new DateTime().minusDays(5).toDate());
		company.setBMGUser(true);
		company.setCSAUser(false);
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// Ben Advisor user and is Internal Date
		schedTbl = new SchedTbl();
		schedTbl.setInternalOpenDate(new DateTime().minusDays(2).toDate());
		schedTbl.setInternalCloseDate(new DateTime().plusDays(2).toDate());
		company.setBenAdvisorUser(true);
		company.setBMGUser(false);
		company.setCSAUser(false);
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertTrue(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// Ben Advisor and Internal close date is null
		schedTbl = new SchedTbl();
		schedTbl.setInternalOpenDate(new DateTime().minusDays(2).toDate());
		company.setBenAdvisorUser(true);
		company.setBMGUser(false);
		company.setCSAUser(false);
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// Ben Advisor and is not Internal Date
		schedTbl = new SchedTbl();
		schedTbl.setInternalOpenDate(new DateTime().minusDays(10).toDate());
		schedTbl.setInternalCloseDate(new DateTime().minusDays(5).toDate());
		company.setBenAdvisorUser(true);
		company.setBMGUser(false);
		company.setCSAUser(false);
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// schedTbl Not In Renewal Period one null date value
		schedTbl = new SchedTbl();
		schedTbl.setExtensionEndDate(new DateTime().minusDays(10).toDate());
		schedTbl.setCloseDate(new DateTime().plusDays(2).toDate());
		realmPlanYear.setPlanYearStart(null);
		company.setBenAdvisorUser(false);
		company.setBMGUser(false);
		company.setCSAUser(false);
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// schedTbl Not In Renewal Period one null date value
		schedTbl = new SchedTbl();
		schedTbl.setExtensionEndDate(null);
		realmPlanYear.setPlanYearStart(new DateTime().minusDays(2).toDate());
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// schedTbl Not In Renewal Period and not transition Period
		schedTbl = new SchedTbl();
		schedTbl.setExtensionEndDate(new DateTime().minusDays(2).toDate());
		schedTbl.setCloseDate(new DateTime().plusDays(2).toDate());
		realmPlanYear.setPlanYearStart(new DateTime().minusDays(1).toDate());
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertFalse(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());

		// schedTbl Not In Renewal Period and in transition Period
		schedTbl = new SchedTbl();
		schedTbl.setExtensionEndDate(new DateTime().minusDays(2).toDate());
		schedTbl.setCloseDate(new DateTime().plusDays(2).toDate());
		realmPlanYear.setPlanYearStart(new DateTime().plusDays(2).toDate());
		actualResult = CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
		assertFalse(actualResult);
		assertTrue(company.isTransitionPeriod());
		assertEquals(schedTbl, company.getSchedTbl());
		assertEquals(company.isPayrollProcessed(), company.getSchedTbl().isPayrollProcessed());
	}

	@Test
	public void getBssBandCodeList() {

		List<CompanyBandCodes> actualResult = new ArrayList<CompanyBandCodes>();
		Company company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(), "NY");

		actualResult = CompanyServiceHelper.getBssBandCodeList(company.getId(), company.getBandCodes());
		assertEquals(20, actualResult.size());
		assertEquals("10", actualResult.get(0).getBandCodeValue());
	}

	@Test
	public void compareBandCodes() {

		boolean actualResult;
		List<CompanyBandCodes> bssBandCodes;
		List<CompanyBandCodes> psBandCodes;

		// both are empty
		bssBandCodes = new ArrayList<CompanyBandCodes>();
		psBandCodes = new ArrayList<CompanyBandCodes>();
		actualResult = CompanyServiceHelper.compareBandCodes(bssBandCodes, psBandCodes);
		assertTrue(actualResult);

		// equal
		bssBandCodes = new ArrayList<CompanyBandCodes>();
		bssBandCodes.add(new CompanyBandCodes(1000L, "AETNA", "10"));
		bssBandCodes.add(new CompanyBandCodes(1000L, "AETNAHMO", "20"));
		bssBandCodes.add(new CompanyBandCodes(1000L, "AETNAPPO", "30"));
		psBandCodes = new ArrayList<CompanyBandCodes>();
		psBandCodes.add(new CompanyBandCodes(1000L, "AETNA", "10"));
		psBandCodes.add(new CompanyBandCodes(1000L, "AETNAHMO", "20"));
		psBandCodes.add(new CompanyBandCodes(1000L, "AETNAPPO", "30"));
		actualResult = CompanyServiceHelper.compareBandCodes(bssBandCodes, psBandCodes);
		assertTrue(actualResult);

		// not equal one missing
		bssBandCodes = new ArrayList<CompanyBandCodes>();
		bssBandCodes.add(new CompanyBandCodes(1000L, "AETNA", "10"));
		bssBandCodes.add(new CompanyBandCodes(1000L, "AETNAHMO", "20"));
		bssBandCodes.add(new CompanyBandCodes(1000L, "AETNAPPO", "30"));
		psBandCodes = new ArrayList<CompanyBandCodes>();
		psBandCodes.add(new CompanyBandCodes(1000L, "AETNA", "10"));
		psBandCodes.add(new CompanyBandCodes(1000L, "AETNAHMO", "20"));
		actualResult = CompanyServiceHelper.compareBandCodes(bssBandCodes, psBandCodes);
		assertFalse(actualResult);

		// not equal
		bssBandCodes = new ArrayList<CompanyBandCodes>();
		bssBandCodes.add(new CompanyBandCodes(1000L, "AETNA", "10"));
		bssBandCodes.add(new CompanyBandCodes(1000L, "AETNAHMO", "20"));
		bssBandCodes.add(new CompanyBandCodes(1000L, "AETNAPPO", "30"));
		psBandCodes = new ArrayList<CompanyBandCodes>();
		psBandCodes.add(new CompanyBandCodes(1000L, "AETNA", "10"));
		psBandCodes.add(new CompanyBandCodes(1000L, "AETNAHMO", "20"));
		psBandCodes.add(new CompanyBandCodes(1000L, "AETNAPPO", "50"));
		actualResult = CompanyServiceHelper.compareBandCodes(bssBandCodes, psBandCodes);
		assertFalse(actualResult);
	}

	
	

	@Test
	public void saveStrategyUpdateCompany() {

		Company company;

		company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(), "NY");
		CompanyServiceHelper.saveStrategyUpdateCompany(10, company);
		assertEquals(10, company.getHeadcount());
		assertEquals(
				CommonServiceHelper.formatDateToString(new Date(), BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY),
				CommonServiceHelper.formatDateToString(company.getUpdateTime(),
						BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));

		company = prepareCompany(51,BenExchngEnums.TRINET_I.getBenExchng(), "NY");
		CompanyServiceHelper.saveStrategyUpdateCompany(10, company);
	}

	// Test plan year minimum funding is getting assigned to company.
	@Test
	public void updateMinimumFunding_test1() {

		Company company = prepareCompany(1,BenExchngEnums.TRINET_III.getBenExchng(), "NY");
		company.getRealmPlanYear().setMinFunding(70L);
		List<RealmRegionMinFunding> realmRegionMinFundings = null;
		Set<MinFundExceptionDto> minFundExceptions = new HashSet<>();
		BigDecimal minFundPct = BigDecimal.valueOf(70);

		CompanyServiceHelper.updateMinimumFunding(company, realmRegionMinFundings, minFundExceptions);

		assertEquals(3, company.getMinFundings().size());
		assertEquals("PCT", company.getMinFundings().stream().filter(e -> e.getPlanType().equals("medical"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(minFundPct, company.getMinFundings().stream().filter(e -> e.getPlanType().equals("medical"))
				.collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", company.getMinFundings().stream().filter(e -> e.getPlanType().equals("dental"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(minFundPct, company.getMinFundings().stream().filter(e -> e.getPlanType().equals("dental"))
				.collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", company.getMinFundings().stream().filter(e -> e.getPlanType().equals("vision"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(minFundPct, company.getMinFundings().stream().filter(e -> e.getPlanType().equals("vision"))
				.collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals(0, company.getRegionalMinimumFundings().size());
		Rules.overrideExceptionMiniumFunding(company, minFundExceptions);
		Rules.overrideMiniumFunding(company);
	}


	// Test plan year minimum funding is getting assigned to company.
	@Test
	public void setPlanYearMinimumFunding_OMSProspect_test() {

		// Given we have OMS prospect with TIB company.
		Company company = prepareCompany(1,BenExchngEnums.TRINET_OMS.getBenExchng(), "NY");
		company.setOmsOffering(OM_OD_OV_TLD.name());
		company.getRealmPlanYear().setMinFunding(70L);
		List<RealmRegionMinFunding> realmRegionMinFundings = null;
		Set<MinFundExceptionDto> minFundExceptions = new HashSet<>();
		BigDecimal minFundPct = BigDecimal.valueOf(70);
		// When
		CompanyServiceHelper.updateMinimumFunding(company, realmRegionMinFundings, minFundExceptions);

		// Then
		assertEquals(3, company.getMinFundings().size());
		assertEquals("PCT", company.getMinFundings().stream().filter(e -> e.getPlanType().equals("medical"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(0), company.getMinFundings().stream().filter(e -> e.getPlanType().equals("medical"))
				.collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", company.getMinFundings().stream().filter(e -> e.getPlanType().equals("dental"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(0), company.getMinFundings().stream().filter(e -> e.getPlanType().equals("dental"))
				.collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", company.getMinFundings().stream().filter(e -> e.getPlanType().equals("vision"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(0), company.getMinFundings().stream().filter(e -> e.getPlanType().equals("vision"))
				.collect(Collectors.toList()).get(0).getMinFundValue());


		// Given we have OMS prospect with Non TIB company.
		company.setAuthBroker(null);

		// When
		CompanyServiceHelper.updateMinimumFunding(company, realmRegionMinFundings, minFundExceptions);
		// Then
		assertEquals(3, company.getMinFundings().size());
	}
	
	@Test
	public void isBundledCompanyTest() {

		Company company = prepareCompanyForBundles(null);

		assertEquals(false, CompanyServiceHelper.isBundledCompany(company));

		Company company2 = prepareCompanyForBundles(BigDecimal.ONE.longValue());

		assertEquals(true, CompanyServiceHelper.isBundledCompany(company2));

	}
	

	// Check if regional min funding is getting assigned to company.
	@Test
	public void updateMinimumFunding_test4() throws Exception {

		Company company = prepareCompany(1,BenExchngEnums.TRINET_III.getBenExchng(), "NY");
		company.getRealm().setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		List<RealmRegionMinFunding> realmRegionMinFundings = prepareRealmRegionMinFunding();
		Set<MinFundExceptionDto> minFundExceptions = new HashSet<>();
		BigDecimal minFundPct = BigDecimal.valueOf(100);


		CompanyServiceHelper.updateMinimumFunding(company, realmRegionMinFundings, minFundExceptions);

		assertEquals(1, company.getRegionalMinimumFundings().size());
		assertEquals(minFundPct, company.getRegionalMinimumFundings().get(0).getFundingPct());
		assertEquals("MA", company.getRegionalMinimumFundings().get(0).getRegion());
		Rules.overrideExceptionMiniumFunding(company, minFundExceptions);
		Rules.overrideMiniumFunding(company);
	}
	
	@Test
	public void isExternalRenewalOpen() {
		Company company = new Company();
		SchedTbl schedTbl = new SchedTbl();
		schedTbl.setOpenDate(DateUtils.addDays(new Date(), 20));
		schedTbl.setCloseDate(DateUtils.addDays(new Date(), 40));
		schedTbl.setExtensionEndDate(DateUtils.addDays(new Date(), -10));
		company.setSchedTbl(schedTbl);

		boolean result = CompanyServiceHelper.isExternalRenewalOpen(company);

		Assert.assertFalse(result);
	}


	@Test
	public void isClientCompanyPatternTest() {
		String companyCode = "ABC";
		boolean isClient = CompanyServiceHelper.isClientCompanyPattern( companyCode );
		assertTrue( isClient );

		companyCode = "A1B2C3D4";
		isClient = CompanyServiceHelper.isClientCompanyPattern( companyCode );
		assertFalse( isClient );
	}

	@Test
	public void isTibProspectTest() {
		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
		company.setRealm(realm);

		company.setOmsOffering(OMB_TLD.name());
		boolean isTibProspect = CompanyServiceHelper.isTibProspect( company );
		assertFalse( isTibProspect );

		company.setOmsOffering(null);
		isTibProspect = CompanyServiceHelper.isTibProspect( company );
		assertFalse(isTibProspect );

		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setOmsOffering(OMB_TLD.name());
		isTibProspect = CompanyServiceHelper.isTibProspect( company );
		assertFalse( isTibProspect );

	}

	@Test
	public void isOMSExchange(){
		Company company = new Company();
		Realm realm = new Realm();

		// Given we have a OMS Product Exchange Company
		realm.setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
		company.setRealm(realm);

		// When
		boolean isOMSProspect = CompanyServiceHelper.isOMSExchange(company);

		// Then
		assertTrue(isOMSProspect);


		// Given we have a PAS exchng Company
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);

		// When
		isOMSProspect = CompanyServiceHelper.isOMSExchange(company);

		// Then
		assertFalse(isOMSProspect);
	}
	
	@Test
	public void isTNXIExchange() {
		//Given
		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_XI.getBenExchng());
		company.setRealm(realm);

		// When
		boolean isTNXICompany = CompanyServiceHelper.isTNXIExchange(company);

		// Then
		assertTrue(isTNXICompany);

		// Given we have a PAS exchange Company
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);

		// When
		isTNXICompany = CompanyServiceHelper.isTNXIExchange(company);

		// Then
		assertFalse(isTNXICompany);
	}

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<?> constructor = CompanyServiceHelper.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            Assert.fail("Expected InvocationTargetException");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }
	
	private List<RealmRegionMinFunding> prepareRealmRegionMinFunding() {
		RealmRegionMinFunding rrmf = new RealmRegionMinFunding();
		RealmRegionMinFundingPK id = new RealmRegionMinFundingPK( "Q1", "MA", java.sql.Date.valueOf( "2001-01-01" ) );
		rrmf.setId(id);
		rrmf.setMinFundingPct(BigDecimal.valueOf(100));
		rrmf.setEnddt( java.sql.Date.valueOf( "2099-12-31" ) );
		return Arrays.asList(rrmf);
	}

	/*
	 * 
	 * Setup methods
	 * 
	 */
	private Company prepareCompany(int naicsCode,String benExchange, String hq) {

		Company company = new Company();

        // to make it 5 digits instead of 2 digits
        naicsCode = naicsCode * 10000 + 1234;

		company.setNaicsCode(naicsCode);

		Realm realm = new Realm();
		realm.setBenExchange(benExchange);
		company.setRealm(realm);

		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearStart(new Date());
		realmPlanYear.setPlanYearEnd(new Date());
		realmPlanYear.setAleAmount(BigDecimal.valueOf(100));
		company.setRealmPlanYear(realmPlanYear);
		company.setHeadQuatersState(hq);
		BandCodes bandCodes = new BandCodes();
		bandCodes.setAetnaBandCode("10");
		bandCodes.setAetnaHmoBandCode("20");
		bandCodes.setAetnaPpoBandCode("30");
		company.setBandCodes(bandCodes);

		company.setCode("TEST");
		company.setDescription("FAST FOOD");
		company.setName("TEST COMPANY");
		company.setEligAle(true);
		company.setRealmPlanYearId(21);
		company.setRenewalCompany(true);
		company.setActualHeadCount(25);
		company.setHeadcount(50);
		company.setPayrollProcessed(true);

		return company;
	}
	
	/**
	 * Creating company for bundles
	 * @return
	 */
	private Company prepareCompanyForBundles(Long bundleId) {

		Company company = new Company();
		company.setNaicsCode(75);

		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);

		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearStart(new Date());
		realmPlanYear.setPlanYearEnd(new Date());
		realmPlanYear.setAleAmount(BigDecimal.valueOf(100));
		company.setRealmPlanYear(realmPlanYear);
		company.setHeadQuatersState("CA");
		BandCodes bandCodes = new BandCodes();
		bandCodes.setAetnaBandCode("10");
		bandCodes.setAetnaHmoBandCode("20");
		bandCodes.setAetnaPpoBandCode("30");
		company.setBandCodes(bandCodes);
		company.setBundleId(bundleId);

		company.setCode("TEST");
		company.setDescription("FAST FOOD");
		company.setName("TEST COMPANY");
		company.setEligAle(true);
		company.setRealmPlanYearId(21);
		company.setRenewalCompany(true);
		company.setActualHeadCount(25);
		company.setHeadcount(50);
		company.setPayrollProcessed(true);

		return company;
	}
	

}
