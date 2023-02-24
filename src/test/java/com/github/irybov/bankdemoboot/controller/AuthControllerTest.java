package com.github.irybov.bankdemoboot.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.OffsetDateTime;

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

import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.security.AccountDetailsService;
import com.github.irybov.bankdemoboot.security.Role;
import com.github.irybov.bankdemoboot.service.AccountService;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

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
		entity.addRole(Role.ADMIN);
		AccountResponseDTO account = new AccountResponseDTO(entity);
		
		when(accountService.getAccountDTO(anyString())).thenReturn(account);
		
		mock.perform(get("/success"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Welcome!")))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(view().name("/auth/success"));
	    
	    verify(accountService).getAccountDTO(anyString());
	}
	
}
