package com.github.exabrial.cdi.common.instanceutil;

import java.util.Set;

import com.github.exabrial.cdi.common.instanceutil.model.InstanceHandle;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

@ApplicationScoped
public class InstanceUtil {
	@Inject
	private BeanManager beanManager;

	@SuppressWarnings("unchecked")
	public <T> InstanceHandle<T> locate(final Class<T> type) {
		final Set<Bean<?>> beans = beanManager.getBeans(type);
		final InstanceHandle<T> handle;
		if (beans.isEmpty()) {
			handle = new InstanceHandle<>();
		} else {
			final Bean<?> bean = beanManager.resolve(beans);
			final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
			final T reference = (T) beanManager.getReference(bean, type, creationalContext);
			final boolean dependentScope = Dependent.class.equals(bean.getScope());
			handle = new InstanceHandle<>(reference, creationalContext, dependentScope);
		}
		return handle;
	}
}
