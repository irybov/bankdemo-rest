package com.github.irybov.bankdemoboot.validation;

import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

//import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

//@ControllerAdvice
public class ConstraintViolationExceptionHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseBody
//	ValidationErrorResponse onConstraintValidationException(ConstraintViolationException e) {
	ModelAndView onConstraintValidationException(ConstraintViolationException e) {		
				
	    ValidationErrorResponse error = new ValidationErrorResponse();
	    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
	      error.getViolations().add(new Violation(violation.getMessage()));
	    }		
		ModelAndView mav = new ModelAndView("errors");
		List<Violation> violations = error.getViolations();
		mav.addObject("violations", violations);
//	    return error;
		return mav;
	}
	
}
