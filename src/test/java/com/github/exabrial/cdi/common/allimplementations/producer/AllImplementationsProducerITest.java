package com.github.exabrial.cdi.common.allimplementations.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.apache.openwebbeans.junit5.Cdi;
import org.junit.jupiter.api.Test;

import com.github.exabrial.cdi.common.allimplementations.producer.test.model.AlternativeSysoutOutputService;
import com.github.exabrial.cdi.common.allimplementations.producer.test.model.NotUsedOutputService;
import com.github.exabrial.cdi.common.allimplementations.producer.test.model.OutputService;
import com.github.exabrial.cdi.common.allimplementations.producer.test.model.Slf4jOutputService;
import com.github.exabrial.cdi.common.allimplementations.producer.test.model.SyserrOutputService;
import com.github.exabrial.cdi.common.allimplementations.producer.test.model.SysoutOutputService;
import com.github.exabrial.cdi.common.allimplementations.producer.test.model.TestInjectionTarget;
import com.github.exabrial.cdi.common.slf4j.Slf4jLogProducer;

import jakarta.inject.Inject;

@Cdi(disableDiscovery = true, classes = { Slf4jLogProducer.class }, recursivePackages = { AllImplementationsProducerITest.class })
class AllImplementationsProducerITest {
	@Inject
	private TestInjectionTarget target;

	@Test
	void testAllImplementationsInjection() {
		final Set<OutputService> outputServices = target.getOutputServices();
		target.callAll("Ignore this test value to output");

		assertEquals(3, outputServices.size());
		// Next two should count the same thing; the Alternative is marked @Specialized
		assertEquals(1, countType(outputServices, SysoutOutputService.class));
		assertEquals(1, countType(outputServices, AlternativeSysoutOutputService.class));

		assertEquals(1, countType(outputServices, Slf4jOutputService.class));
		assertEquals(1, countType(outputServices, SyserrOutputService.class));
		assertEquals(0, countType(outputServices, NotUsedOutputService.class));
	}

	static final long countType(final Set<OutputService> outputServices, final Class<? extends OutputService> cls) {
		return outputServices.stream().filter((final OutputService os) -> cls.isAssignableFrom(os.getClass())).count();
	}
}
