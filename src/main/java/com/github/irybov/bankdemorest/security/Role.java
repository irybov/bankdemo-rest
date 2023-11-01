package com.github.irybov.bankdemorest.security;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority{
	ADMIN, CLIENT, REMOTE;

	@Override
	public String getAuthority() {
		return "ROLE_" + name();
	}
}
