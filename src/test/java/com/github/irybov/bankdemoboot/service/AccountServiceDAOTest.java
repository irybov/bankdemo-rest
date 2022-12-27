package com.github.irybov.bankdemoboot.service;

//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.parallel.Execution;
//import org.junit.jupiter.api.parallel.ExecutionMode;
//import org.junit.jupiter.api.TestInstance;
//import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.dao.AccountDAO;
import com.github.irybov.bankdemoboot.entity.Account;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@ExtendWith(MockitoExtension.class)
//@Execution(ExecutionMode.CONCURRENT)
class AccountServiceDAOTest {

	@Mock
	private AccountDAO accountDAO;
	@InjectMocks
	private AccountServiceDAO accountService;
	
	private AutoCloseable autoClosable;
	
	private static String phone;
	private static Account account;
	
	@BeforeAll
	static void prepare() {
		phone = new String("0000000000");
		account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true);
	}
	
    @BeforeEach
    void set_up() {
    	autoClosable = MockitoAnnotations.openMocks(this);
    	accountService = new AccountServiceDAO();
		ReflectionTestUtils.setField(accountService, "accountDAO", accountDAO);		
    }
	
    @Test
    void can_get_account() {
//    	when(accountDAO.getAccount(phone)).thenReturn(account);
		given(accountDAO.getAccount(phone)).willReturn(account);
    	try {
			accountService.getAccount(phone);
		}
    	catch (Exception exc) {
			exc.printStackTrace();
		}
        verify(accountDAO).getAccount(phone);
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
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}		
		ArgumentCaptor<Account> argumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountDAO).saveAccount(argumentCaptor.capture());

//    	when(accountRepository.findByPhone(phone)).thenReturn(account);        
		given(accountDAO.getAccount(phone)).willReturn(account);
		try {
//			assertThat(accountService.verifyAccount(phone, account.getPhone())).isTrue();
			then(accountService.verifyAccount(phone, account.getPhone())).isTrue();
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
//      verify(accountDAO).getAccount(phone);
        org.mockito.BDDMockito.then(accountDAO).should().getAccount(phone);
	}
    
    @AfterEach
    void tear_down() throws Exception {
    	autoClosable.close();
    	accountService = null;
    }
    
    @AfterAll
    static void clear() {    	
    	phone = null;
    	account = null;
    }
    
}
