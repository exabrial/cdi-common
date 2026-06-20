package com.github.exabrial.cdi.common.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class Slf4jLogProducer {
	private Logger log = LoggerFactory.getLogger(Slf4jLogProducer.class);

	@Produces
	@Dependent
	public Logger createLogger(final InjectionPoint injectionPoint) {
		Class<?> declaringClass = injectionPoint.getMember().getDeclaringClass();
		log.trace("createLogger() declaringClass:{}", declaringClass);
		return LoggerFactory.getLogger(declaringClass);
	}
}
