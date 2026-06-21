package com.github.exabrial.cdi.common.config.util;

import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.github.exabrial.cdi.common.config.model.annotation.Config;

public final class ConfigUtils {

	private ConfigUtils() {
	}

	public static String readDefaultValue(final Class<?> clazz, final String fieldName) {
		try {
			final Field field = FieldUtils.getField(clazz, fieldName, true);
			if (field != null) {
				return field.getAnnotation(Config.class).defaultValue();
			} else {
				throw new NoSuchFieldException(fieldName);
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
