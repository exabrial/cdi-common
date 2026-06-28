package com.github.exabrial.cdi.common.async.api;

import java.lang.annotation.Annotation;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import com.github.exabrial.cdi.common.async.api.model.event.AsyncInitializableReady;
import com.github.exabrial.cdi.common.async.model.exception.AsyncInitializationException;
import com.github.exabrial.cdi.common.config.api.model.annotation.Config;

import lombok.Getter;

@AwaitInitialized
public abstract class AsyncInitializable {
	private final CountDownLatch latch = new CountDownLatch(1);
	private volatile Thread initializingThread;
	@Getter
	private volatile Throwable initializationError;

	@Inject
	private Logger log;

	@Inject
	@Config(value = "cdi-common.async.default-initialization-timeout", defaultValue = "30000")
	private Long initializationTimeout;

	public abstract Class<?> getTriggerEventType();

	public abstract Annotation getTriggerEventQualifier();

	protected abstract void initialize();

	protected void initializeComplete() {
	}

	public long getInitializationTimeout() {
		return initializationTimeout;
	}

	public boolean ready() {
		return latch.getCount() == 0;
	}

	protected boolean isInitializingThread() {
		return initializingThread == Thread.currentThread();
	}

	protected void performInitialization(final BeanManager beanManager, final Class<? extends AsyncInitializable> concreteType) {
		log.info("performInitialization() starting concreteType:{}", concreteType.getName());
		initializingThread = Thread.currentThread();
		try {
			initialize();
		} catch (final Throwable throwable) {
			initializationError = throwable;
			log.error("performInitialization() initialization failed concreteType:{}", concreteType.getName(), throwable);
			return;
		} finally {
			initializingThread = null;
			latch.countDown();
		}
		log.info("performInitialization() complete concreteType:{}", concreteType.getName());
		final AsyncInitializableReady readyEvent = new AsyncInitializableReady(concreteType);
		beanManager.getEvent().select(AsyncInitializableReady.class).fire(readyEvent);
		beanManager.getEvent().select(AsyncInitializableReady.class).fireAsync(readyEvent);
		initializeComplete();
	}

	protected void awaitInitialization() {
		if (isInitializingThread() || ready()) {
			return;
		} else {
			try {
				final boolean completed = latch.await(initializationTimeout, TimeUnit.MILLISECONDS);
				if (!completed) {
					throw new AsyncInitializationException("awaitInitialization() timed out after timeout:" + getInitializationTimeout());
				}
			} catch (final InterruptedException interruptedException) {
				Thread.currentThread().interrupt();
				throw new AsyncInitializationException("awaitInitialization() interrupted", interruptedException);
			}
			if (initializationError != null) {
				throw new AsyncInitializationException("awaitInitialization() initialization failed", initializationError);
			}
		}
	}
}
