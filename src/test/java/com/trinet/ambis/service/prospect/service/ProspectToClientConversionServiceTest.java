package com.trinet.ambis.service.prospect.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import com.trinet.ambis.exception.BSSBadDataException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectToClientConversionResponse;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectToClientConversionRequest;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.EmployeeStrategyGroupService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.email.dto.ClientConversionFailureEmailDto;
import com.trinet.ambis.service.prospect.impl.ProspectToClientConversionServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.enums.RiskTypeEnum;

@RunWith(MockitoJUnitRunner.class)
public class ProspectToClientConversionServiceTest extends ServiceUnitTest {

	@InjectMocks
	ProspectToClientConversionServiceImpl prospectToClientConversionServiceImpl;

	@Mock
	CompanyService companyService;

	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	StrategyService strategyService;
	
	@Mock
	BenefitGroupService benefitGroupService;
	
	@Mock
	StrategyGroupService strategyGroupService;

	@Mock
	EmailGenService emailGenService;

	@Captor
	private ArgumentCaptor<BenefitGroup> benefitGroup;

	@Captor
	private ArgumentCaptor<Company> company;
	
	@Mock
	EmployeeStrategyGroupService employeeStrategyGroupService;

	@Mock
	StrategyDataDao strategyDataDao;

    @Mock
    Configuration configuration;

    @Mock
    Template template;

	@Mock
	BenefitsBundleService benefitsBundleService;

	private static final String COMPANY_CODE = "2R23";
	private static final String PROSPECT_ID = "a1b2c3";
    private static final String STREAM_EVENT_ID= "a6xEa000000TPaaIAG";
    private static final String BUNDLE_ID= "100";

	@Test
	public void processProspectToClientConversionTest() {

		Company psCompany = prepareCompany();
		Company prospectCompany = prepareProspectCompany();
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();
		List<Strategy> prospectStrategyList = prepareProspectStrategies();
		List<BenefitGroup> benefitGroups = prepareBenGroups();
		Bundle bundle = prepareBundle();
		prospectCompany.setLargeDealProspect(1);
		prospectCompany.setBundleId(100L); // match request bundleId "100" to pass validateBundleId

		when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenReturn(psCompany);
		when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
		when(companyService.findCompanyBy(PROSPECT_ID, 76)).thenReturn(prospectCompany);
		when(benefitsBundleService.getBundleByCompanyCode(PROSPECT_ID)).thenReturn(bundle);

		// Mock the new refactored method calls
		when(strategyService.findBy(PROSPECT_ID)).thenReturn(prospectStrategyList);
		doNothing().when(companyService).updatePsCompanyCodeForProspect(anyLong(), anyString());
		doNothing().when(strategyService).updateSubmittedStrategyDetails(666L);
		doNothing().when(employeeStrategyGroupService).updateEmployeesToDefaultStrategyGroup(anyList(), anyLong());
		
		// Mock strategy groups for invalidateUnsubmittedStrategies
		List<BenefitGroupStrategy> strategyGroups = prepareBenefitGroupStrategies();
        when(strategyGroupService.findBy(PROSPECT_ID)).thenReturn(strategyGroups);

        when(benefitGroupService
                .getBenefitGroupByStrategy(anyLong(), anyString())).thenReturn(benefitGroups);
		when(benefitGroupService
				.getBenefitGroupByStrategy(anyLong(), anyList())).thenReturn(benefitGroups);
		when(benefitGroupService.updateGroupWithPSDetails(benefitGroup.capture(),
				company.capture(), any(Boolean.class))).thenReturn(prepareSTDGroup());
		when(benefitGroupService.saveBenefitGroup(benefitGroup.capture())).thenReturn(prepareSTDGroup());

		ProspectToClientConversionResponse actualResult = prospectToClientConversionServiceImpl
				.processProspectToClientConversion(buildRequest());

		assertNotNull(actualResult);
		assertEquals( COMPANY_CODE, bundle.getCompanyCode());
		verify(companyService, times(1)).getPsCompanyDetails(COMPANY_CODE);
		verify(realmPlanYearService, times(1)).getRealmPlanYear(anyLong(), anyString(), any());
		verify(companyService, times(1)).findCompanyBy(PROSPECT_ID, 76);
		verify(strategyService, times(1)).updateSubmittedStrategyDetails(666L);
        verify(benefitGroupService, times(1)).getBenefitGroupByStrategy(666L, "A");
        verify(benefitGroupService, times(1)).getBenefitGroupByStrategy(666L, List.of("A", "P", "D"));
        verify(benefitGroupService, times(2)).updateGroupWithPSDetails(
                any(BenefitGroup.class), any(Company.class), any(Boolean.class));
		verify(benefitGroupService, times(1)).saveBenefitGroup(benefitGroup.getValue());
		verify(benefitGroupService, times(1)).updateBenefitGroupStatus(any(),anyString());
		verify(strategyGroupService, times(1)).updateBenefitGroupStrategyStatus(any(),anyString());
		verify(employeeStrategyGroupService, times(1)).updateEmployeesToDefaultStrategyGroup(anyList(), anyLong());
		
		// Verify invalidateUnsubmittedStrategies calls
		verify(strategyGroupService, times(1)).findBy(PROSPECT_ID);
		verify(strategyService, times(1)).deleteExistingStrategies(Set.of(333L));
		verify(strategyDataDao, times(1)).deleteGroupCovHeadCountByGroupIds(Set.of(100L, 300L));
		verify(strategyDataDao, times(1)).deleteGroupRateByGroupIds(Set.of(100L, 300L));
		verify(strategyDataDao, times(1)).deleteGroupByIds(Set.of(100L, 300L));
		verify(benefitsBundleService, times(1)).save(bundle);
	}

