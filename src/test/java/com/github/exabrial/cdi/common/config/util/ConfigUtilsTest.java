package com.github.exabrial.cdi.common.config.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfigUtilsTest {

	@Test
	void test() {
		final String expected = "testFieldValue";
		final String actual = ConfigUtils.readDefaultValue(ConfigUtilsTestMule.class, "testFieldName");
		assertEquals(expected, actual);
	}
}
