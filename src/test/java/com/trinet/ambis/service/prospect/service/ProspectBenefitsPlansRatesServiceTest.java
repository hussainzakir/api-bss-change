package com.trinet.ambis.service.prospect.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.TierRate;
import com.trinet.ambis.service.prospect.exception.ProspectApiCallException;
import com.trinet.ambis.service.prospect.response.ApiRes;
import com.trinet.ambis.service.prospect.response.BenefitsPlansRatesRes;
import com.trinet.ambis.service.prospect.response.BenefitsPlansRatesRes.AgeBandedRates;
import com.trinet.ambis.service.prospect.response.BenefitsPlansRatesRes.TierRates;
import com.trinet.ambis.service.prospect.service.impl.ProspectBenefitsPlansRatesServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;

@RunWith(MockitoJUnitRunner.class)
public class ProspectBenefitsPlansRatesServiceTest extends ServiceUnitTest {

	@InjectMocks
	private ProspectBenefitsPlansRatesServiceImpl prospectBenefitsPlansRatesService;

	@Mock
	private RestTemplate restTemplate;

	@Captor
	private ArgumentCaptor<String> uriCaptor;

	@Captor
	private ArgumentCaptor<HttpMethod> httpMethodCaptor;

	@Captor
	private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

	@Captor
	private ArgumentCaptor<ParameterizedTypeReference<ApiRes<List<BenefitsPlansRatesRes>>>> responseTypeCaptor;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

    private MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;

    @Before
    public void setUp() {
        mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
    }

    @After
    public void tearDown() {
        if (mockStaticBSSMessageConfig != null) mockStaticBSSMessageConfig.close();
    }

