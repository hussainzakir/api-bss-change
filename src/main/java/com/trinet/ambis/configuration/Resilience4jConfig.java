package com.trinet.ambis.configuration;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Duration;
import java.util.Arrays;

/**
 * Programmatic Resilience4j configuration for plain Spring MVC (non-Boot) applications.
 *
 * <p>Because this project is deployed as a WAR on Tomcat (not as a Spring Boot application),
 * the {@code resilience4j-spring-boot2} auto-configuration never activates and therefore
 * {@link RetryRegistry} is never registered in the application context automatically.
 * This class fills that gap by constructing the registry manually.</p>
 *
 * <p>Properties are read directly from Spring's {@link Environment}, which is populated
 * from {@code api-bss.properties} via {@link BSSMessageConfig}'s {@code @PropertySource}
 * declarations. {@link PropertySourcesConfig} registers the
 * {@code PropertySourcesPlaceholderConfigurer} that enables property resolution.</p>
 *
 * <p>Note: {@code resilience4j.retry.configs.default.retry-exceptions} is intentionally
 * not consumed here — retry and ignore exception lists are driven exclusively by the
 * {@code flexRate} instance properties.</p>
 */
@Configuration
public class Resilience4jConfig {

    // ── default-config keys ───────────────────────────────────────────────────
    private static final String DEFAULT_MAX_ATTEMPTS_KEY           = "resilience4j.retry.configs.default.max-attempts";
    private static final String DEFAULT_WAIT_DURATION_KEY          = "resilience4j.retry.configs.default.wait-duration";
    private static final String DEFAULT_ENABLE_EXP_BACKOFF_KEY     = "resilience4j.retry.configs.default.enable-exponential-backoff";
    private static final String DEFAULT_EXP_BACKOFF_MULTIPLIER_KEY = "resilience4j.retry.configs.default.exponential-backoff-multiplier";

    // ── flexRate instance keys ────────────────────────────────────────────────
    private static final String FLEX_RATE_MAX_ATTEMPTS_KEY         = "resilience4j.retry.instances.flexRate.max-attempts";
    private static final String FLEX_RATE_WAIT_DURATION_KEY        = "resilience4j.retry.instances.flexRate.wait-duration";
    private static final String FLEX_RATE_RETRY_EXCEPTIONS_KEY     = "resilience4j.retry.instances.flexRate.retry-exceptions";
    private static final String FLEX_RATE_IGNORE_EXCEPTIONS_KEY    = "resilience4j.retry.instances.flexRate.ignore-exceptions";

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a {@link RetryRegistry} with a pre-configured {@code "flexRate"} instance.
     * All exception lists are driven exclusively by {@code api-bss.properties}.
     */
    @Bean
    public RetryRegistry retryRegistry(Environment environment) {
        // Resolve max-attempts: instance override → default config fallback
        String flexRateMaxAttemptsStr = environment.getProperty(FLEX_RATE_MAX_ATTEMPTS_KEY, "");
        int maxAttempts = !flexRateMaxAttemptsStr.isEmpty()
                ? Integer.parseInt(flexRateMaxAttemptsStr.trim())
                : Integer.parseInt(environment.getProperty(DEFAULT_MAX_ATTEMPTS_KEY, "2"));

        // Resolve wait-duration: instance override → default config fallback
        String flexRateWaitDurationStr = environment.getProperty(FLEX_RATE_WAIT_DURATION_KEY, "");
        Duration waitDuration = !flexRateWaitDurationStr.isEmpty()
                ? parseDuration(flexRateWaitDurationStr)
                : parseDuration(environment.getProperty(DEFAULT_WAIT_DURATION_KEY, "1s"));

        boolean enableExponentialBackoff = Boolean.parseBoolean(
                environment.getProperty(DEFAULT_ENABLE_EXP_BACKOFF_KEY, "true"));
        double exponentialBackoffMultiplier = Double.parseDouble(
                environment.getProperty(DEFAULT_EXP_BACKOFF_MULTIPLIER_KEY, "2"));

        String flexRateRetryExceptions  = environment.getProperty(FLEX_RATE_RETRY_EXCEPTIONS_KEY, "");
        String flexRateIgnoreExceptions = environment.getProperty(FLEX_RATE_IGNORE_EXCEPTIONS_KEY, "");

        RetryConfig.Builder<Object> builder = RetryConfig.custom()
                .maxAttempts(maxAttempts);

        if (!flexRateRetryExceptions.isEmpty()) {
            builder.retryExceptions(resolveClasses(flexRateRetryExceptions));
        }
        if (!flexRateIgnoreExceptions.isEmpty()) {
            builder.ignoreExceptions(resolveClasses(flexRateIgnoreExceptions));
        }

        // intervalFunction and waitDuration both set the same underlying function —
        // setting both causes IllegalStateException. Use one or the other exclusively.
        if (enableExponentialBackoff) {
            builder.intervalFunction(
                    io.github.resilience4j.core.IntervalFunction
                            .ofExponentialBackoff(waitDuration.toMillis(), exponentialBackoffMultiplier));
        } else {
            builder.waitDuration(waitDuration);
        }

        return RetryRegistry.of(builder.build());
    }

    /**
     * Parses Spring Boot–style duration strings: {@code "1s"}, {@code "500ms"}, or plain millis.
     */
    private static Duration parseDuration(String value) {
        String v = value.trim();
        if (v.endsWith("ms")) {
            return Duration.ofMillis(Long.parseLong(v.substring(0, v.length() - 2).trim()));
        } else if (v.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(v.substring(0, v.length() - 1).trim()));
        }
        return Duration.ofMillis(Long.parseLong(v));
    }

    /**
     * Resolves a comma-separated list of fully-qualified class names from a property value
     * into a {@code Class<? extends Throwable>[]} array.
     * Fails fast at startup with a clear message if a class name is not found on the classpath.
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Throwable>[] resolveClasses(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(name -> {
                    try {
                        return (Class<? extends Throwable>) Class.forName(name);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException(
                                "Resilience4j: cannot load exception class '" + name + "'", e);
                    }
                })
                .toArray(Class[]::new);
    }
}
