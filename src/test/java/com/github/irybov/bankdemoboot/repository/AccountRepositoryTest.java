package com.github.irybov.bankdemoboot.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
//import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
//import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.security.Role;

//@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class AccountRepositoryTest {

	@Autowired
	private AccountRepository accountRepository;
	
	private String oldPN;
	private String newPN;
	private Account account;
	
	@BeforeAll
	void prepare() {
		oldPN = new String("0000000000");
		newPN = new String("9999999999");
		account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true);
		account.addRole(Role.ADMIN);
		accountRepository.save(account);
	}
	
	@Test
//	@Order(1)
	void check_that_phone_present() {
        assertThat(accountRepository.getPhone(oldPN)).isEqualTo(account.getPhone());
	}
	
    @Test
//	@Order(2)
	void check_that_phone_not_present() {
		assertThat(accountRepository.getPhone(newPN)).isNull();
	}
	
	@Test
//	@Order(3)
	void search_by_phone() {
		Account fromDB = accountRepository.findByPhone(oldPN);
		assertThat(fromDB).isEqualTo(account);
		fromDB = accountRepository.getByPhone(oldPN);
		assertThat(fromDB).isEqualTo(account);
	}
	
    @Test
//	@Order(4)
    void update_and_compare() {    	
		Account fromDB = accountRepository.findByPhone(oldPN);
		fromDB.setPhone(newPN);
		accountRepository.save(fromDB);
		Account updated = accountRepository.getByPhone(newPN);
		assertThat(fromDB).isEqualTo(updated);
    }
    
    @Test
//	@Order(5)
    void check_that_no_clients_present() {
    	List<Account> clients = accountRepository.getAll();
    	assertThat(clients).isNotNull();
    	assertThat(clients.isEmpty()).isTrue();
    }
	
    @AfterAll
    void clear() {
    	oldPN = null;
    	newPN = null;
    	accountRepository.delete(account);
    	account = null;
    }
    
}
