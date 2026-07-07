package com.github.exabrial.cdi.common.errorhandling.interceptor;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;

import org.slf4j.MDC;

public class XaUuidInterceptor {
	public static final String XA_UUID = "xa-uuid";
	public static final String OG_XA_UUID = "og-xa-uuid";

	@AroundTimeout
	@AroundInvoke
	public Object intercept(final InvocationContext ctx) throws Exception {
		boolean clearMdc = false;
		try {
			if (getMdc() == null) {
				startMdc();
				clearMdc = true;
			}
			return ctx.proceed();
		} finally {
			if (clearMdc) {
				clear();
			}
		}
	}

	public static final void clear() {
		MDC.remove(XA_UUID);
	}

	public static final void startMdc() {
		MDC.put(XA_UUID, randomUuid());
	}

	public static final String randomUuid() {
		final String xauuidCandidate = UUID.randomUUID().toString().substring(25);
		final char initialChar = randomNonNumeric();
		return initialChar + xauuidCandidate;
	}

	public static char randomNonNumeric() {
		final int index = ThreadLocalRandom.current().nextInt(40);
		if (index < 20) {
			return (char) ('G' + index);
		} else {
			return (char) ('g' + index - 20);
		}
	}

	public static final String getMdc() {
		return MDC.get(XA_UUID);
	}
}
