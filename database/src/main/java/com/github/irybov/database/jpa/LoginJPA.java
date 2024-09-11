package com.github.irybov.database.jpa;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.irybov.database.entity.Login;
import com.github.irybov.database.entity.LoginFailure;

public interface LoginJPA extends JpaRepository<Login, Long> {

	List<LoginFailure> findByAccountIdAndCreatedAtIsAfter(int id, OffsetDateTime createdAt);
	
}
