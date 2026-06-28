package com.github.exabrial.cdi.common.async.api;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@AwaitInitialized
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
class AwaitInitializedInterceptor {
	@AroundInvoke
	Object intercept(final InvocationContext invocationContext) throws Exception {
		final Object target = invocationContext.getTarget();
		if (target instanceof final AsyncInitializable asyncInitializable
				&& invocationContext.getMethod().getDeclaringClass() != AsyncInitializable.class) {
			asyncInitializable.awaitInitialization();
		}
		return invocationContext.proceed();
	}
}
