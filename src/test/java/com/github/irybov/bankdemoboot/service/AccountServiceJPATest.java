package com.github.irybov.bankdemoboot.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
//import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.repository.AccountRepository;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@ExtendWith(MockitoExtension.class)
class AccountServiceJPATest {

	@Mock
	private AccountRepository accountRepository;
	@InjectMocks
	private AccountServiceJPA accountService;
	
	private static String search;
	
	@BeforeAll
	static void prepare() {
		search = new String("0000000000");
	}
	
    @BeforeEach
    void setUp() {
    	MockitoAnnotations.openMocks(this);
    	accountService = new AccountServiceJPA();
		ReflectionTestUtils.setField(accountService, "accountRepository", accountRepository);		
    }
	
    @Test
    void can_get_account() {
    	accountService.getAccount(search);
        verify(accountRepository).findByPhone(search);
    }
    
	@Test
	void save_and_check_identity() {

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
		
		ArgumentCaptor<Account> argumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(argumentCaptor.capture());

		Account account = new Account
			("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin");
		given(accountRepository.getPhone(search)).willReturn("0000000000");
		assertThat(accountService.verifyAccount(search, account.getPhone())).isTrue();
        verify(accountRepository).getPhone(search);
	}
    
    @AfterEach
    void tear_down() {
    	accountService = null;
    }
    
    @AfterAll
    static void clear() {    	
    	search = null;
    }
    
}
