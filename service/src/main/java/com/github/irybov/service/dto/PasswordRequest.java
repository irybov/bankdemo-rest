package com.github.irybov.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.Value;

@ApiModel
@Value
public class PasswordRequest {

	@JsonProperty("oldPassword")
	@NotBlank(message = "Old password must not be blank")
	@Size(min=10, max=60, message = "Old password should be 10-60 symbols length")
	String oldPassword;
	
	@JsonProperty("newPassword")
	@NotBlank(message = "New password must not be blank")
	@Size(min=10, max=60, message = "New password should be 10-60 symbols length")
	String newPassword;
	
}
