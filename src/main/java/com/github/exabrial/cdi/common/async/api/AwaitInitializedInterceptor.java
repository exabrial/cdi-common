package com.github.exabrial.cdi.common.async.api;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import com.github.exabrial.cdi.common.async.api.model.annotation.SkipInitializationGuard;

@Interceptor
@AwaitInitialized
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
class AwaitInitializedInterceptor {
	@AroundInvoke
	Object intercept(final InvocationContext invocationContext) throws Exception {
		final AsyncInitializable target = (AsyncInitializable) invocationContext.getTarget();
		if (invocationContext.getMethod().getDeclaringClass() != AsyncInitializable.class
				&& !invocationContext.getMethod().isAnnotationPresent(SkipInitializationGuard.class)) {
			target.awaitInitialization();
		}
		return invocationContext.proceed();
	}
}
