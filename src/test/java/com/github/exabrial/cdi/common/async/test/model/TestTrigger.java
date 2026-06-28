package com.github.exabrial.cdi.common.async.test.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

import com.github.exabrial.cdi.microspike.AnnotationInstanceProvider;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE })
public @interface TestTrigger {
	TestTrigger LITERAL = AnnotationInstanceProvider.of(TestTrigger.class);
}
