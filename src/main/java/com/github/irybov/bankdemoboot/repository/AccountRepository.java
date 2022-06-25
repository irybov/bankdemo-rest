package com.github.irybov.bankdemoboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.irybov.bankdemoboot.entity.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
	
	Account findByPhone(String phone);
	
	@Query("SELECT phone FROM Account accounts WHERE phone=:check")
	String getPhone(String check);
}
