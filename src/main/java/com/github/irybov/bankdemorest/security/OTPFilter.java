package com.github.irybov.bankdemorest.security;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class OTPFilter extends OncePerRequestFilter {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	private final JSONAuthenticationConverter authenticationConverter = new JSONAuthenticationConverter();
	
	private final RequestMatcher allowedPath = new AntPathRequestMatcher("/token");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
        if(!this.allowedPath.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }		
		if(!request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}
		
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if(header != null && header.startsWith("OTP ")) {
			
			String code = header.substring(4);
			code.trim();			
			if(code.isEmpty()) {
			    response.resetBuffer();
				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
			    response.getOutputStream().print("No code provided with header");
			    response.flushBuffer();
				return;
			}
			
			Pattern pattern = Pattern.compile("^\\d{4}$");
			if(!pattern.matcher(code).matches()) {
			    response.resetBuffer();
				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
			    response.getOutputStream().print("Invalid code provided");
			    response.flushBuffer();
				return;
			}
			
			try {
				UsernamePasswordAuthenticationToken authRequest = this.authenticationConverter.convert(request);
				Authentication authResult = this.authenticationManager.authenticate(authRequest);
			}
			catch (AuthenticationException ex) {
				
			}
			
		}		
//		filterChain.doFilter(request, response);
	    response.resetBuffer();
		response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "No OTP header present");
	    response.flushBuffer();
	}

}
