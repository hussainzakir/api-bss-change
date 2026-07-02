package com.trinet.ambis.util;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.trinet.ambis.enums.BenExchngEnums;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

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
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.rest.controllers.dto.outputs.BSSReportDetails;
import com.trinet.ambis.service.dto.CmsLogoDto;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.security.util.SecurityUtils;

@RunWith(MockitoJUnitRunner.class)
public class RestApiClientTest extends ServiceUnitTest {

	@InjectMocks
	private RestApiClient restApiClient;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private TokenUtils tokenUtils;

	@Mock
	private HttpServletRequest httpRequest;

	private final String URL = "https://trinetqen1.hrpassport.com/api-docgen/v1/doc-gen/";

    private MockedStatic<SecurityUtils> securityUtilsMockedStatic;
    private MockedStatic<BenExchngEnums> benExchngEnumsMockedStatic;

    @Before
    public void setUp() {
        securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class);
        benExchngEnumsMockedStatic = Mockito.mockStatic(BenExchngEnums.class);
        when(SecurityUtils.parseAuthenticationToken(httpRequest)).thenReturn("token");
    }

    @After
    public void tearDown() {
        if(securityUtilsMockedStatic != null)
        securityUtilsMockedStatic.close();
        if(benExchngEnumsMockedStatic != null)
        benExchngEnumsMockedStatic.close();
    }

	@Test
	public void getReturnResponse() {
		// Given
		BSSReportDetails reportDetails = new BSSReportDetails();

        when(restTemplate.exchange(anyString(), any(), any(), ArgumentMatchers.any(Class.class)))
                .thenReturn(new ResponseEntity<>(new byte[10], HttpStatus.OK));
		when(tokenUtils.getHeaders(anyString())).thenCallRealMethod();

		byte[] template = restApiClient.getReturnResponse(httpRequest, reportDetails, URL, HttpMethod.POST);

		// Assert
		assertNotNull(template);
		assertTrue(template.length > 0);
	}

	/**
	 * Test with standard CMS configuration to simulate real usage.
	 * Asserts the final URL and validates the headers being sent:
	 *  - Authorization: cs15acd836a458979e6ccf553f
	 *  - User-Agent: insomnia/12.4.0
	 *  - api_key: bltab010fdefd6ceb60
	 */
	@Test
	public void testFetchCarrierLogos() {
		// Arrange
		String standardCmsUrl = "https://api.contentstack.io/v3/assets";
		String standardCmsId = "bltba72733abe665bff";

		CmsLogoDto expectedResponse = createMockCmsLogoDto();
		HttpHeaders mockHeaders = new HttpHeaders();
		// Mock the CMS headers with expected header values
		mockHeaders.set("Authorization", "cs15acd836a458979e6ccf553f");
		mockHeaders.set("User-Agent", "insomnia/12.4.0");
		mockHeaders.set("api_key", "bltab010fdefd6ceb60");

		when(tokenUtils.getCMSHeader()).thenReturn(mockHeaders);
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(CmsLogoDto.class)))
				.thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

		// Act
		CmsLogoDto result = restApiClient.fetchCarrierLogos(standardCmsUrl, standardCmsId);

		// Assert
		assertNotNull("Result should not be null", result);
		assertEquals("Result should match expected response", expectedResponse, result);

		// Capture the URL that was passed to restTemplate.exchange()
		ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(), eq(CmsLogoDto.class));

		// Assert the final URL
		String finalUrl = urlCaptor.getValue();
		assertEquals("URL should match expected format",
				"https://api.contentstack.io/v3/assets?folder=bltba72733abe665bff&include_metadata=true",
				finalUrl);

		// Assert the headers in the HttpEntity
		HttpEntity<?> capturedEntity = entityCaptor.getValue();
		assertNotNull("HttpEntity should not be null", capturedEntity);

		HttpHeaders capturedHeaders = capturedEntity.getHeaders();
		assertNotNull("Headers should not be null", capturedHeaders);

		// Verify Authorization header
		assertTrue("Authorization header should be present", capturedHeaders.containsKey("Authorization"));
		assertEquals("Authorization header should have correct value",
				"cs15acd836a458979e6ccf553f",
				capturedHeaders.getFirst("Authorization"));

		// Verify User-Agent header
		assertTrue("User-Agent header should be present", capturedHeaders.containsKey("User-Agent"));
		assertEquals("User-Agent header should have correct value",
				"insomnia/12.4.0",
				capturedHeaders.getFirst("User-Agent"));

		// Verify api_key header
		assertTrue("api_key header should be present", capturedHeaders.containsKey("api_key"));
		assertEquals("api_key header should have correct value",
				"bltab010fdefd6ceb60",
				capturedHeaders.getFirst("api_key"));
	}

	/**
	 * Test that fetchCarrierLogos returns null when RestTemplate throws an exception.
	 */
	@Test
	public void testFetchCarrierLogos_ReturnsNullOnException() {
		// Arrange
		String baseUrl = "https://api.contentstack.io/v3/assets";
		String cmsLogosId = "bltba72733abe665bff";
		HttpHeaders mockHeaders = new HttpHeaders();

		when(tokenUtils.getCMSHeader()).thenReturn(mockHeaders);
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(CmsLogoDto.class)))
				.thenThrow(new RuntimeException("Network error"));

		// Act
		CmsLogoDto result = restApiClient.fetchCarrierLogos(baseUrl, cmsLogosId);

		// Assert
		assertNull("Result should be null when exception is thrown", result);
	}

	/**
	 * Helper method to create a mock CmsLogoDto for testing.
	 */
	private CmsLogoDto createMockCmsLogoDto() {
		CmsLogoDto logDto = new CmsLogoDto();
		List<CmsLogoDto.LogoDto> assets = new ArrayList<>();

		CmsLogoDto.LogoDto logo = new CmsLogoDto.LogoDto();
		logo.setUid("test-uid-123");
		logo.setUrl("https://images.contentstack.io/v3/assets/bltab010fdefd6ceb60/test-logo.png");
		assets.add(logo);

		logDto.setAssets(assets);
		return logDto;
	}

}