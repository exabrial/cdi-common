package com.github.exabrial.cdi.common.async.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.EventContext;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncInitializableExtension implements Extension {
	private static final Logger log = LoggerFactory.getLogger(AsyncInitializableExtension.class);

	private final List<Class<? extends AsyncInitializable>> discoveredTypes = new ArrayList<>();

	<T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> processAnnotatedType) {
		final Class<T> javaClass = processAnnotatedType.getAnnotatedType().getJavaClass();
		if (AsyncInitializable.class.isAssignableFrom(javaClass) && !Modifier.isAbstract(javaClass.getModifiers())) {
			@SuppressWarnings("unchecked")
			final Class<? extends AsyncInitializable> concreteType = (Class<? extends AsyncInitializable>) javaClass;
			discoveredTypes.add(concreteType);
			log.info("processAnnotatedType() discovered AsyncInitializable concreteType:{}", concreteType.getName());
		}
	}

	void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
		log.info("afterBeanDiscovery() registering synthetic observers count:{}", discoveredTypes.size());
		for (final Class<? extends AsyncInitializable> concreteType : discoveredTypes) {
			registerObserver(afterBeanDiscovery, beanManager, concreteType);
		}
	}

	@SuppressWarnings("unused")
	private void registerObserver(final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager,
			final Class<? extends AsyncInitializable> concreteType) {
		try {
			final AsyncInitializable throwaway = concreteType.getDeclaredConstructor().newInstance();
			final Class<?> triggerEventType = throwaway.getTriggerEventType();
			final Annotation triggerQualifier = throwaway.getTriggerEventQualifier();
			afterBeanDiscovery.addObserverMethod().observedType(triggerEventType).addQualifier(triggerQualifier).async(true)
					.notifyWith((final EventContext<Object> eventContext) -> onTriggerEvent(beanManager, concreteType));
			log.debug("registerObserver() registered async observer concreteType:{} triggerEventType:{} triggerQualifier:{}",
					concreteType.getName(), triggerEventType.getName(), triggerQualifier);
		} catch (final ReflectiveOperationException reflectiveOperationException) {
			throw new IllegalStateException("registerObserver() failed to instantiate throwaway for concreteType:" + concreteType.getName(),
					reflectiveOperationException);
		}
	}

	private static void onTriggerEvent(final BeanManager beanManager, final Class<? extends AsyncInitializable> concreteType) {
		log.debug("onTriggerEvent() resolving bean concreteType:{}", concreteType.getName());
		final Set<Bean<?>> beans = beanManager.getBeans(concreteType);
		final Bean<?> bean = beanManager.resolve(beans);
		if (bean == null) {
			log.error("onTriggerEvent() no bean resolved for concreteType:{}", concreteType.getName());
		} else {
			final AsyncInitializable instance = (AsyncInitializable) beanManager.getReference(bean, concreteType,
					beanManager.createCreationalContext(bean));
			instance.performInitialization(beanManager, concreteType);
		}
	}
}
