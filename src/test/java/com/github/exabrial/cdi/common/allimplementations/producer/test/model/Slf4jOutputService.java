package com.github.exabrial.cdi.common.allimplementations.producer.test.model;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class Slf4jOutputService implements OutputService {
	@Inject
	private Logger log;

	@Override
	public void out(final String string) {
		log.info("out() string:{}", string);
	}
}
