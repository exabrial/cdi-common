package com.github.exabrial.cdi.common.config.producer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.github.exabrial.cdi.common.config.api.PropertyProducerOverrider;
import com.github.exabrial.cdi.common.config.api.model.annotation.Config;
import com.github.exabrial.cdi.common.config.api.model.annotation.FileContents;
import com.github.exabrial.cdi.common.instanceutil.api.InstanceUtil;
import com.github.exabrial.cdi.common.instanceutil.api.model.InstanceHandle;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("WMI_WRONG_MAP_ITERATOR")
@ApplicationScoped
public class PropertyProducer {
	@Inject
	private Logger log;
	@Inject
	private InstanceUtil instanceUtil;

	private Map<String, String> properties;

	@PostConstruct
	void postConstruct() {
		log.info("postConstruct() loading properties...");
		final Properties propsToLoad = loadPropertiesFile("cdi-common.maven.properties");
		propsToLoad.putAll(loadPropertiesFile("cdi-common.externalized-strings.properties"));
		propsToLoad.putAll(loadPropertiesFile("cdi-common.config.properties"));
		propsToLoad.putAll(loadPropertiesFile("cdi-common.local.properties"));
		propsToLoad.putAll(loadPropertiesFile("cdi-common.test.properties"));
		propsToLoad.putAll(System.getProperties());
		propsToLoad.putAll(getEnv());
		properties = Collections.unmodifiableMap(toHashMap(propsToLoad));
		if (log.isTraceEnabled()) {
			final StringBuilder sb = new StringBuilder();
			propsToLoad.keySet().stream().sorted().forEach((final Object key) -> {
				sb.append(key);
				sb.append("=");
				sb.append(properties.get(key));
				sb.append("\n");
			});
			log.trace("loadProperties() properties:\n{}", sb);
		} else {
			log.debug("loadProperties() properties.keySet():{}", properties.keySet());
		}
	}

