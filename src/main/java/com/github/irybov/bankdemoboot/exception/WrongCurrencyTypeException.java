package com.github.irybov.bankdemoboot.exception;

public class WrongCurrencyTypeException extends RuntimeException{

	public WrongCurrencyTypeException(String message) {
		super(message);
	}
}
