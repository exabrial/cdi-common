package com.github.exabrial.cdi.common.allimplementations.producer.test.model;

import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class Slf4jOutputService implements OutputService {
	@Inject
	private Logger log;

	@Override
	public void out(final String string) {
		log.info("out() string:{}", string);
	}
}
