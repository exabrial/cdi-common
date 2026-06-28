package com.github.exabrial.cdi.common.async.api;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import com.github.exabrial.cdi.common.async.api.model.event.AsyncInitializableReady;
import com.github.exabrial.cdi.common.async.model.exception.AsyncInitializationException;
import com.github.exabrial.cdi.common.config.api.model.annotation.Config;

import lombok.Getter;

@AwaitInitialized
public abstract class AsyncInitializable {
	@Getter
	private final CompletableFuture<Object> completedFuture = new CompletableFuture<>();
	private volatile Thread initializingThread;

	@Inject
	private Logger log;

	@Inject
	@Config(value = "cdi-common.async.default-initialization-timeout", defaultValue = "30000")
	private Long initializationTimeout;

	public abstract Class<?> getTriggerEventType();

	public abstract Annotation getTriggerEventQualifier();

	protected abstract Object initialize();

	protected void initializeComplete() {
	}

	public long getInitializationTimeout() {
		return initializationTimeout;
	}

	public boolean ready() {
		return completedFuture.isDone();
	}

	protected boolean isInitializingThread() {
		return initializingThread == Thread.currentThread();
	}

	protected void performInitialization(final BeanManager beanManager, final Class<? extends AsyncInitializable> concreteType) {
		log.info("performInitialization() starting concreteType:{}", concreteType.getName());
		initializingThread = Thread.currentThread();
		try {
			final Object result = initialize();
			completedFuture.complete(result);
			log.info("performInitialization() complete concreteType:{}", concreteType.getName());
			final AsyncInitializableReady readyEvent = new AsyncInitializableReady(concreteType);
			initializeComplete();
			beanManager.getEvent().select(AsyncInitializableReady.class).fire(readyEvent);
			beanManager.getEvent().select(AsyncInitializableReady.class).fireAsync(readyEvent);
		} catch (final Throwable throwable) {
			completedFuture.completeExceptionally(throwable);
			log.error("performInitialization() initialization failed concreteType:{}", concreteType.getName(), throwable);
		} finally {
			initializingThread = null;
		}
	}

	protected void awaitInitialization() {
		if (!isInitializingThread() && !ready()) {
			try {
				completedFuture.get(initializationTimeout, TimeUnit.MILLISECONDS);
			} catch (final TimeoutException timeoutException) {
				throw new AsyncInitializationException("awaitInitialization() timed out after timeout:" + initializationTimeout,
						timeoutException);
			} catch (final ExecutionException executionException) {
				throw new AsyncInitializationException("awaitInitialization() initialization failed", executionException.getCause());
			} catch (final InterruptedException interruptedException) {
				Thread.currentThread().interrupt();
				throw new AsyncInitializationException("awaitInitialization() interrupted", interruptedException);
			}
		}
	}
}
