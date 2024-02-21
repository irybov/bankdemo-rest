package com.github.irybov.bankdemorest.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Duration;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.irybov.bankdemorest.entity.Account;
import com.github.irybov.bankdemorest.security.AccountDetails;
import com.github.irybov.bankdemorest.security.Role;

class JWTUtilityTest {
	
	private JWTUtility jwtUtility;
	
    @BeforeEach
    void set_up() {
    	jwtUtility = new JWTUtility();
    	ReflectionTestUtils.setField(jwtUtility, "secret", "secret");
    	ReflectionTestUtils.setField(jwtUtility, "lifetime", Duration.ofMinutes(5));
    }

	@Test
	void test() {
		
		Account account = new Account("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01),
				 BCrypt.hashpw("superadmin", BCrypt.gensalt(4)), true);
		account.addRole(Role.ADMIN);
		UserDetails details = new AccountDetails(account);
		
		String token = jwtUtility.generate(details);
		assertThat(jwtUtility.validate(token)).isInstanceOf(DecodedJWT.class);
		assertThatThrownBy(() -> jwtUtility.validate(token + "fake"))
			.isInstanceOf(JWTVerificationException.class);
	}

    @AfterEach
    void tear_down() {
    	jwtUtility = null;
    }
    	
}
