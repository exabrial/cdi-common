package com.github.exabrial.cdi.common.errorhandling.interceptor;

import java.util.UUID;

import org.slf4j.MDC;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;

public class XaUuidInterceptor {
	public static final String XA_UUID = "xa-uuid";
	public static final String OG_XA_UUID = "og-xa-uuid";

	@AroundTimeout
	@AroundInvoke
	Object intercept(final InvocationContext ctx) throws Exception {
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

	public static final String getMdc() {
		return MDC.get(XA_UUID);
	}

	public static final void startMdc() {
		MDC.put(XA_UUID, randomUuid());
	}

	public static final String randomUuid() {
		return UUID.randomUUID().toString().substring(24);
	}

	public static final void clear() {
		MDC.remove(XA_UUID);
	}
}
