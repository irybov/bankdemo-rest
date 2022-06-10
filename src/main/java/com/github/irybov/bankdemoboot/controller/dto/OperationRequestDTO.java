package com.github.irybov.bankdemoboot.controller.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationRequestDTO {

	@JsonProperty("amount")
	@NotNull
	private double amount;
	
	@JsonProperty("action")
	@NotNull
	private String action;
	
	@JsonProperty("currency")	
	@NotNull
	private String currency;
	
	@JsonProperty("sender")
	private int sender;
	
	@JsonProperty("recipient")	
	private int recipient;
	
}
