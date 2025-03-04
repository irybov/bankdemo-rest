package com.github.irybov.web.controller;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import java.util.Map;

import javax.mail.MessagingException;
import javax.persistence.PersistenceException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

//import java.io.File;
//import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
//import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.irybov.service.dto.AccountRequest;
import com.github.irybov.service.dto.AccountResponse;
import com.github.irybov.service.dto.LoginRequest;
import com.github.irybov.database.entity.Account;
import com.github.irybov.service.exception.RegistrationException;
import com.github.irybov.service.security.AccountDetails;
import com.github.irybov.service.security.AccountDetailsService;
import com.github.irybov.service.security.EmailService;
import com.github.irybov.database.entity.Role;
import com.github.irybov.service.service.AccountService;
import com.github.irybov.service.service.AccountServiceDAO;
import com.github.irybov.service.service.AccountServiceJPA;
import com.github.irybov.web.config.SecurityBeans;
import com.github.irybov.web.config.SecurityConfig;
import com.github.irybov.web.controller.AuthController;
import com.github.irybov.web.security.JWTUtility;

import net.bytebuddy.utility.RandomString;

@WebMvcTest(AuthController.class)
@Import(value = {SecurityConfig.class, SecurityBeans.class})
@TestPropertySource(locations = "classpath:jwt.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
	@Autowired
	ApplicationContext context;
	
	@MockBean
	private EmailService emailService;
	private Map<String, AccountRequest> accounts;
	private String mailbox;
	@MockBean
	private Cache<String, LoginRequest> cache;
	
	@Value("${external.payment-service}")
	private String externalURL;
	
	@BeforeAll
	void set_up() {
		mailbox = "@greenmail.io";
		accounts = mock(Map.class);
		ReflectionTestUtils.setField(context.getBean(AuthController.class), "accounts", accounts);
		ReflectionTestUtils.setField(context.getBean(AuthController.class), "accountService", accountService);
	}
	
	private AccountRequest buildCorrectAccountRequest() {
		AccountRequest accountRequest = new AccountRequest();
		accountRequest.setBirthday(LocalDate.of(2001, 01, 01));
		accountRequest.setName("Admin");
		accountRequest.setPassword("superadmin");
		accountRequest.setPhone("0000000000");
		accountRequest.setSurname("Adminov");
		accountRequest.setEmail(accountRequest.getSurname().toLowerCase() + mailbox);
		return accountRequest;
	}
	
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
		
		AccountRequest accountRequest = buildCorrectAccountRequest();
		String tail = RandomString.make();

		when(emailService.sendActivationLink(accountRequest.getEmail())).thenReturn(tail);
		
		mockMVC.perform(post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isOk())
/*	        .andExpect(model().size(2))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().attribute("success", "Your account has been created"))
			.andExpect(view().name("auth/login"));*/
			.andExpect(content().string("Check your email"));
		
		verify(emailService).sendActivationLink(accountRequest.getEmail());
		assertEquals(tail, emailService.sendActivationLink(accountRequest.getEmail()));
	}
	
	@Test
	void rejected_registration() throws Exception {
		
		AccountRequest accountRequest = new AccountRequest();
		accountRequest.setBirthday(null);
		accountRequest.setName("i");
		accountRequest.setPassword("superb");
		accountRequest.setPhone("xxx");
		accountRequest.setSurname("a");

		mockMVC.perform(post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isBadRequest())
/*	        .andExpect(model().size(1))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().hasErrors())
			.andExpect(view().name("auth/register"));*/
			.andExpect(jsonPath("$", hasItem("Please select your date of birth")))
			.andExpect(jsonPath("$.length()", equalTo(8)))
			.andExpect(jsonPath("$").isArray());
		
		accountRequest.setBirthday(LocalDate.now().plusYears(10L));
		mockMVC.perform(post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isBadRequest())
/*	        .andExpect(model().size(1))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().hasErrors())
			.andExpect(view().name("auth/register"));*/
			.andExpect(jsonPath("$", hasItem("Birthday can't be future time")))
			.andExpect(jsonPath("$.length()", equalTo(8)))
			.andExpect(jsonPath("$").isArray());
		
		accountRequest.setBirthday(LocalDate.now().minusYears(10L));
		mockMVC.perform(post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isBadRequest())
/*	        .andExpect(model().size(1))
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().hasErrors())
			.andExpect(view().name("auth/register"));*/
			.andExpect(jsonPath("$.length()", equalTo(7)))
			.andExpect(jsonPath("$").isArray());
		
		verify(emailService, never()).sendActivationLink(accountRequest.getEmail());
	}
	
	@Test
	void interrupted_registration() throws Exception {
		
		AccountRequest accountRequest = buildCorrectAccountRequest();
		
		doThrow(new MessagingException("Shit happens")).when(emailService).sendActivationLink(accountRequest.getEmail());
		
		mockMVC.perform(post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(accountRequest)))
			.andExpect(status().isInternalServerError())
			.andExpect(content().string("Shit happens"));
		
		verify(emailService).sendActivationLink(accountRequest.getEmail());
	}
	
	@Test
	void successful_activation() throws Exception {
		
		AccountRequest accountRequest = buildCorrectAccountRequest();
		String tail = RandomString.make();
		
		doNothing().when(accountService).saveAccount(refEq(accountRequest));
		when(accounts.containsKey(tail)).thenReturn(true);
		when(accounts.get(tail)).thenReturn(accountRequest);
		when(accounts.remove(tail)).thenReturn(accountRequest);
		
		mockMVC.perform(get("/activate/{tail}", tail).header(HttpHeaders.ORIGIN, externalURL))
			.andExpect(status().isCreated())
			.andExpect(content().string("Your account has been created"));
		
	    verify(accountService).saveAccount(refEq(accountRequest));
		verify(accounts).containsKey(tail);
	    verify(accounts).get(tail);
	    verify(accounts).remove(tail);
	}
	
	@Test
	void failed_activation() throws Exception {
		
		AccountRequest accountRequest = buildCorrectAccountRequest();
		String tail = RandomString.make();
		
		doThrow(new RuntimeException("This number is already in use"))
			.when(accountService).saveAccount(refEq(accountRequest));
		when(accounts.containsKey(tail)).thenReturn(true);
		when(accounts.get(tail)).thenReturn(accountRequest);
		
		mockMVC.perform(get("/activate/{tail}", tail).header(HttpHeaders.ORIGIN, externalURL))
			.andExpect(status().isConflict())
			.andExpect(content().string("This number is already in use"));
		
	    verify(accountService).saveAccount(refEq(accountRequest));
	    verify(accounts).get(tail);
		
		
	    when(accounts.containsKey(tail)).thenReturn(false);
	    
		mockMVC.perform(get("/activate/{tail}", tail).header(HttpHeaders.ORIGIN, externalURL))
			.andExpect(status().isGone())
			.andExpect(content().string("Link has been expired, try to register again"));
	    
		verify(accounts, times(2)).containsKey(tail);
	}
	
	@Test
	void violated_activation() throws Exception {
		
		String tail = "tail";
		mockMVC.perform(get("/activate/{tail}", tail).header(HttpHeaders.ORIGIN, externalURL))
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString("Path variable should be 8 chars length")));
		
		tail = " ";
		mockMVC.perform(get("/activate/{tail}", tail).header(HttpHeaders.ORIGIN, externalURL))
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString("Path variable must not be blank")));
		
		tail = "";
		mockMVC.perform(get("/activate/{tail}", tail).header(HttpHeaders.ORIGIN, externalURL))
			.andExpect(status().isNotFound());
		
		tail = null;
		mockMVC.perform(get("/activate/{tail}", tail).header(HttpHeaders.ORIGIN, externalURL))
			.andExpect(status().isNotFound());
	}
	
	@Test
	void correct_creds() throws Exception {
		
		Account account = new Account("Admin", "Adminov", "0000000000", "@greenmail.io", LocalDate.of(2001, 01, 01),
				 BCrypt.hashpw("superadmin", BCrypt.gensalt(4)), true);
		account.addRole(Role.ADMIN);
		AccountDetails details = new AccountDetails(account);
		String code = "1234";
		
		LoginRequest loginRequest = new LoginRequest("0000000000", "superadmin");
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken
				(loginRequest.getPhone(), loginRequest.getPassword());
		
		doReturn(auth).when(authenticationManager).authenticate(refEq(auth));
		doReturn(details).when(accountDetailsService).loadUserByUsername(refEq(loginRequest.getPhone()));
//		doReturn(new String("XXL")).when(jwtUtility).generate(refEq(details));
		doReturn(code).when(emailService).sendVerificationCode(details.getAccount().getEmail());
		doNothing().when(cache).put(code, loginRequest);
			
		mockMVC.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(loginRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isString())
			.andExpect(content().string("Check your email"));
		
		verify(authenticationManager).authenticate(refEq(auth));
		verify(accountDetailsService).loadUserByUsername(refEq(loginRequest.getPhone()));
//		verify(jwtUtility).generate(refEq(details));
		verify(emailService).sendVerificationCode(details.getAccount().getEmail());
		verify(cache).put(code, loginRequest);
	}
	
	@Test
	void wrong_password() throws Exception {
		
		LoginRequest loginRequest = new LoginRequest("0000000000", "localadmin");
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken
				(loginRequest.getPhone(), loginRequest.getPassword());
		
		doThrow(new BadCredentialsException("Bad credentials"))
			.when(authenticationManager).authenticate(refEq(auth));
		for(int i = 1; i < 4; i++) {
			mockMVC.perform(post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(loginRequest)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(containsString("Bad credentials")))
			.andExpect(jsonPath("$").isString())
			.andExpect(result -> assertThat
				(result.getResolvedException() instanceof BadCredentialsException).isTrue())
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
		mockMVC.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(loginRequest)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(containsString("User is disabled")))
			.andExpect(jsonPath("$").isString())
			.andExpect(result -> assertThat
				(result.getResolvedException() instanceof DisabledException).isTrue())
			.andExpect(result -> assertEquals
				("User is disabled", result.getResolvedException().getMessage()))
			.andDo(print());
		
		verify(authenticationManager).authenticate(refEq(auth));
	}
	
	@Test
	void abscent_creds() throws Exception {
		
		Account account = new Account("Kylie", "Bunbury", "4444444444", "@greenmail.io", LocalDate.of(1989, 01, 30),
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
		
		mockMVC.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(loginRequest)))
			.andExpect(status().isNotFound())
			.andExpect(content().string(containsString("User " + loginRequest.getPhone() + " not found")))
			.andExpect(jsonPath("$").isString())
			.andExpect(result -> assertThat
				(result.getResolvedException() instanceof UsernameNotFoundException).isTrue())
			.andExpect(result -> assertEquals
				("User " + loginRequest.getPhone() + " not found", result.getResolvedException().getMessage()))
			.andDo(print());
		
		verify(authenticationManager).authenticate(refEq(auth));
//		verify(accountDetailsService).loadUserByUsername(loginRequest.getPhone());
	}
	
	@Test
	void invalid_creds() throws Exception {
		
		LoginRequest loginRequest = new LoginRequest("00000", "super");
		mockMVC.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(loginRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$", hasItem("Please input phone number like a row of 10 digits")))
			.andExpect(jsonPath("$", hasItem("Password should be 10-60 symbols length")))
			.andExpect(jsonPath("$.length()", equalTo(2)))
			.andExpect(jsonPath("$").isArray())
		    .andExpect(result -> assertTrue
		    		(result.getResolvedException() instanceof MethodArgumentNotValidException).isTrue());
	}
	
    @AfterAll
    void tear_down() {
    	accounts = null;
    	mailbox = null;
    }
}
