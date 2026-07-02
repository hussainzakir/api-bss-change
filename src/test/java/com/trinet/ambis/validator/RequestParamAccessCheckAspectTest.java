package com.trinet.ambis.validator;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.configuration.BSSMessageConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UrlPathHelper;

import com.trinet.ambis.exception.BssSecurityException;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class RequestParamAccessCheckAspectTest extends ServiceUnitTest {

	@InjectMocks
	private RequestParamAccessCheckAspect aspect;

	@Mock
	private StrategyService strategyService;

	@Mock
	private StrategyGroupService strategyGroupService;

	@Mock
	private HttpServletRequest request;
	
	@Mock
	private UrlPathHelper urlPathHelper;
	/*
	 * @Rule public PowerMockRule rule = new PowerMockRule();
	 */

	private ProceedingJoinPoint joinPoint;
	MethodSignature signature;
	private static final String EMPLID = "0000000123456";
	private static final String COMPANY_CODE = "ABC";
	private static final String PROSPECT_COMPANY_CODE = "PROSPECT";

    private MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;
    private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

    @After
    public void tearDown() {
        if (mockStaticBSSMessageConfig != null) mockStaticBSSMessageConfig.close();
        if (mockStaticBSSSecurityUtils != null) mockStaticBSSSecurityUtils.close();

    }

	@Before
	public void setUp() {
        mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
        mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);

        joinPoint = mock(ProceedingJoinPoint.class);
		signature = mock(MethodSignature.class);

        mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
        mockStaticBSSSecurityUtils.when(() -> BSSSecurityUtils.getAuthenticatedCompanyCode(request)).thenReturn(COMPANY_CODE);
        when(urlPathHelper.getRequestUri(request)).thenReturn("/some-resource/" + COMPANY_CODE);
		when(joinPoint.getSignature()).thenReturn(signature);
	}

	@Test
	public void hasAccessToStrategy() throws NoSuchMethodException, SecurityException {
		Long strategyId = 1234L;
		Object[] args = { request, strategyId };
		Strategy strategy = new Strategy();
		strategy.setId(strategyId);

		when(signature.getMethod()).thenReturn(RequestParamAccessCheckAspectTest.class
				.getMethod("methodWithStrategyIdAsLong", HttpServletRequest.class, Long.class));
		when(joinPoint.getArgs()).thenReturn(args);
		when(strategyService.findBy(COMPANY_CODE)).thenReturn(Arrays.asList(strategy));

		aspect.beforeStrategyIdValidator(joinPoint);
	}

	@Test(expected = BssSecurityException.class)
	public void doesNotHaveAccessToStrategy() throws NoSuchMethodException, SecurityException {
		Long strategyId = 1234L;
		Object[] args = { request, strategyId };
		Strategy strategy = new Strategy();
		strategy.setId(2222L);

		when(signature.getMethod()).thenReturn(RequestParamAccessCheckAspectTest.class
				.getMethod("methodWithStrategyIdAsLong", HttpServletRequest.class, Long.class));
		when(joinPoint.getArgs()).thenReturn(args);
		when(strategyService.findBy(COMPANY_CODE)).thenReturn(Arrays.asList(strategy));

		aspect.beforeStrategyIdValidator(joinPoint);
	}

	@Test
	public void hasAccessToProspectStrategy() throws NoSuchMethodException, SecurityException {
		Long strategyId = 0L;
		Object[] args = { request, strategyId };
		Strategy strategy = new Strategy();
		strategy.setId(1234L);
		List<Strategy> strategies = new ArrayList<>();
		strategies.add(strategy);

        mockStaticBSSSecurityUtils.when(() -> BSSSecurityUtils.getAuthenticatedCompanyCode(request)).thenReturn(PROSPECT_COMPANY_CODE);		when(urlPathHelper.getRequestUri(request)).thenReturn("/some-resource/" + PROSPECT_COMPANY_CODE);

		when(signature.getMethod()).thenReturn(RequestParamAccessCheckAspectTest.class
				.getMethod("methodWithStrategyIdAsLong", HttpServletRequest.class, Long.class));
		when(joinPoint.getArgs()).thenReturn(args);
		when(strategyService.findBy(PROSPECT_COMPANY_CODE)).thenReturn(strategies);

		aspect.beforeStrategyIdValidator(joinPoint);
		
		verify(strategyService, times(1)).findBy(PROSPECT_COMPANY_CODE);
	}

	@Test(expected = BssSecurityException.class)
	public void doesNotHaveAccessToProspectStrategy() throws NoSuchMethodException, SecurityException {
		Long strategyId = 0L;
		Object[] args = { request, strategyId };
		Strategy strategy = new Strategy();
		strategy.setId(1234L);
		List<Strategy> strategies = new ArrayList<>();
		strategies.add(strategy);

		when(signature.getMethod()).thenReturn(RequestParamAccessCheckAspectTest.class
				.getMethod("methodWithStrategyIdAsLong", HttpServletRequest.class, Long.class));
		when(joinPoint.getArgs()).thenReturn(args);
		when(strategyService.findBy(COMPANY_CODE)).thenReturn(strategies);

		aspect.beforeStrategyIdValidator(joinPoint);
	}

	@Test
	public void strategyIdParamAsString() throws NoSuchMethodException, SecurityException {
		String strategyId = "1111,2222";
		Object[] args = { request, strategyId };
		Strategy strategy = new Strategy();
		strategy.setId(1111L);
		Strategy strategy1 = new Strategy();
		strategy1.setId(2222L);

		when(signature.getMethod()).thenReturn(RequestParamAccessCheckAspectTest.class
				.getMethod("methodWithStrategyIdsAsString", HttpServletRequest.class, String.class));
		when(joinPoint.getArgs()).thenReturn(args);
		when(strategyService.findBy(COMPANY_CODE)).thenReturn(Arrays.asList(strategy, strategy1));

		aspect.beforeStrategyIdValidator(joinPoint);
		
		verify(strategyService, times(1)).findBy(COMPANY_CODE);
	}

	@Test
	public void strategyIdParamAsList() throws NoSuchMethodException, SecurityException {
		List<Long> strategyIds = Arrays.asList(1111L, 2222L);
		Object[] args = { request, strategyIds };
		Strategy strategy = new Strategy();
		strategy.setId(1111L);
		Strategy strategy1 = new Strategy();
		strategy1.setId(2222L);

		when(signature.getMethod()).thenReturn(RequestParamAccessCheckAspectTest.class
				.getMethod("methodWithStrategyIdsAsList", HttpServletRequest.class, List.class));
		when(joinPoint.getArgs()).thenReturn(args);
		when(strategyService.findBy(COMPANY_CODE)).thenReturn(Arrays.asList(strategy, strategy1));

		aspect.beforeStrategyIdValidator(joinPoint);
		
		verify(strategyService, times(1)).findBy(COMPANY_CODE);
	}

	@Test
	public void hasAccessToBenGroup() throws NoSuchMethodException, SecurityException {
		Long groupId = 1111L;
		Object[] args = { request, groupId };
		BenefitGroupStrategy benGrpStrategy = new BenefitGroupStrategy();
		benGrpStrategy.setGroupId(groupId);

		when(signature.getMethod()).thenReturn(RequestParamAccessCheckAspectTest.class.getMethod("methodWithBenGroupId",
				HttpServletRequest.class, Long.class));
		when(joinPoint.getArgs()).thenReturn(args);
		when(strategyGroupService.findBy(COMPANY_CODE)).thenReturn(Arrays.asList(benGrpStrategy));

		aspect.beforeGroupIdValidator(joinPoint);
		
		verify(strategyGroupService, times(1)).findBy(COMPANY_CODE);
	}

	@Test(expected = BssSecurityException.class)
	public void doesNotHaveAccessToBenGroup() throws NoSuchMethodException, SecurityException {
		Long groupId = 1111L;
		Object[] args = { request, groupId };
		BenefitGroupStrategy benGrpStrategy = new BenefitGroupStrategy();
		benGrpStrategy.setId(2222L);

		when(signature.getMethod()).thenReturn(RequestParamAccessCheckAspectTest.class.getMethod("methodWithBenGroupId",
				HttpServletRequest.class, Long.class));
		when(joinPoint.getArgs()).thenReturn(args);
		when(strategyGroupService.findBy(COMPANY_CODE)).thenReturn(Arrays.asList(benGrpStrategy));

		aspect.beforeGroupIdValidator(joinPoint);
	}
	
	@Test
	public void hasAccessToProspectCurrentStrategyBenGroup() throws NoSuchMethodException, SecurityException {
		Long groupId = 1111L;
		Object[] args = { request, groupId };

		when(urlPathHelper.getRequestUri(request)).thenReturn("/some-resource/" + PROSPECT_COMPANY_CODE);
		when(signature.getMethod()).thenReturn(RequestParamAccessCheckAspectTest.class.getMethod("methodWithBenGroupId",
				HttpServletRequest.class, Long.class));
		when(joinPoint.getArgs()).thenReturn(args);

		aspect.beforeGroupIdValidator(joinPoint);
		
		verify(strategyGroupService, times(0)).findBy(anyString());
	}

	public void methodWithStrategyIdAsLong(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyId") final Long strategyId) {

	}

	public void methodWithStrategyIdsAsString(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyIds") final String strategyIds) {

	}

	public void methodWithStrategyIdsAsList(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyIds") final List<Long> strategyIds) {

	}

	public void methodWithBenGroupId(HttpServletRequest request,
			@GroupIdValidator @PathVariable("groupId") final Long groupId) {

	}
}
