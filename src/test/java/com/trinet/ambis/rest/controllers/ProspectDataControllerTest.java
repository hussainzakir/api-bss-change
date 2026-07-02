package com.trinet.ambis.rest.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.exception.BSSApplicationExceptionHandler;
import com.trinet.ambis.rest.controllers.dto.ProspectDataUpdateRequest;
import com.trinet.ambis.service.ProspectDataService;
import com.trinet.ambis.service.unit.ServiceUnitTest;

/**
 * Unit tests for ProspectDataController
 *
 * @author echavarria
 */
@RunWith(MockitoJUnitRunner.class)
public class ProspectDataControllerTest extends ServiceUnitTest {

    @InjectMocks
    private ProspectDataController prospectDataController;

    @Mock
    private ProspectDataService prospectDataService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final String COMPANY_CODE = "TEST123";
    private static final String NAICS_CODE = "541330";

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(prospectDataController)
            .setControllerAdvice(new BSSApplicationExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testUpdateProspectData_LocationUpdateOnly_Success() throws Exception {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(true)
            .naicsCodeUpdate(false)
            .build();

        doNothing().when(prospectDataService).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));

        // When
        mockMvc.perform(MockMvcRequestBuilders
            .put(URIConstants.VERSION_AND_ROOT + URIConstants.UPDATE_PROSPECT_DATA, "001", "00002222256", COMPANY_CODE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            // Then
            .andExpect(status().isNoContent());

        verify(prospectDataService, times(1)).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));
    }

    @Test
    public void testUpdateProspectData_NaicsCodeUpdateOnly_Success() throws Exception {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(true)
            .naicsCode(NAICS_CODE)
            .build();

        doNothing().when(prospectDataService).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));

        // When
        mockMvc.perform(MockMvcRequestBuilders
            .put(URIConstants.VERSION_AND_ROOT + URIConstants.UPDATE_PROSPECT_DATA, "001", "00002222256", COMPANY_CODE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            // Then
            .andExpect(status().isNoContent());

        verify(prospectDataService, times(1)).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));
    }

    @Test
    public void testUpdateProspectData_BothUpdates_Success() throws Exception {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(true)
            .naicsCodeUpdate(true)
            .naicsCode(NAICS_CODE)
            .build();

        doNothing().when(prospectDataService).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));

        // When
        mockMvc.perform(MockMvcRequestBuilders
            .put(URIConstants.VERSION_AND_ROOT + URIConstants.UPDATE_PROSPECT_DATA, "001", "00002222256", COMPANY_CODE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            // Then
            .andExpect(status().isNoContent());

        verify(prospectDataService, times(1)).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));
    }

    @Test
    public void testUpdateProspectData_NoUpdates_Success() throws Exception {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(false)
            .build();

        doNothing().when(prospectDataService).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));

        // When
        mockMvc.perform(MockMvcRequestBuilders
            .put(URIConstants.VERSION_AND_ROOT + URIConstants.UPDATE_PROSPECT_DATA, "001", "00002222256", COMPANY_CODE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            // Then
            .andExpect(status().isNoContent());

        verify(prospectDataService, times(1)).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));
    }

    @Test
    public void testUpdateProspectData_TwoDigitNaicsCode_Success() throws Exception {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(true)
            .naicsCode("11")
            .build();

        doNothing().when(prospectDataService).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));

        // When
        mockMvc.perform(MockMvcRequestBuilders
            .put(URIConstants.VERSION_AND_ROOT + URIConstants.UPDATE_PROSPECT_DATA, "001", "00002222256", COMPANY_CODE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            // Then
            .andExpect(status().isNoContent());

        verify(prospectDataService, times(1)).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));
    }

    @Test
    public void testUpdateProspectData_SixDigitNaicsCode_Success() throws Exception {
        // Given
        ProspectDataUpdateRequest request = ProspectDataUpdateRequest.builder()
            .locationUpdate(false)
            .naicsCodeUpdate(true)
            .naicsCode("541330")
            .build();

        doNothing().when(prospectDataService).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));

        // When
        mockMvc.perform(MockMvcRequestBuilders
            .put(URIConstants.VERSION_AND_ROOT + URIConstants.UPDATE_PROSPECT_DATA, "001", "00002222256", COMPANY_CODE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            // Then
            .andExpect(status().isNoContent());

        verify(prospectDataService, times(1)).updateProspectData(eq(COMPANY_CODE), any(ProspectDataUpdateRequest.class));
    }
}

