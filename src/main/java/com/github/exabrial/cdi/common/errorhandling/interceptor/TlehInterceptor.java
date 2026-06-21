package com.github.exabrial.cdi.common.errorhandling.interceptor;

import java.util.Set;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

public class TlehInterceptor {
	@Inject
	private Logger log;

	@AroundTimeout
	@AroundInvoke
	Object intercept(final InvocationContext ctx) throws Exception {
		try {
			return ctx.proceed();
		} catch (final Exception e) {
			final Throwable rootCause = ExceptionUtils.getRootCause(e);
			if (rootCause instanceof final ConstraintViolationException constraintViolationException) {
				final String sb = logLineForConstraintException(constraintViolationException);
				log.error("intercept() ConstraintViolations:{}, parameters:{}", sb, ctx.getParameters(), e);
			} else if (rootCause instanceof final WebApplicationException webApplicationException
					&& webApplicationException.getResponse().getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
				log.warn("intercept() caught exception", e);
			} else {
				log.error("intercept() caught exception", e);
			}
			throw e;
		}
	}

	public static final String logLineForConstraintException(final ConstraintViolationException rootCause) {
		final Set<ConstraintViolation<?>> violations = rootCause.getConstraintViolations();
		final StringBuilder sb = new StringBuilder();
		for (final ConstraintViolation<?> violation : violations) {
			sb.append("\n");
			sb.append("propertyPath='");
			sb.append(violation.getPropertyPath());
			sb.append("' message='");
			sb.append(violation.getMessage());
			sb.append("' rootBean='");
			sb.append(violation.getRootBean().getClass().getSimpleName());
			sb.append("'");
		}
		return sb.toString();
	}
}
