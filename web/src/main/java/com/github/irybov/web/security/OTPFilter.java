package com.github.irybov.web.security;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.irybov.service.dto.LoginRequest;

//@Component
public class OTPFilter extends OncePerRequestFilter {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	private final AuthenticationEntryPoint authenticationEntryPoint = new OTPAuthenticationEntryPoint();
	private final RequestMatcher allowedPath = new AntPathRequestMatcher("/token");
	@Autowired
	private Cache<String, LoginRequest> cache;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
        if(!this.allowedPath.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }		
		if(!request.getMethod().equals("POST")) {
//			response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}
		
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if(header != null && header.startsWith("OTP ")) {
			
			String code = header.substring(4);
			code.trim();			
			if(code == null || code.isEmpty()) {
				this.authenticationEntryPoint.commence(request, response, 
						new InsufficientAuthenticationException("No code provided with header"));
//			    response.resetBuffer();
//				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
//			    response.getOutputStream().print("No code provided with header");
//			    response.flushBuffer();
				return;
			}			
			Pattern pattern = Pattern.compile("^\\d{4}$");
			if(!pattern.matcher(code).matches()) {
				this.authenticationEntryPoint.commence(request, response, 
						new BadCredentialsException("Invalid format of code"));
//			    response.resetBuffer();
//				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
//			    response.getOutputStream().print("Invalid format of code");
//			    response.flushBuffer();
				return;
			}
			
			LoginRequest loginRequest = cache.getIfPresent(code);
			if(loginRequest == null) {
				this.authenticationEntryPoint.commence(request, response, 
						new AuthenticationCredentialsNotFoundException("Wrong or expired code"));
//			    response.resetBuffer();
//				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
//			    response.getOutputStream().print("Wrong or expired code");
//			    response.flushBuffer();
				return;
			}
			try {
				Authentication authentication = this.authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken
						(loginRequest.getPhone(), loginRequest.getPassword()));				
				SecurityContextHolder.getContext().setAuthentication(authentication);
//				cache.invalidate(code);
				filterChain.doFilter(request, response);
			}
			catch (AuthenticationException exc) {
				SecurityContextHolder.clearContext();
				this.authenticationEntryPoint.commence(request, response, exc);
			}
		}
//		filterChain.doFilter(request, response);
		else {
			this.authenticationEntryPoint.commence(request, response, 
					new InsufficientAuthenticationException("No OTP header present"));
//		    response.resetBuffer();
//			response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
//			response.getOutputStream().print("No OTP header present");
//		    response.flushBuffer();
		}
	}

}
