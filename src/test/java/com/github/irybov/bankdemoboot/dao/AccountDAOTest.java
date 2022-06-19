package com.github.irybov.bankdemoboot.dao;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.entity.Account;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class AccountDAOTest {
	
	@PersistenceContext
	private EntityManager entityManager;
	@InjectMocks
	private AccountDAO accountDAO;
	
	@BeforeEach
	void setUp() {
		accountDAO = new AccountDAO();
		ReflectionTestUtils.setField(accountDAO, "entityManager", entityManager);
	}
	
	@Test
	void check_save_and_search_for_account_phone() {	
	
		String search = new String("0000000000");
		Account account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin");
		accountDAO.saveAccount(account);		
        assertThat(accountDAO.checkPhone(search).equals(account.getPhone())).isTrue();
	}	

}
