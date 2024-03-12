package com.github.irybov.bankdemorest.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.irybov.bankdemorest.entity.Account;

public interface AccountJPA extends JpaRepository<Account, Integer> {
	
	Optional<Account> findByPhone(String phone);
	
	@Query("SELECT phone FROM Account WHERE phone=:check")
	Optional<String> getPhone(String check);
	
	@Query("SELECT DISTINCT a FROM Account a JOIN a.roles r WHERE r = 'CLIENT' ORDER BY a.id ASC")
	List<Account> getAll();
	
	@Query("SELECT a FROM Account a LEFT JOIN FETCH a.bills WHERE a.phone=:phone")
	Optional<Account> getWithBills(String phone);
	
}