	@Test(expected = BSSBadDataException.class)
	public void processProspectToClientConversionMultipleSubmittedStrategiesForSameCompanyTest() throws IOException {

		Company psCompany = prepareCompany();
		Company prospectCompany = prepareProspectCompany();
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();

		List<Strategy> prospectStrategyList = prepareProspectStrategies();
        prospectStrategyList.get(0).setCompanyId(1111);
        prospectStrategyList.get(0).setSubmitted(true);
        prospectStrategyList.get(0).setSubmitDate(new Date());
        prospectStrategyList.get(1).setCompanyId(1111);
        prospectStrategyList.get(1).setSubmitted(true);

		when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenReturn(psCompany);
		when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
		when(companyService.findCompanyBy(PROSPECT_ID, 76)).thenReturn(prospectCompany);
        when(configuration.getTemplate("prospectToClientConversion.ftl", "UTF-8")).thenReturn(template);

        ProspectToClientConversionResponse actualResult = prospectToClientConversionServiceImpl
                .processProspectToClientConversion(buildRequest());

		assertNotNull(actualResult);
		verify(companyService, times(1)).getPsCompanyDetails(COMPANY_CODE);
		verify(realmPlanYearService, times(1)).getRealmPlanYear(anyLong(), anyString(), any());
		verify(companyService, times(1)).findCompanyBy(PROSPECT_ID, 76);
		verify(strategyService, times(0)).updateSubmittedStrategyDetails(666L);

	}

