package com.github.irybov.bankdemoboot.validation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.service.AccountService;

//@Component
public class AccountValidator implements Validator {

	private final AccountService accountService;
	public AccountValidator(@Qualifier("accountServiceAlias")AccountService accountService) {
		this.accountService = accountService;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return AccountRequestDTO.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		AccountRequestDTO account = (AccountRequestDTO) target;
		if(accountService.getAccountDTO(account.getPhone()) != null){
			errors.rejectValue("phone", "", "Validator in action! This number is already in use.");
		}
	}

}
