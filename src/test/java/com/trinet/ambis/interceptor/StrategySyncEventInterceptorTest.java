package com.trinet.ambis.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpClientErrorException;

import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.service.ProcessStatusService;

@RunWith(MockitoJUnitRunner.class)
public class StrategySyncEventInterceptorTest {

    @InjectMocks
    private StrategySyncEventInterceptor interceptor;

    @Mock
    private ProcessStatusService processStatusService;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String COMPANY_CODE = "ABC123";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    /**
     * Paths in the excluded list must pass through without any service calls.
     */
    @Test
    public void preHandle_excludedPath_returnsTrue() throws Exception {
        request.setRequestURI("/v1.0/benefits/prospect/census/" + COMPANY_CODE);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(processStatusService, never()).findStrategySyncProcessStatus(anyString());
        verify(processStatusService, never()).findBssCoreProcessStatus(anyString());
    }

    /**
     * When the band flag is non-empty the request must be blocked with the
     * BSS_STRATEGY_SYNC_EVENT_IN_PROCESS error code. Both service methods are
     * invoked before the check.
     */
    @Test
    public void preHandle_bandsBlocked_throwsStrategyBandsSyncException() throws Exception {
        request.setRequestURI("/v1.0/benefits/strategy-summary/" + COMPANY_CODE);
        when(processStatusService.findStrategySyncProcessStatus(COMPANY_CODE)).thenReturn("N");

        try {
            interceptor.preHandle(request, response, new Object());
            fail("Expected BSSApplicationException for band sync gate");
        } catch (BSSApplicationException e) {
            assertEquals(BSSErrorResponseCodes.BSS_STRATEGY_SYNC_EVENT_IN_PROCESS,
                    e.getBssError().getCode());
        }

        verify(processStatusService).findStrategySyncProcessStatus(COMPANY_CODE);
        verify(processStatusService, never()).findBssCoreProcessStatus(anyString());
    }

    /**
     * When the bundle flag is false (BSS Core processes not all completed) the
     * request must be blocked with the BSS_BUNDLE_SYNC_EVENT_IN_PROCESS error
     * code, even when the band flag is clear.
     */
//    @Test
//    public void preHandle_bundlesBlocked_throwsBundleSyncException() throws Exception {
//        request.setRequestURI("/v1.0/benefits/strategy-summary/" + COMPANY_CODE);
//        when(processStatusService.findStrategySyncProcessStatus(COMPANY_CODE)).thenReturn("");
//        when(processStatusService.findBssCoreProcessStatus(COMPANY_CODE)).thenReturn(false);
//
//        try {
//            interceptor.preHandle(request, response, new Object());
//            fail("Expected BSSApplicationException for bundle sync gate");
//        } catch (BSSApplicationException e) {
//            assertEquals(BSSErrorResponseCodes.BSS_BUNDLE_SYNC_EVENT_IN_PROCESS,
//                    e.getBssError().getCode());
//        }
//
//        verify(processStatusService).findStrategySyncProcessStatus(COMPANY_CODE);
//        verify(processStatusService).findBssCoreProcessStatus(COMPANY_CODE);
//    }

    /**
     * When both flags are clear the request must pass through.
     */
    @Test
    public void preHandle_bothClear_returnsTrue() throws Exception {
        request.setRequestURI("/v1.0/benefits/strategy-summary/" + COMPANY_CODE);
        when(processStatusService.findStrategySyncProcessStatus(COMPANY_CODE)).thenReturn("");
//        when(processStatusService.findBssCoreProcessStatus(COMPANY_CODE)).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(processStatusService).findStrategySyncProcessStatus(COMPANY_CODE);
//        verify(processStatusService).findBssCoreProcessStatus(COMPANY_CODE);
    }

    /**
     * When bands are blocking, the bands gate fires first and the bundle check
     * is never reached — only BSS_STRATEGY_SYNC_EVENT_IN_PROCESS is thrown.
     */
    @Test
    public void preHandle_bothBlocked_bandGateTakesPrecedence() throws Exception {
        request.setRequestURI("/v1.0/benefits/strategy-summary/" + COMPANY_CODE);
        when(processStatusService.findStrategySyncProcessStatus(COMPANY_CODE)).thenReturn("N");

        try {
            interceptor.preHandle(request, response, new Object());
            fail("Expected BSSApplicationException when both gates are blocking");
        } catch (BSSApplicationException e) {
            assertEquals(BSSErrorResponseCodes.BSS_STRATEGY_SYNC_EVENT_IN_PROCESS,
                    e.getBssError().getCode());
        }

        verify(processStatusService).findStrategySyncProcessStatus(COMPANY_CODE);
        verify(processStatusService, never()).findBssCoreProcessStatus(anyString());
    }

    /**
     * When BSS Core returns a 401/403, findBssCoreProcessStatus throws
     * BSSApplicationException(BSS_CORE_AUTH_ERROR) which must propagate through
     * the interceptor unchanged.
     */
//    @Test
//    public void preHandle_bssCoreAuthError_propagatesBSSApplicationException() throws Exception {
//        request.setRequestURI("/v1.0/benefits/strategy-summary/" + COMPANY_CODE);
//        when(processStatusService.findStrategySyncProcessStatus(COMPANY_CODE)).thenReturn("");
//        when(processStatusService.findBssCoreProcessStatus(COMPANY_CODE))
//                .thenThrow(new BSSApplicationException(
//                        new HttpClientErrorException(HttpStatus.FORBIDDEN),
//                        new com.trinet.ambis.exception.BSSApplicationError(
//                                BSSErrorResponseCodes.BSS_CORE_AUTH_ERROR,
//                                com.trinet.ambis.common.BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
//                                StrategySyncEventInterceptor.class.getName(),
//                                "BSS Core authorization failed.", null, null)));
//
//        try {
//            interceptor.preHandle(request, response, new Object());
//            fail("Expected BSSApplicationException for BSS Core auth error");
//        } catch (BSSApplicationException e) {
//            assertEquals(BSSErrorResponseCodes.BSS_CORE_AUTH_ERROR, e.getBssError().getCode());
//        }
//
//        verify(processStatusService).findStrategySyncProcessStatus(COMPANY_CODE);
//        verify(processStatusService).findBssCoreProcessStatus(COMPANY_CODE);
//    }

    /**
     * When BSS Core returns a non-auth HTTP error (e.g. 404), findBssCoreProcessStatus
     * re-throws the raw HttpClientErrorException which must propagate through the interceptor.
     */
//    @Test
//    public void preHandle_bssCoreNonAuthHttpError_propagatesHttpClientErrorException() throws Exception {
//        request.setRequestURI("/v1.0/benefits/strategy-summary/" + COMPANY_CODE);
//        when(processStatusService.findStrategySyncProcessStatus(COMPANY_CODE)).thenReturn("");
//        when(processStatusService.findBssCoreProcessStatus(COMPANY_CODE))
//                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
//                        org.springframework.http.HttpHeaders.EMPTY, null, null));
//
//        try {
//            interceptor.preHandle(request, response, new Object());
//            fail("Expected HttpClientErrorException for non-auth BSS Core error");
//        } catch (HttpClientErrorException e) {
//            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
//        }
//
//        verify(processStatusService).findStrategySyncProcessStatus(COMPANY_CODE);
//        verify(processStatusService).findBssCoreProcessStatus(COMPANY_CODE);
//    }
}