    @Test
    public void testProcessProspectToClientConversionWithNonK1Company() {
        Company psCompany = prepareCompany();
        psCompany.setK1Company(false);
        Company prospectCompany = prepareProspectCompany();
        prospectCompany.setLargeDealProspect(0);
        prospectCompany.setBundleId(100L); // match request bundleId "100" to pass validateBundleId
        RealmPlanYear realmPlanYear = prepareRealmPlanYear();
        List<Strategy> prospectStrategyList = prepareProspectStrategies();
        List<BenefitGroup> benefitGroups = prepareBenGroups();
        List<BenefitGroupStrategy> strategyGroups = prepareBenefitGroupStrategies();

        when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenReturn(psCompany);
        when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
        when(companyService.findCompanyBy(PROSPECT_ID, 76)).thenReturn(prospectCompany);
        when(strategyService.findBy(PROSPECT_ID)).thenReturn(prospectStrategyList);
        when(strategyGroupService.findBy(PROSPECT_ID)).thenReturn(strategyGroups);
        when(benefitGroupService.getBenefitGroupByStrategy(anyLong(), anyString())).thenReturn(benefitGroups);
        when(benefitGroupService.getBenefitGroupByStrategy(anyLong(), anyList())).thenReturn(benefitGroups);
        when(benefitGroupService.updateGroupWithPSDetails(any(BenefitGroup.class), any(Company.class), any(Boolean.class)))
                .thenReturn(prepareSTDGroup());
        when(benefitGroupService.saveBenefitGroup(any(BenefitGroup.class))).thenReturn(prepareSTDGroup());
        doNothing().when(companyService).updatePsCompanyCodeForProspect(anyLong(), anyString());
        doNothing().when(strategyService).updateSubmittedStrategyDetails(666L);
        doNothing().when(employeeStrategyGroupService).updateEmployeesToDefaultStrategyGroup(anyList(), anyLong());

        ProspectToClientConversionResponse actualResult = prospectToClientConversionServiceImpl
                .processProspectToClientConversion(buildRequest());

        assertNotNull(actualResult);
        assertFalse(actualResult.isK1());
    }

	@Test(expected = BSSBadDataException.class)
	public void processProspectToClientConversionEmptyListTest() throws IOException {

		Company psCompany = prepareCompany();
		Company prospectCompany = prepareProspectCompany();
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();

		when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenReturn(psCompany);
		when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
		when(companyService.findCompanyBy(PROSPECT_ID, 76)).thenReturn(prospectCompany);
        when(configuration.getTemplate("prospectToClientConversion.ftl", "UTF-8")).thenReturn(template);

        ProspectToClientConversionRequest request = new ProspectToClientConversionRequest();
        request.setCompanyCode(COMPANY_CODE);
        request.setProspectId(PROSPECT_ID);
        request.setStreamEventId(STREAM_EVENT_ID);
		ProspectToClientConversionResponse actualResult = prospectToClientConversionServiceImpl
                .processProspectToClientConversion(buildRequest());

		assertNotNull(actualResult);
		verify(companyService, times(1)).getPsCompanyDetails(COMPANY_CODE);
		verify(realmPlanYearService, times(1)).getRealmPlanYear(anyLong(), anyString(), any());
		verify(companyService, times(1)).findCompanyBy(PROSPECT_ID, 76);
		verify(strategyService, times(0)).updateSubmittedStrategyDetails(666L);

	}

    @Test(expected = RuntimeException.class)
    public void testProcessProspectToClientConversion_whenExceptionOccurs_shouldSendSupportEmail() throws IOException {
        when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenThrow(new RuntimeException("Simulated failure"));
        when(configuration.getTemplate("prospectToClientConversion.ftl", "UTF-8")).thenReturn(template);

        try {
            prospectToClientConversionServiceImpl.processProspectToClientConversion(buildRequest());
        } finally {
            // Assert: verify support email is sent
            ArgumentCaptor<ClientConversionFailureEmailDto> captor = ArgumentCaptor.forClass(
                    ClientConversionFailureEmailDto.class);
            verify(emailGenService, times(1)).createSupportEmail(captor.capture());

            ClientConversionFailureEmailDto dto = captor.getValue();
            Assert.assertEquals(COMPANY_CODE, dto.getCompanyCode());
            Assert.assertTrue(dto.isSendToBSS());
        }
    }

	@Test(expected = BSSBadDataException.class)
	public void testProcessProspectToClientConversion_companyNotFound_throwsException() throws IOException {
		Company psCompany = prepareCompany();
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();

		when(companyService.getPsCompanyDetails(anyString())).thenReturn(psCompany);
		when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
		when(companyService.findCompanyBy(anyString(), anyLong())).thenReturn(null);
        when(configuration.getTemplate("prospectToClientConversion.ftl", "UTF-8")).thenReturn(template);

		prospectToClientConversionServiceImpl.processProspectToClientConversion(
                ProspectToClientConversionRequest.builder().companyCode("PS001").prospectId("UNKNOWN").streamEventId(STREAM_EVENT_ID).build());
	}

