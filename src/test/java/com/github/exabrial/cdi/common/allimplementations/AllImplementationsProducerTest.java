package com.github.exabrial.cdi.common.allimplementations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.exabrial.cdi.common.allimplementations.model.exception.NotASetException;
import com.github.exabrial.cdi.common.allimplementations.model.exception.NotAnInterfaceException;
import com.github.exabrial.cdi.common.allimplementations.model.exception.NotApplicationScopedException;
import com.github.exabrial.cdi.common.allimplementations.test.model.OutputService;
import com.github.exabrial.cdi.common.allimplementations.test.model.SysoutOutputService;
import com.github.exabrial.junit5.injectmap.InjectExtension;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;

@SuppressWarnings("unused")
@ExtendWith({ MockitoExtension.class, InjectExtension.class })
class AllImplementationsProducerTest {

	@InjectMocks
	private AllImplementationsProducer producer;
	@Mock
	private BeanManager beanManager;

	private List<OutputService> notASet;
	private Set<SysoutOutputService> notAnInterface;
	private Set<OutputService> aSet;

	@Test
	void testCreateSet_notASet() throws Exception {
		final InjectionPoint injectionPoint = mock(InjectionPoint.class);
		final AnnotatedField<?> annotated = mock(AnnotatedField.class);
		final Field field = FieldUtils.getDeclaredField(getClass(), "notASet", true);

		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getJavaMember()).thenReturn(field);

		assertThrows(NotASetException.class, () -> {
			producer.createSet(injectionPoint);
		});
	}

	@Test
	void testCreateSet_notAnInterface() throws Exception {
		final InjectionPoint injectionPoint = mock(InjectionPoint.class);
		final AnnotatedField<?> annotated = mock(AnnotatedField.class);
		final Field field = FieldUtils.getDeclaredField(getClass(), "notAnInterface", true);

		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getJavaMember()).thenReturn(field);

		assertThrows(NotAnInterfaceException.class, () -> {
			producer.createSet(injectionPoint);
		});
	}

	@Test
	void testCreateSet_notApplicationScoped() throws Exception {
		final InjectionPoint injectionPoint = mock(InjectionPoint.class);
		final AnnotatedField<?> annotated = mock(AnnotatedField.class);
		final Field field = FieldUtils.getDeclaredField(getClass(), "aSet", true);
		final Bean<?> nonApplicationScopedBean = mock(Bean.class);
		final Set<Bean<?>> beanSet = Set.of(nonApplicationScopedBean);

		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getJavaMember()).thenReturn(field);
		when(beanManager.getBeans(OutputService.class, Any.Literal.INSTANCE)).thenReturn(beanSet);

		assertThrows(NotApplicationScopedException.class, () -> {
			producer.createSet(injectionPoint);
		});
	}
}
