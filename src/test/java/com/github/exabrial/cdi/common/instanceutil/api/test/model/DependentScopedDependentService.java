package com.github.exabrial.cdi.common.instanceutil.api.test.model;

import jakarta.enterprise.context.Dependent;

@Dependent
public class DependentScopedDependentService implements DependentService {
	@Override
	public String getValue() {
		return "dependent";
	}
}