	@Test(expected = BSSBadDataException.class)
	public void processProspectToClientConversionTest_withLargeDealProspectBundleAsNull() throws IOException{
		Company psCompany = prepareCompany();
		Company prospectCompany = prepareProspectCompany();
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();
		prospectCompany.setLargeDealProspect(1);

		when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenReturn(psCompany);
		when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
		when(companyService.findCompanyBy(PROSPECT_ID, 76)).thenReturn(prospectCompany);
		when(configuration.getTemplate("prospectToClientConversion.ftl", "UTF-8")).thenReturn(template);

		prospectToClientConversionServiceImpl.processProspectToClientConversion(buildRequest());
		verify(benefitsBundleService, times(0)).save(any());
	}

	@Test(expected = BSSBadDataException.class)
	public void testValidateBundleId_requestNotNullCompanyNull_throwsException() throws IOException {
		Company psCompany = prepareCompany();
		Company prospectCompany = prepareProspectCompany();
		// prospectCompany.bundleId is null (default)
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();

		when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenReturn(psCompany);
		when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
		when(companyService.findCompanyBy(PROSPECT_ID, 76)).thenReturn(prospectCompany);
		when(configuration.getTemplate("prospectToClientConversion.ftl", "UTF-8")).thenReturn(template);
		ProspectToClientConversionRequest request = buildRequest();

		prospectToClientConversionServiceImpl.processProspectToClientConversion(request);
	}

	@Test(expected = BSSBadDataException.class)
	public void testValidateRiskType_requestNullCompanyNotNull_throwsException() throws IOException {
		Company psCompany = prepareCompany();
		Company prospectCompany = prepareProspectCompany();
		prospectCompany.setRiskType(RiskTypeEnum.DIFFERENTIALS); // company has DIFFERENTIALS
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();

		when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenReturn(psCompany);
		when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
		when(companyService.findCompanyBy(PROSPECT_ID, 76)).thenReturn(prospectCompany);
		when(configuration.getTemplate("prospectToClientConversion.ftl", "UTF-8")).thenReturn(template);

		// riskType null in request => treated as BANDS; company has DIFFERENTIALS => mismatch
		ProspectToClientConversionRequest request = buildRequest();
		request.setRiskType(null);

		prospectToClientConversionServiceImpl.processProspectToClientConversion(request);
	}

	@Test(expected = BSSBadDataException.class)
	public void testValidateRiskType_mismatch_throwsException() throws IOException {
		Company psCompany = prepareCompany();
		Company prospectCompany = prepareProspectCompany();
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();
		prospectCompany.setRiskType(RiskTypeEnum.BANDS);

		when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenReturn(psCompany);
		when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
		when(companyService.findCompanyBy(PROSPECT_ID, 76)).thenReturn(prospectCompany);
		when(configuration.getTemplate("prospectToClientConversion.ftl", "UTF-8")).thenReturn(template);

		// riskType DIFFERENTIALS in request; company has BANDS => mismatch
		ProspectToClientConversionRequest request = buildRequest();
		request.setRiskType(RiskTypeEnum.DIFFERENTIALS.name());

		prospectToClientConversionServiceImpl.processProspectToClientConversion(request);
	}

	@Test(expected = BSSBadDataException.class)
	public void testValidateBundleId_mismatch_throwsException() throws IOException {
		Company psCompany = prepareCompany();
		Company prospectCompany = prepareProspectCompany();
		prospectCompany.setBundleId(200L); // request has "100", company has 200 => mismatch
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();

		when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenReturn(psCompany);
		when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
		when(companyService.findCompanyBy(PROSPECT_ID, 76)).thenReturn(prospectCompany);
		when(configuration.getTemplate("prospectToClientConversion.ftl", "UTF-8")).thenReturn(template);

		ProspectToClientConversionRequest request = buildRequest();

		prospectToClientConversionServiceImpl.processProspectToClientConversion(request);
	}

