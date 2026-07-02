package com.trinet.ambis.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.common.BssCoreURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.service.model.bsscore.BssCoreProcessStatus;
import com.trinet.ambis.service.model.bsscore.BssCoreProcessStatusResponse;
import com.trinet.ambis.service.model.bsscore.CompanyResponse;
import com.trinet.ambis.service.model.bsscore.GraphQLResponse;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.unit.ServiceUnitTest;


@RunWith(MockitoJUnitRunner.class)
public class BssCoreServiceClientTest extends ServiceUnitTest {

    @InjectMocks
    BssCoreServiceClient bssCoreServiceClient;
    @Mock
    private RestTemplate restTemplate;

    private static final String BSS_CORE_API_URL = "http://localhost:8080/api-bs-hw-bss-core/graphql";

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
    public void testGetCensus_Success() {

        // Given
        String companyCode = "NEW_COMPANY";

        ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference =
                new ParameterizedTypeReference<>() {
                };

        // Moc
        when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
                Mockito.eq(typeReference))).thenReturn(buildResponseEntity());

        // When
        Map<String, Employee> employees = bssCoreServiceClient.getCensusByCode(companyCode);

        // Then
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.POST.getDeclaringClass()), any(HttpEntity.class),
                Mockito.eq(new ParameterizedTypeReference<GraphQLResponse<CompanyResponse>>() {
                }));
        assertNotNull(employees);
        assertEquals(2, employees.size());
        assertEquals("Doe John", employees.get("prospect-1").getEmplName());
        assertEquals("Smith Jane", employees.get("prospect-2").getEmplName());
        assertFalse(employees.get("prospect-1").isK1());
        assertTrue(employees.get("prospect-2").isK1());
    }

    @Test
    public void testGetCensus_Success_No_Records() {

        // Given
        String companyCode = "NEW_COMPANY";

        ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference =
                new ParameterizedTypeReference<>() {
                };

        // Mock
        when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
                Mockito.eq(typeReference))).thenReturn(buildResponseEntityNoRecordsFound());

        // When
        Map<String, Employee> employees = bssCoreServiceClient.getCensusByCode(companyCode);

        // Then
        when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.POST.getDeclaringClass()), any(HttpEntity.class),
                Mockito.eq(new ParameterizedTypeReference<GraphQLResponse<CompanyResponse>>() {
                }));
        assertNotNull(employees);
    }


    @Test
    public void testGetCensus_Failure() {

        // Given
        String companyCode = "NEW_COMPANY";
        ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference =
                new ParameterizedTypeReference<>() {
                };

        // Mock
        when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
                Mockito.eq(typeReference))).thenReturn(ResponseEntity.status(500).build());

        // When
        Map<String, Employee> employees = bssCoreServiceClient.getCensusByCode(companyCode);

        // Then
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.POST.getDeclaringClass()), any(HttpEntity.class),
                Mockito.eq(new ParameterizedTypeReference<GraphQLResponse<CompanyResponse>>() {
                }));
        assertNotNull(employees);
    }
    
    @Test
	public void testGetAle_False_Success() {
		// Given
		long companyId = 1L;

		ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference = new ParameterizedTypeReference<>() {
		};

		// Moc
		when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
		when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(typeReference))).thenReturn(buildResponseEntityAleStatusFalse());

		// When
		boolean aleStatus = bssCoreServiceClient.getAleStatus(companyId);

		// Then
		verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.POST.getDeclaringClass()),
				any(HttpEntity.class), Mockito.eq(new ParameterizedTypeReference<GraphQLResponse<CompanyResponse>>() {
				}));
		assertEquals(false, aleStatus);
	}
    
    @Test
	public void testGetAle_True_Success() {
		// Given
		long companyId = 1L;

		ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference = new ParameterizedTypeReference<>() {
		};

		// Moc
		when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
		when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(typeReference))).thenReturn(buildResponseEntityAleStatusTrue());

		// When
		boolean aleStatus = bssCoreServiceClient.getAleStatus(companyId);

		// Then
		verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.POST.getDeclaringClass()),
				any(HttpEntity.class), Mockito.eq(new ParameterizedTypeReference<GraphQLResponse<CompanyResponse>>() {
				}));
		assertEquals(true, aleStatus);
	}
    
    
    @Test
	public void testGetAle_Success_No_Records() {
		// Given
		long companyId = 1L;

		ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference = new ParameterizedTypeReference<>() {
		};

		// Moc
		when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
		when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(typeReference))).thenReturn(null);

		// When
		boolean aleStatus = bssCoreServiceClient.getAleStatus(companyId);

		// Then
		verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.POST.getDeclaringClass()),
				any(HttpEntity.class), Mockito.eq(new ParameterizedTypeReference<GraphQLResponse<CompanyResponse>>() {
				}));
		assertEquals(false, aleStatus);
	}


    @Test
	public void testGetAle_Failure() {

		// Given
		long companyId = 1L;
		ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference = new ParameterizedTypeReference<>() {
		};

		// Mock
		when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
		when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(typeReference))).thenReturn(ResponseEntity.status(500).build());

		// When
		boolean aleStatus = bssCoreServiceClient.getAleStatus(companyId);

		// Then
		verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.POST.getDeclaringClass()),
				any(HttpEntity.class), Mockito.eq(new ParameterizedTypeReference<GraphQLResponse<CompanyResponse>>() {
				}));
		assertEquals(false, aleStatus);
	}

	@Test
	public void testGetCensus2_Success() {

		// Given
		String companyCode = "NEW_COMPANY";

		ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference = new ParameterizedTypeReference<>() {
		};

		// Moc
		when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
		when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(typeReference))).thenReturn(buildResponseEntity());

		// When
		List<ProspectCensusResponse> census = bssCoreServiceClient.getCensusByCompanyCode(companyCode);

		// Then
		verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.POST.getDeclaringClass()),
				any(HttpEntity.class), Mockito.eq(new ParameterizedTypeReference<GraphQLResponse<CompanyResponse>>() {
				}));
		assertNotNull(census);
		assertEquals(2, census.size());
		assertEquals("Doe John", census.get(0).getEmployeeName());
		assertEquals("1", census.get(0).getMedicalTier());
		assertEquals("2", census.get(0).getDentalTier());
		assertEquals("4", census.get(0).getVisionTier());
		assertEquals("Smith Jane", census.get(1).getEmployeeName());
		assertEquals("1", census.get(1).getMedicalTier());
		assertEquals("2", census.get(1).getDentalTier());
		assertEquals("W", census.get(1).getVisionTier());
	}

	@Test
	public void testGetCensus2_Success_No_Records() {

		// Given
		String companyCode = "NEW_COMPANY";

		ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference = new ParameterizedTypeReference<>() {
		};

		// Mock
		when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
		when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(typeReference))).thenReturn(buildResponseEntityNoRecordsFound());

		// When
		List<ProspectCensusResponse> census = bssCoreServiceClient.getCensusByCompanyCode(companyCode);

		// Then
		when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
		verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.POST.getDeclaringClass()),
				any(HttpEntity.class), Mockito.eq(new ParameterizedTypeReference<GraphQLResponse<CompanyResponse>>() {
				}));
		assertNotNull(census);
	}

	@Test
	public void testGetCensus2_Failure() {

		// Given
		String companyCode = "NEW_COMPANY";
		ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference = new ParameterizedTypeReference<>() {
		};

		// Mock
		when(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI)).thenReturn(BSS_CORE_API_URL);
		when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(typeReference))).thenReturn(ResponseEntity.status(500).build());
		// When
		List<ProspectCensusResponse> census = bssCoreServiceClient.getCensusByCompanyCode(companyCode);

		// Then
		verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.POST.getDeclaringClass()),
				any(HttpEntity.class), Mockito.eq(new ParameterizedTypeReference<GraphQLResponse<CompanyResponse>>() {
				}));
		assertNotNull(census);
	}

    private ResponseEntity<GraphQLResponse<CompanyResponse>> buildResponseEntity() {

        CompanyResponse.CompanyPayload payload = CompanyResponse.CompanyPayload.builder()
                .census(buildCensus()).build();
        
      

        CompanyResponse responseData = CompanyResponse.builder()
                .companyByCode(payload)
                .build();

        GraphQLResponse<CompanyResponse> body =
                GraphQLResponse.<CompanyResponse>builder()
                        .data(responseData)
                        .build();

        return ResponseEntity.ok(body);
    }

    private ResponseEntity<GraphQLResponse<CompanyResponse>> buildResponseEntityNoRecordsFound() {

        CompanyResponse responseData = CompanyResponse.builder()
                .companyByCode(null)
                .build();
        GraphQLResponse<CompanyResponse> body =
                GraphQLResponse.<CompanyResponse>builder()
                        .data(responseData)
                        .build();

        return ResponseEntity.ok(body);
    }

    private List<CompanyResponse.CompanyPayload.Census> buildCensus() {
        return List.of(new CompanyResponse.CompanyPayload.Census("prospect-1", "Doe", "John",new BigDecimal(10000),"NY","00000","1","2","4",false),
                new CompanyResponse.CompanyPayload.Census("prospect-2", "Smith", "Jane",new BigDecimal(120000),"NY","00000","1","2","W", true));
    }
    
    private ResponseEntity<GraphQLResponse<CompanyResponse>> buildResponseEntityAleStatusTrue() {
		CompanyResponse.CompanyPayload payload = CompanyResponse.CompanyPayload.builder().aleStatus(buildAleStatusTrue())
				.build();
		CompanyResponse responseData = CompanyResponse.builder().companyByPeoCompanyId(payload).build();
		GraphQLResponse<CompanyResponse> body = GraphQLResponse.<CompanyResponse>builder().data(responseData).build();
		return ResponseEntity.ok(body);
	}
    
    private ResponseEntity<GraphQLResponse<CompanyResponse>> buildResponseEntityAleStatusFalse() {
		CompanyResponse.CompanyPayload payload = CompanyResponse.CompanyPayload.builder().aleStatus(buildAleStatusFalse())
				.build();
		CompanyResponse responseData = CompanyResponse.builder().companyByPeoCompanyId(payload).build();
		GraphQLResponse<CompanyResponse> body = GraphQLResponse.<CompanyResponse>builder().data(responseData).build();
		return ResponseEntity.ok(body);
	}
    
    private CompanyResponse.CompanyPayload.AleStatus buildAleStatusTrue() {
        return new CompanyResponse.CompanyPayload.AleStatus(true, 1);
    }
    
    private CompanyResponse.CompanyPayload.AleStatus buildAleStatusFalse() {
        return new CompanyResponse.CompanyPayload.AleStatus(false, 1);
    }

	// ── getProcessStatusesBy tests ────────────────────────────────────────────

	@Test
	public void getProcessStatusesBy_success_returnsResponse() {
		// Given
		String companyCode = "ABC123";
		String processStatusUrl = "http://localhost:8080/api-bs-hw-bss-core";
		BssCoreProcessStatus processStatus = BssCoreProcessStatus.builder()
				.processName("BUNDLE_STRATEGY_SYNC").status("COMPLETED").finishedAt("2026-03-30T09:12:45Z").build();
		BssCoreProcessStatusResponse expectedResponse = BssCoreProcessStatusResponse.builder()
				.companyCode(companyCode).processStatuses(List.of(processStatus)).build();

		mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_REST_API_URI))
				.thenReturn(processStatusUrl);
		when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.GET), any(HttpEntity.class),
				Mockito.eq(BssCoreProcessStatusResponse.class)))
				.thenReturn(ResponseEntity.ok(expectedResponse));

		// When
		BssCoreProcessStatusResponse result = bssCoreServiceClient.getProcessStatusesBy(companyCode);

		// Then
		assertNotNull(result);
		assertEquals(companyCode, result.getCompanyCode());
		assertEquals(1, result.getProcessStatuses().size());
		assertEquals("COMPLETED", result.getProcessStatuses().get(0).getStatus());
	}

	@Test(expected = RestClientException.class)
	public void getProcessStatusesBy_networkError_propagatesException() {
		// Given
		String companyCode = "ABC123";
		String processStatusUrl = "http://localhost:8080/api-bs-hw-bss-core";

		mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_REST_API_URI))
				.thenReturn(processStatusUrl);
		when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.GET), any(HttpEntity.class),
				Mockito.eq(BssCoreProcessStatusResponse.class)))
				.thenThrow(new RestClientException("Connection refused"));

		// When — exception propagates (no longer swallowed in the client)
		bssCoreServiceClient.getProcessStatusesBy(companyCode);
	}

	@Test(expected = HttpClientErrorException.class)
	public void getProcessStatusesBy_403Forbidden_propagatesException() {
		// Given
		String companyCode = "ABC123";
		String processStatusUrl = "http://localhost:8080/api-bs-hw-bss-core";

		mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_REST_API_URI))
				.thenReturn(processStatusUrl);
		when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.GET), any(HttpEntity.class),
				Mockito.eq(BssCoreProcessStatusResponse.class)))
				.thenThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden",
						HttpHeaders.EMPTY, null, null));

		// When — 403 propagates from client; service layer converts to BSSApplicationException
		bssCoreServiceClient.getProcessStatusesBy(companyCode);
	}
}

