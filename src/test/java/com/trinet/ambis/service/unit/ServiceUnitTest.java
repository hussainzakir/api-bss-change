package com.trinet.ambis.service.unit;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("test")
@ContextConfiguration(locations = { "/service-unit-test-context.xml" })
public class ServiceUnitTest {

}