	@Test(expected = BSSBadDataException.class)
	public void testValidateBundleId_requestNullCompanyNotNull_throwsException() throws IOException {
		Company psCompany = prepareCompany();
		Company prospectCompany = prepareProspectCompany();
		prospectCompany.setBundleId(100L);
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();

		when(companyService.getPsCompanyDetails(COMPANY_CODE)).thenReturn(psCompany);
		when(realmPlanYearService.getRealmPlanYear(anyLong(), anyString(), any())).thenReturn(realmPlanYear);
		when(companyService.findCompanyBy(PROSPECT_ID, 76)).thenReturn(prospectCompany);
		when(configuration.getTemplate("prospectToClientConversion.ftl", "UTF-8")).thenReturn(template);

		// bundleId null in request, but company has bundleId set
		ProspectToClientConversionRequest request = buildRequest();
		request.setBundleId(null);

		prospectToClientConversionServiceImpl.processProspectToClientConversion(request);
	}


	// ---- helper methods ----

	private Company prepareCompany() {
		Realm realm = new Realm();
		realm.setId(1);
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setRealm(realm);
		company.setRealmPlanYearId(76);
		company.setDescription("Company G48");
		company.setName("Trinet Inc.");
		company.setQuater("8Y");
		company.setPlanStartDate("01-JAN-2018");
		return company;
	}

	private Company prepareProspectCompany() {
		Company company = new Company();
		company.setId(1111);
		company.setCode(PROSPECT_ID);
		company.setRealmPlanYearId(76);
		company.setDescription("Company G48");
		company.setName("Trinet Inc.");
		return company;
	}

