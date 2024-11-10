package com.github.irybov.service.dto;

import javax.validation.constraints.NotBlank;
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
public class PasswordRequest {

	@JsonProperty("oldPassword")
	@NotBlank(message = "Old password must not be blank")
	@Size(min=10, max=60, message = "Old password should be 10-60 symbols length")
	private String oldPassword;
	
	@JsonProperty("newPassword")
	@NotBlank(message = "New password must not be blank")
	@Size(min=10, max=60, message = "New password should be 10-60 symbols length")
	private String newPassword;
	
}
