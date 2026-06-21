package com.github.exabrial.cdi.common.config.producer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import com.github.exabrial.cdi.common.config.api.model.annotation.Config;
import com.github.exabrial.cdi.common.config.api.model.annotation.FileContents;
import com.github.exabrial.cdi.common.instanceutil.api.InstanceUtil;
import com.github.exabrial.cdi.common.instanceutil.api.model.InstanceHandle;
import com.github.exabrial.junit5.injectmap.InjectExtension;
import com.github.exabrial.junit5.injectmap.InjectionSource;

@ExtendWith({ MockitoExtension.class, InjectExtension.class })
class PropertyProducerTest {
	@Test
	void testGetEnv() {
		final Map<String, String> env = new HashMap<>();
		env.put("JDBC_USERNAME", "username");
		env.put("app_core__module__apiToken", "token");
		final Map<String, String> parsedEnv = PropertyProducer.getEnv(env);

		assertTrue(parsedEnv.containsKey("JDBC_USERNAME"));
		assertTrue(parsedEnv.containsKey("JDBC-USERNAME"));
		assertTrue(parsedEnv.containsKey("app_core.module.apiToken"));
		assertTrue(parsedEnv.containsKey("app-core.module.apiToken"));
		assertTrue(parsedEnv.size() == 4);
	}

	@Nested
	class InjectionTest {
		@Mock
		private Logger log;
		@Mock
		private Annotated annotated;
		@Mock
		private InjectionPoint injectionPoint;
		@Mock
		private Config config;
		@Mock
		private FileContents fileContents;
		@Mock
		private InstanceUtil instanceUtil;
		@InjectMocks
		private PropertyProducer propertyProducer;
		@InjectionSource
		private Properties properties;

		@BeforeEach
		public void beforeEach() throws Exception {
			properties = new Properties();
			when(injectionPoint.getAnnotated()).thenReturn(annotated);
			when(annotated.getAnnotation(Config.class)).thenReturn(config);
			when(config.defaultValue()).thenReturn("/testFile.txt");
			when(config.value()).thenReturn("testKey");
			when(instanceUtil.locate(any())).thenReturn(new InstanceHandle<>());
		}

		@Test
		void testInjectFile_as_byteArray() {
			when(annotated.getAnnotation(FileContents.class)).thenReturn(fileContents);
			final byte[] fileContents = propertyProducer.injectFile(injectionPoint);
			assertArrayEquals(new byte[] { 'A' }, fileContents);
		}

		@Test
		void testInjectFile_as_byteArray_fromBase64() {
			when(annotated.getAnnotation(FileContents.class)).thenReturn(null);
			when(config.defaultValue()).thenReturn("QQ==");
			final byte[] fileContents = propertyProducer.injectFile(injectionPoint);
			assertArrayEquals(new byte[] { 'A' }, fileContents);
		}

		@Test
		void testInjectString_fromFile() {
			when(annotated.getAnnotation(FileContents.class)).thenReturn(fileContents);
			final String fileContents = propertyProducer.injectString(injectionPoint);
			assertEquals("A", fileContents);
		}

		@Test
		void testInjectString() {
			when(annotated.getAnnotation(FileContents.class)).thenReturn(null);
			final String stringValue = propertyProducer.injectString(injectionPoint);
			assertEquals("/testFile.txt", stringValue);
		}
	}
}
