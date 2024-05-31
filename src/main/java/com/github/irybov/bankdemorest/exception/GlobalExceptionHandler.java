package com.github.irybov.bankdemorest.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import com.github.irybov.bankdemorest.controller.AuthController;
import com.github.irybov.bankdemorest.controller.BankController;
import com.github.irybov.bankdemorest.validation.ValidationErrorResponse;
import com.github.irybov.bankdemorest.validation.Violation;

import lombok.extern.slf4j.Slf4j;

//@ControllerAdvice(basePackages = "com.github.irybov.bankdemorest.controller")
@RestControllerAdvice(basePackageClasses = {BankController.class, AuthController.class})
@Slf4j
public class GlobalExceptionHandler {

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
//	@ResponseBody
//	ValidationErrorResponse onConstraintValidationException(ConstraintViolationException exc) {
	List<Violation> handleConstraintValidation(ConstraintViolationException exc) {		
				
	    ValidationErrorResponse error = new ValidationErrorResponse();	    
	    for(ConstraintViolation<?> violation : exc.getConstraintViolations()) {
	    	error.getViolations().add(new Violation(violation.getMessage()));
	    }
		List<Violation> violations = error.getViolations();
		return violations;
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
//	@ResponseBody
	List<String> handleMethodArgumentNotValid(MethodArgumentNotValidException exc) {		
				
	    List<String> errors = new ArrayList<String>();
	    for(FieldError error : exc.getBindingResult().getFieldErrors()) {
	        errors.add(error.getDefaultMessage());
	    }
		return errors;
	}
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = {PaymentException.class, PersistenceException.class})
//	@ResponseBody
	String handleRuntimeException(RuntimeException exc) {
		log.warn(exc.getMessage(), exc);
		return exc.getMessage();
	}
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(value = {BadCredentialsException.class, DisabledException.class})
//	@ResponseBody
	String handleAuthenticationException(AuthenticationException exc) {
		log.warn(exc.getMessage(), exc);
		return exc.getMessage();
	}
	
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(UsernameNotFoundException.class)
//	@ResponseBody
	String handleUsernameNotFoundException(UsernameNotFoundException exc) {
		log.warn(exc.getMessage(), exc);
		return exc.getMessage();
	}
	
}
