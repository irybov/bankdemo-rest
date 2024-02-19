package com.github.irybov.bankdemorest.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.irybov.bankdemorest.util.JWTUtility;

@Component
public class JWTFilter extends OncePerRequestFilter {
	
	private final JWTUtility jwtUtility;
	public JWTFilter(JWTUtility jwtUtility) {
		this.jwtUtility = jwtUtility;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
			FilterChain filterChain) throws ServletException, IOException {
		
		String username = null;
		List<String> roles = null;
		String header = request.getHeader("Authorization");
		
		if(header != null && header.startsWith("Bearer ")) {
			String jwt = header.substring(7);
			jwt.trim();
			
			if(jwt.isEmpty()) {
				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, 
						"No token provided with Bearer");
			}
			else {
				try {
					DecodedJWT decoded = jwtUtility.validate(jwt);
					username = decoded.getSubject();
					roles = decoded.getClaim("roles").asList(String.class);
					
			        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
			                    username,
			                    null,
			                    roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
//			            token.setAuthenticated(true);
			            SecurityContextHolder.getContext().setAuthentication(token);
			        }
				}
				catch (JWTVerificationException exc) {
					response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, 
							"Invalid token provided");
				}
			}
		}		
		filterChain.doFilter(request, response);
	}

}
