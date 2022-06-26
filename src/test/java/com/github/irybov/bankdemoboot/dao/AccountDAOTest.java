package com.github.irybov.bankdemoboot.dao;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

	private static String search;
	private Account account;
	
	@BeforeAll
	static void prepare() {		
		search = new String("0000000000");
	}
	
	@BeforeEach
	void set_up() {
		
		accountDAO = new AccountDAO();
		ReflectionTestUtils.setField(accountDAO, "entityManager", entityManager);
		account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin");
	}
	
	@Test
//	@Order(1)
	void save_and_search_phone() {	

		accountDAO.saveAccount(account);		
        assertThat(accountDAO.getPhone(search).equals(account.getPhone())).isTrue();
	}	

	@Test
//	@Order(2)
	void update_and_compare() {

		accountDAO.saveAccount(account);
		Account fromDB = accountDAO.getAccount(search);
		fromDB.setPhone("9999999999");
		accountDAO.updateAccount(fromDB);
		Account updated = accountDAO.getAccount("9999999999");		
		assertThat(fromDB).isEqualTo(updated);		
	}
	
    @AfterEach
    void tear_down() {    	
    	accountDAO = null;
    	account = null;
    }
    
}
