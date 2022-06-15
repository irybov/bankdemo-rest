package com.github.irybov.bankdemoboot.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.dao.AccountDAO;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.repository.AccountRepository;

@DataJpaTest
public class AccountServiceTest {

	@Autowired
	@Mock
	private AccountRepository accountRepository;
	@PersistenceContext
	private EntityManager entityManager;
	@InjectMocks
	private AccountDAO accountDAO;
	@InjectMocks
	private AccountService accountService;
	
    @BeforeEach
    void setUp() {
    	accountService = new AccountService();
		ReflectionTestUtils.setField(accountDAO, "entityManager", entityManager);
		ReflectionTestUtils.setField(accountService, "accountDAO", accountDAO);
		ReflectionTestUtils.setField(accountService, "accountRepository", accountRepository);		
    }
	
	@Test
	void checkIfAccountServiceWorskFine() {

		String search = new String("0000000000");
		AccountRequestDTO accountRequestDTO = new AccountRequestDTO();
		accountRequestDTO.setBirthday("2001-01-01");
		accountRequestDTO.setName("Admin");
		accountRequestDTO.setPassword("superadmin");
		accountRequestDTO.setPhone("0000000000");
		accountRequestDTO.setSurname("Adminov");
		try {
			accountService.saveAccount(accountRequestDTO);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		/*		ArgumentCaptor<Account> argumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(argumentCaptor.capture());

        Account captured = argumentCaptor.getValue();
        assertThat(captured).isEqualTo(account);*/
		
		Account account = accountService.getAccount(search);
        assertThat(accountService.verifyAccount(search, account.getPhone())).isTrue();
	}	
}
