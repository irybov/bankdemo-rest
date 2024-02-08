package com.github.irybov.bankdemorest.controller.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

@ApiModel
@Value
public class BillRequest {

	@JsonProperty("phone")
	@NotBlank(message = "Phone number must not be blank")
	@Pattern(regexp = "^\\d{10}$", message = "Please input phone number like a row of 10 digits")
	String phone;
	
	@ApiModelProperty(value = "Currency's standard name", required = true, example = "USD")
	@JsonProperty("currency")
	@NotBlank(message = "Currency must not be blank")
	@Pattern(regexp = "^[A-Z]{3}$", message = "Currency code should be 3 capital characters length")
	String currency;
	
}
