package com.github.exabrial.cdi.common.config.api;

import jakarta.enterprise.inject.spi.InjectionPoint;

public interface PropertyProducerOverrider {
	String override(final String configPropertyName, final InjectionPoint injectionPoint, final String originalValue);
}
