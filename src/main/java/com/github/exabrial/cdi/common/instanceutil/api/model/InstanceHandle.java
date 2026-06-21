package com.github.exabrial.cdi.common.instanceutil.api.model;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.inject.Provider;

public class InstanceHandle<T> implements Provider<T>, AutoCloseable {
	private final T reference;
	private final boolean resolvable;
	private final boolean dependentScope;
	private final CreationalContext<?> creationalContext;

	public InstanceHandle() {
		this.reference = null;
		this.resolvable = false;
		this.dependentScope = false;
		this.creationalContext = null;
	}

	public InstanceHandle(final T reference, final CreationalContext<?> creationalContext, final boolean dependentScope) {
		this.reference = reference;
		this.resolvable = true;
		this.dependentScope = dependentScope;
		this.creationalContext = creationalContext;
	}

	public boolean isResolvable() {
		return resolvable;
	}

	@Override
	public T get() {
		return reference;
	}

	@Override
	public void close() {
		if (resolvable && dependentScope && creationalContext != null) {
			creationalContext.release();
		}
	}
}
