package com.github.exabrial.cdi.common.allimplementations.producer.test.model;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Vetoed;

@ApplicationScoped
@Vetoed
public class NotUsedOutputService implements OutputService {

	@Override
	public void out(final String string) {
		throw new RuntimeException("shouldn't be invoked!");
	}
}
