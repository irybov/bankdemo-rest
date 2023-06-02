package com.github.irybov.bankdemoboot.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import javax.persistence.EntityNotFoundException;

//import java.io.File;
//import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Validator;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequest;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponse;
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
//	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	@MockBean
	private AccountDetailsService accountDetailsService;
	@Autowired
	private MockMvc mockMVC;
	
	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	@Test
	void can_get_start_html() throws Exception {
		
//		File home = new ClassPathResource("templates/auth/home.html").getFile();
//		String html = new String(Files.readAllBytes(home.toPath()));
		
        mockMVC.perform(get("/home"))
	        .andExpect(status().isOk())
	//        .andExpect(content().string(html))
	        .andExpect(content().string(containsString("Welcome!")))
	        .andExpect(view().name("auth/home"));
	}
	
	@Test
	void can_get_registration_form() throws Exception {
		
        mockMVC.perform(get("/register"))
	        .andExpect(status().isOk())
	        .andExpect(content().string(containsString("Registration")))
	        .andExpect(model().size(1))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(view().name("auth/register"));
	}

	@Test
	void can_get_login_form() throws Exception {
		
//		File login = new ClassPathResource("templates/auth/login.html").getFile();
//		String html = new String(Files.readAllBytes(login.toPath()));
		
        mockMVC.perform(get("/login"))
	        .andExpect(status().isOk())
	//        .andExpect(content().string(html))
	        .andExpect(content().string(containsString("Log In")))
	        .andExpect(view().name("auth/login"));
	}
	
	@WithMockUser(username = "0000000000", roles = {"ADMIN", "CLIENT"})
	@Test
	void can_get_menu_html() throws Exception {

		AccountResponse account = new AccountResponse(new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true));
		
		when(accountService.getAccountDTO(anyString())).thenReturn(account);
		
		String roles = authentication().getAuthorities().toString();
		assertThat(authentication().getName()).isEqualTo("0000000000");
		assertThat(roles).isEqualTo("[ROLE_ADMIN, ROLE_CLIENT]");
		
		mockMVC.perform(get("/success"))
			.andExpect(status().isOk())
			.andExpect(authenticated())
			.andExpect(content().string(containsString("Welcome!")))
			.andExpect(content().string(containsString(account.getName()+" "+account.getSurname())))
			.andExpect(content().string(containsString(roles)))
	        .andExpect(model().size(1))
	        .andExpect(model().attribute("account", account))
	        .andExpect(view().name("auth/success"));
	    
	    verify(accountService).getAccountDTO(anyString());
	}
	
	@WithMockUser(username = "9999999999")
	@Test
	void entity_not_found() throws Exception {
		
		String phone = authentication().getName();
		when(accountService.getAccountDTO(anyString())).thenThrow(new EntityNotFoundException
							("Account with phone " + phone + " not found"));
		
		assertThat(phone).isEqualTo("9999999999");
		
		mockMVC.perform(get("/success"))
			.andExpect(authenticated())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/home"));
	    
	    verify(accountService).getAccountDTO(anyString());
	}
	
	@Test
	void unauthorized_success() throws Exception {
		mockMVC.perform(get("/success"))
//			.andExpect(status().is3xxRedirection())
//			.andExpect(redirectedUrl("http://localhost/home"));
			.andExpect(status().isUnauthorized());
	}
	@Test
	void unauthorized_confirm() throws Exception {
		mockMVC.perform(post("/confirm")).andExpect(status().isForbidden());
	}
	
	@Test
	void accepted_registration() throws Exception {
		
		AccountRequest accountRequestDTO = new AccountRequest();
//		accountRequestDTO.setBirthday("2001-01-01");
		accountRequestDTO.setBirthday(LocalDate.of(2001, 01, 01));
		accountRequestDTO.setName("Admin");
		accountRequestDTO.setPassword("superadmin");
		accountRequestDTO.setPhone("0000000000");
		accountRequestDTO.setSurname("Adminov");

		mockMVC.perform(post("/confirm").with(csrf()).flashAttr("account", accountRequestDTO))
			.andExpect(status().isCreated())
			.andExpect(view().name("auth/login"));
	}
	
	@Test
	void rejected_registration() throws Exception {
		
		AccountRequest accountRequestDTO = new AccountRequest();
		accountRequestDTO.setBirthday(null);
		accountRequestDTO.setName("i");
		accountRequestDTO.setPassword("superb");
		accountRequestDTO.setPhone("xxx");
		accountRequestDTO.setSurname("a");

		mockMVC.perform(post("/confirm").with(csrf()).flashAttr("account", accountRequestDTO))
			.andExpect(status().isBadRequest())
	        .andExpect(model().size(1))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().hasErrors())
			.andExpect(view().name("auth/register"));
		
		accountRequestDTO.setBirthday(LocalDate.now().plusYears(10L));
		
		mockMVC.perform(post("/confirm").with(csrf()).flashAttr("account", accountRequestDTO))
			.andExpect(status().isBadRequest())
	        .andExpect(model().size(1))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().hasErrors())
			.andExpect(view().name("auth/register"));
	}
	
	@Test
	void interrupted_registration() throws Exception {
		
		AccountRequest accountRequestDTO = new AccountRequest();
		accountRequestDTO.setBirthday(LocalDate.now().minusYears(10L));
		accountRequestDTO.setName("Admin");
		accountRequestDTO.setPassword("superadmin");
		accountRequestDTO.setPhone("0000000000");
		accountRequestDTO.setSurname("Adminov");
		
		doThrow(new RegistrationException("You must be 18+ to register"))
		.when(accountService).saveAccount(refEq(accountRequestDTO));

		mockMVC.perform(post("/confirm").with(csrf()).flashAttr("account", accountRequestDTO))
			.andExpect(status().isConflict())
	        .andExpect(model().size(2))
	        .andExpect(model().attributeExists("account"))
        	.andExpect(model().attribute("message", "You must be 18+ to register"))
			.andExpect(view().name("auth/register"));
		
	    verify(accountService).saveAccount(refEq(accountRequestDTO));
	}
	
	@Test
	void violated_registration() throws Exception {
		
		AccountRequest accountRequestDTO = new AccountRequest();
//		accountRequestDTO.setBirthday("2001-01-01");
		accountRequestDTO.setBirthday(LocalDate.of(2001, 01, 01));
		accountRequestDTO.setName("Admin");
		accountRequestDTO.setPassword("superadmin");
		accountRequestDTO.setPhone("0000000000");
		accountRequestDTO.setSurname("Adminov");
		
		doThrow(new RuntimeException("This number is already in use."))
		.when(accountService).saveAccount(refEq(accountRequestDTO));

		mockMVC.perform(post("/confirm").with(csrf()).flashAttr("account", accountRequestDTO))
			.andExpect(status().isConflict())
	        .andExpect(model().size(2))
	        .andExpect(model().attributeExists("account"))
        	.andExpect(model().attribute("message", "This number is already in use."))
			.andExpect(view().name("auth/register"));
		
	    verify(accountService).saveAccount(refEq(accountRequestDTO));
	}
	
}
