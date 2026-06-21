package com.github.exabrial.cdi.common.config.api.util;

import com.github.exabrial.cdi.common.config.model.annotation.Config;

import jakarta.inject.Inject;

public class ConfigUtilsTestMule {
	@Inject
	@Config(defaultValue = "testFieldValue")
	private String testFieldName;
}
