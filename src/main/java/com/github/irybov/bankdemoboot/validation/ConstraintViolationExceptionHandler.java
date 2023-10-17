package com.github.irybov.bankdemoboot.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.irybov.bankdemoboot.controller.BankController;

//@ControllerAdvice(basePackages = "com.github.irybov.bankdemoboot.controller")
@ControllerAdvice(basePackageClasses = BankController.class)
public class ConstraintViolationExceptionHandler {

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
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	ResponseEntity<List<String>> handleMethodArgumentNotValid(MethodArgumentNotValidException exc) {		
				
	    List<String> errors = new ArrayList<String>();
	    for(FieldError error : exc.getBindingResult().getFieldErrors()) {
	        errors.add(error.getDefaultMessage());
	    }
		return new ResponseEntity<List<String>>(errors, HttpStatus.BAD_REQUEST);
	}
	
}
