package com.github.irybov.bankdemorest.util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
@PropertySource("classpath:jwt.properties")
public class JWTUtility {
	
    @Value("${secret}")
    private String secret;

    @Value("${lifetime}")
    private Duration lifetime;
    
    public String generate(UserDetails details) {

//        Map<String, Object> claims = new HashMap<>();
        List<String> roles = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
//        claims.put("roles", roles);

    	Date expiration = Date.from(ZonedDateTime.now().plusMinutes(lifetime.toMinutes()).toInstant());
    	
        return JWT.create()
                .withSubject(details.getUsername())
                .withClaim("roles", roles)
                .withIssuer("bankdemo")
                .withIssuedAt(new Date())
                .withExpiresAt(expiration)
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT validate(String token) {

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("bankdemo")
                .build();
		return verifier.verify(token);
    }
    
}
