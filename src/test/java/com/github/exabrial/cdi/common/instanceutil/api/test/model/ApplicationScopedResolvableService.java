package com.github.exabrial.cdi.common.instanceutil.api.test.model;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationScopedResolvableService implements ResolvableService {
	@Override
	public String getValue() {
		return "applicationScoped";
	}
}
