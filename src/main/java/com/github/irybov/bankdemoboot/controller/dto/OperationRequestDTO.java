package com.github.irybov.bankdemoboot.controller.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OperationRequestDTO {

	@JsonProperty("sender")
	@NotNull(message = "Sender must not be null")
	@Max(value = 999_999_999, message = "Sender's bill number should be less than 10 digits length")
	@Positive(message = "Sender's bill number should be positive number")
	private int sender;
	
	@JsonProperty("recipient")
	@NotNull(message = "Recepient must not be null")
	@Max(value = 999_999_999, message = "Recepient's bill number should be less than 10 digits length")
	@Positive(message = "Recepient's bill number should be positive number")
	private int recipient;
	
	@JsonProperty("currency")
	@NotBlank(message = "Currency must not be empty")
	@Pattern(regexp = "^[A-Z]{3}$", message = "Currency code should be 3 capital characters length")
	private String currency;
	
	@JsonProperty("amount")
	@NotNull(message = "Amount must not be null")
	@Positive(message = "Amount of money should be higher than zero")
	private double amount;
	
}
