package com.github.irybov.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class PrivacyException extends AccessDeniedException{

	public PrivacyException(String message) {
		super(message);
	}
}
