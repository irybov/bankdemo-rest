package com.github.irybov.bankdemorest.jpa;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.irybov.bankdemorest.entity.Login;
import com.github.irybov.bankdemorest.entity.LoginFailure;

public interface LoginJPA extends JpaRepository<Login, Long> {

	List<LoginFailure> findByAccountIdAndCreatedAtIsAfter(int id, OffsetDateTime createdAt);
	
}
