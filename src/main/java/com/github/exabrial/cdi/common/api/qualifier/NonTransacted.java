package com.github.exabrial.cdi.common.api.qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

import com.github.exabrial.cdi.microspike.AnnotationInstanceProvider;

/**
 * Qualifies a resource that operates outside any JTA transaction.
 *
 * @author jonathan.fisher
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE })
public @interface NonTransacted {
	NonTransacted LITERAL = AnnotationInstanceProvider.of(NonTransacted.class);
}
