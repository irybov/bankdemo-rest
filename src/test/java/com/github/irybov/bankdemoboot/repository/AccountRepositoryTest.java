package com.github.irybov.bankdemoboot.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.github.irybov.bankdemoboot.entity.Account;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class AccountRepositoryTest {

	@Autowired
	private AccountRepository accountRepository;
	
	private static String search;
	private Account account;
	
	@BeforeAll
	static void prepare() {		
		search = new String("0000000000");		
	}
	
	@BeforeEach
	void set_up() {		
		account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin");
		accountRepository.save(account);
	}
	
	@Test
	void save_and_search_by_phone() {		
		Account fromDB = accountRepository.findByPhone(search);
		assertThat(fromDB).isEqualTo(account);
	}
	
    @Test
    void check_if_phone_exists() {   	
        String phone = "0000000000";
        String expected = accountRepository.checkPhone(phone);
        assertThat(expected).isEqualTo(phone);
    }
		
    @AfterEach
    void tear_down() {    	
    	accountRepository.deleteAll();
    	account = null;
    }
	
    @AfterAll
    static void clear() {    	
    	search = null;
    }
    
}
