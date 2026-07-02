package com.trinet.ambis.rest.controllers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.email.impl.AleServiceImpl;
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

@RunWith(MockitoJUnitRunner.class)
public class AleControllerTest {

    @InjectMocks
    private AleController aleController;

    @Mock
    private AleServiceImpl aleService;

    private MockMvc mockMvc;

    private static final Long BSS_COMPANY_ID = 12345L;
    private static final String COMPANY_CODE = "TestCompany";

    private static final String URI = URIConstants.VERSION_AND_ROOT + URIConstants.UPDATE_ALE;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aleController).build();
    }

    /**
     * Given: Valid bssCompanyId and companyCode
     * When: A PUT request is made to update ALE status
     * Then: Status is 200 OK and service method is called
     */
    @Test
    public void testUpdateAleStatus_Success() throws Exception {
        // Given
        doNothing().when(aleService).updateAleChangeStatus(BSS_COMPANY_ID, COMPANY_CODE);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put(URI, "001", "00002222256", BSS_COMPANY_ID, COMPANY_CODE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify
        verify(aleService).updateAleChangeStatus(BSS_COMPANY_ID, COMPANY_CODE);
    }

    /**
     * Given: Missing path variables
     * When: A PUT request is made without required parameters
     * Then: Status is 404 Not Found
     */
    @Test
    public void testUpdateAleStatus_MissingPathVariables() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(URI,"001", "00002222256", null, null)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
