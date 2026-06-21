package com.github.exabrial.cdi.common.config.cdi.producer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.exabrial.cdi.common.config.model.annotation.Config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Data;

@ApplicationScoped
@Data
public class TestInjectionOfProperty {
	@Inject
	@Config(value = "TestInjectionOfProperty.testDefault", defaultValue = "defaulted to this")
	private String testDefault;
	@Inject
	@Config(value = "TestInjectionOfProperty.testFromFile", defaultValue = "this would be wrong")
	private String testFromFile;
	@Inject
	@Config
	private String testWithNoDefaults;
	@Inject
	@Config
	private Set<String> testOfSets;
	@Inject
	@Config
	private List<String> testOfLists;
	@Inject
	@Config(defaultValue = "42.1379")
	private BigDecimal testOfBigDecimal;
	@Inject
	@Config(defaultValue = "c|1,b|2,a|3,aa|4")
	private Map<String, String> testOfMap;
}
