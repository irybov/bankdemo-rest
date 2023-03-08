package com.github.irybov.bankdemoboot.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import javax.persistence.EntityNotFoundException;

//import java.io.File;
//import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Validator;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.exception.RegistrationException;
import com.github.irybov.bankdemoboot.security.AccountDetailsService;
import com.github.irybov.bankdemoboot.service.AccountService;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

	@MockBean
	@Qualifier("beforeCreateAccountValidator")
	private Validator accountValidator;
	@MockBean
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	@MockBean
	private AccountDetailsService accountDetailsService;
	@Autowired
	private MockMvc mock;
	
	@Test
	void can_get_start_page() throws Exception {
		
//		File home = new ClassPathResource("templates/auth/home.html").getFile();
//		String html = new String(Files.readAllBytes(home.toPath()));
		
        mock.perform(get("/home"))
	        .andExpect(status().isOk())
	//        .andExpect(content().string(html))
	        .andExpect(content().string(containsString("Welcome!")))
	        .andExpect(view().name("/auth/home"));
	}
	
	@Test
	void can_get_registration_form() throws Exception {
		
        mock.perform(get("/register"))
	        .andExpect(status().isOk())
	        .andExpect(content().string(containsString("Registration")))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(view().name("/auth/register"));
	}

	@Test
	void can_get_login_page() throws Exception {
		
//		File login = new ClassPathResource("templates/auth/login.html").getFile();
//		String html = new String(Files.readAllBytes(login.toPath()));
		
        mock.perform(get("/login"))
	        .andExpect(status().isOk())
	//        .andExpect(content().string(html))
	        .andExpect(content().string(containsString("Log In")))
	        .andExpect(view().name("/auth/login"));
	}
	
	@WithMockUser
	@Test
	void can_get_menu_page() throws Exception {
			
		Account entity = new Account("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01),
											 BCrypt.hashpw("superadmin", BCrypt.gensalt(4)), true);
		entity.setId(0);
		entity.setCreatedAt(OffsetDateTime.now());
		AccountResponseDTO account = new AccountResponseDTO(entity);
		
		when(accountService.getAccountDTO(anyString())).thenReturn(account);
		
		mock.perform(get("/success"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Welcome!")))
	        .andExpect(model().attribute("account", account))
	        .andExpect(view().name("/auth/success"));
	    
	    verify(accountService).getAccountDTO(anyString());
	}
	
	@WithMockUser
	@Test
	void entity_not_found() throws Exception {
		
		when(accountService.getAccountDTO(anyString())).thenThrow(EntityNotFoundException.class);
		
		mock.perform(get("/success"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/home"));
	    
	    verify(accountService).getAccountDTO(anyString());
	}
	
	@Test
	void accepted_registration() throws Exception {
		
		AccountRequestDTO accountRequestDTO = new AccountRequestDTO();
		accountRequestDTO.setBirthday("2001-01-01");
		accountRequestDTO.setName("Admin");
		accountRequestDTO.setPassword("superadmin");
		accountRequestDTO.setPhone("0000000000");
		accountRequestDTO.setSurname("Adminov");
		
		mock.perform(post("/confirm").with(csrf())
									 .param("birthday", accountRequestDTO.getBirthday())
									 .param("name", accountRequestDTO.getName())
									 .param("password", accountRequestDTO.getPassword())
									 .param("phone", accountRequestDTO.getPhone())
									 .param("surname", accountRequestDTO.getSurname())
					)
			.andExpect(status().isCreated())
			.andExpect(view().name("/auth/login"));
	}
	
	@Test
	void rejected_registration() throws Exception {
		
		AccountRequestDTO accountRequestDTO = new AccountRequestDTO();
		accountRequestDTO.setBirthday(null);
		accountRequestDTO.setName("i");
		accountRequestDTO.setPassword("superb");
		accountRequestDTO.setPhone("xxx");
		accountRequestDTO.setSurname("a");
		
		mock.perform(post("/confirm").with(csrf())
									 .param("birthday", accountRequestDTO.getBirthday())
									 .param("name", accountRequestDTO.getName())
									 .param("password", accountRequestDTO.getPassword())
									 .param("phone", accountRequestDTO.getPhone())
									 .param("surname", accountRequestDTO.getSurname())
					)
			.andExpect(status().isBadRequest())
			.andExpect(view().name("/auth/register"));
	}
	
	@Test
	void interrupted_registration() throws Exception {
		
		AccountRequestDTO accountRequestDTO = new AccountRequestDTO();
		accountRequestDTO.setBirthday(LocalDate.now().minusYears(10L).toString());
		accountRequestDTO.setName("Admin");
		accountRequestDTO.setPassword("superadmin");
		accountRequestDTO.setPhone("0000000000");
		accountRequestDTO.setSurname("Adminov");
		
		doThrow(new RegistrationException("You must be 18+ to register"))
		.when(accountService).saveAccount(refEq(accountRequestDTO));
		
		mock.perform(post("/confirm").with(csrf())
									 .param("birthday", accountRequestDTO.getBirthday())
									 .param("name", accountRequestDTO.getName())
									 .param("password", accountRequestDTO.getPassword())
									 .param("phone", accountRequestDTO.getPhone())
									 .param("surname", accountRequestDTO.getSurname())
					)
			.andExpect(status().isBadRequest())
        	.andExpect(model().attribute("message", "You must be 18+ to register"))
			.andExpect(view().name("/auth/register"));
		
	    verify(accountService).saveAccount(refEq(accountRequestDTO));
	}
	
	@Test
	void violated_registration() throws Exception {
		
		AccountRequestDTO accountRequestDTO = new AccountRequestDTO();
		accountRequestDTO.setBirthday("2001-01-01");
		accountRequestDTO.setName("Admin");
		accountRequestDTO.setPassword("superadmin");
		accountRequestDTO.setPhone("0000000000");
		accountRequestDTO.setSurname("Adminov");
		
		doThrow(new Exception("This number is already in use."))
		.when(accountService).saveAccount(refEq(accountRequestDTO));
		
		mock.perform(post("/confirm").with(csrf())
									 .param("birthday", accountRequestDTO.getBirthday())
									 .param("name", accountRequestDTO.getName())
									 .param("password", accountRequestDTO.getPassword())
									 .param("phone", accountRequestDTO.getPhone())
									 .param("surname", accountRequestDTO.getSurname())
					)
			.andExpect(status().isConflict())
        	.andExpect(model().attribute("message", "This number is already in use."))
			.andExpect(view().name("/auth/register"));
		
	    verify(accountService).saveAccount(refEq(accountRequestDTO));
	}
	
}
