package com.trinet.ambis.exception;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.util.BSSSecurityUtils;

@RunWith(MockitoJUnitRunner.class)
public class BSSApplicationExceptionHandlerTest {

    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMockedStatic;

    @Before
    public void setUp() {
        bssSecurityUtilsMockedStatic = org.mockito.Mockito.mockStatic(BSSSecurityUtils.class);
    }

    @After
    public void tearDown() {
        bssSecurityUtilsMockedStatic.close();
    }
	
	@Test
	public void handleBSSApplicationExceptionTest() throws IOException {
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn("00002222276");
		
		HttpServletRequest request = new MockHttpServletRequest();
		
		BSSApplicationException bssApplicationException = new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_COMPANY_NOT_FOUND,
				BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, BSSApplicationExceptionHandler.class.toString(),
				"Error retrieving company details from XBSS_COMPANY table", null, null));
		bssApplicationException.initCause(new RuntimeException());

		BSSApplicationExceptionHandler handler = new BSSApplicationExceptionHandler();
		ResponseEntity<Object> result = handler.handleBSSApplicationException(bssApplicationException, request);
		
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
	}

	@Test
	public void handleBSSApplicationExceptionTestWithSystemAccount() throws IOException {
		when(BSSSecurityUtils.checkSystemAccount()).thenReturn(true);

		HttpServletRequest request = new MockHttpServletRequest();

		BSSApplicationException bssApplicationException = new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_COMPANY_NOT_FOUND,
				BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, BSSApplicationExceptionHandler.class.toString(),
				"Prices and strategy estimates are being updated. The BSS should be available momentarily, refresh this page to try again", null, null));
		bssApplicationException.initCause(new RuntimeException());

		BSSApplicationExceptionHandler handler = new BSSApplicationExceptionHandler();
		ResponseEntity<Object> result = handler.handleBSSApplicationException(bssApplicationException, request);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
	}
}
