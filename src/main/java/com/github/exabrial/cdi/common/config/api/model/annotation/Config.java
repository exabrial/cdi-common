package com.github.exabrial.cdi.common.config.api.model.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD })
@Documented
public @interface Config {
	@Nonbinding
	String value() default "";

	@Nonbinding
	String defaultValue() default "";
}
