package com.github.exabrial.cdi.common.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import org.apache.openwebbeans.junit5.Cdi;
import org.junit.jupiter.api.Test;

import com.github.exabrial.cdi.common.async.test.model.TestAsyncService;
import com.github.exabrial.cdi.common.async.test.model.TestTrigger;
import com.github.exabrial.cdi.common.async.test.model.TestTriggerEvent;
import com.github.exabrial.cdi.common.config.ConfigFeature;
import com.github.exabrial.cdi.common.instanceutil.InstanceUtilFeature;
import com.github.exabrial.cdi.common.slf4j.Slf4jFeature;

@Cdi(disableDiscovery = true,
		recursivePackages = { AsyncFeature.class, Slf4jFeature.class, ConfigFeature.class, InstanceUtilFeature.class })
class AsyncInitializableITest {
	@Inject
	private TestAsyncService testAsyncService;

	@Inject
	@TestTrigger
	private Event<TestTriggerEvent> triggerEvent;

	@Test
	void testBlocksUntilInitialized() {
		final long start = System.currentTimeMillis();
		assertFalse(testAsyncService.ready());
		triggerEvent.fireAsync(new TestTriggerEvent());
		final String result = testAsyncService.getValue();
		final long stop = System.currentTimeMillis();
		assertTrue(testAsyncService.ready());
		assertEquals("initialized", result);
		assertTrue(stop - start >= 1234);
		assertTrue(stop - start < testAsyncService.getInitializationTimeout());
	}
}
