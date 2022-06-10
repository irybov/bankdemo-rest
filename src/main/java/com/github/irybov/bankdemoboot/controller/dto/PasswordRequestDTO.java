package com.github.irybov.bankdemoboot.controller.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRequestDTO {

	@NotBlank
	private String oldPassword;
	
	@NotBlank
	@Size(min=10, max=50, message = "Password should be 10-50 symbols length")
	private String newPassword;
	
}
