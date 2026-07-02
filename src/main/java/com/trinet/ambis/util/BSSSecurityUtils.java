package com.trinet.ambis.util;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.exception.GatewaySecurityException;
import com.trinet.security.common.SecurityConstants;
import com.trinet.security.domain.AuthenticationProfile;
import com.trinet.security.domain.ImpersonationRequest;
import com.trinet.security.domain.InvocationProfile;
import com.trinet.security.util.SecurityUtilComponent;
import com.trinet.security.util.SecurityUtils;

public class BSSSecurityUtils extends SecurityUtilComponent {

	private static final Logger LOGGER = LoggerFactory.getLogger(BSSSecurityUtils.class);

	private BSSSecurityUtils() {
		throw new IllegalStateException("Utility class, do not instantiate");
	}

	public static AuthenticationProfile getAuthenticationProfile() {
		InvocationProfile invocationProfile = SecurityUtils.getInvocationProfileFromSecurityContext();
		if (invocationProfile == null) {
			LOGGER.error("Invocation profile was found to be NULL when calling getAuthenticationProfile()");
			throw new GatewaySecurityException("Unable to acquire invocation profile from the security context");
		}
		AuthenticationProfile authenticationProfile = invocationProfile.getAuthenticationProfile();
		if (authenticationProfile == null || authenticationProfile.getEmplid().isEmpty()) {
			LOGGER.error(
					"Authenication profile was NULL or Empty Emplid List was found when calling getAuthenticationProfile()");
			throw new GatewaySecurityException(
					"Unable to acquire authentication profile nor the employee from the invocation profile");
		}
		return authenticationProfile;
	}
	
	public static void addImpersonationHeaders(HttpServletRequest request, HttpHeaders headers) {
		ImpersonationRequest impReq = SecurityUtils.parseImpersonationRequest(request);
	    if(impReq != null) {
	    	headers.add(SecurityConstants.IMPERSONATE_EMPLID_HEADER, impReq.getImpersonatedEmployeeId());
			headers.add(SecurityConstants.IMPERSONATE_COMPANY_HEADER, impReq.getImpersonatedCompanyId());
	    }
	}

	public static String getAuthenticatedPersonId() {
		return getAuthenticationProfile().getEmplid().get(0);
	}

	public static List<String> getCompanyCode() {
		return getAuthenticationProfile().getCompanyid();
	}

	public static List<String> getAuthenticatedEmployeeList() {
		return getAuthenticationProfile().getEmplid();
	}
	
	public static String getAuthenticatedCompanyCode(HttpServletRequest request) {
		Map<String, String> resultMap = SecurityUtils.parseCompanyAndEmployeeIdsFromURL(request);
		return resultMap.get(SecurityConstants.COMPANY_ID);
	}
	
	public static String getAuthenticatedEmplId(HttpServletRequest request) {
		Map<String, String> resultMap = SecurityUtils.parseCompanyAndEmployeeIdsFromURL(request);
		return resultMap.get(SecurityConstants.EMPLOYEE_ID);
	}
	
	public static boolean checkSystemAccount() {
		InvocationProfile invocationProfile = SecurityUtils.getInvocationProfileFromSecurityContext();
		if (invocationProfile == null) {
			LOGGER.error("Invocation profile was found to be NULL when calling getAuthenticationProfile()");
			throw new GatewaySecurityException("Unable to acquire invocation profile from the security context");
		}
		AuthenticationProfile authenticationProfile = invocationProfile.getAuthenticationProfile();
		if (authenticationProfile == null) {
			LOGGER.error("Authenication profile was NULL when calling getAuthenticationProfile()");
			throw new GatewaySecurityException("Unable to acquire authentication profile from the invocation profile");
		}
		if (invocationProfile.getResolverType().equals(BSSApplicationConstants.SYSTEM_ACCOUNT))
			return true;
		return false;
	}

	
}