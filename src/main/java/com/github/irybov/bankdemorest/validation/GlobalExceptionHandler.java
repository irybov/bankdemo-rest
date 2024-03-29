package com.github.irybov.bankdemorest.validation;

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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import com.github.irybov.bankdemorest.controller.AuthController;
import com.github.irybov.bankdemorest.controller.BankController;
import com.github.irybov.bankdemorest.exception.PaymentException;

import lombok.extern.slf4j.Slf4j;

//@ControllerAdvice(basePackages = "com.github.irybov.bankdemorest.controller")
@ControllerAdvice(basePackageClasses = {BankController.class, AuthController.class})
@Slf4j
public class GlobalExceptionHandler {

//	@ExceptionHandler(ConstraintViolationException.class)
//	@ResponseBody
//	ValidationErrorResponse onConstraintValidationException(ConstraintViolationException exc) {
	ModelAndView handleConstraintValidation(ConstraintViolationException exc) {		
				
	    ValidationErrorResponse error = new ValidationErrorResponse();	    
/*	    for(ConstraintViolation<?> violation : exc.getConstraintViolations()) {
	    	error.getViolations().add(new Violation(violation.getMessage()));
	    }
	    return error;*/
		ModelAndView mav = new ModelAndView("error");
		List<Violation> violations = error.getViolations();
		mav.addObject("violations", violations);
		return mav;
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	List<String> handleMethodArgumentNotValid(MethodArgumentNotValidException exc) {		
				
	    List<String> errors = new ArrayList<String>();
	    for(FieldError error : exc.getBindingResult().getFieldErrors()) {
	        errors.add(error.getDefaultMessage());
	    }
		return errors;
	}
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({PaymentException.class, PersistenceException.class})
	@ResponseBody
	String handleRuntimeException(RuntimeException exc) {
		log.warn(exc.getMessage(), exc);
		return exc.getMessage();
	}
	
}
