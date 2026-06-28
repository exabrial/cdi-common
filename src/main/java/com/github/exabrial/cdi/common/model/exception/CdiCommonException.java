package com.github.exabrial.cdi.common.model.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(inherited = true, rollback = true)
public class CdiCommonException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CdiCommonException() {
		super();
	}

	public CdiCommonException(final String message) {
		super(message);
	}

	public CdiCommonException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CdiCommonException(final Throwable cause) {
		super(cause);
	}
}