	Properties loadPropertiesFile(final String fileName) {
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
			final Properties props = new Properties();
			props.load(inputStream);
			return props;
		} catch (final IOException | NullPointerException e) {
			log.trace("loadPropertiesFile() exception when loading:{}", fileName, e);
			return new Properties();
		}
	}

	@Produces
	@Config
	BigDecimal injectBigDecimal(final InjectionPoint injectionPoint) {
		final String property = getProperty(getPropertyName(injectionPoint), injectionPoint);
		if (property != null) {
			return new BigDecimal(property);
		} else {
			return null;
		}
	}

	@Produces
	@Config
	@SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
	Boolean injectBoolean(final InjectionPoint injectionPoint) {
		final String property = getProperty(getPropertyName(injectionPoint), injectionPoint);
		if (property != null) {
			return Boolean.parseBoolean(property);
		} else {
			return null;
		}
	}

	@Produces
	@Config
	byte[] injectFile(final InjectionPoint injectionPoint) {
		final FileContents fileContents = injectionPoint.getAnnotated().getAnnotation(FileContents.class);
		final String property = getProperty(getPropertyName(injectionPoint), injectionPoint);
		if (fileContents != null) {
			final InputStream inputStream = getFileContentsInputStream(fileContents, property);
			return readBytes(inputStream);
		} else {
			final String base64 = getProperty(property, injectionPoint);
			return Base64.getDecoder().decode(base64);
		}
	}

	@Produces
	@Config
	Integer injectInteger(final InjectionPoint injectionPoint) {
		final String property = getProperty(getPropertyName(injectionPoint), injectionPoint);
		if (property != null) {
			return Integer.parseInt(property);
		} else {
			return null;
		}
	}

	@Produces
	@Config
	Long injectLong(final InjectionPoint injectionPoint) {
		final String property = getProperty(getPropertyName(injectionPoint), injectionPoint);
		if (property != null) {
			return Long.parseLong(property);
		} else {
			return null;
		}
	}

	@Produces
	@Config
	String injectString(final InjectionPoint injectionPoint) {
		final FileContents fileContents = injectionPoint.getAnnotated().getAnnotation(FileContents.class);
		final String property = getProperty(getPropertyName(injectionPoint), injectionPoint);
		if (fileContents != null) {
			final InputStream inputStream = getFileContentsInputStream(fileContents, property);
			return convertStreamToString(inputStream);
		} else {
			return property;
		}
	}

	@Produces
	@Config
	List<String> injectStringList(final InjectionPoint injectionPoint) {
		final String config = getProperty(getPropertyName(injectionPoint), injectionPoint);
		if (config != null) {
			return List.of(config.split(","));
		} else {
			return List.of();
		}
	}

	@Produces
	@Config
	Set<String> injectStringSet(final InjectionPoint injectionPoint) {
		final String config = getProperty(getPropertyName(injectionPoint), injectionPoint);
		if (config != null) {
			return Set.copyOf(Arrays.asList(config.split(",")));
		} else {
			return Collections.emptySet();
		}
	}

	@Produces
	@Config
	Map<String, String> injectStringToStringMap(final InjectionPoint injectionPoint) {
		final String config = getProperty(getPropertyName(injectionPoint), injectionPoint);
		if (config != null) {
			final String[] keyAndValueList = config.split(",");
			final Map<String, String> configMap = new LinkedHashMap<>();
			for (final String keyAndValue : keyAndValueList) {
				final String[] keyAndValueArray = keyAndValue.split(Pattern.quote("|"));
				configMap.put(keyAndValueArray[0], keyAndValueArray[1]);
			}
			return Collections.unmodifiableMap(configMap);
		} else {
			return Map.of();
		}
	}

	/**
	 * Note this method is incapable of reading injection point specific defaultValue's.
	 */
	<K> K getProperty(final Class<K> targetClass, final String propertyName) {
		if (targetClass == String.class || targetClass == Boolean.class || targetClass == Integer.class || targetClass == Long.class
				|| targetClass == BigDecimal.class) {
			final String value = properties.get(propertyName);
			final K returnValue;
			if (value == null) {
				returnValue = null;
			} else {
				final Object converted;
				if (targetClass == String.class) {
					converted = value;
				} else if (targetClass == Boolean.class) {
					converted = toBoolean(value);
				} else if (targetClass == Integer.class) {
					converted = Integer.valueOf(value);
				} else if (targetClass == Long.class) {
					converted = Long.valueOf(value);
				} else {
					converted = new BigDecimal(value);
				}
				returnValue = targetClass.cast(converted);
			}
			return returnValue;
		} else {
			throw new RuntimeException(
					String.format("Target type:%s not supported. Must be one of:{ String, Boolean, Integer, Long, BigDecimal}", targetClass));
		}
	}

	String getProperty(final String configPropertyName, final InjectionPoint injectionPoint) {
		final Config config = injectionPoint.getAnnotated().getAnnotation(Config.class);
		final String defaultPropertyValue = StringUtils.trimToNull(config.defaultValue());
		String propertyValue = properties.getOrDefault(configPropertyName, defaultPropertyValue);
		try (InstanceHandle<PropertyProducerOverrider> handle = instanceUtil.locate(PropertyProducerOverrider.class)) {
			if (handle.isResolvable()) {
				try {
					propertyValue = handle.get().override(configPropertyName, injectionPoint, propertyValue);
				} catch (final Exception exception) {
					log.trace("getProperty() Ignoring exception. configPropertyName:{} injectionPoint:{} propertyValue:{}", configPropertyName,
							injectionPoint, propertyValue, exception);
				}
			}
		}
		return propertyValue;
	}

	String getPropertyName(final InjectionPoint injectionPoint) {
		final Config config = injectionPoint.getAnnotated().getAnnotation(Config.class);
		String configPropertyName = StringUtils.trimToNull(config.value());
		if (configPropertyName == null) {
			final String className = injectionPoint.getMember().getDeclaringClass().getSimpleName();
			final String fieldName = injectionPoint.getMember().getName();
			configPropertyName = className + "." + fieldName;
		}
		return configPropertyName;
	}

	static final String convertStreamToString(final InputStream inputStream) {
		try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A")) {
			return scanner.hasNext() ? scanner.next() : null;
		}
	}

	static final Map<String, String> getEnv() {
		return getEnv(System.getenv());
	}

	static final Map<String, String> getEnv(final Map<String, String> env) {
		final Map<String, String> parsedEnv = new HashMap<>();
		for (final String key : env.keySet()) {
			parsedEnv.put(key.replace("__", "."), env.get(key));
			parsedEnv.put(key.replace("__", ".").replace("_", "-"), env.get(key));
		}
		return parsedEnv;
	}

	static final InputStream getFileContentsInputStream(final FileContents fileContents, final String property) {
		final InputStream inputStream;
		if (!fileContents.isAbsoluteFileSystemPath()) {
			inputStream = PropertyProducer.class.getResourceAsStream(property);
		} else {
			try {
				inputStream = new FileInputStream(property);
			} catch (final FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return inputStream;
	}

	static final byte[] readBytes(final InputStream inputStream) {
		final byte[] buffer;
		if (inputStream == null) {
			buffer = null;
		} else {
			try {
				buffer = inputStream.readAllBytes();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
		return buffer;
	}

	/**
	 * Mirrors the truthy/falsey tokens of commons-beanutils' default BooleanConverter (true|yes|y|on|1 / false|no|n|off|0), so "1"
	 * resolves to Boolean.TRUE.
	 */
	static final Boolean toBoolean(final String value) {
		return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
			case "true", "yes", "y", "on", "1" -> Boolean.TRUE;
			case "false", "no", "n", "off", "0" -> Boolean.FALSE;
			default -> throw new IllegalArgumentException("Cannot convert value to Boolean:" + value);
		};
	}

	@SuppressWarnings("unused")
	static final HashMap<String, String> toHashMap(final Properties properties) {
		return properties.entrySet().stream()
				.collect(Collectors.toMap((final Entry<Object, Object> entry) -> String.valueOf(entry.getKey()),
						(final Entry<Object, Object> entry) -> String.valueOf(entry.getValue()), (prev, next) -> next, HashMap::new));
	}
}
