package com.github.exabrial.cdi.common.async.api.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.exabrial.cdi.microspike.AnnotationInstanceProvider;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SkipInitializationGuard {
	SkipInitializationGuard LITERAL = AnnotationInstanceProvider.of(SkipInitializationGuard.class);
}
