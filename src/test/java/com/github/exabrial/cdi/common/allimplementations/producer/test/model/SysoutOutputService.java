package com.github.exabrial.cdi.common.allimplementations.producer.test.model;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SysoutOutputService implements OutputService {
	@Override
	public void out(final String string) {
		System.out.println(string);
	}
}
