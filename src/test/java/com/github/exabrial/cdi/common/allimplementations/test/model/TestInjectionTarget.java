package com.github.exabrial.cdi.common.allimplementations.test.model;

import java.util.Set;

import com.github.exabrial.cdi.common.allimplementations.AllImplementations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;

@ApplicationScoped
public class TestInjectionTarget {
	@Inject
	@AllImplementations
	@Getter
	private Set<OutputService> outputServices;

	public void callAll(final String test) {
		outputServices.forEach((final OutputService os) -> os.out(test));
	}
}
