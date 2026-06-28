package com.github.exabrial.cdi.common.async.model.exception;

import com.github.exabrial.cdi.common.model.exception.CdiCommonException;

public class AsyncInitializationException extends CdiCommonException {
	private static final long serialVersionUID = 1L;

	public AsyncInitializationException(final String message) {
		super(message);
	}

	public AsyncInitializationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
