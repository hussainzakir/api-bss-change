package com.trinet.ambis.validator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class RequestValidatorTest {

	@Mock
	AppRulesConfigService appRulesConfigService;

	@Before
	public void setUp() {
		AppRulesAndConfigsUtils.setAppRuleConfigService(appRulesConfigService);
	}


	private static final String ALLOWED_CHARACTERS_KEY = "ALLOWED_CHARACTERS";
	private static final String ALLOWED_CHARACTERS_VALUE = "^[-a-zA-Z0-9,():_ ]+$";

	@Test
	public void isValidCharacterSet() {

		Map<String, String> rulesMap = new HashMap<>();
		rulesMap.put(ALLOWED_CHARACTERS_KEY, ALLOWED_CHARACTERS_VALUE);
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rulesMap);

		String testString = "";
		boolean actualResult = RequestValidator.isValidCharacterSet(testString);
		assertEquals(false, actualResult);

		testString = "(:_ ALLOWED-TEST)";
		actualResult = RequestValidator.isValidCharacterSet(testString);
		assertEquals(true, actualResult);

		testString = "NOT	ALLOWED TAB TEST";
		actualResult = RequestValidator.isValidCharacterSet(testString);
		assertEquals(false, actualResult);

		testString = "NOT	ALLOWED TEST!";
		actualResult = RequestValidator.isValidCharacterSet(testString);
		assertEquals(false, actualResult);
	}

	@Test
	public void getValidatedStrategyName() {

		Map<String, String> rulesMap = new HashMap<>();
		rulesMap.put(ALLOWED_CHARACTERS_KEY, ALLOWED_CHARACTERS_VALUE);
		rulesMap.put("STRATEGY_NAME_MAX_LENGTH", "50");
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rulesMap);

		String testString = "  (:_ ALLOWED-TEST)  ";
		String actualResult = RequestValidator.getValidatedStrategyName(testString);
		assertEquals("(:_ ALLOWED-TEST)", actualResult);

	}

	@Test(expected = BSSApplicationException.class)
	public void getValidatedStrategyNameLengthException() {

		Map<String, String> rulesMap = new HashMap<>();
		rulesMap.put(ALLOWED_CHARACTERS_KEY, ALLOWED_CHARACTERS_VALUE);
		rulesMap.put("STRATEGY_NAME_MAX_LENGTH", "10");
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rulesMap);

		String testString = "NAME TOO LONG TEST";
		RequestValidator.getValidatedStrategyName(testString);

	}

	@Test(expected = BSSApplicationException.class)
	public void getValidatedStrategyNameCharacterException() {

		Map<String, String> rulesMap = new HashMap<>();
		rulesMap.put(ALLOWED_CHARACTERS_KEY, ALLOWED_CHARACTERS_VALUE);
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rulesMap);

		String testString = "NOT	ALLOWED TAB TEST";
		RequestValidator.getValidatedStrategyName(testString);

	}

	@Test
	public void getValidatedGroupName() {

		Map<String, String> rulesMap = new HashMap<>();
		rulesMap.put(ALLOWED_CHARACTERS_KEY, ALLOWED_CHARACTERS_VALUE);
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rulesMap);

		String testString = "  (:_ ALLOWED-TEST)  ";
		String actualResult = RequestValidator.getValidatedGroupName(testString);
		assertEquals("(:_ ALLOWED-TEST)", actualResult);

	}

	@Test(expected = BSSApplicationException.class)
	public void getValidatedGroupNameLengthException() {

		Map<String, String> rulesMap = new HashMap<>();
		rulesMap.put(ALLOWED_CHARACTERS_KEY, ALLOWED_CHARACTERS_VALUE);
		rulesMap.put("GROUP_NAME_MAX_LENGTH", "10");
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rulesMap);

		String testString = "NAME TOO LONG TEST";
		RequestValidator.getValidatedGroupName(testString);

	}

	@Test(expected = BSSApplicationException.class)
	public void getValidatedGroupNameCharacterException() {

		Map<String, String> rulesMap = new HashMap<>();
		rulesMap.put(ALLOWED_CHARACTERS_KEY, ALLOWED_CHARACTERS_VALUE);
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rulesMap);

		String testString = "NOT	ALLOWED TAB TEST";
		RequestValidator.getValidatedGroupName(testString);

	}
}
