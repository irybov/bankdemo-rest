package com.github.irybov.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
	
	@JsonProperty("phone")
	@NotBlank(message = "Phone number must not be blank")
	@Pattern(regexp = "^\\d{10}$", message = "Please input phone number like a row of 10 digits")
	private String phone;

	@JsonProperty("password")
	@NotBlank(message = "Password must not be blank")
	@Size(min=10, max=60, message = "Password should be 10-60 symbols length")
	private String password;

}
