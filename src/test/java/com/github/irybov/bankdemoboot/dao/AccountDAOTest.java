package com.github.irybov.bankdemoboot.dao;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
//import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
//import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.entity.Account;

//@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class AccountDAOTest {
	
	@PersistenceContext
	private EntityManager entityManager;
	@InjectMocks
	private AccountDAO accountDAO;

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
		accountDAO = new AccountDAO();
		ReflectionTestUtils.setField(accountDAO, "entityManager", entityManager);
		account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true);
		accountDAO.saveAccount(account);
	}
	
	@Test
//	@Order(1)
	void check_if_phone_presents() {
        assertThat(accountDAO.getPhone(rightPN).equals(account.getPhone())).isTrue();
	}
	
    @Test
//	@Order(2)
	void check_if_phone_not_presents() {
		assertThatExceptionOfType(NoResultException.class)
		.isThrownBy(() -> {accountDAO.getPhone(wrongPN);});
//		assertThat(accountDAO.getPhone(newPhone)).isNull();
	}
	
	@Test
//	@Order(3)
	void save_and_search_by_phone() {
		Account fromDB = accountDAO.getAccount(rightPN);
		assertThat(fromDB).isEqualTo(account);
	}

	@Test
//	@Order(4)
	void update_and_compare() {
		Account fromDB = accountDAO.getAccount(rightPN);
		fromDB.setPhone(wrongPN);
		accountDAO.updateAccount(fromDB);
		Account updated = accountDAO.getAccount(wrongPN);		
		assertThat(fromDB).isEqualTo(updated);		
	}
    
    @Test
//	@Order(5)
    void check_if_no_clients_present() {
    	List<Account> clients = accountDAO.getAll();
    	assertThat(clients.isEmpty()).isTrue();
    }
	
    @AfterEach
    void tear_down() {
    	accountDAO = null;
    	account = null;
    }
    
    @AfterAll
    static void clear() {
    	rightPN = null;
    	wrongPN = null;
    }
    
}
