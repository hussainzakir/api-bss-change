package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.configuration.AsyncConfig;
import com.trinet.ambis.enums.RealTimeSyncServiceStatusEnum;
import com.trinet.ambis.persistence.dao.hrp.RealTimeSyncDao;
import com.trinet.ambis.persistence.dao.hrp.dto.CensusHcSyncEventDto;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.impl.RealTimeSyncServiceImpl;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class RealTimeSyncServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	private RealTimeSyncServiceImpl realTimeSyncServiceMocks;

	@Mock
	private RealTimeSyncDao realTimeSyncDaoMock;

	@Mock
	private CompanyService companyServiceMock;

	@Mock
	private StrategySyncService strategySyncServiceMock;

	@Mock
	private ProcessStatusService processStatusServiceMock;

	@Mock
	private EmailGenService emailGenService;

	private Executor executor;

	@Captor
	private ArgumentCaptor<String> companyCodeCaptor;

	@Captor
	private ArgumentCaptor<String> companyCodeRefreshCensusCaptor;

	@Captor
	private ArgumentCaptor<Optional<String>> companyCodeOptCaptor;

	@Captor
	ArgumentCaptor<Long> realmYearId;

	private static final String COMPANY_CODE = "G48";
	private static final long REALM_PLAN_YEAR_ID = 53;
	private static final String PROCESS_STATUS_PROCESSED = "P";
	private static final String PROCESS_STATUS_FAILED = "F";
	private static final String EXCEPTION_ISTERMEDCOMPANY_METHOD = "Exception occurred in isTermedCompany method";
	private static final String EXCEPTION_GETCOMPANYDETAILS_METHOD = "Exception occurred in getCompanyDetails method";
	private static final String EXCEPTION_REFRESHCOMPANYCENSUSSYNCHRONOUSLY_METHOD = "Exception occurred in refreshCompanyCensusSynchronously method";
	private static final String EXCEPTION_SYNCSTRATEGYDATA_METHOD = "Exception occurred in syncStrategyData method";
    private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMock;

	@Before
	public void setUp() {
        appRulesAndConfigsUtilsMock = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
		AsyncConfig asyncConfig = new AsyncConfig();
		if (executor == null) {
			executor = asyncConfig.getAsyncExecutor();
			realTimeSyncServiceMocks.setExecutor(executor);
		}
	}

    @After
    public void tearDown() {
        appRulesAndConfigsUtilsMock.close();
    }

	@Test
	public void syncNoNewEventsTest() {
		// given
		when(realTimeSyncDaoMock.findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture()))
		.thenReturn(Optional.empty());
		// when
		realTimeSyncServiceMocks.eventDrivenSync(Optional.empty());
		// then
		verify(realTimeSyncDaoMock, times(1))
		.findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.getValue());
		verify(companyServiceMock, times(0)).isTermedCompany(any());
		verify(companyServiceMock, times(0)).getCompanyDetails(any());
		verify(companyServiceMock, times(0)).refreshCompanyCensusSynchronously(companyCodeRefreshCensusCaptor.capture(),
				realmYearId.capture());
		verify(strategySyncServiceMock, times(0)).syncStrategyData(any(), any());
		verify(processStatusServiceMock, times(0)).updateProcessStatus(any(), any());
	}

	@Test
	public void syncNewEventsForNonTermedCompanyTest() {
		// given
		CensusHcSyncEventDto censusHcSyncEventDto = prepareCensusHcSyncEventDto();
		Optional<CensusHcSyncEventDto> censusHcSyncEventDtoOpt = Optional.of(censusHcSyncEventDto);
		when(realTimeSyncDaoMock.findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty()))
				.thenReturn(censusHcSyncEventDtoOpt);
		Company company = prepareCompany();
		when(companyServiceMock.getCompanyDetails(censusHcSyncEventDto.getCompanyCode())).thenReturn(company);
		// when
		realTimeSyncServiceMocks.eventDrivenSync(Optional.empty());
		// then
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).isTermedCompany(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(1)).getCompanyDetails(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(1)).syncStrategyData(company, null);
		verify(processStatusServiceMock, times(1)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> ps != null && PROCESS_STATUS_PROCESSED.equals(ps.getProcessStatus())));
		verify(processStatusServiceMock, times(0)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> ps != null && PROCESS_STATUS_FAILED.equals(ps.getProcessStatus())));
	}

	@Test
	public void syncNewEventsForTermedCompanyTest() {
		// given
		CensusHcSyncEventDto censusHcSyncEventDto = prepareCensusHcSyncEventDto();
		Optional<CensusHcSyncEventDto> censusHcSyncEventDtoOpt = Optional.of(censusHcSyncEventDto);
		when(realTimeSyncDaoMock.findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty()))
				.thenReturn(censusHcSyncEventDtoOpt);
		when(companyServiceMock.isTermedCompany(censusHcSyncEventDtoOpt.get().getCompanyCode()))
				.thenReturn(Boolean.TRUE);
		Company company = prepareCompany();
		// when
		realTimeSyncServiceMocks.eventDrivenSync(Optional.empty());
		// then
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).isTermedCompany(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(0)).getCompanyDetails(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(0)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(0)).syncStrategyData(company, null);
		verify(processStatusServiceMock, times(1)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> ps != null && PROCESS_STATUS_PROCESSED.equals(ps.getProcessStatus())));
		verify(processStatusServiceMock, times(0)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> ps != null && PROCESS_STATUS_FAILED.equals(ps.getProcessStatus())));
	}

	@Test
	public void isTermedCompanyMethodCallExceptionTest() {
		// given
		CensusHcSyncEventDto censusHcSyncEventDto = prepareCensusHcSyncEventDto();
		Optional<CensusHcSyncEventDto> censusHcSyncEventDtoOpt = Optional.of(censusHcSyncEventDto);
		when(realTimeSyncDaoMock.findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty()))
				.thenReturn(censusHcSyncEventDtoOpt);
		when(companyServiceMock.isTermedCompany(censusHcSyncEventDtoOpt.get().getCompanyCode()))
				.thenThrow(new RuntimeException(EXCEPTION_ISTERMEDCOMPANY_METHOD));
		Company company = prepareCompany();
		// when
		try {
			realTimeSyncServiceMocks.eventDrivenSync(Optional.empty());
		} catch (Exception e) {
			assertEquals(EXCEPTION_ISTERMEDCOMPANY_METHOD, e.getMessage());
		}
		// then
		verify(emailGenService, timeout(200).times(1)).createSyncFailureEmail(any(), any());
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).isTermedCompany(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(0)).getCompanyDetails(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(0)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(0)).syncStrategyData(company, null);
		verify(processStatusServiceMock, times(0))
				.updateProcessStatus(
						eq(censusHcSyncEventDto.getProcessStatusIds()),
						argThat(ps ->
								ps != null &&
										PROCESS_STATUS_PROCESSED.equals(ps.getProcessStatus())
						)
				);

		verify(processStatusServiceMock, times(1))
				.updateProcessStatus(
						eq(censusHcSyncEventDto.getProcessStatusIds()),
						argThat(ps ->
								ps != null &&
										PROCESS_STATUS_FAILED.equals(ps.getProcessStatus())
						)
				);

	}

	@Test
	public void getCompanyDetailsMethodCallExceptionTest() {
		// given
		CensusHcSyncEventDto censusHcSyncEventDto = prepareCensusHcSyncEventDto();
		Optional<CensusHcSyncEventDto> censusHcSyncEventDtoOpt = Optional.of(censusHcSyncEventDto);
		when(realTimeSyncDaoMock.findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty()))
				.thenReturn(censusHcSyncEventDtoOpt);
		when(companyServiceMock.isTermedCompany(censusHcSyncEventDtoOpt.get().getCompanyCode()))
				.thenReturn(Boolean.FALSE);
		Company company = prepareCompany();
		when(companyServiceMock.getCompanyDetails(censusHcSyncEventDto.getCompanyCode()))
				.thenThrow(new RuntimeException(EXCEPTION_GETCOMPANYDETAILS_METHOD));
		// when
		try {
			realTimeSyncServiceMocks.eventDrivenSync(Optional.empty());
		} catch (Exception e) {
			assertEquals(EXCEPTION_GETCOMPANYDETAILS_METHOD, e.getMessage());
		}
		// then
		verify(emailGenService, timeout(200).times(1)).createSyncFailureEmail(any(), any());
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).isTermedCompany(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(1)).getCompanyDetails(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(0)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(0)).syncStrategyData(company, null);
		verify(processStatusServiceMock, times(0)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> ps != null && PROCESS_STATUS_PROCESSED.equals(ps.getProcessStatus())));
		verify(processStatusServiceMock, times(1)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> ps != null && PROCESS_STATUS_FAILED.equals(ps.getProcessStatus())));
	}

	@Test
	public void refreshCompanyCensusSynchronouslyMethodCallExceptionTest() {
		// given
		CensusHcSyncEventDto censusHcSyncEventDto = prepareCensusHcSyncEventDto();
		Optional<CensusHcSyncEventDto> censusHcSyncEventDtoOpt = Optional.of(censusHcSyncEventDto);
		when(realTimeSyncDaoMock.findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty()))
				.thenReturn(censusHcSyncEventDtoOpt);
		when(companyServiceMock.isTermedCompany(censusHcSyncEventDtoOpt.get().getCompanyCode()))
				.thenReturn(Boolean.FALSE);
		Company company = prepareCompany();
		when(companyServiceMock.getCompanyDetails(censusHcSyncEventDto.getCompanyCode())).thenReturn(company);
		doThrow(new RuntimeException(EXCEPTION_REFRESHCOMPANYCENSUSSYNCHRONOUSLY_METHOD)).when(companyServiceMock)
				.refreshCompanyCensusSynchronously(company.getCode(), company.getRealmPlanYearId());
		// when
		try {
			realTimeSyncServiceMocks.eventDrivenSync(Optional.empty());
		} catch (Exception e) {
			assertEquals(EXCEPTION_REFRESHCOMPANYCENSUSSYNCHRONOUSLY_METHOD, e.getMessage());
		}
		// then
		verify(emailGenService, timeout(200).times(1)).createSyncFailureEmail(any(), any());
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).isTermedCompany(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(1)).getCompanyDetails(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(0)).syncStrategyData(company, null);
		verify(processStatusServiceMock, times(0)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> PROCESS_STATUS_PROCESSED.equals(ps.getProcessStatus())));
		verify(processStatusServiceMock, times(1)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> PROCESS_STATUS_FAILED.equals(ps.getProcessStatus())));
	}

	@Test
	public void syncRefreshCompanyCensusSynchronouslyMethodCallExceptionTest() {
		// given
		CensusHcSyncEventDto censusHcSyncEventDto = prepareCensusHcSyncEventDto();
		Optional<CensusHcSyncEventDto> censusHcSyncEventDtoOpt = Optional.of(censusHcSyncEventDto);
		when(realTimeSyncDaoMock.findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty()))
				.thenReturn(censusHcSyncEventDtoOpt);
		when(companyServiceMock.isTermedCompany(censusHcSyncEventDtoOpt.get().getCompanyCode()))
				.thenReturn(Boolean.FALSE);
		Company company = prepareCompany();
		when(companyServiceMock.getCompanyDetails(censusHcSyncEventDto.getCompanyCode())).thenReturn(company);
		doThrow(new RuntimeException(EXCEPTION_REFRESHCOMPANYCENSUSSYNCHRONOUSLY_METHOD)).when(companyServiceMock)
				.refreshCompanyCensusSynchronously(company.getCode(), company.getRealmPlanYearId());
		// when
		try {
			realTimeSyncServiceMocks.eventDrivenSync(Optional.empty());
		} catch (Exception e) {
			assertEquals(EXCEPTION_REFRESHCOMPANYCENSUSSYNCHRONOUSLY_METHOD, e.getMessage());
		}
		// then
		verify(emailGenService, timeout(200).times(1)).createSyncFailureEmail(any(), any());
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).isTermedCompany(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(1)).getCompanyDetails(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(0)).syncStrategyData(company, null);
		verify(processStatusServiceMock, times(0)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> PROCESS_STATUS_PROCESSED.equals(ps.getProcessStatus())));
		verify(processStatusServiceMock, times(1)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> PROCESS_STATUS_FAILED.equals(ps.getProcessStatus())));
	}

	@Test
	public void strategySyncServiceMethodCallExceptionTest() {
		// given
		CensusHcSyncEventDto censusHcSyncEventDto = prepareCensusHcSyncEventDto();
		Optional<CensusHcSyncEventDto> censusHcSyncEventDtoOpt = Optional.of(censusHcSyncEventDto);
		when(realTimeSyncDaoMock.findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty()))
				.thenReturn(censusHcSyncEventDtoOpt);
		when(companyServiceMock.isTermedCompany(censusHcSyncEventDtoOpt.get().getCompanyCode()))
				.thenReturn(Boolean.FALSE);
		Company company = prepareCompany();
		when(companyServiceMock.getCompanyDetails(censusHcSyncEventDto.getCompanyCode())).thenReturn(company);
		doThrow(new RuntimeException(EXCEPTION_SYNCSTRATEGYDATA_METHOD)).when(strategySyncServiceMock)
				.syncStrategyData(company, null);
		// when
		try {
			realTimeSyncServiceMocks.eventDrivenSync(Optional.empty());
		} catch (Exception e) {
			assertEquals(EXCEPTION_SYNCSTRATEGYDATA_METHOD, e.getMessage());
		}
		// then
		verify(emailGenService, timeout(200).times(1)).createSyncFailureEmail(any(), any());
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).isTermedCompany(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(1)).getCompanyDetails(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(1)).syncStrategyData(company, null);
		verify(processStatusServiceMock, times(0)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> PROCESS_STATUS_PROCESSED.equals(ps.getProcessStatus())));
		verify(processStatusServiceMock, times(1)).updateProcessStatus(eq(censusHcSyncEventDto.getProcessStatusIds()),
				argThat(ps -> PROCESS_STATUS_FAILED.equals(ps.getProcessStatus())));
	}

	/**
	 * Event Driven Sync is disabled
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void syncOnDemand1Test() throws InterruptedException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.FALSE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		Company company = prepareCompany();
		when(companyServiceMock.getCompanyDetails(company.getCode())).thenReturn(company);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(0)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(0)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(1)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is disabled <br>
	 * Band code is not updated <br>
	 * Ale is not updated
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void syncOnDemand2Test() throws InterruptedException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.FALSE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.FALSE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.FALSE);
		when(companyServiceMock.getCompanyDetails(company.getCode())).thenReturn(company);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(0)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(0)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(1)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is disabled <br>
	 * Band code is updated <br>
	 * Ale is not updated
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void syncOnDemand3Test() throws InterruptedException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.FALSE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.TRUE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.FALSE);
		when(companyServiceMock.getCompanyDetails(company.getCode())).thenReturn(company);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(0)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(0)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(1)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is disabled <br>
	 * Band code is not updated <br>
	 * Ale is updated
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void syncOnDemand4Test() throws InterruptedException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.FALSE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.FALSE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.TRUE);
		when(companyServiceMock.getCompanyDetails(company.getCode())).thenReturn(company);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(0)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(0)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(1)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is disabled <br>
	 * Band code is updated <br>
	 * Ale is updated
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void syncOnDemand5Test() throws InterruptedException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.FALSE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.TRUE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.TRUE);
		when(companyServiceMock.getCompanyDetails(company.getCode())).thenReturn(company);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(0)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(0)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(1)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is enabled <br>
	 * Band code is not updated <br>
	 * Ale is not updated <br>
	 * No new and in progress census hc sync events
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void syncOnDemand6Test() throws InterruptedException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.TRUE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.FALSE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.FALSE);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(1)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(0)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(0)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(0)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(0)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is enabled <br>
	 * Band code is updated <br>
	 * Ale is not updated <br>
	 * No new and in progress census hc sync events
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void syncOnDemand7Test() throws InterruptedException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.TRUE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.TRUE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.FALSE);
		when(companyServiceMock.getCompanyDetails(company.getCode())).thenReturn(company);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(1)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(0)).findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		verify(companyServiceMock, times(1)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(1)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is enabled <br>
	 * Band code is not updated <br>
	 * Ale is updated <br>
	 * No new and in progress census hc sync events
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void syncOnDemand8Test() throws InterruptedException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.TRUE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		doReturn(Optional.empty()).when(realTimeSyncDaoMock)
		.findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture());
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.FALSE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.TRUE);
		when(companyServiceMock.getCompanyDetails(company.getCode())).thenReturn(company);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(1)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture());
		verify(companyServiceMock, times(1)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(1)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is enabled <br>
	 * Band code is updated <br>
	 * Ale is updated <br>
	 * No new and in progress census hc sync events
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void syncOnDemand9Test() throws InterruptedException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.TRUE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		doReturn(Optional.empty()).when(realTimeSyncDaoMock)
		.findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture());
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.TRUE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.TRUE);
		when(companyServiceMock.getCompanyDetails(company.getCode())).thenReturn(company);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(1)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture());		verify(companyServiceMock, times(1)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(1)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(1)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is enabled <br>
	 * Band code is not updated <br>
	 * Ale is not updated <br>
	 * Only new census hc sync events <br>
	 * eventDrivenSync method returns @link RealTimeSyncServiceStatusEnum.STATUS_PROCESSED<br>
	 * No in progress census hc sync events
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	@Test
	public void syncOnDemand10Test() throws InterruptedException, ParseException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.TRUE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		List<ProcessStatus> processStatusList = prepareCensusHcSyncProcessStatus();
		CensusHcSyncEventDto censusHcSyncEventDto = prepareCensusHcSyncEventDto();
		Optional<CensusHcSyncEventDto> censusHcSyncEventDtoOpt = Optional.of(censusHcSyncEventDto);
		when(realTimeSyncDaoMock.findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture()))
		.thenReturn(censusHcSyncEventDtoOpt);
		when(companyServiceMock.isTermedCompany(censusHcSyncEventDtoOpt.get().getCompanyCode()))
		.thenReturn(Boolean.TRUE);
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.FALSE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.FALSE);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(1)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture());		
		verify(companyServiceMock, times(0)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(1)).isTermedCompany(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(0)).getCompanyDetails(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(0)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(0)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is enabled <br>
	 * Band code is not updated <br>
	 * Ale is not updated <br>
	 * Only new census hc sync events <br>
	 * eventDrivenSync method returns @link RealTimeSyncServiceStatusEnum.STATUS_FAILED<br>
	 * No in progress census hc sync events
	 * 
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	@Test
	public void syncOnDemand11Test() throws InterruptedException, ParseException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.TRUE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		List<ProcessStatus> processStatusList = prepareCensusHcSyncProcessStatus();
		CensusHcSyncEventDto censusHcSyncEventDto = prepareCensusHcSyncEventDto();
		Optional<CensusHcSyncEventDto> censusHcSyncEventDtoOpt = Optional.of(censusHcSyncEventDto);
		when(realTimeSyncDaoMock.findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture()))
		.thenReturn(censusHcSyncEventDtoOpt);
		when(companyServiceMock.isTermedCompany(censusHcSyncEventDtoOpt.get().getCompanyCode()))
		.thenThrow(new RuntimeException(EXCEPTION_ISTERMEDCOMPANY_METHOD));
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.FALSE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.FALSE);
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(1)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture());		
		verify(companyServiceMock, times(0)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(1)).isTermedCompany(censusHcSyncEventDto.getCompanyCode());
		verify(companyServiceMock, times(0)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(0)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(0)).syncStrategyData(company, null);
	}

	/**
	 * Event Driven Sync is enabled <br>
	 * Band code is not updated <br>
	 * Ale is not updated <br>
	 * For first iteration of while loop in progress events will be fetched <br>
	 * For second iteration of while loop zero In Progress events will be fetched
	 * <br>
	 * eventDrivenSync method returns @link RealTimeSyncServiceStatusEnum.NO_RECORDS<br>
	 * 
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	@Test
	public void syncOnDemand12Test() throws InterruptedException, ParseException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.TRUE);
		RealTimeSyncServiceImpl realTimeSyncServiceSpy = Mockito.spy(realTimeSyncServiceMocks);
		Company company = prepareCompany();
		company.setBandCodeUpdated(Boolean.FALSE);
		company.setAcaLargeEmplrStatusUpdated(Boolean.FALSE);
		List<ProcessStatus> processStatusList = prepareCensusHcSyncProcessStatus();
		when(processStatusServiceMock.findInProgressCenusHcSyncEvent(company.getCode())).thenReturn(processStatusList)
		.thenReturn(Collections.emptyList());
		doReturn(Optional.empty()).when(realTimeSyncDaoMock)
		.findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture());
		// when
		realTimeSyncServiceSpy.onDemandSync(company);
		// then
		verify(processStatusServiceMock, times(2)).findInProgressCenusHcSyncEvent(company.getCode());
		verify(realTimeSyncDaoMock, times(1)).findNewCensusHcSyncEventAndUpdateToInProgress(companyCodeOptCaptor.capture());		
		verify(companyServiceMock, times(0)).getCompanyDetails(company.getCode());
		verify(companyServiceMock, times(0)).refreshCompanyCensusSynchronously(company.getCode(),
				company.getRealmPlanYearId());
		verify(strategySyncServiceMock, times(0)).syncStrategyData(company, null);
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setRealmPlanYearId(REALM_PLAN_YEAR_ID);
		return company;
	}

	private CensusHcSyncEventDto prepareCensusHcSyncEventDto() {
		return CensusHcSyncEventDto.builder().companyCode(COMPANY_CODE).processStatusIds(Set.of(1L, 2L, 3L)).build();
	}

	private List<ProcessStatus> prepareCensusHcSyncProcessStatus() throws ParseException {
		List<ProcessStatus> events = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer("COMPANY_CODE");
		processStatus.setProcessName("CENSUS_HC_SYNC");
		processStatus.setProcessIdentiferValue("6EK");
		processStatus.setProcessStatus("N");
		processStatus.setUserId("1");
		processStatus.setEffDt(DateUtils.parseDate("01-08-2022", "dd-mm-yyyy"));
		events.add(processStatus);
		ProcessStatus processStatus1 = new ProcessStatus();
		processStatus1.setCreateTime(new Date());
		processStatus1.setId(1);
		processStatus1.setProcessIdentifer("COMPANY_CODE");
		processStatus1.setProcessName("CENSUS_HC_SYNC");
		processStatus1.setProcessIdentiferValue("6EK");
		processStatus1.setProcessStatus("N");
		processStatus1.setUserId("2");
		processStatus.setEffDt(DateUtils.parseDate("01-08-2022", "dd-mm-yyyy"));
		events.add(processStatus1);
		return events;
	}

}

