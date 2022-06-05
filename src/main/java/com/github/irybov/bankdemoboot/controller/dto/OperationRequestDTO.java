package com.github.irybov.bankdemoboot.controller.dto;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationRequestDTO {

	@NotNull
	private double amount;

	@NotNull
	private String action;
	
	@NotNull
	private String currency;
	
	private int sender;
		
	private int recipient;
	
}
