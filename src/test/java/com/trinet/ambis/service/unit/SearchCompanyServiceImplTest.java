package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.configuration.BSSMessageConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.persistence.dao.ps.SearchCompanyDao;
import com.trinet.ambis.service.impl.SearchCompanyServiceImpl;
import com.trinet.ambis.service.model.SearchCompanyResultData;
import com.trinet.common.AppConfig;
import com.trinet.domain.common.ReturnResponse;
import com.trinet.security.util.SecurityUtils;

@RunWith(MockitoJUnitRunner.class)
public class SearchCompanyServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	SearchCompanyServiceImpl searchCompanyService;

	@Mock
	SearchCompanyDao searchCompanyDao;

	@Mock
	private HttpServletRequest request;

	@Mock
	private RestTemplate restTemplate;

	private static final String INPUT_TEXT = "sometext";
	private static final String COMP_ID = "001";
	private static final String EMPL_ID = "123456";
	private static final String Q1 = "Q1";
	private static final String AC = "AC";
	private static final String SM = "SM";
	private static final String AMB = "8Y";
	private static final String AL = "AL";
	private static final String ALL = "ALL";
	private static final String NEW = "NEW";
	private static final String RENEWAL = "RENEWAL";
	public static final String QUERY_FOR_ALL_CLIENTS = "getResultsForAllCT";
	public static final String QUERY_FOR_NEW_CLIENTS = "getResultsForNewCT";
	public static final String QUERY_FOR_RENEWAL_CLIENTS = "getResultsForRenewalCT";
	public static final String token = "AQIC5wM2LY4SfcyaCHj61cbE-i1pQwGjCjDszTcW7LHGX_w.*AAJTSQACMDMAAlNLABIzMTUzNjM2MDg3MDI4MDM4OTYAAlMxAAIwMQ..*";
    private MockedStatic<SecurityUtils> securityUtilsMockedStatic;
    private MockedStatic<AppConfig> appConfigMockedStatic;
    private MockedStatic<BSSMessageConfig> bssMessageConfigMockedStatic;

    @Before
    public void setup() {
        securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class);
        appConfigMockedStatic = Mockito.mockStatic(AppConfig.class);
        bssMessageConfigMockedStatic = Mockito.mockStatic(BSSMessageConfig.class);
    }

    @After
    public void tearDown() {
        securityUtilsMockedStatic.close();
        appConfigMockedStatic.close();
        bssMessageConfigMockedStatic.close();
    }
	/*
	 * 
	 * 
	 * When quarter and clientType result is empty.
	 */
	@Test
	public void getSearchResults_test1() {
		Map<String, String> quarterAndCTMappings = Collections.emptyMap();
		when(searchCompanyDao.getQuarterAndClientType(EMPL_ID)).thenReturn(quarterAndCTMappings);

		when(SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);
		when(AppConfig.getBenefitsServiceURL()).thenReturn("12121");

		List<SearchCompanyResultData> searchResults = searchCompanyService.getSearchResults(INPUT_TEXT, COMP_ID, EMPL_ID);

		verify(searchCompanyDao, times(1)).getQuarterAndClientType(EMPL_ID);
		assertEquals(Collections.emptyList(), searchResults);
	}

	/*
	 * When All and new type present and renewal not present.
	 */
	@Test
	public void getSearchResults_test2() {

		List<String> expected = Arrays.asList("new code1", "new code2", "all code1", "all code2");
		Map<String, String> quarterAndCTMappings = new HashMap<String, String>();
		quarterAndCTMappings.put(Q1, ALL);
		quarterAndCTMappings.put(AL, NEW);
		quarterAndCTMappings.put(AMB, ALL);
		quarterAndCTMappings.put(SM, NEW);
		quarterAndCTMappings.put(AC, ALL);

		List<SearchCompanyResultData> resultsFromAllCT = Arrays.asList(
				new SearchCompanyResultData("all code1", "all company 1"),
				new SearchCompanyResultData("all code2", "all company 2"));
		List<SearchCompanyResultData> resultsFromNewCT = Arrays.asList(
				new SearchCompanyResultData("new code1", "new company 1"),
				new SearchCompanyResultData("new code2", "new company 2"));

		ArgumentCaptor<String> inputTextArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<List> quarterArgCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<String> queryArgCaptor = ArgumentCaptor.forClass(String.class);

		ResponseEntity<ReturnResponse<Map<String, Object>>> resp = populateResponseEntity();
		when(SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);
		when(AppConfig.getBenefitsServiceURL()).thenReturn("microbib.url");

		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<ParameterizedTypeReference<ReturnResponse<Map<String, Object>>>>any())).thenReturn(resp);

		when(searchCompanyDao.getQuarterAndClientType(EMPL_ID)).thenReturn(quarterAndCTMappings);
		when(searchCompanyDao.getCompanyIdAndName(inputTextArgCaptor.capture(), quarterArgCaptor.capture(),
				queryArgCaptor.capture())).thenReturn(resultsFromAllCT).thenReturn(resultsFromNewCT);

		List<SearchCompanyResultData> searchResults = searchCompanyService.getSearchResults(INPUT_TEXT, COMP_ID, EMPL_ID);

		verify(searchCompanyDao, times(1)).getQuarterAndClientType(EMPL_ID);
		assertEquals(INPUT_TEXT, inputTextArgCaptor.getAllValues().get(0));
		assertEquals(QUERY_FOR_ALL_CLIENTS, queryArgCaptor.getAllValues().get(0));
		assertTrue(Arrays.asList(AC, Q1, AMB).containsAll(quarterArgCaptor.getAllValues().get(0)));
		assertEquals(INPUT_TEXT, inputTextArgCaptor.getAllValues().get(1));
		assertEquals(QUERY_FOR_NEW_CLIENTS, queryArgCaptor.getAllValues().get(1));
		assertTrue(Arrays.asList(AL, SM).containsAll(quarterArgCaptor.getAllValues().get(1)));
		assertEquals(4, searchResults.size());
		assertTrue(expected.contains(searchResults.get(0).getCompanyCode()));
		assertTrue(expected.contains(searchResults.get(1).getCompanyCode()));
		assertTrue(expected.contains(searchResults.get(2).getCompanyCode()));
		assertTrue(expected.contains(searchResults.get(3).getCompanyCode()));
	}

	/*
	 * When new and renewal type present and all not present.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void getSearchResults_test3() {
		List<String> expected = Arrays.asList("renewal code1", "renewal code2", "new code1", "new code2");
		Map<String, String> quarterAndCTMappings = new HashMap<String, String>();
		quarterAndCTMappings.put(Q1, RENEWAL);
		quarterAndCTMappings.put(AL, NEW);
		quarterAndCTMappings.put(AMB, RENEWAL);
		quarterAndCTMappings.put(SM, NEW);
		quarterAndCTMappings.put(AC, RENEWAL);

		List<SearchCompanyResultData> resultsFromRenewalCT = Arrays.asList(
				new SearchCompanyResultData("renewal code1", "renewal company 1"),
				new SearchCompanyResultData("renewal code2", "renewal company 2"));
		List<SearchCompanyResultData> resultsFromNewCT = Arrays.asList(
				new SearchCompanyResultData("new code1", "new company 1"),
				new SearchCompanyResultData("new code2", "new company 2"));

		ArgumentCaptor<String> inputTextArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<List> quarterArgCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<String> queryArgCaptor = ArgumentCaptor.forClass(String.class);

		ResponseEntity<ReturnResponse<Map<String, Object>>> resp = populateResponseEntity();
		when(SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);
		when(AppConfig.getBenefitsServiceURL()).thenReturn("microbib.url");

		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<ParameterizedTypeReference<ReturnResponse<Map<String, Object>>>>any())).thenReturn(resp);

		when(searchCompanyDao.getQuarterAndClientType(EMPL_ID)).thenReturn(quarterAndCTMappings);
		when(searchCompanyDao.getCompanyIdAndName(inputTextArgCaptor.capture(), quarterArgCaptor.capture(),
				queryArgCaptor.capture())).thenReturn(resultsFromRenewalCT).thenReturn(resultsFromNewCT);

		List<SearchCompanyResultData> searchResults = searchCompanyService.getSearchResults(INPUT_TEXT, COMP_ID, EMPL_ID);

		verify(searchCompanyDao, times(1)).getQuarterAndClientType(EMPL_ID);
		assertEquals(INPUT_TEXT, inputTextArgCaptor.getAllValues().get(0));
		assertEquals(QUERY_FOR_NEW_CLIENTS, queryArgCaptor.getAllValues().get(0));
		assertTrue(Arrays.asList(AL, SM).containsAll(quarterArgCaptor.getAllValues().get(0)));
		assertEquals(INPUT_TEXT, inputTextArgCaptor.getAllValues().get(1));
		assertEquals(QUERY_FOR_RENEWAL_CLIENTS, queryArgCaptor.getAllValues().get(1));
		assertTrue(Arrays.asList(AC, Q1, AMB).containsAll(quarterArgCaptor.getAllValues().get(1)));
		assertEquals(4, searchResults.size());
		assertTrue(expected.contains(searchResults.get(0).getCompanyCode()));
		assertTrue(expected.contains(searchResults.get(1).getCompanyCode()));
		assertTrue(expected.contains(searchResults.get(2).getCompanyCode()));
		assertTrue(expected.contains(searchResults.get(3).getCompanyCode()));
	}

	/*
	 * When all and renewal type present and new not present.
	 */
	@Test
	public void getSearchResults_test4() {
		List<String> expected = Arrays.asList("renewal code1", "renewal code2", "all code1", "all code2");
		Map<String, String> quarterAndCTMappings = new HashMap<String, String>();
		quarterAndCTMappings.put(Q1, RENEWAL);
		quarterAndCTMappings.put(AL, ALL);
		quarterAndCTMappings.put(AMB, RENEWAL);
		quarterAndCTMappings.put(SM, ALL);
		quarterAndCTMappings.put(AC, RENEWAL);

		List<SearchCompanyResultData> resultsFromRenewalCT = Arrays.asList(
			new SearchCompanyResultData("renewal code1", "renewal company 1"),
			new SearchCompanyResultData("renewal code2", "renewal company 2"));
		List<SearchCompanyResultData> resultsFromAllCT = Arrays.asList(
			new SearchCompanyResultData("all code1", "new company 1"),
			new SearchCompanyResultData("all code2", "new company 2"));

		ArgumentCaptor<String> inputTextArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<List> quarterArgCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<String> queryArgCaptor = ArgumentCaptor.forClass(String.class);

		ResponseEntity<ReturnResponse<Map<String, Object>>> resp = populateResponseEntity();

		when(SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);
		when(AppConfig.getBenefitsServiceURL()).thenReturn("microbib.url");

		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
			ArgumentMatchers.<ParameterizedTypeReference<ReturnResponse<Map<String, Object>>>>any())).thenReturn(resp);

		when(searchCompanyDao.getQuarterAndClientType(EMPL_ID)).thenReturn(quarterAndCTMappings);
		when(searchCompanyDao.getCompanyIdAndName(inputTextArgCaptor.capture(), quarterArgCaptor.capture(),
			queryArgCaptor.capture())).thenReturn(resultsFromAllCT).thenReturn(resultsFromRenewalCT);

		List<SearchCompanyResultData> searchResults = searchCompanyService.getSearchResults(INPUT_TEXT, COMP_ID, EMPL_ID);

		verify(searchCompanyDao, times(1)).getQuarterAndClientType(EMPL_ID);
		verify(searchCompanyDao, times(2)).getCompanyIdAndName(anyString(), Mockito.anyList(), anyString());

		assertEquals(INPUT_TEXT, inputTextArgCaptor.getAllValues().get(0));
		assertEquals(QUERY_FOR_ALL_CLIENTS, queryArgCaptor.getAllValues().get(0));
		assertTrue(Arrays.asList(AL, SM).containsAll(quarterArgCaptor.getAllValues().get(0)));

		assertEquals(INPUT_TEXT, inputTextArgCaptor.getAllValues().get(1));
		assertEquals(QUERY_FOR_RENEWAL_CLIENTS, queryArgCaptor.getAllValues().get(1));
		assertTrue(Arrays.asList(AC, Q1, AMB).containsAll(quarterArgCaptor.getAllValues().get(1)));

		assertEquals(4, searchResults.size());
		assertTrue(expected.contains(searchResults.get(0).getCompanyCode()));
		assertTrue(expected.contains(searchResults.get(1).getCompanyCode()));
		assertTrue(expected.contains(searchResults.get(2).getCompanyCode()));
		assertTrue(expected.contains(searchResults.get(3).getCompanyCode()));
	}


	/*
	 * When none company is found.
	 */
	@Test
	public void getSearchResults_test5() {
		Map<String, String> quarterAndCTMappings = new HashMap<String, String>();
		quarterAndCTMappings.put(Q1, RENEWAL);
		quarterAndCTMappings.put(AL, ALL);
		quarterAndCTMappings.put(AMB, RENEWAL);
		quarterAndCTMappings.put(SM, ALL);
		quarterAndCTMappings.put(AC, NEW);

		ArgumentCaptor<String> inputTextArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<List> quarterArgCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<String> queryArgCaptor = ArgumentCaptor.forClass(String.class);

		ResponseEntity<ReturnResponse<Map<String, Object>>> resp = populateResponseEntity();
		when(SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);
		when(AppConfig.getBenefitsServiceURL()).thenReturn("microbib.url");

		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<ParameterizedTypeReference<ReturnResponse<Map<String, Object>>>>any())).thenReturn(resp);

		when(searchCompanyDao.getQuarterAndClientType(EMPL_ID)).thenReturn(quarterAndCTMappings);
		List<SearchCompanyResultData> emptyList = Collections.<SearchCompanyResultData>emptyList();
		when(searchCompanyDao.getCompanyIdAndName(inputTextArgCaptor.capture(), quarterArgCaptor.capture(),
				queryArgCaptor.capture())).thenReturn(emptyList).thenReturn(emptyList).thenReturn(emptyList);

		List<SearchCompanyResultData> searchResults = searchCompanyService.getSearchResults(INPUT_TEXT, COMP_ID, EMPL_ID);

		verify(searchCompanyDao, times(1)).getQuarterAndClientType(EMPL_ID);
		assertEquals(INPUT_TEXT, inputTextArgCaptor.getAllValues().get(0));
		assertEquals(QUERY_FOR_ALL_CLIENTS, queryArgCaptor.getAllValues().get(0));
		assertTrue(Arrays.asList(AL, SM).containsAll(quarterArgCaptor.getAllValues().get(0)));
		assertEquals(INPUT_TEXT, inputTextArgCaptor.getAllValues().get(1));
		assertEquals(QUERY_FOR_NEW_CLIENTS, queryArgCaptor.getAllValues().get(1));
		assertTrue(Arrays.asList(AC).containsAll(quarterArgCaptor.getAllValues().get(1)));
		assertEquals(INPUT_TEXT, inputTextArgCaptor.getAllValues().get(2));
		assertEquals(QUERY_FOR_RENEWAL_CLIENTS, queryArgCaptor.getAllValues().get(2));
		assertTrue(Arrays.asList(Q1, AMB).containsAll(quarterArgCaptor.getAllValues().get(2)));
		assertEquals(0, searchResults.size());
	}

	private ResponseEntity<ReturnResponse<Map<String, Object>>> populateResponseEntity() {
		ReturnResponse<Map<String, Object>> rr = new ReturnResponse<>();
		Map<String, String> fundingBenefitProviderDetails = new HashMap<>();
		fundingBenefitProviderDetails.put("service", "BSS");

		Map<String, Object> benefitsAlertsMap = new HashMap<>();
		benefitsAlertsMap.put("fundingServiceProvider", fundingBenefitProviderDetails);

		rr.setData(benefitsAlertsMap);
		return new ResponseEntity<>(rr, HttpStatus.OK);
	}

}