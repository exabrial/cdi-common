package com.github.exabrial.cdi.common.allimplementations.producer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.exabrial.cdi.common.allimplementations.api.model.exception.NotASetException;
import com.github.exabrial.cdi.common.allimplementations.api.model.exception.NotAnInterfaceException;
import com.github.exabrial.cdi.common.allimplementations.api.model.exception.NotApplicationScopedException;
import com.github.exabrial.cdi.common.allimplementations.api.qualifier.AllImplementations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

@ApplicationScoped
class AllImplementationsProducer {
	private static final String JAVA_UTIL_SET = "java.util.Set<";
	@Inject
	private BeanManager beanManager;

	@SuppressWarnings("unchecked")
	@Produces
	@AllImplementations
	<T> Set<T> createSet(final InjectionPoint injectionPoint) throws Exception {
		final AnnotatedField<?> annotated = (AnnotatedField<?>) injectionPoint.getAnnotated();
		if (!annotated.getJavaMember().getType().equals(Set.class)) {
			throw new NotASetException(
					"@AllImplementations only supports injecting a java.util.Set. A target type was found that is not compatible:"
							+ annotated.getJavaMember().getType() + " @" + injectionPoint);
		} else {
			final String fullGenericTypeName = annotated.getJavaMember().getGenericType().getTypeName();
			final String genericTypeName = fullGenericTypeName.substring(JAVA_UTIL_SET.length(), fullGenericTypeName.length() - 1);
			final Class<T> genericType = (Class<T>) Class.forName(genericTypeName);
			if (genericType.isInterface()) {
				final Set<Bean<?>> availableBeans = beanManager.getBeans(genericType, Any.Literal.INSTANCE);
				final Set<T> proxies = new HashSet<>();
				for (final Bean<?> bean : availableBeans) {
					if (ApplicationScoped.class.equals(bean.getScope())) {
						final CreationalContext<T> creationalContext = (CreationalContext<T>) beanManager.createCreationalContext(bean);
						final T reference = (T) beanManager.getReference(bean, bean.getBeanClass(), creationalContext);
						proxies.add(reference);
					} else {
						throw new NotApplicationScopedException(
								"@AllImplementations only supports @ApplicationScoped beans. All implementations must be @ApplicationScoped. "
										+ "An implementation of " + genericTypeName + " was found that is not compatible:" + bean);
					}
				}
				return Collections.unmodifiableSet(proxies);
			} else {
				throw new NotAnInterfaceException(
						"@AllImplementations only supports injecting a Set<Interface>. The Set's generic type is not an interface:" + genericType
								+ " @" + injectionPoint);
			}
		}
	}
}
