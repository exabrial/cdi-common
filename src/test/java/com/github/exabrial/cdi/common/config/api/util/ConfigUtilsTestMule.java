package com.github.exabrial.cdi.common.config.api.util;

import jakarta.inject.Inject;

import com.github.exabrial.cdi.common.config.api.model.annotation.Config;

public class ConfigUtilsTestMule {
	@Inject
	@Config(defaultValue = "testFieldValue")
	private String testFieldName;
}
