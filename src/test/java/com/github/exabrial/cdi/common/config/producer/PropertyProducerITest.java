package com.github.exabrial.cdi.common.config.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.openwebbeans.junit5.Cdi;
import org.junit.jupiter.api.Test;

import com.github.exabrial.cdi.common.instanceutil.api.InstanceUtil;
import com.github.exabrial.cdi.common.slf4j.producer.Slf4jLogProducer;

import jakarta.inject.Inject;

@Cdi(disableDiscovery = true,
		classes = { Slf4jLogProducer.class, InstanceUtil.class, PropertyProducer.class, TestInjectionOfProperty.class })
class PropertyProducerITest {
	@Inject
	private TestInjectionOfProperty testInjectionOfProperty;
	@Inject
	private PropertyProducer propertyProducer;

	@Test
	public void testInjection() {
		assertEquals("defaulted to this", testInjectionOfProperty.getTestDefault());
		assertEquals("This is from app properties file", testInjectionOfProperty.getTestFromFile());
		assertEquals("No Defaults!", testInjectionOfProperty.getTestWithNoDefaults());
		assertEquals(Set.of("1", "2", "3", "4", "5"), testInjectionOfProperty.getTestOfSets());
		assertIterableEquals(
				Arrays.asList("1", "2", "3", "4", "5", "5", "5", "5", "5", "5", "5", "5", "5", "5", "5", "5", "5", "5", "5", "5"),
				testInjectionOfProperty.getTestOfLists());
		assertEquals(new BigDecimal("42.1379"), testInjectionOfProperty.getTestOfBigDecimal());
		final Map<String, String> testMap = new LinkedHashMap<>();
		testMap.put("c", "1");
		testMap.put("b", "2");
		testMap.put("a", "3");
		testMap.put("aa", "4");
		assertIterableEquals(testMap.entrySet(), testInjectionOfProperty.getTestOfMap().entrySet());
	}

	@Test
	void testGetPropertyAsString() {
		final String result = propertyProducer.getProperty(String.class, "TestInjectionOfProperty.unlistedProperty");
		assertEquals("1", result);
	}

	@Test
	void testGetPropertyAsBoolean() {
		final Boolean result = propertyProducer.getProperty(Boolean.class, "TestInjectionOfProperty.unlistedProperty");
		assertTrue(result);
	}

	@Test
	void testGetPropertyAsInteger() {
		final Integer result = propertyProducer.getProperty(Integer.class, "TestInjectionOfProperty.unlistedProperty");
		assertEquals(1, result);
	}

	@Test
	void testGetPropertyAsLong() {
		final Long result = propertyProducer.getProperty(Long.class, "TestInjectionOfProperty.unlistedProperty");
		assertEquals(1L, result);
	}

	@Test
	void testGetPropertyAsBigDecimal() {
		final BigDecimal result = propertyProducer.getProperty(BigDecimal.class, "TestInjectionOfProperty.unlistedProperty");
		assertEquals(new BigDecimal("1"), result);
	}

	@Test
	void testGetPropertyUnsupportedType() {
		assertThrows(RuntimeException.class, () -> {
			propertyProducer.getProperty(Double.class, "TestInjectionOfProperty.unlistedProperty");
		});
	}

	@Test
	void testGetPropertyNull() {
		assertNull(propertyProducer.getProperty(Long.class, "non-existent-property"));
	}
}
