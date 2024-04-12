package com.github.irybov.bankdemorest.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import javax.persistence.PersistenceException;

import org.junit.jupiter.api.Disabled;

//import java.io.File;
//import java.nio.file.Files;

import org.junit.jupiter.api.Test;
//import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.irybov.bankdemorest.config.SecurityBeans;
import com.github.irybov.bankdemorest.config.SecurityConfig;
import com.github.irybov.bankdemorest.controller.AuthController;
import com.github.irybov.bankdemorest.controller.dto.AccountRequest;
import com.github.irybov.bankdemorest.controller.dto.AccountResponse;
import com.github.irybov.bankdemorest.controller.dto.LoginRequest;
import com.github.irybov.bankdemorest.entity.Account;
import com.github.irybov.bankdemorest.exception.RegistrationException;
import com.github.irybov.bankdemorest.security.AccountDetails;
import com.github.irybov.bankdemorest.security.AccountDetailsService;
import com.github.irybov.bankdemorest.security.Role;
import com.github.irybov.bankdemorest.service.AccountService;
import com.github.irybov.bankdemorest.service.AccountServiceDAO;
import com.github.irybov.bankdemorest.service.AccountServiceJPA;
import com.github.irybov.bankdemorest.util.JWTUtility;

@WebMvcTest(AuthController.class)
@Import(value = {SecurityConfig.class, SecurityBeans.class})
@TestPropertySource(locations = "classpath:jwt.properties")
class AuthControllerTest {

	@MockBean
	@Qualifier("beforeCreateAccountValidator")
	private Validator accountValidator;
	@MockBean
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	@MockBean
	private UserDetailsService accountDetailsService;
	
	@Autowired
	private MockMvc mockMVC;
	@Autowired
	private ObjectMapper mapper;

	@SpyBean
	private JWTUtility jwtUtility;
	@MockBean
	private AuthenticationManager authenticationManager;
/*	
	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	*/

/*	@Test
	void can_get_start_html() throws Exception {
		
//		File home = new ClassPathResource("templates/auth/home.html").getFile();
//		String html = new String(Files.readAllBytes(home.toPath()));
		
        mockMVC.perform(get("/home"))
	        .andExpect(status().isOk())
	//        .andExpect(content().string(html))
	        .andExpect(content().string(containsString("Welcome!")))
	        .andExpect(view().name("auth/home"));
	}*/
	
/*	@Test
	void can_get_registration_form() throws Exception {
		
        mockMVC.perform(get("/register"))
	        .andExpect(status().isOk())
	        .andExpect(content().string(containsString("Registration")))
	        .andExpect(model().size(1))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(view().name("auth/register"));
	}*/

/*	@Test
	void can_get_login_form() throws Exception {
		
//		File login = new ClassPathResource("templates/auth/login.html").getFile();
//		String html = new String(Files.readAllBytes(login.toPath()));
		
        mockMVC.perform(get("/login"))
	        .andExpect(status().isOk())
	//        .andExpect(content().string(html))
	        .andExpect(content().string(containsString("Log In")))
	        .andExpect(view().name("auth/login"));
	}*/
	
/*	@WithMockUser(username = "0000000000", roles = {"ADMIN", "CLIENT"})
	@Test
	void can_get_menu_html() throws Exception {

		ModelMapper modelMapper = new ModelMapper();
		AccountResponse accountResponse = modelMapper.map(new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true), 
				AccountResponse.class);
		
		when(accountService.getAccountDTO(anyString())).thenReturn(accountResponse);
		
		String roles = authentication().getAuthorities().toString();
		assertThat(authentication().getName()).isEqualTo("0000000000");
		assertThat(roles).isEqualTo("[ROLE_ADMIN, ROLE_CLIENT]");
		
		mockMVC.perform(get("/success"))
			.andExpect(status().isOk())
			.andExpect(authenticated())
			.andExpect(content().string(containsString("Welcome!")))
			.andExpect(content().string(containsString(accountResponse.getName()+" "
					+accountResponse.getSurname())))
			.andExpect(content().string(containsString(roles)))
	        .andExpect(model().size(1))
	        .andExpect(model().attribute("account", accountResponse))
	        .andExpect(view().name("auth/success"));
	    
	    verify(accountService).getAccountDTO(anyString());
	}*/
	
/*	@Test
	void correct_user_creds() throws Exception {

		final String hashedPW = BCrypt.hashpw("superadmin", BCrypt.gensalt(4));
		
		when(accountDetailsService.loadUserByUsername(anyString()))
			.thenReturn(new AccountDetails(new Account
			("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), hashedPW, true)));

		mockMVC.perform(formLogin("/auth").user("phone", "0000000000").password("superadmin"))
			.andExpect(authenticated())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/success"));
		
	    verify(accountDetailsService).loadUserByUsername(anyString());
	}*/
	
/*	@Test
	void wrong_user_creds() throws Exception {
		
		when(accountDetailsService.loadUserByUsername(anyString()))
			.thenThrow(new UsernameNotFoundException("User 9999999999 not found"));
		
		mockMVC.perform(formLogin("/auth").user("phone", "9999999999").password("localadmin"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?error=true"));
		
	    verify(accountDetailsService, times(3)).loadUserByUsername(anyString());
	}*/
	
/*	@WithMockUser(username = "9999999999")
	@Test
	void entity_not_found() throws Exception {
		
		String phone = authentication().getName();
		when(accountService.getAccountDTO(anyString())).thenThrow(new PersistenceException
							("Account with phone " + phone + " not found"));
		
		assertThat(phone).isEqualTo("9999999999");
		
		mockMVC.perform(get("/success"))
			.andExpect(authenticated())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/home"));
	    
	    verify(accountService).getAccountDTO(anyString());
	}*/
	
/*	@Test
	void unauthorized_denied() throws Exception {
//		mockMVC.perform(get("/success")).andExpect(status().isUnauthorized());
		mockMVC.perform(post("/confirm")).andExpect(status().isForbidden());
	}*/
	
