package com.github.exabrial.cdi.common.async.test.model;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.ApplicationScoped;

import com.github.exabrial.cdi.common.async.api.AsyncInitializable;
import com.github.exabrial.cdi.common.async.api.model.annotation.SkipInitializationGuard;

@ApplicationScoped
public class TestAsyncService extends AsyncInitializable {
	private String value;

	@Override
	public Class<?> getTriggerEventType() {
		return TestTriggerEvent.class;
	}

	@Override
	public Annotation getTriggerEventQualifier() {
		return TestTrigger.LITERAL;
	}

	@Override
	protected void initialize() {
		try {
			Thread.sleep(534);
		} catch (final InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
		}
		value = "initialized";
	}

	public String getValue() {
		return value;
	}

	@SkipInitializationGuard
	public String getStaticInfo() {
		return "available";
	}
}
