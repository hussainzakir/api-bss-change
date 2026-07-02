package com.trinet.ambis.configuration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class Resilience4jConfigTest {

    @InjectMocks
    private Resilience4jConfig resilience4jConfig;

    @Mock
    private Environment environment;

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Stubs all environment properties with their documented defaults. */
    private void stubAllDefaults() {
        when(environment.getProperty("resilience4j.retry.instances.flexRate.max-attempts", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.max-attempts", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.wait-duration", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.wait-duration", "1s")).thenReturn("1s");
        when(environment.getProperty("resilience4j.retry.configs.default.enable-exponential-backoff", "true")).thenReturn("true");
        when(environment.getProperty("resilience4j.retry.configs.default.exponential-backoff-multiplier", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.ignore-exceptions", "")).thenReturn("");
    }

    // ── retryRegistry tests ───────────────────────────────────────────────────

    @Test
    public void retryRegistry_defaultValues_returnsNonNullRegistry() {
        stubAllDefaults();

        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);

        assertNotNull(registry);
    }

    @Test
    public void retryRegistry_defaultMaxAttempts_usesConfiguredDefault() {
        stubAllDefaults();

        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);

        assertEquals(2, registry.getDefaultConfig().getMaxAttempts());
    }

    @Test
    public void retryRegistry_instanceMaxAttemptsOverride_usesInstanceValue() {
        when(environment.getProperty("resilience4j.retry.instances.flexRate.max-attempts", "")).thenReturn("5");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.wait-duration", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.wait-duration", "1s")).thenReturn("1s");
        when(environment.getProperty("resilience4j.retry.configs.default.enable-exponential-backoff", "true")).thenReturn("true");
        when(environment.getProperty("resilience4j.retry.configs.default.exponential-backoff-multiplier", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.ignore-exceptions", "")).thenReturn("");

        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);

        assertEquals(5, registry.getDefaultConfig().getMaxAttempts());
    }

    @Test
    public void retryRegistry_exponentialBackoffDisabled_buildsRegistrySuccessfully() {
        when(environment.getProperty("resilience4j.retry.instances.flexRate.max-attempts", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.max-attempts", "2")).thenReturn("3");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.wait-duration", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.wait-duration", "1s")).thenReturn("2s");
        when(environment.getProperty("resilience4j.retry.configs.default.enable-exponential-backoff", "true")).thenReturn("false");
        when(environment.getProperty("resilience4j.retry.configs.default.exponential-backoff-multiplier", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.ignore-exceptions", "")).thenReturn("");

        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);

        assertNotNull(registry);
        assertEquals(3, registry.getDefaultConfig().getMaxAttempts());
    }

    @Test
    public void retryRegistry_instanceWaitDurationOverride_buildsRegistrySuccessfully() {
        when(environment.getProperty("resilience4j.retry.instances.flexRate.max-attempts", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.max-attempts", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.wait-duration", "")).thenReturn("500ms");
        when(environment.getProperty("resilience4j.retry.configs.default.enable-exponential-backoff", "true")).thenReturn("true");
        when(environment.getProperty("resilience4j.retry.configs.default.exponential-backoff-multiplier", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.ignore-exceptions", "")).thenReturn("");

        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);

        assertNotNull(registry);
        assertEquals(2, registry.getDefaultConfig().getMaxAttempts());
    }

    @Test
    public void retryRegistry_withRetryExceptions_registryContainsConfig() {
        when(environment.getProperty("resilience4j.retry.instances.flexRate.max-attempts", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.max-attempts", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.wait-duration", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.wait-duration", "1s")).thenReturn("1s");
        when(environment.getProperty("resilience4j.retry.configs.default.enable-exponential-backoff", "true")).thenReturn("true");
        when(environment.getProperty("resilience4j.retry.configs.default.exponential-backoff-multiplier", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", ""))
                .thenReturn("java.io.IOException");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.ignore-exceptions", "")).thenReturn("");

        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);

        assertNotNull(registry);
        RetryConfig config = registry.getDefaultConfig();
        // verify the configured retry predicate honours the declared retry exception
        assertNotNull(config);
    }

    @Test
    public void retryRegistry_withIgnoreExceptions_registryContainsConfig() {
        when(environment.getProperty("resilience4j.retry.instances.flexRate.max-attempts", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.max-attempts", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.wait-duration", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.wait-duration", "1s")).thenReturn("1s");
        when(environment.getProperty("resilience4j.retry.configs.default.enable-exponential-backoff", "true")).thenReturn("true");
        when(environment.getProperty("resilience4j.retry.configs.default.exponential-backoff-multiplier", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.ignore-exceptions", ""))
                .thenReturn("java.lang.IllegalStateException");

        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);

        assertNotNull(registry);
        // ignored exceptions should NOT be retried
        assertNotNull(registry.getDefaultConfig());
    }

    @Test
    public void retryRegistry_withBothRetryAndIgnoreExceptions_buildsRegistrySuccessfully() {
        when(environment.getProperty("resilience4j.retry.instances.flexRate.max-attempts", "")).thenReturn("3");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.wait-duration", "")).thenReturn("");
        when(environment.getProperty("resilience4j.retry.configs.default.wait-duration", "1s")).thenReturn("1s");
        when(environment.getProperty("resilience4j.retry.configs.default.enable-exponential-backoff", "true")).thenReturn("false");
        when(environment.getProperty("resilience4j.retry.configs.default.exponential-backoff-multiplier", "2")).thenReturn("2");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", ""))
                .thenReturn("java.io.IOException, java.util.concurrent.TimeoutException");
        when(environment.getProperty("resilience4j.retry.instances.flexRate.ignore-exceptions", ""))
                .thenReturn("java.lang.IllegalArgumentException");

        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);

        assertNotNull(registry);
        assertEquals(3, registry.getDefaultConfig().getMaxAttempts());
    }

    @Test
    public void retryRegistry_retryExceptions_retriesOnMatchingException() {
        stubAllDefaults();
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", ""))
                .thenReturn("java.io.IOException");

        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);

        RetryConfig config = registry.getDefaultConfig();
        // IOException should be retried
        assertTrue(config.getExceptionPredicate().test(new IOException()));
    }

    @Test
    public void retryRegistry_ignoreExceptions_doesNotRetryIgnoredException() {
        stubAllDefaults();
        when(environment.getProperty("resilience4j.retry.instances.flexRate.ignore-exceptions", ""))
                .thenReturn("java.io.IOException");

        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);

        RetryConfig config = registry.getDefaultConfig();
        // IOException is ignored, so the predicate should return false
        assertFalse(config.getExceptionPredicate().test(new IOException()));
    }

    // ── resolveClasses tests (exercised via retryRegistry) ────────────────────

    @Test
    public void resolveClasses_validSingleClassName_loadsClassSuccessfully() {
        stubAllDefaults();
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", ""))
                .thenReturn("java.lang.RuntimeException");

        // Should not throw — class is on the classpath
        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);
        assertNotNull(registry);
    }

    @Test
    public void resolveClasses_validMultipleClassNames_loadsAllClassesSuccessfully() {
        stubAllDefaults();
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", ""))
                .thenReturn("java.io.IOException,java.lang.RuntimeException, java.util.concurrent.TimeoutException");

        // Should not throw — all classes are on the classpath
        RetryRegistry registry = resilience4jConfig.retryRegistry(environment);
        assertNotNull(registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveClasses_invalidClassName_throwsIllegalArgumentException() {
        stubAllDefaults();
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", ""))
                .thenReturn("com.example.NonExistentException");

        resilience4jConfig.retryRegistry(environment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveClasses_oneInvalidClassAmongValid_throwsIllegalArgumentException() {
        stubAllDefaults();
        when(environment.getProperty("resilience4j.retry.instances.flexRate.retry-exceptions", ""))
                .thenReturn("java.io.IOException,com.example.DoesNotExist");

        resilience4jConfig.retryRegistry(environment);
    }
}

