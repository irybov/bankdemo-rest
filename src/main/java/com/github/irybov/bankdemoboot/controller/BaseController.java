package com.github.irybov.bankdemoboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

abstract class BaseController {
	
    @Autowired
    ApplicationContext context;

	Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	abstract String setServiceImpl(String impl);
	
}
