package com.github.irybov.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;

@Api(description = "Abstrcat controller, parent of all controllers")
//@CrossOrigin(origins="http://"+"${server.address}"+":"+"${server.port}", allowCredentials="true")
abstract class BaseController {
	
    @Autowired
    ApplicationContext context;
    
	@Autowired
	ObjectMapper mapper;

	Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
}
