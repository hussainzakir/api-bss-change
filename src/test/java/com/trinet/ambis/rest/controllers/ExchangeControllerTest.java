package com.trinet.ambis.rest.controllers;

import static com.trinet.ambis.enums.OmsOfferingEnum.OMB_TLD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.rest.controllers.dto.exchange.CarrierDto;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.CarrierBand;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeCarrierDto;
import com.trinet.ambis.service.ExchangeService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.CommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeControllerTest extends ServiceUnitTest {

	@InjectMocks
	ExchangeController exchangeController;

	@Mock
	ExchangeService exchangeServiceMock;

	private MockMvc mockMvc;

	@Captor
	ArgumentCaptor<List<ExchangeBandsDto>> exchangeBandsPutReqDtosCaptor;

	@Captor
	ArgumentCaptor<String> companyCodeCaptor;

	private static final String COMPANY_CODE = "G48";
	
	private static final String EXCHANGE_ID = "";

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(exchangeController).build();
	}

	@Test
	public void getExchangeCarriersWithRecordsTest() throws Exception {
		// given
		// data
		List<ExchangeCarrierDto> exchangeCarrierDtos = prepareExchangeCarrierDtos();
		// method mocks
		when(exchangeServiceMock.getExchangeCarriers(COMPANY_CODE, BenExchngEnums.getByExchangeId(EXCHANGE_ID))).thenReturn(exchangeCarrierDtos);
		// when
		ResultActions actualResult = mockMvc.perform(MockMvcRequestBuilders
				.get(URIConstants.VERSION_AND_ROOT + URIConstants.GET_EXCHANGE_CARRIERS, "001", "00002222256", COMPANY_CODE));
		// then
		// assertions
		assertNotNull(actualResult);
		actualResult.andExpect(status().isOk()).andExpect(content().contentType("application/json"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value("2"));
		// TNIV assertion
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].exchangeId").value("TNIV"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].exchangeName").value("Exchange IV"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].isStrategyCreated").value(true));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].isCarrierSelectionRequired").value(true));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].isBenefitsStartDateValid").value(false));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].carriers.size()").value(2));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].carriers.[0].portfolioId").value(13L));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].carriers.[0].portfolioName").value("Tufts"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].carriers.[1].portfolioId").value(21L));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].carriers.[1].portfolioName").value("BCBS ID"));
		
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].exchangeId").value("TNXI"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].exchangeName").value("Exchange XI"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].isStrategyCreated").value(false));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].isCarrierSelectionRequired").value(false));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].isBenefitsStartDateValid").value(true));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].carriers.size()").value(2));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].carriers.[0].portfolioId").value(14L));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].carriers.[0].portfolioName").value("Guardian"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].carriers.[1].portfolioId").value(22L));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].carriers.[1].portfolioName").value("BCBS MN"));
		
		// verify
		verify(exchangeServiceMock, times(1)).getExchangeCarriers(COMPANY_CODE, BenExchngEnums.getByExchangeId(EXCHANGE_ID));
	}

	@Test
	public void getExchangeCarriersWithNoRecordsTest() throws Exception {
		// given
		// method mocks
		when(exchangeServiceMock.getExchangeCarriers(COMPANY_CODE, BenExchngEnums.getByExchangeId(EXCHANGE_ID))).thenReturn(Collections.emptyList());
		// when
		// when
				ResultActions actualResult = mockMvc.perform(MockMvcRequestBuilders
						.get(URIConstants.VERSION_AND_ROOT + URIConstants.GET_EXCHANGE_CARRIERS, "001", "00002222256", COMPANY_CODE));
		// then
		// assertions
		assertNotNull(actualResult);
		actualResult.andExpect(status().isOk()).andExpect(content().contentType("application/json"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value("0"));
		// verify
		verify(exchangeServiceMock, times(1)).getExchangeCarriers(COMPANY_CODE, BenExchngEnums.getByExchangeId(EXCHANGE_ID));
	}

	/**
	 * given exchange bands and company code</br>
	 * when put exchange bands api called </br>
	 * then create company and save exchange bands </br>
	 **/
	@Test
	public void saveExchangeBandsTest() throws Exception {
		// given
		// data
		String companyCode = "G48";
		List<ExchangeBandsDto> exchangeBandsPutReqDtos = prepareExchangeBandsPutReqDtos("TNII");
		List<ExchangeBandsDto> exchangeBandsPutResDtos = prepareExchangeBandsPutResDtos("TNII");
		// method mocks
		when(exchangeServiceMock.saveExchangeBands(exchangeBandsPutReqDtosCaptor.capture(),
				companyCodeCaptor.capture())).thenReturn(exchangeBandsPutResDtos);
		// when
		ResultActions actualResult = mockMvc.perform(MockMvcRequestBuilders
				.put(URIConstants.VERSION_AND_ROOT + URIConstants.EXCHANGE_BANDS, "001", "00002222256", companyCode)
				.content(new ObjectMapper().writeValueAsString(exchangeBandsPutReqDtos))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
		// then
		// assertions;
		actualResult.andExpect(status().isOk()).andExpect(content().contentType("application/json"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value("1"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].exchangeId").value("TNII"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].effectiveDate").value("2024-04-01"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].companyId").value("1"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].oeQuarter").value("Q2"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[1].effectiveDate").value("2024-07-01"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[1].companyId").value("2"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[1].oeQuarter").value("Q3"));
		// verify
		verify(exchangeServiceMock, times(1)).saveExchangeBands(exchangeBandsPutReqDtosCaptor.getValue(),
				companyCodeCaptor.getValue());
	}

	@Test
	public void givenExchangeBandWithOmsOfferingShouldReturnOkWhenSaveExchangeBands() throws Exception {
		// given
		// data
		String companyCode = "G48";
		List<ExchangeBandsDto> exchangeBandsPutReqDtos = prepareExchangeBandsPutReqDtosWithOmsOffering();
		List<ExchangeBandsDto> exchangeBandsPutResDtos = prepareExchangeBandsPutResDtosForOMS();
		// method mocks
		when(exchangeServiceMock.saveExchangeBands(any(List.class),
				anyString())).thenReturn(exchangeBandsPutResDtos);
		// when
		ResultActions actualResult = mockMvc.perform(MockMvcRequestBuilders
				.put(URIConstants.VERSION_AND_ROOT + URIConstants.EXCHANGE_BANDS, "001", "00002222256", companyCode)
				.content(new ObjectMapper().writeValueAsString(exchangeBandsPutReqDtos))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
		// then
		// assertions;
		actualResult.andExpect(status().isOk()).andExpect(content().contentType("application/json"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value("1"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].exchangeId").value("OMS"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].omsOffering").value("OMB_TLD"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].effectiveDate").value("2024-04-01"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].companyId").value("1"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].oeQuarter").value("Q2"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[1].effectiveDate").value("2024-07-01"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[1].companyId").value("2"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[1].oeQuarter").value("Q3"));
		// verify
		verify(exchangeServiceMock).saveExchangeBands(exchangeBandsPutReqDtosCaptor.capture(),
				companyCodeCaptor.capture());

		assertEquals("G48", companyCodeCaptor.getValue());
		ObjectMapper mapper = new ObjectMapper();
		assertEquals(mapper.writeValueAsString(exchangeBandsPutReqDtos), mapper.writeValueAsString(exchangeBandsPutReqDtosCaptor.getValue()));
	}

	@Test
	public void getExchangeBands() throws Exception {
		// Given
		String companyCode = "001";
		when(exchangeServiceMock.getExchangeBands(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID))).thenReturn(prepareExchangeBands());

		// When
		ResultActions actualResult = mockMvc.perform(MockMvcRequestBuilders
				.get(URIConstants.VERSION_AND_ROOT + URIConstants.EXCHANGE_BANDS, "001", "00002222256", companyCode)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		// Then
		actualResult.andExpect(status().isOk()).andExpect(content().contentType("application/json"))
				// exchange TNII 01-JAN-2023
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].exchangeId").value("TNII"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].exchangeName").value("TriNet II"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].effectiveDate").value("2023-01-01"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].bandType").value("primary"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].carrierBands[0].carrier").value("Aetna"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].carrierBands[0].bandCode").value("21A"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].carrierBands[1].carrier").value("AETNAHMO"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].bands[0].carrierBands[1].bandCode").value(" "))
				// exchange TNIIII 01-APR-2023
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].exchangeId").value("TNIII"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].exchangeName").value("TriNet III"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[0].effectiveDate").value("2024-04-01"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[0].bandType").value("primary"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[0].carrierBands[0].carrier").value("AETNAPPO"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[0].carrierBands[0].bandCode").value("19A"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[0].carrierBands[1].carrier").value("BCBS"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[0].carrierBands[1].bandCode").value("22"))
		
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[1].effectiveDate").value("2024-07-01"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[1].bandType").value("alternate"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[1].carrierBands[0].carrier").value("AETNAPPO"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[1].carrierBands[0].bandCode").value("20A"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[1].carrierBands[1].carrier").value("BCBS"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].bands[1].carrierBands[1].bandCode").value("24"));
	}

	private List<ExchangeCarrierDto> prepareExchangeCarrierDtos() {
		List<ExchangeCarrierDto> exchangeCarrierDtos = new ArrayList<>();
		List<CarrierDto> carriers1 = new ArrayList<>();
		carriers1.add(CarrierDto.builder().portfolioId(13L).portfolioName("Tufts").build());
		carriers1.add(CarrierDto.builder().portfolioId(21L).portfolioName("BCBS ID").build());
		exchangeCarrierDtos.add(ExchangeCarrierDto.builder().exchangeId("TNIV").exchangeName("Exchange IV")
				.strategyCreated(true).carrierSelectionRequired(true).benefitsStartDateValid(false)
				.carriers(carriers1).build());
		List<CarrierDto> carriers2 = new ArrayList<>();
		carriers2.add(CarrierDto.builder().portfolioId(14L).portfolioName("Guardian").build());
		carriers2.add(CarrierDto.builder().portfolioId(22L).portfolioName("BCBS MN").build());
		exchangeCarrierDtos.add(ExchangeCarrierDto.builder().exchangeId("TNXI").exchangeName("Exchange XI")
				.strategyCreated(false).carrierSelectionRequired(false).benefitsStartDateValid(true)
				.carriers(carriers2).build());
		return exchangeCarrierDtos;
	}

	private List<ExchangeBandsDto> prepareExchangeBands() {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId("TNII").exchangeName("TriNet II")
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2023-01-01", "yyyy-MM-dd"))
								.bandType(ExchangeBandsDto.BandTypeEnum.PRIMARY.name().toLowerCase())
								.carrierBands(Arrays.asList(
										CarrierBand.CarrierBandDetails.builder().carrier("Aetna").bandCode("21A")
												.buildCarrierBandDetails(),
										CarrierBand.CarrierBandDetails.builder().carrier("AETNAHMO").bandCode(" ")
												.buildCarrierBandDetails()))
								.buildCarrierBand()))
				.buildExchangeBands();

		ExchangeBandsDto dto1 = ExchangeBandsDto.builder().exchangeId("TNIII").exchangeName("TriNet III")
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
								.bandType(ExchangeBandsDto.BandTypeEnum.PRIMARY.name().toLowerCase())
								.carrierBands(Arrays.asList(
										CarrierBand.CarrierBandDetails.builder().carrier("AETNAPPO").bandCode("19A")
												.buildCarrierBandDetails(),
										CarrierBand.CarrierBandDetails.builder().carrier("BCBS").bandCode("22")
												.buildCarrierBandDetails()))
								.buildCarrierBand(),
								CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-07-01", "yyyy-MM-dd"))
										.bandType(ExchangeBandsDto.BandTypeEnum.ALTERNATE.name().toLowerCase())
										.carrierBands(Arrays.asList(
												CarrierBand.CarrierBandDetails.builder().carrier("AETNAPPO").bandCode("20A")
														.buildCarrierBandDetails(),
												CarrierBand.CarrierBandDetails.builder().carrier("BCBS").bandCode("24")
														.buildCarrierBandDetails()))
										.buildCarrierBand()))
				.buildExchangeBands();

		return Arrays.asList(dto, dto1);
	}

	private List<ExchangeBandsDto> prepareExchangeBandsPutReqDtosWithOmsOffering() {
		List<ExchangeBandsDto> exchangeBandsDtos = prepareExchangeBandsPutReqDtos("OMS");
		exchangeBandsDtos.forEach(dto -> dto.setOmsOffering(OMB_TLD));
		return exchangeBandsDtos;
	}

	private List<ExchangeBandsDto> prepareExchangeBandsPutReqDtos(String exchangeId) {
		BenExchngEnums benExchngEnums = BenExchngEnums.getByExchangeId(exchangeId);
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId(benExchngEnums.getExchangeId()).exchangeName(benExchngEnums.getBenExchng())
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBand.CarrierBandDetails.builder().carrier("Aetna").bandCode("21A")
												.buildCarrierBandDetails(),
										CarrierBand.CarrierBandDetails.builder().carrier("AETNAHMO").bandCode(" ")
												.buildCarrierBandDetails()))
								.buildCarrierBand(),
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-07-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBand.CarrierBandDetails.builder().carrier("EMPIRENY").bandCode("31")
												.buildCarrierBandDetails(),
										CarrierBand.CarrierBandDetails.builder().carrier("KAISER").bandCode("49")
												.buildCarrierBandDetails()))
								.buildCarrierBand()))
				.buildExchangeBands();
		return Arrays.asList(dto);
	}

	private List<ExchangeBandsDto> prepareExchangeBandsPutResDtosForOMS() {
		List<ExchangeBandsDto> exchangeBandsDtos = prepareExchangeBandsPutResDtos("OMS");
		exchangeBandsDtos.forEach(dto -> dto.setOmsOffering(OMB_TLD));
		return exchangeBandsDtos;
	}

	private List<ExchangeBandsDto> prepareExchangeBandsPutResDtos(String exchangeId) {
		ExchangeBandsDto dto = ExchangeBandsDto
				.builder().exchangeId(
						exchangeId)
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
								.companyId(1).oeQuarter("Q2").buildCarrierBand(),
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-07-01", "yyyy-MM-dd"))
								.companyId(2).oeQuarter("Q3").buildCarrierBand()))
				.buildExchangeBands();
		return Arrays.asList(dto);
	}

}
