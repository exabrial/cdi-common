package com.github.exabrial.cdi.common.instanceutil.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.openwebbeans.junit5.Cdi;
import org.junit.jupiter.api.Test;

import com.github.exabrial.cdi.common.instanceutil.api.model.InstanceHandle;
import com.github.exabrial.cdi.common.instanceutil.api.test.model.DependentService;
import com.github.exabrial.cdi.common.instanceutil.api.test.model.ResolvableService;
import com.github.exabrial.cdi.common.instanceutil.api.test.model.UnresolvableService;

import jakarta.inject.Inject;

@Cdi(disableDiscovery = true, recursivePackages = { InstanceUtilITest.class })
class InstanceUtilITest {
	@Inject
	private InstanceUtil instanceUtil;

	@Test
	void testLocateApplicationScopedBean() {
		try (final InstanceHandle<ResolvableService> handle = instanceUtil.locate(ResolvableService.class)) {
			assertTrue(handle.isResolvable());
			assertNotNull(handle.get());
			assertEquals("applicationScoped", handle.get().getValue());
		}
	}

	@Test
	void testLocateDependentScopedBean() {
		try (final InstanceHandle<DependentService> handle = instanceUtil.locate(DependentService.class)) {
			assertTrue(handle.isResolvable());
			assertNotNull(handle.get());
			assertEquals("dependent", handle.get().getValue());
		}
	}

	@Test
	void testLocateUnresolvableBean() {
		try (final InstanceHandle<UnresolvableService> handle = instanceUtil.locate(UnresolvableService.class)) {
			assertFalse(handle.isResolvable());
		}
	}
}
