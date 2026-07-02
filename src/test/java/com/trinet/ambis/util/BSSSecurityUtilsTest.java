package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.trinet.security.domain.AuthenticationProfile;
import com.trinet.security.domain.InvocationProfile;
import com.trinet.security.util.SecurityUtils;

@RunWith(JUnit4.class)
public class BSSSecurityUtilsTest {

    @Test
    public void getAuthenticatedPersonId_returnsFirstEmplIdFromProfile() {
        List<String> emplIds = Arrays.asList("12345", "67890");
        AuthenticationProfile authProfile = mock(AuthenticationProfile.class);
        when(authProfile.getEmplid()).thenReturn(emplIds);

        InvocationProfile invocationProfile = mock(InvocationProfile.class);
        when(invocationProfile.getAuthenticationProfile()).thenReturn(authProfile);

        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getInvocationProfileFromSecurityContext).thenReturn(invocationProfile);

            String result = BSSSecurityUtils.getAuthenticatedPersonId();
            assertEquals("12345", result);
        }
    }

    @Test
    public void getCompanyCode_returnsCompanyIdListFromProfile() {
        List<String> companyIds = Arrays.asList("D07", "D99");
        List<String> emplIds = Arrays.asList("12345");
        AuthenticationProfile authProfile = mock(AuthenticationProfile.class);
        when(authProfile.getCompanyid()).thenReturn(companyIds);
        when(authProfile.getEmplid()).thenReturn(emplIds);

        InvocationProfile invocationProfile = mock(InvocationProfile.class);
        when(invocationProfile.getAuthenticationProfile()).thenReturn(authProfile);

        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getInvocationProfileFromSecurityContext).thenReturn(invocationProfile);

            List<String> result = BSSSecurityUtils.getCompanyCode();
            assertEquals(2, result.size());
            assertEquals("D07", result.get(0));
            assertEquals("D99", result.get(1));
        }
    }
}
