package com.github.irybov.bankdemoboot;

import javax.validation.ConstraintViolationException;

//import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

//@ControllerAdvice
public class ConstraintViolationExceptionHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	ModelAndView onConstraintValidationException(ConstraintViolationException e) {
		ModelAndView mav = new ModelAndView("password");
		mav.addObject("message", e.getMessage());
		return mav;
	}
	
}
