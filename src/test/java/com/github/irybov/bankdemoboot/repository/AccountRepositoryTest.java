package com.github.irybov.bankdemoboot.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.github.irybov.bankdemoboot.entity.Account;

//@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class AccountRepositoryTest {

	@Autowired
	private AccountRepository accountRepository;
	
	private static String rightPN;
	private static String wrongPN;
	private Account account;
	
	@BeforeAll
	static void prepare() {
		rightPN = new String("0000000000");
		wrongPN = new String("0000000000");
	}
	
	@BeforeEach
	void setup() {
		account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true);
		accountRepository.save(account);
	}
	
	@Test
//	@Order(1)
	void check_if_phone_presents() {
        assertThat(accountRepository.getPhone(rightPN).equals(account.getPhone())).isTrue();
	}
	
    
    @Test
//	@Order(2)
	void check_if_phone_not_presents() {
		assertThat(accountRepository.getPhone(wrongPN)).isNull();
	}
	
	@Test
//	@Order(3)
	void save_and_search_by_phone() {
		Account fromDB = accountRepository.findByPhone(rightPN);
		assertThat(fromDB).isEqualTo(account);
		fromDB = accountRepository.getByPhone(rightPN);
		assertThat(fromDB).isEqualTo(account);
	}
	
    @Test
//	@Order(4)
    void update_and_compare() {    	
		Account fromDB = accountRepository.findByPhone(rightPN);
		fromDB.setPhone(wrongPN);
		accountRepository.save(fromDB);
		Account updated = accountRepository.getByPhone(wrongPN);
		assertThat(fromDB).isEqualTo(updated);
    }
    
    @Test
//	@Order(5)
    void check_if_no_clients_present() {
    	List<Account> clients = accountRepository.getAll();
    	assertThat(clients.isEmpty()).isTrue();
    }
    
    @AfterEach
    void tear_down() {
    	accountRepository.deleteAll();
    	account = null;
    }
	
    @AfterAll
    static void clear() {
    	rightPN = null;
    	wrongPN = null;
    }
    
}
