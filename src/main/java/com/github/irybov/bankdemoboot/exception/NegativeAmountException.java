package com.github.irybov.bankdemoboot.exception;

public class NegativeAmountException extends RuntimeException{

	public NegativeAmountException(String message) {
		super(message);
	}
}
