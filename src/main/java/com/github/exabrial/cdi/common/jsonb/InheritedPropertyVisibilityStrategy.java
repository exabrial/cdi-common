package com.github.exabrial.cdi.common.jsonb;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.config.PropertyVisibilityStrategy;

/**
 * JSON-B visibility strategy that honors @JsonbTransient and @JsonbProperty, treats public fields as visible, and additionally
 * walks a getter's declaring-class interfaces so an annotation on an interface method (e.g. @JsonbTransient on a contract) is
 * respected even when the concrete override is unannotated. Results are cached per Field/Method.
 *
 * @author jonathan.fisher
 */
public class InheritedPropertyVisibilityStrategy implements PropertyVisibilityStrategy, Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<Field, Boolean> fieldCache = new ConcurrentHashMap<>();
	private final Map<Method, Boolean> methodCache = new ConcurrentHashMap<>();

	@Override
	public boolean isVisible(final Field field) {
		return fieldCache.computeIfAbsent(field, InheritedPropertyVisibilityStrategy::isVisibleViaField);
	}

	@Override
	public boolean isVisible(final Method method) {
		return methodCache.computeIfAbsent(method, InheritedPropertyVisibilityStrategy::isVisibleViaMethod);
	}

	static final boolean isVisibleViaField(final Field field) {
		final boolean isVisible;
		if (field.isAnnotationPresent(JsonbTransient.class)) {
			isVisible = false;
		} else if (field.isAnnotationPresent(JsonbProperty.class)) {
			isVisible = true;
		} else {
			isVisible = Modifier.isPublic(field.getModifiers());
		}
		return isVisible;
	}

	static final boolean isVisibleViaMethod(final Method method) {
		final boolean isVisible;
		if (method.isAnnotationPresent(JsonbTransient.class)) {
			isVisible = false;
		} else if (method.isAnnotationPresent(JsonbProperty.class)) {
			isVisible = true;
		} else {
			isVisible = ClassUtils.getAllInterfaces(method.getDeclaringClass()).stream()
					.map((final Class<?> iface) -> MethodUtils.getMatchingAccessibleMethod(iface, method.getName(), method.getParameterTypes()))
					.filter(Objects::nonNull).findFirst().map((final Method m) -> !m.isAnnotationPresent(JsonbTransient.class)).orElse(true);
		}
		return isVisible;
	}
}
