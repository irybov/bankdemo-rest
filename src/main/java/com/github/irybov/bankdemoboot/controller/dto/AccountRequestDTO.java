package com.github.irybov.bankdemoboot.controller.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRequestDTO {

	@JsonProperty("name")
	@NotBlank(message = "Name must not be empty")
	@Size(min=2, max=20, message = "Name should be 2-20 chars length")
	@Pattern(regexp = "^[A-Z][a-z]{1,19}", message = "Please input name like Xx")
	private String name;

	@JsonProperty("surname")
	@NotBlank(message = "Surname must not be empty")
	@Size(min=2, max=40, message = "Surname should be 2-40 chars length")
	@Pattern(regexp = "^[A-Z][a-z]{1,19}([-][A-Z][a-z]{1,19})?",
			message = "Please input surname like Xx or Xx-Xx")
	private String surname;

	@JsonProperty("phone")
	@NotBlank(message = "Phone number must not be empty")
	@Size(min=10, max=10, message = "Phone number should be 10 digits length")
	@Pattern(regexp = "^\\d{10}$", message = "Please input phone like XXXXXXXXXX")
	private String phone;

	@JsonProperty("birthday")
	@NotNull(message = "Please select your date of birth")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private String birthday;

	@JsonProperty("password")
	@NotBlank(message = "Password must not be empty")
	@Size(min=10, max=50, message = "Password should be 10-50 symbols length")
	private String password;
	
}
