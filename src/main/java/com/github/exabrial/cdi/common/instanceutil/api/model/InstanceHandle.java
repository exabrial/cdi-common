package com.github.exabrial.cdi.common.instanceutil.api.model;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.inject.Provider;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class InstanceHandle<T> implements Provider<T>, AutoCloseable {
	private final T reference;
	private final boolean resolvable;
	private final boolean dependentScope;
	private final CreationalContext<?> creationalContext;

	public InstanceHandle() {
		reference = null;
		resolvable = false;
		dependentScope = false;
		creationalContext = null;
	}

	public InstanceHandle(final T reference, final CreationalContext<?> creationalContext, final boolean dependentScope) {
		this.reference = reference;
		resolvable = true;
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
