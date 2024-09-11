package com.github.irybov.service.dto;

import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@Getter
@Setter
public class AccountRequest {

	@JsonProperty("name")
	@NotBlank(message = "Name must not be blank")
	@Size(min=2, max=20, message = "Name should be 2-20 chars length")
	@Pattern(regexp = "^[A-Z][a-z]{1,19}", message = "Please input name like Xx")
	private String name;

	@JsonProperty("surname")
	@NotBlank(message = "Surname must not be blank")
	@Size(min=2, max=40, message = "Surname should be 2-40 chars length")
	@Pattern(regexp = "^[A-Z][a-z]{1,19}([-][A-Z][a-z]{1,19})?",
			message = "Please input surname like Xx or Xx-Xx")
	private String surname;

	@JsonProperty("phone")
	@NotBlank(message = "Phone number must not be blank")
	@Pattern(regexp = "^\\d{10}$", message = "Please input phone number like a row of 10 digits")
	private String phone;
	
	@JsonProperty("email")
	@NotBlank(message = "Email address must not be blank")
	@Email(message = "Email address is not valid")
	@Size(min=10, max=60, message = "Email address should be 10-60 symbols length")
	private String email;

	@JsonProperty("birthday")
	@NotNull(message = "Please select your date of birth")
	@Past(message = "Birthday can't be future time")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate birthday;

	@JsonProperty("password")
	@NotBlank(message = "Password must not be blank")
	@Size(min=10, max=60, message = "Password should be 10-60 symbols length")
	private String password;
	
}
