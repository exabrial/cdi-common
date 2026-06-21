package com.github.exabrial.cdi.common.allimplementations.producer.test.model;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SyserrOutputService implements OutputService {
	@Override
	public void out(final String string) {
		System.err.println(string);
	}
}