	/**
	 * given plan ids </br>
	 * when getBenefitsPlansRateDetails is called </br>
	 * then return rate details for the plan ids</br>
	 **/
	@Test
	public void getBenefitsPlansRateDetailsTest1() {
		// given
		// data
		String prospectId = "P1PC1";
		List<String> planIds = buildPlanIds();
		ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>> responseEntity = buildResponseEntity();
		// method mocks
		when(restTemplate.exchange(uriCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(),
				responseTypeCaptor.capture())).thenReturn(responseEntity);
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI))
				.thenReturn("http://localhost:8087/api-wf-hw-bss-prospect/v1");
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROPERTY_PROSPECT_BENEFITS_PLANS_RATES_API_URI))
				.thenReturn("/benefits-plans/rates/{planIds}");
		// when
		Map<String, RateDetail> actualResult = prospectBenefitsPlansRatesService.getBenefitsPlansRateDetails(planIds,
				prospectId);
		// then
		// assertions
		assertEquals(2, actualResult.size());
		RateDetail rateDetail1 = actualResult.get("1");
		List<String> regionCodes1 = rateDetail1.getRegionCode();
		assertEquals(1, regionCodes1.size());
		assertEquals("ALL", regionCodes1.get(0));
		assertEquals("4Tier", rateDetail1.getRateType());
		List<TierRate> tierRates = rateDetail1.getTierRates();
		assertEquals(4, tierRates.size());
		assertEquals("employee", tierRates.get(0).getCvgTierCode());
		assertEquals(new BigDecimal(100.11), tierRates.get(0).getCost());
		assertEquals("employeePlusSpouse", tierRates.get(1).getCvgTierCode());
		assertEquals(new BigDecimal(200.22), tierRates.get(1).getCost());
		assertEquals("employeePlusChild", tierRates.get(2).getCvgTierCode());
		assertEquals(new BigDecimal(300.33), tierRates.get(2).getCost());
		assertEquals("employeePlusFamily", tierRates.get(3).getCvgTierCode());
		assertEquals(new BigDecimal(400.44), tierRates.get(3).getCost());
		RateDetail rateDetail2 = actualResult.get("2");
		List<String> regionCodes2 = rateDetail2.getRegionCode();
		assertEquals(1, regionCodes2.size());
		assertEquals("ALL", regionCodes2.get(0));
		assertEquals("Age Banded", rateDetail2.getRateType());
		List<TierRate> tierRates2 = rateDetail2.getTierRates();
		assertEquals(2, tierRates2.size());
		assertEquals("15", tierRates2.get(0).getCvgTierCode());
		assertEquals(new BigDecimal(500.55), tierRates2.get(0).getCost());
		assertEquals("60", tierRates2.get(1).getCvgTierCode());
		assertEquals(new BigDecimal(600.66), tierRates2.get(1).getCost());
		// verify
		verify(restTemplate, times(1)).exchange(uriCaptor.getValue(), httpMethodCaptor.getValue(),
				httpEntityCaptor.getValue(), responseTypeCaptor.getValue());
	}

	/**
	 * given plan ids and prospect api call is not successful due to bad
	 * request</br>
	 * when getBenefitsPlansRateDetails is called </br>
	 * then throw ProspectApiCallException</br>
	 **/
	@Test
	public void getBenefitsPlansRateDetailsTest2() {
		// given
		// data
		String prospectId = "P1PC1";
		List<String> planIds = buildPlanIds();
		ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>> responseEntity = buildErrorResponseEntity();
		// method mocks
		when(restTemplate.exchange(uriCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(),
				responseTypeCaptor.capture())).thenReturn(responseEntity);
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI))
				.thenReturn("http://localhost:8087/api-wf-hw-bss-prospect/v1");
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROPERTY_PROSPECT_BENEFITS_PLANS_RATES_API_URI))
				.thenReturn("/benefits-plans/rates/{planIds}");
		exception.expect(ProspectApiCallException.class);
		exception.expectMessage("Error occured while getting prospect's benefits plans rates");
		// when
		prospectBenefitsPlansRatesService.getBenefitsPlansRateDetails(planIds, prospectId);
		// then
		// assertions
		// verify
		verify(restTemplate, times(1)).exchange(uriCaptor.getValue(), httpMethodCaptor.getValue(),
				httpEntityCaptor.getValue(), responseTypeCaptor.getValue());
	}

	/**
	 * given plan ids and prospect api call is not successful due null response
	 * entity data</br>
	 * when getBenefitsPlansRateDetails is called </br>
	 * then throw ProspectApiCallException</br>
	 **/
	@Test
	public void getBenefitsPlansRateDetailsTest3() {
		// given
		// data
		String prospectId = "P1PC1";
		List<String> planIds = buildPlanIds();
		ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>> responseEntity = buildErrorResponseEntity1();
		// method mocks
		when(restTemplate.exchange(uriCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(),
				responseTypeCaptor.capture())).thenReturn(responseEntity);
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI))
				.thenReturn("http://localhost:8087/api-wf-hw-bss-prospect/v1");
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROPERTY_PROSPECT_BENEFITS_PLANS_RATES_API_URI))
				.thenReturn("/benefits-plans/rates/{planIds}");
		exception.expect(ProspectApiCallException.class);
		exception.expectMessage("Error occured while getting prospect's benefits plans rates");
		// when
		prospectBenefitsPlansRatesService.getBenefitsPlansRateDetails(planIds, prospectId);
		// then
		// verify
		verify(restTemplate, times(1)).exchange(uriCaptor.getValue(), httpMethodCaptor.getValue(),
				httpEntityCaptor.getValue(), responseTypeCaptor.getValue());
	}

	private List<String> buildPlanIds() {
		return List.of("1", "2");
	}

	private ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>> buildResponseEntity() {
		return new ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>>(buildApiRes(), HttpStatus.OK);
	}

	private ApiRes<List<BenefitsPlansRatesRes>> buildApiRes() {
		ApiRes<List<BenefitsPlansRatesRes>> apiRes = new ApiRes<List<BenefitsPlansRatesRes>>();
		apiRes.setData(buildBenefitsPlansRatesRes());
		return apiRes;
	}

	private ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>> buildErrorResponseEntity() {
		return new ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>>(buildErrorApiRes(), HttpStatus.OK);
	}

	private ApiRes<List<BenefitsPlansRatesRes>> buildErrorApiRes() {
		ApiRes<List<BenefitsPlansRatesRes>> apiRes = new ApiRes<List<BenefitsPlansRatesRes>>();
		apiRes.setData(null);
		apiRes.setError(new ApiRes.Error());
		return apiRes;
	}

	private ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>> buildErrorResponseEntity1() {
		return new ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>>(buildNullResponseEntity(), HttpStatus.OK);
	}

	private ApiRes<List<BenefitsPlansRatesRes>> buildNullResponseEntity() {
		return null;
	}

	private List<BenefitsPlansRatesRes> buildBenefitsPlansRatesRes() {
		return List.of(
				BenefitsPlansRatesRes.builder().benefitPlanId("1")
						.tierRates(List.of(TierRates.builder().cvgTierCode("1").cost(new BigDecimal(100.11)).build(),
								TierRates.builder().cvgTierCode("2").cost(new BigDecimal(200.22)).build(),
								TierRates.builder().cvgTierCode("C").cost(new BigDecimal(300.33)).build(),
								TierRates.builder().cvgTierCode("4").cost(new BigDecimal(400.44)).build()))
						.build(),
				BenefitsPlansRatesRes.builder().benefitPlanId("2")
						.ageBandedRates(List.of(
								AgeBandedRates.builder().ageBandCode("15").cost(new BigDecimal(500.55)).build(),
								AgeBandedRates.builder().ageBandCode("60").cost(new BigDecimal(600.66)).build()))
						.build());
	}

}
