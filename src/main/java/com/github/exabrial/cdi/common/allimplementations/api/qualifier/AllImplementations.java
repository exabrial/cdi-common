package com.github.exabrial.cdi.common.allimplementations.api.qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

import com.github.exabrial.cdi.microspike.AnnotationInstanceProvider;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Qualifier
public @interface AllImplementations {
	AllImplementations LITERAL = AnnotationInstanceProvider.of(AllImplementations.class);
}
