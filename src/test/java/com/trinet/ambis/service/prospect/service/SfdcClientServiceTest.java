package com.trinet.ambis.service.prospect.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.prospect.impl.SfdcClientServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;
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
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class SfdcClientServiceTest extends ServiceUnitTest {

	@InjectMocks
	private SfdcClientServiceImpl sfdcClientService;

	@Mock
	private RestTemplate restTemplate;

	@Captor
	ArgumentCaptor<String> hipServiceUrlCaptor;

	@Captor
	ArgumentCaptor<Class<String>> responseTypeCaptor;

	@Captor
	ArgumentCaptor<HttpEntity<MultiValueMap<String, Object>>> httpEntityCaptor;

    private MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;

    @Before
    public void setUp() {
        mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
        mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty("prospect.HIP.proposal.submit.url"))
                .thenReturn("http://localhost:8080/hip");
    }

    @After
    public void tearDown() {
        if (mockStaticBSSMessageConfig != null) mockStaticBSSMessageConfig.close();
    }

	/**
	 * given bodyMap </br>
	 * when sendProposal method called </br>
	 * then verify all method calls as expected
	 **/
	@Test
	public void sendProposalTest1() {
		// given
		// data
		String responseType = "{}";
		MultiValueMap<String, Object> bodyMap = buildBodyMap();
		// when
		when(restTemplate.postForObject(hipServiceUrlCaptor.capture(), httpEntityCaptor.capture(),
				responseTypeCaptor.capture())).thenReturn(responseType);
		// then
		sfdcClientService.sendProposal(bodyMap);
		// verify
		verify(restTemplate).postForObject(hipServiceUrlCaptor.capture(), httpEntityCaptor.capture(),
				responseTypeCaptor.capture());
	}

	private MultiValueMap<String, Object> buildBodyMap() {
		MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
		bodyMap.add("i_attachment", "i_attachment");
		bodyMap.add("i_attachment_apndx", "i_attachment_apndx");
		bodyMap.add("i_quoteSummary", "i_quoteSummary");
		return bodyMap;
	}

}