package com.github.exabrial.cdi.common.allimplementations.model.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(inherited = true, rollback = true)
public class AllImplementationsException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AllImplementationsException(final String message) {
		super(message);
	}
}
