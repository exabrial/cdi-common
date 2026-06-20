package com.github.exabrial.cdi.common.allimplementations.test.model;

import jakarta.enterprise.inject.Specializes;

@Specializes
// @Alternative // Junit5 CDI extension doesn't seem to activate alternatives
public class AlternativeSysoutOutputService extends SysoutOutputService {

	@Override
	public void out(final String string) {
		System.out.println("alternative:" + string);
	}
}