	@Test
	void accepted_registration() throws Exception {
		
		AccountRequest accountRequest = new AccountRequest();
//		accountRequestDTO.setBirthday("2001-01-01");
		accountRequest.setBirthday(LocalDate.of(2001, 01, 01));
		accountRequest.setName("Admin");
		accountRequest.setPassword("superadmin");
		accountRequest.setPhone("0000000000");
		accountRequest.setSurname("Adminov");

		mockMVC.perform(post("/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isCreated())
/*	        .andExpect(model().size(2))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().attribute("success", "Your account has been created"))
			.andExpect(view().name("auth/login"));*/
			.andExpect(content().string("Your account has been created"));
		
	    verify(accountService).saveAccount(refEq(accountRequest));
	}
	
	@Test
	void rejected_registration() throws Exception {
		
		AccountRequest accountRequest = new AccountRequest();
		accountRequest.setBirthday(null);
		accountRequest.setName("i");
		accountRequest.setPassword("superb");
		accountRequest.setPhone("xxx");
		accountRequest.setSurname("a");

		mockMVC.perform(post("/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isBadRequest())
/*	        .andExpect(model().size(1))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().hasErrors())
			.andExpect(view().name("auth/register"));*/
			.andExpect(jsonPath("$", hasItem("Please select your date of birth")))
			.andExpect(jsonPath("$.length()", equalTo(7)))
			.andExpect(jsonPath("$").isArray());
		
		accountRequest.setBirthday(LocalDate.now().plusYears(10L));
		mockMVC.perform(post("/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isBadRequest())
/*	        .andExpect(model().size(1))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().hasErrors())
			.andExpect(view().name("auth/register"));*/
			.andExpect(jsonPath("$", hasItem("Birthday can't be future time")))
			.andExpect(jsonPath("$.length()", equalTo(7)))
			.andExpect(jsonPath("$").isArray());
		
		accountRequest.setBirthday(LocalDate.now().minusYears(17L));
		mockMVC.perform(post("/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isBadRequest())
/*	        .andExpect(model().size(1))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().hasErrors())
			.andExpect(view().name("auth/register"));*/
			.andExpect(jsonPath("$.length()", equalTo(6)))
			.andExpect(jsonPath("$").isArray());
	}
	
	@Test
	void interrupted_registration() throws Exception {
		
		AccountRequest accountRequest = new AccountRequest();
		accountRequest.setBirthday(LocalDate.now().minusYears(17L));
		accountRequest.setName("Admin");
		accountRequest.setPassword("superadmin");
		accountRequest.setPhone("5555555555");
		accountRequest.setSurname("Adminov");
		
		doThrow(new RegistrationException("You must be 18+ to register"))
		.when(accountService).saveAccount(refEq(accountRequest));

		mockMVC.perform(post("/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isConflict())
/*	        .andExpect(model().size(2))
	        .andExpect(model().attributeExists("account"))
        	.andExpect(model().attribute("message", "You must be 18+ to register"))
			.andExpect(view().name("auth/register"));*/
			.andExpect(content().string("You must be 18+ to register"));
		
	    verify(accountService).saveAccount(refEq(accountRequest));
	    
//		accountRequestDTO.setBirthday("2001-01-01");
		accountRequest.setBirthday(LocalDate.of(2001, 01, 01));
		accountRequest.setPhone("0000000000");
		
		doThrow(new RuntimeException("This number is already in use"))
		.when(accountService).saveAccount(refEq(accountRequest));

		mockMVC.perform(post("/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isConflict())
/*	        .andExpect(model().size(2))
	        .andExpect(model().attributeExists("account"))
        	.andExpect(model().attribute("message", "This number is already in use."))
			.andExpect(view().name("auth/register"));*/
			.andExpect(content().string("This number is already in use"));
		
	    verify(accountService).saveAccount(refEq(accountRequest));
	}
	
	@Disabled
	@Test
	void violated_registration() throws Exception {
		
		AccountRequest accountRequest = new AccountRequest();
//		accountRequestDTO.setBirthday("2001-01-01");
		accountRequest.setBirthday(LocalDate.of(2001, 01, 01));
		accountRequest.setName("Admin");
		accountRequest.setPassword("superadmin");
		accountRequest.setPhone("0000000000");
		accountRequest.setSurname("Adminov");
		
		doThrow(new RuntimeException("This number is already in use"))
		.when(accountService).saveAccount(refEq(accountRequest));

		mockMVC.perform(post("/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isConflict())
/*	        .andExpect(model().size(2))
	        .andExpect(model().attributeExists("account"))
        	.andExpect(model().attribute("message", "This number is already in use."))
			.andExpect(view().name("auth/register"));*/
			.andExpect(content().string("This number is already in use"));
		
	    verify(accountService).saveAccount(refEq(accountRequest));
	}
	
	@Test
	void correct_creds_jwt() throws Exception {
		
		Account account = new Account("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01),
				 BCrypt.hashpw("superadmin", BCrypt.gensalt(4)), true);
		account.addRole(Role.ADMIN);
		UserDetails details = new AccountDetails(account);
		
		LoginRequest loginRequest = new LoginRequest("0000000000", "superadmin");
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken
				(loginRequest.getPhone(), loginRequest.getPassword());
		
		doReturn(auth).when(authenticationManager).authenticate(refEq(auth));
		doReturn(details).when(accountDetailsService).loadUserByUsername(refEq(loginRequest.getPhone()));
//		doReturn(new String("XXL")).when(jwtUtility).generate(refEq(details));
			
		mockMVC.perform(get("/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(loginRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isString());
		
		verify(authenticationManager).authenticate(refEq(auth));
		verify(accountDetailsService).loadUserByUsername(refEq(loginRequest.getPhone()));
//		verify(jwtUtility).generate(refEq(details));
	}
	
	@Test
	void wrong_password_jwt() throws Exception {
		
		LoginRequest loginRequest = new LoginRequest("0000000000", "localadmin");
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken
				(loginRequest.getPhone(), loginRequest.getPassword());
		
		doThrow(new BadCredentialsException("Bad credentials"))
			.when(authenticationManager).authenticate(refEq(auth));
		for(int i = 1; i < 4; i++) {
			mockMVC.perform(get("/token")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(loginRequest)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(containsString("Bad credentials")))
			.andExpect(jsonPath("$").isString())
			.andExpect(result -> assertThat
				(result.getResolvedException() instanceof BadCredentialsException))
			.andExpect(result -> assertEquals
				("Bad credentials", result.getResolvedException().getMessage()))
			.andDo(print());
		}
		verify(authenticationManager, times(3)).authenticate(refEq(auth));
		
		loginRequest = new LoginRequest("0000000000", "superadmin");
		auth = new UsernamePasswordAuthenticationToken
				(loginRequest.getPhone(), loginRequest.getPassword());
		
		doThrow(new DisabledException("User is disabled"))
			.when(authenticationManager).authenticate(refEq(auth));
		mockMVC.perform(get("/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(loginRequest)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(containsString("User is disabled")))
			.andExpect(jsonPath("$").isString())
			.andExpect(result -> assertThat
				(result.getResolvedException() instanceof DisabledException))
			.andExpect(result -> assertEquals
				("User is disabled", result.getResolvedException().getMessage()))
			.andDo(print());
		
		verify(authenticationManager).authenticate(refEq(auth));
	}
	
	@Test
	void abscent_creds_jwt() throws Exception {
		
		Account account = new Account("Kylie", "Bunbury", "4444444444", LocalDate.of(1989, 01, 30),
				 BCrypt.hashpw("blackmamba", BCrypt.gensalt(4)), true);
		account.addRole(Role.CLIENT);
//		UserDetails details = new AccountDetails(account);
		
		LoginRequest loginRequest = new LoginRequest("4444444444", "blackmamba");
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken
				(loginRequest.getPhone(), loginRequest.getPassword());
		
		doThrow(new UsernameNotFoundException("User " + loginRequest.getPhone() + " not found"))
			.when(authenticationManager).authenticate(refEq(auth));
//		doCallRealMethod().when(authenticationManager).authenticate(refEq(auth));
//		doThrow(new UsernameNotFoundException("User " + loginRequest.getPhone() + " not found"))
//			.when(accountDetailsService).loadUserByUsername(loginRequest.getPhone());
		
		mockMVC.perform(get("/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(loginRequest)))
			.andExpect(status().isNotFound())
			.andExpect(content().string(containsString("User " + loginRequest.getPhone() + " not found")))
			.andExpect(jsonPath("$").isString())
			.andExpect(result -> assertThat
				(result.getResolvedException() instanceof UsernameNotFoundException))
			.andExpect(result -> assertEquals
				("User " + loginRequest.getPhone() + " not found", result.getResolvedException().getMessage()))
			.andDo(print());
		
		verify(authenticationManager).authenticate(refEq(auth));
//		verify(accountDetailsService).loadUserByUsername(loginRequest.getPhone());
	}
	
	@Test
	void invalid_creds_jwt() throws Exception {
		
		LoginRequest loginRequest = new LoginRequest("00000", "super");
		mockMVC.perform(get("/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(loginRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$", hasItem("Please input phone number like a row of 10 digits")))
			.andExpect(jsonPath("$", hasItem("Password should be 10-60 symbols length")))
			.andExpect(jsonPath("$.length()", equalTo(2)))
			.andExpect(jsonPath("$").isArray())
		    .andExpect(result -> assertTrue
		    		(result.getResolvedException() instanceof MethodArgumentNotValidException));
	}
	
}