	private RealmPlanYear prepareRealmPlanYear() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(76);
		realmPlanYear.setRealmId(1);
		realmPlanYear.setOeQuarter("8Y");
		realmPlanYear.setPlanYearStart(CommonUtils.formatStringToDate("2018-01-01", "yyyy-MM-dd"));
		realmPlanYear.setPlanYearEnd(CommonUtils.formatStringToDate("2019-01-30", "yyyy-MM-dd"));
		return realmPlanYear;
	}

	private List<Strategy> prepareClientUnsubmittedStrategies() {
		Strategy strategy1 = new Strategy();
		strategy1.setName("Strategy 1");
		strategy1.setId(111L);
		strategy1.setSubmitted(false);
		Strategy strategy2 = new Strategy();
		strategy2.setName("Strategy 2");
		strategy2.setSubmitted(false);
		strategy2.setId(222L);
        Strategy strategy3 = new Strategy();
        strategy2.setName("Strategy 3");
        strategy2.setSubmitted(false);
        strategy2.setId(444L);

		return List.of(strategy1, strategy2, strategy3);
	}

	private List<Strategy> prepareProspectStrategies() {
		Strategy strategy1 = new Strategy();
		strategy1.setName("Strategy 3");
		strategy1.setId(333L);
		strategy1.setCompanyId(2222L); // Different company
		strategy1.setSubmitted(false);
		
		Strategy strategy2 = new Strategy();
		strategy2.setName("Strategy 4");
		strategy2.setSubmitted(true);
		strategy2.setId(666L);
		strategy2.setCompanyId(1111L); // Same as prospect company
		strategy2.setSubmitDate(new java.util.Date());
		
		return List.of(strategy1, strategy2);
	}
	
	private List<Strategy> prepareClientSubmittedStrategies() {
		Strategy strategy2 = new Strategy();
		strategy2.setName("Strategy 6");
		strategy2.setSubmitted(true);
		strategy2.setId(666L);
		strategy2.setStatus("A");

		return List.of(strategy2);
	}
	
	private List<Strategy> prepareMultipleStrategies() {
		Strategy strategy1 = new Strategy();
		strategy1.setName("Strategy 5");
	 strategy1.setId(555L);
		strategy1.setSubmitted(false);
		strategy1.setStatus("A");
		Strategy strategy2 = new Strategy();
		strategy2.setName("Strategy 6");
		strategy2.setSubmitted(true);
		strategy2.setId(666L);
		strategy2.setStatus("A");

		return List.of(strategy1, strategy2);
	}

	private List<BenefitGroup> prepareBenGroups() {
		List<BenefitGroup> groups = new ArrayList<>();
		groups.add( prepareK1Group() );
		groups.add( prepareSTDGroup() );
		return groups;
	}
	

	private BenefitGroup prepareSTDGroup() {
		BenefitGroup grp = new BenefitGroup();
		BenefitGroupStrategy benefitGroupStrategy = new BenefitGroupStrategy();
		benefitGroupStrategy.setStrategyId(666L);
		benefitGroupStrategy.setDefaultGroup(true);
		benefitGroupStrategy.setHeadcount(5);
		benefitGroupStrategy.setWaitingPeriod("FDOH");
		benefitGroupStrategy.setStatus("A");
		benefitGroupStrategy.setId(1111);
		Set<BenefitGroupStrategy> benefitGroupStrategies = Set.of(benefitGroupStrategy);
		grp.setId(11L);
		grp.setType("STD");
		grp.setName("Workers");
		grp.setState("MA");
		grp.setCompanyId(1111L);
		grp.setBenefitGroupStrategy(benefitGroupStrategies);
		return grp;
	}

	private BenefitGroup prepareK1Group() {
		BenefitGroup grp = new BenefitGroup();
		BenefitGroupStrategy benefitGroupStrategy = new BenefitGroupStrategy();
		benefitGroupStrategy.setStrategyId(666L);
		benefitGroupStrategy.setDefaultGroup(true);
		benefitGroupStrategy.setHeadcount(5);
		benefitGroupStrategy.setWaitingPeriod("FDOH");
		benefitGroupStrategy.setStatus("A");
		benefitGroupStrategy.setId(2222);
		Set<BenefitGroupStrategy> benefitGroupStrategies = Set.of(benefitGroupStrategy);
		grp.setId(10L);
		grp.setType("K1");
		grp.setName("Directors");
		grp.setState("MA");
		grp.setBenefitGroupStrategy(benefitGroupStrategies);
		return grp;
	}
	
	
	private Company prepareOMSCompany() {
		Realm realm = new Realm();
		realm.setId(1);
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setRealm(realm);
		company.setRealmPlanYearId(95);
		company.setDescription("Company G48");
		company.setName("Trinet Inc.");
		company.setQuater("A1");
		company.setPlanStartDate("01-JAN-2026");
		return company;
	}
	
	private List<BenefitGroupStrategy> prepareBenefitGroupStrategies() {
		BenefitGroupStrategy bgs1 = new BenefitGroupStrategy();
		bgs1.setStrategyId(333L); // Unsubmitted strategy
		bgs1.setGroupId(100L);
		
		BenefitGroupStrategy bgs2 = new BenefitGroupStrategy();
		bgs2.setStrategyId(666L); // Submitted strategy
		bgs2.setGroupId(200L);
		
		BenefitGroupStrategy bgs3 = new BenefitGroupStrategy();
		bgs3.setStrategyId(333L); // Another group for unsubmitted strategy
		bgs3.setGroupId(300L);

        BenefitGroupStrategy bgs4 = new BenefitGroupStrategy();
        bgs4.setStrategyId(333L);
        bgs4.setGroupId(200L);   // Group same as submitted strategy. This group should not be deleted
		
		return List.of(bgs1, bgs2, bgs3, bgs4);
	}

	private Bundle prepareBundle() {
		return Bundle.builder()
				.id(101L)
				.effectiveDate(LocalDate.parse("2025-12-31"))
				.endDate(LocalDate.parse("2026-12-31"))
				.name("PROSPECT_BUNDLE")
				.companyCode(PROSPECT_ID)
				.build();
	}

	// Helper to build default request
	private ProspectToClientConversionRequest buildRequest() {
		return ProspectToClientConversionRequest.builder()
				.companyCode(COMPANY_CODE)
				.prospectId(PROSPECT_ID)
				.bundleId(BUNDLE_ID)
				.riskType(RiskTypeEnum.BANDS.name())  // fixed: was .bundleId(RiskTypeEnum.BANDS.name())
				.streamEventId(STREAM_EVENT_ID)
				.build();
	}

}
