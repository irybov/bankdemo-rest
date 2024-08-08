package com.github.irybov.bankdemorest.security;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.irybov.bankdemorest.controller.dto.LoginRequest;

public class JSONAuthenticationConverter implements AuthenticationConverter {
	
	@Autowired
	private ObjectMapper mapper;

	@Override
	public UsernamePasswordAuthenticationToken convert(HttpServletRequest request) {
		
		String header = request.getHeader(HttpHeaders.CONTENT_TYPE);
		if(header != null && header.equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)) {

			String body = null;
			try {
				body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			}
			catch(IOException exc) {
				exc.printStackTrace();
			}
			
			LoginRequest loginRequest = null;
			try {
				loginRequest = mapper.readValue(body, LoginRequest.class);
			}
			catch(JsonProcessingException exc) {
				throw new BadCredentialsException("Invalid authentication json");
			}
			
			return UsernamePasswordAuthenticationToken.unauthenticated
					(loginRequest.getPhone(), loginRequest.getPassword());
		}
		
		return null;
	}
	private LoginRequest jsonToDTO(@Valid @RequestBody LoginRequest loginRequest) {
		return loginRequest;		
	}
	
}
