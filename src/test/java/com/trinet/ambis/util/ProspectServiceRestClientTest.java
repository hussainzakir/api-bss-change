package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.rest.controllers.dto.prospect.EmployeeGroupAssignmentDto;
import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.ambis.service.model.prospect.ProspectInfoResponse;
import com.trinet.ambis.service.prospect.dto.BenefitGroupRes;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.domain.common.ReturnResponse;

@RunWith(MockitoJUnitRunner.class)
public class ProspectServiceRestClientTest extends ServiceUnitTest {

	@InjectMocks
	ProspectServiceRestClient prospectServiceRestClient;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private HttpServletRequest httpRequest;

	private static final String PROSPECT_API_URL = "http://localhost:8087/api-wf-hw-bss-prospect/v1";

	private MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;

	@Before
	public void setUp() {
		if (mockStaticBSSMessageConfig == null) {
			mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
		}
	}

	@org.junit.After
	public void tearDown() {
		if (mockStaticBSSMessageConfig != null) {
			mockStaticBSSMessageConfig.close();
			mockStaticBSSMessageConfig = null;
		}
	}

	@Test
	public void prepareRequestAndCallEndPointTest()
	{
		
		when( BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)).thenReturn(PROSPECT_API_URL);
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.put("prospectId", List.of("1"));
		ProspectApiRequest<ProspectInfoResponse> prospectApiGetRequest = ProspectApiRequest.<ProspectInfoResponse>builder()
				.method(HttpMethod.GET).uri("/prospect-info")
				.queryParams(map).parameterizedTypeReference(new ParameterizedTypeReference<>() {
		}).build();
		ResponseEntity<ReturnResponse<ProspectInfoResponse>> apiResponse = populateResponseEntity();
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(new ParameterizedTypeReference<ReturnResponse<ProspectInfoResponse>>() {
				}))).thenReturn(apiResponse);
		
		ProspectInfoResponse data = prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
		
		assertEquals("CA",data.getHqState());
		assertEquals("93456",data.getZipCode());
		assertEquals("25-8-2024",data.getExpiryDate());

	}

	@Test
	public void prepareRequestAndCallEndPointTest1()
	{
		
		when( BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)).thenReturn(PROSPECT_API_URL);
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.put("prospectID", List.of("1"));
		ProspectApiRequest<ProspectInfoResponse> prospectApiGetRequest = ProspectApiRequest.<ProspectInfoResponse>builder()
				.method(HttpMethod.GET).uri("/prospect-info")
				.queryParams(map).parameterizedTypeReference(new ParameterizedTypeReference<>() {
		}).build();
		ResponseEntity<ReturnResponse<ProspectInfoResponse>> apiResponse = new ResponseEntity<>(
				HttpStatus.NOT_FOUND);
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(new ParameterizedTypeReference<ReturnResponse<ProspectInfoResponse>>() {
				}))).thenReturn(apiResponse);
		
		ProspectInfoResponse data = (ProspectInfoResponse)prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
		
		verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(new ParameterizedTypeReference<ReturnResponse<ProspectInfoResponse>>() {
				}));
		assertNull(data);
	}

	@Test
	public void prepareRequestAndCallEndPointTest2() {
		ParameterizedTypeReference<ReturnResponse<Object>> parameterizedTypeReference = new ParameterizedTypeReference<>() {
		};
		List<EmployeeGroupAssignmentDto> employeeGroupAssignmentDtoList = new ArrayList<>();
		ProspectApiRequest prospectApiPutRequest = ProspectApiRequest.builder().method(HttpMethod.PUT)
				.uri(ProspectURIConstants.EMPLOYEE_GROUP_ASSIGNMENT)
				.parameterizedTypeReference(parameterizedTypeReference).requestBody(employeeGroupAssignmentDtoList).build();

		ResponseEntity<ReturnResponse<Object>> apiResponse = new ResponseEntity<>(null, HttpStatus.OK);

		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)).thenReturn(PROSPECT_API_URL);
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(new ParameterizedTypeReference<ReturnResponse<Object>>() {
				}))).thenReturn(apiResponse);

		ResponseEntity<ReturnResponse<Object>> responseEntity = (ResponseEntity<ReturnResponse<Object>>) prospectServiceRestClient
				.prepareRequestAndCallEndPoint(prospectApiPutRequest);

		assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());
		verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(new ParameterizedTypeReference<ReturnResponse<Object>>() {
				}));
	}

	@Test
	public void prepareRequestAndCallEndPointTest3() {
		long groupId = 111;
		Map<String, Long> map = new HashMap<>();
		map.put("groupId", groupId);

		ProspectApiRequest prospectApiPutRequest = ProspectApiRequest.builder().method(HttpMethod.DELETE)
				.uri(ProspectURIConstants.BENEFIT_GROUP_ID_PARAM)
				.parameterizedTypeReference(new ParameterizedTypeReference<>() {
				}).pathParams(map).build();

		ResponseEntity<ReturnResponse<Object>> apiResponse = new ResponseEntity<>(null, HttpStatus.OK);

		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)).thenReturn(PROSPECT_API_URL);
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(new ParameterizedTypeReference<ReturnResponse<Object>>() {
				}))).thenReturn(apiResponse);

		ResponseEntity<ReturnResponse<Object>> responseEntity = (ResponseEntity<ReturnResponse<Object>>) prospectServiceRestClient
				.prepareRequestAndCallEndPoint(prospectApiPutRequest);

		assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());
		verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(new ParameterizedTypeReference<ReturnResponse<Object>>() {
				}));
	}
	
	/**
	 * given ProspectApiPostRequest attributes</br>
	 * when prepareRequestAndCallEndPoint</br>
	 * then verify prospect add group api method call is successfull
	 **/
	@Test
	public void prepareRequestAndCallEndPointTest4() {
		// given
		// data
		String prospectId = "0014Z00001IDLTPAAC";
		long sourceGroupId = 2;
		String groupName = "Managers";
		MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
		requestParams.add(ProspectConstants.PROSPECT_ID_REQ_PARAM, prospectId);
		ParameterizedTypeReference<ReturnResponse<BenefitGroupRes>> updateBenefitGroupNameBean = new ParameterizedTypeReference<>() {
		};
		String addGroupReqJson = "{\"id\" : " + sourceGroupId + " , \"name\": \"" + groupName + "\"}";
		ProspectApiRequest<BenefitGroupRes> prospectApiPostRequest = ProspectApiRequest.<BenefitGroupRes>builder().method(HttpMethod.POST)
				.uri(ProspectURIConstants.BENEFIT_GROUP).queryParams(requestParams).requestBody(addGroupReqJson)
				.parameterizedTypeReference(updateBenefitGroupNameBean).build();
		BenefitGroupRes benefitGroupRes = BenefitGroupRes.builder().id(1L).build();
		ReturnResponse<BenefitGroupRes> returnResponse = new ReturnResponse<>();
		returnResponse.setData(benefitGroupRes);
		ResponseEntity<ReturnResponse<BenefitGroupRes>> apiResponse = new ResponseEntity<>(returnResponse, HttpStatus.OK);
		// method mocks
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)).thenReturn(PROSPECT_API_URL);
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(updateBenefitGroupNameBean))).thenReturn(apiResponse);
		// when
		Object object = prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiPostRequest);
		// then
		// assertions
		assertTrue(object instanceof BenefitGroupRes);
		// verify
		verify(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(updateBenefitGroupNameBean));
	}

	private ResponseEntity<ReturnResponse<ProspectInfoResponse>> populateResponseEntity() {
		ReturnResponse<ProspectInfoResponse> rr = new ReturnResponse<ProspectInfoResponse>();
		ProspectInfoResponse apiData = new ProspectInfoResponse("Test Company", "CA", null, "93456", "25-8-2023", "81",
				true, "Q-993889", "25-8-2024", false, "G48", false, "25-8-2023");
		rr.setData(apiData);
		ResponseEntity<ReturnResponse<ProspectInfoResponse>> resp = new ResponseEntity<ReturnResponse<ProspectInfoResponse>>(
				rr, HttpStatus.OK);
		return resp;
	}

}
