package com.github.irybov.bankdemoboot.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.PersistenceException;

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
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.Role;
import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.exception.RegistrationException;
import com.github.irybov.bankdemoboot.repository.AccountRepository;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@ExtendWith(MockitoExtension.class)
//@Execution(ExecutionMode.CONCURRENT)
class AccountServiceJPATest {

	@Mock
	private BillService billService;
	@Spy
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Mock
	AccountServiceJPA accountServiceJPA;
	@Mock
	private AccountRepository accountRepository;
	@InjectMocks
	private AccountServiceJPA accountService;
	
	private AutoCloseable autoClosable;
	
	private static String phone;
	private static Account adminEntity;
	
	@BeforeAll
	static void prepare() {
		phone = new String("0000000000");
		adminEntity = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01),
						BCrypt.hashpw("superadmin", BCrypt.gensalt(4)), true);
	}
	
    @BeforeEach
    void set_up() {
    	autoClosable = MockitoAnnotations.openMocks(this);
    	accountService = new AccountServiceJPA();
		ReflectionTestUtils.setField(accountService, "accountRepository", accountRepository);
		ReflectionTestUtils.setField(accountService, "accountService", accountServiceJPA);
		ReflectionTestUtils.setField(accountService, "bCryptPasswordEncoder", bCryptPasswordEncoder);
		ReflectionTestUtils.setField(accountService, "billService", billService);
    }
    
    @Test
    void add_new_bills_and_get_them() {
    	
    	String currency = "SEA";
    	Bill billOne = new Bill(currency, true, adminEntity);
    	Bill billTwo = new Bill(currency, true, adminEntity);
    	
    	given(accountServiceJPA.getAccount(phone)).willReturn(adminEntity);
    	doAnswer(new Answer<Account>() {
			@Override
			public Account answer(InvocationOnMock invocation) throws Throwable {
				adminEntity.addBill(billOne);
				adminEntity.addBill(billTwo);
				return adminEntity;
			}}).when(accountServiceJPA).updateAccount(adminEntity);
    	
    	try {
    		org.assertj.core.api.BDDAssertions.then(accountService.addBill(phone, currency))
    											.isExactlyInstanceOf(BillResponseDTO.class);
		}
    	catch (Exception exc) {
			exc.printStackTrace();
		}
    	verify(accountServiceJPA).getAccount(phone);

    	given(accountRepository.getById(anyInt())).willReturn(adminEntity);
    	then(accountService.getBills(anyInt())).hasSize(3);
    	verify(accountRepository).getById(anyInt());
    }
    
    @Test
    void can_get_phone_if_presents() {
        
        given(accountRepository.getPhone(phone)).willReturn(phone);
        org.assertj.core.api.BDDAssertions.then(accountService.getPhone(phone))
        									.isExactlyInstanceOf(String.class)
        									.hasSize(phone.length());
        verify(accountRepository).getPhone(phone);
    }
	
    @Test
    void can_get_single_account() {

		given(accountRepository.findByPhone(phone)).willReturn(adminEntity);
    	try {
    		org.assertj.core.api.BDDAssertions.then(accountService.getAccount(phone))
    											 .isExactlyInstanceOf(Account.class);
		}
    	catch (Exception exc) {
			exc.printStackTrace();
		}
        verify(accountRepository).findByPhone(phone);
    }
    
    @Test
    void can_get_accounts_list() {
    	
		Account vixenEntity = new Account
		("Marica", "Hase", "1111111111", LocalDate.of(1981, Month.SEPTEMBER, 26), "supervixen", true);
		vixenEntity.addRole(Role.CLIENT);
		vixenEntity.setId(3);
		Account blondeEntity = new Account
		("Sarah", "Vandella", "2222222222", LocalDate.of(1983, Month.DECEMBER, 02), "bustyblonde", true);
		blondeEntity.addRole(Role.CLIENT);
		blondeEntity.setId(2);
		Account gingerEntity = new Account
		("Lily", "Cade", "3333333333", LocalDate.of(1995, Month.JANUARY, 25), "gingerchick", true);
		gingerEntity.addRole(Role.CLIENT);
		gingerEntity.addRole(Role.ADMIN);
		gingerEntity.setId(1);
    	
    	List<Account> clients = new ArrayList<>();
    	clients.add(vixenEntity);
    	clients.add(blondeEntity);
    	clients.add(gingerEntity);
    	
    	given(accountRepository.getAll()).willReturn(clients);
    	then(accountService.getAll()).hasSameSizeAs(clients)
    								 .isSortedAccordingTo((a1, a2) -> a1.getId() - a2.getId())
    								 .containsAll(new ArrayList<AccountResponseDTO>());
    	org.mockito.BDDMockito.then(accountRepository).should().getAll();
    }
    
    @Test
    void can_change_status() {
    	
    	given(accountRepository.findById(anyInt())).willReturn(Optional.of(adminEntity));
    	then(accountService.changeStatus(anyInt())).isFalse();
    	verify(accountRepository).findById(anyInt());
    }
    
    @Test
    void can_change_password() {
    	
    	String password = "nightmare";
    	given(accountServiceJPA.getAccount(phone)).willReturn(adminEntity);
    	doAnswer(new Answer<Account>() {
			@Override
			public Account answer(InvocationOnMock invocation) throws Throwable {
				adminEntity.setPassword(password);
				return adminEntity;
			}}).when(accountServiceJPA).updateAccount(adminEntity);
    	
    	try {
			accountService.changePassword(phone, password);
		}
    	catch (Exception exc) {
			exc.printStackTrace();
		}
    	verify(accountServiceJPA).getAccount(phone);
    	org.assertj.core.api.BDDAssertions.then(adminEntity.getPassword()).isEqualTo(password);
    }
    
    @Test
    void password_comparison() {
    	
    	given(accountServiceJPA.getAccount(phone)).willReturn(adminEntity);
//    	given(bCryptPasswordEncoder.matches("superadmin", adminEntity.getPassword())).willReturn(true);
    	try {
			then(accountService.comparePassword("superadmin", phone)).isTrue();
		}
    	catch (Exception exc) {
			exc.printStackTrace();
		}
    	verify(accountServiceJPA).getAccount(phone);
    	verify(bCryptPasswordEncoder).matches("superadmin", adminEntity.getPassword());
    }
    
	@Test
	void save_and_verify_identity() {

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
		org.mockito.BDDMockito.then(accountRepository).should().save(argumentCaptor.capture());
        
		given(accountRepository.findByPhone(phone)).willReturn(adminEntity);
		try {
			then(accountService.verifyAccount(phone, adminEntity.getPhone())).isTrue();
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
        org.mockito.BDDMockito.then(accountRepository).should().findByPhone(phone);
	}
	
	@Test
	void save_and_catch_exceptions() {
		
		AccountRequestDTO accountRequestDTO = new AccountRequestDTO();
		accountRequestDTO.setBirthday(LocalDate.now().minusYears(10L).toString());
		accountRequestDTO.setName("Admin");
		accountRequestDTO.setPassword("superadmin");
		accountRequestDTO.setPhone("0000000000");
		accountRequestDTO.setSurname("Adminov");

		assertThatExceptionOfType(RegistrationException.class)
		.isThrownBy(() -> {accountService.saveAccount(accountRequestDTO);});
		
		accountRequestDTO.setBirthday(LocalDate.now().minusYears(20L).toString());		
		doThrow(new PersistenceException()).when(accountRepository).save(adminEntity);
		
		assertThatExceptionOfType(PersistenceException.class)
		.isThrownBy(() -> {accountService.saveAccount(accountRequestDTO);});
		org.mockito.BDDMockito.then(accountRepository).should().save(adminEntity);
	}
    
    @AfterEach
    void tear_down() throws Exception {
    	autoClosable.close();
    	accountService = null;
    }
    
    @AfterAll
    static void clear() {    	
    	phone = null;
    	adminEntity = null;
    }
    
}
