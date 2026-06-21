package com.github.exabrial.cdi.common.slf4j.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class Slf4jLogProducer {
	private final Logger log = LoggerFactory.getLogger(Slf4jLogProducer.class);

	@Produces
	@Dependent
	Logger createLogger(final InjectionPoint injectionPoint) {
		final Class<?> declaringClass = injectionPoint.getMember().getDeclaringClass();
		log.trace("createLogger() declaringClass:{}", declaringClass);
		return LoggerFactory.getLogger(declaringClass);
	}
}
