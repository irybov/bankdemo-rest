package com.github.irybov.bankdemoboot.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequest;
import com.github.irybov.bankdemoboot.service.AccountService;

@Component
public class AccountValidator implements org.springframework.validation.Validator {
	
    @Autowired
    private Validator validator;
    
	@Autowired
	@Qualifier("accountServiceAlias")
	private AccountService accountService;

/*	private final AccountService accountService;
	public AccountValidator(@Qualifier("accountServiceAlias")AccountService accountService) {
		this.accountService = accountService;
	}*/

	@Override
	public boolean supports(Class<?> clazz) {
		return AccountRequest.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
	    Set<ConstraintViolation<Object>> validates = validator.validate(target);
	    for (ConstraintViolation<Object> constraintViolation : validates) {
	        String propertyPath = constraintViolation.getPropertyPath().toString();
	        String message = constraintViolation.getMessage();
	        errors.rejectValue(propertyPath, "", message);
	    }
		
		AccountRequest account = (AccountRequest) target;
		if(accountService.getPhone(account.getPhone()) == null) return;
		errors.rejectValue("phone", "", "Validator in action!");
	}

}
