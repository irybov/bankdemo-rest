package com.github.irybov.bankdemoboot.exception;

public class BillNotFoundException extends RuntimeException{

	public BillNotFoundException(String message) {
		super(message);
	}
}
