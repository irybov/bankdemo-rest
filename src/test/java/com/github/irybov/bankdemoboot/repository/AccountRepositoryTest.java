package com.github.irybov.bankdemoboot.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.github.irybov.bankdemoboot.entity.Account;

@DataJpaTest
public class AccountRepositoryTest {

	@Autowired
	private AccountRepository accountRepository;
	
    @AfterEach
    void tearDown() {
    	accountRepository.deleteAll();
    }
	
	@Test
	void checkIfAccountRepositoryWorskFine() {
		
		String search = new String("0000000000");
		Account account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin");
		accountRepository.save(account);
		
		Account fromDB = accountRepository.findByPhone(search);
		assertThat(fromDB).isEqualTo(account);
	}
	
}
