package com.github.irybov.bankdemoboot.controller.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRequestDTO {

	@JsonProperty("oldPassword")
	@NotBlank
	private String oldPassword;
	
	@JsonProperty("newPassword")
	@NotBlank
	@Size(min=10, max=50, message = "New password should be 10-50 symbols length")
	private String newPassword;
	
}
