package com.github.irybov.web;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.irybov.service.dto.AccountRequest;
import com.github.irybov.service.dto.AccountResponse;
import com.github.irybov.service.dto.BillRequest;
import com.github.irybov.service.dto.LoginRequest;
import com.github.irybov.service.dto.OperationRequest;
import com.github.irybov.service.dto.OperationResponse;
import com.github.irybov.service.dto.PasswordRequest;
import com.github.irybov.database.dao.AccountDAO;
import com.github.irybov.database.entity.Account;
import com.github.irybov.service.exception.RegistrationException;
import com.github.irybov.database.jpa.AccountJPA;
import com.github.irybov.service.security.AccountDetails;
import com.github.irybov.service.security.AccountDetailsService;
import com.github.irybov.service.security.UnlockService;
import com.github.irybov.service.service.AccountService;
import com.github.irybov.service.service.AccountServiceDAO;
import com.github.irybov.service.service.AccountServiceJPA;
import com.github.irybov.service.service.BillServiceDAO;
import com.github.irybov.service.service.BillServiceJPA;
import com.github.irybov.service.service.OperationServiceDAO;
import com.github.irybov.service.service.OperationServiceJPA;
import com.github.irybov.web.controller.AuthController;
import com.github.irybov.web.security.LoginListener;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@Sql("/test-data-h2.sql")
@AutoConfigureMockMvc
@Transactional
public class BankDemoBootApplicationIT {

	@Autowired
	private MockMvc mockMVC;

	@Autowired
	private ObjectMapper mapper;
	@Autowired
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
/*	@Autowired
	private AccountJPA jpa;
	@Autowired
	private AccountDAO dao;
	*/
	private static WireMockServer wireMockServer;
	@Value("${external.payment-service}")
	private static String externalURL;
	
	@BeforeAll
	static void prepare() {
		wireMockServer = new WireMockServer(new WireMockConfiguration().port(4567));
		wireMockServer.start();
		WireMock.configureFor(externalURL, 4567);
	}
	
	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	private void hashPassword(String phone, String password) {
		
		accountService.changePassword(phone, password);
/*		
		Account account = null;
		if(accountService instanceof AccountServiceJPA) {
			account = jpa.findByPhone(phone).get();
			account.setPassword(BCrypt.hashpw(account.getPassword(), BCrypt.gensalt(4)));
			jpa.saveAndFlush(account);
		}
		else if(accountService instanceof AccountServiceDAO) {
			account = dao.getAccount(phone);
			account.setPassword(BCrypt.hashpw(account.getPassword(), BCrypt.gensalt(4)));
			dao.updateAccount(account);
		}*/
	}

	@Test
	void context_loading(ApplicationContext context) {
		assertThat(context).isNotNull();
	}
	
    @Nested
    class ActuatorAccessIT{
    	
//		@WithMockUser(username = "remote", roles = "REMOTE")
		@Test
		void actuator_allowed() throws Exception {
			
	        mockMVC.perform(get("/actuator/").with(httpBasic("remote", "remote")))
	    		.andExpect(status().isOk());
		}
		
		@Test
		void actuator_denied() throws Exception {
			
	        mockMVC.perform(get("/actuator/"))
	    		.andExpect(status().isUnauthorized());
		}
		
//		@WithMockUser(username = "3333333333", roles = {"ADMIN", "CLIENT"})
		@Test
		void actuator_forbidden() throws Exception {
			
			hashPassword("3333333333", "gingerchick");
			
	        mockMVC.perform(get("/actuator/").with(httpBasic("3333333333", "gingerchick")))
	    		.andExpect(status().isForbidden());
		}
		
	}	
	
//	@WithMockUser(username = "3333333333", roles = {"ADMIN", "CLIENT"})
    @Nested
    class SwaggerAccessIT{
    	
		private static final String ADMIN_PHONE = "3333333333";
		private static final String CLIENT_PHONE = "1111111111";
    	
		@Test
		void swagger_allowed() throws Exception {
			
			hashPassword(ADMIN_PHONE, "gingerchick");
			
	        mockMVC.perform(get("/dox/swagger-ui/").with(httpBasic(ADMIN_PHONE, "gingerchick")))
	    		.andExpect(status().isOk());
		}
		
//		@WithMockUser(username = "1111111111", roles = "CLIENT")
		@Test
		void swagger_denied() throws Exception {
			
			hashPassword(CLIENT_PHONE, "supervixen");
			
	        mockMVC.perform(get("/dox/swagger-ui/").with(httpBasic(CLIENT_PHONE, "supervixen")))
				.andExpect(status().isForbidden());
		}

	    @Disabled
	    @Test
	    void can_get_swagger_html() throws Exception {

	        mockMVC.perform(get("/swagger-ui.html"))
	        	.andExpect(status().isOk());
	    }
		
	    @Test
	    void can_get_api_docs() throws Exception {
	    	
	    	hashPassword(ADMIN_PHONE, "gingerchick");

	        mockMVC.perform(get("/dox/v3/api-docs").with(httpBasic(ADMIN_PHONE, "gingerchick")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));		
		}

	    @Test
	    void denied_api_docs() throws Exception {
	    	
	    	hashPassword(CLIENT_PHONE, "supervixen");

	        mockMVC.perform(get("/dox/v3/api-docs").with(httpBasic(CLIENT_PHONE, "supervixen")))
				.andExpect(status().isForbidden());		
		}
	    
	    @Disabled
	    @Test
	    void can_get_swagger_config() throws Exception {

	        mockMVC.perform(get("/dox/v3/api-docs/swagger-config"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));		
		}
		
    }
    
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class AuthControllerIT{
    	
	    @Autowired
	    ApplicationContext context;
	    private Map<String, AccountRequest> accounts = new ConcurrentReferenceHashMap<>();
	    @Autowired
		private Cache<String, LoginRequest> cache;
	    @Autowired
	    private AccountDetailsService accountDetailsService;
	    
		@Value("${server.address}")
		private String uri;
		@Value("${local.server.port}")
		private int port;
		@Value("${server.servlet.context-path}")
		private String path;
		@Value("${external.payment-service}")
		private String externalURL;
	    
		@RegisterExtension
		GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
		        .withConfiguration(GreenMailConfiguration.aConfig().withUser("irybov", "bankdemo"))
		        .withPerMethodLifecycle(false);
    	
		private static final String PHONE = "0000000000";
		
		private String mailbox = "@greenmail.io";
		private AccountRequest buildCorrectAccountRequest() {
			AccountRequest accountRequest = new AccountRequest();
			accountRequest.setBirthday(LocalDate.of(1989, 01, 30));
			accountRequest.setName("Kylie");
			accountRequest.setPassword("blackmamba");
			accountRequest.setPhone("4444444444");
			accountRequest.setSurname("Bunbury");
			accountRequest.setEmail(accountRequest.getSurname().toLowerCase() + mailbox);
			return accountRequest;
		}
	
/*		@Test
		void can_get_start_html() throws Exception {
			
	        mockMVC.perform(get("/home"))
	        	.andExpect(status().isOk())
	        	.andExpect(content().string(containsString("Welcome!")))
	        	.andExpect(view().name("auth/home"));
		}*/
		
/*		@Test
		void can_get_registration_form() throws Exception {

	        mockMVC.perform(get("/register"))
		        .andExpect(status().isOk())
		        .andExpect(content().string(containsString("Registration")))
		        .andExpect(model().size(1))
		        .andExpect(model().attribute("account", any(AccountRequest.class)))
		        .andExpect(view().name("auth/register"));
		}*/
	
/*		@Test
		void can_get_login_form() throws Exception {
			
	        mockMVC.perform(get("/login"))
		        .andExpect(status().isOk())
		        .andExpect(content().string(containsString("Log In")))
		        .andExpect(view().name("auth/login"));
		}*/
		
/*		@WithMockUser(username = "0000000000", roles = "ADMIN")
		@Test
		void can_get_menu_html() throws Exception {
			
			String roles = authentication().getAuthorities().toString();
			assertThat(authentication().getName()).isEqualTo(PHONE);
			assertThat(roles).isEqualTo("[ROLE_ADMIN]");

			mockMVC.perform(get("/success").with(csrf()))
				.andExpect(status().isOk())
				.andExpect(authenticated())
				.andExpect(content().string(containsString("Welcome!")))
				.andExpect(content().string(containsString("Admin Adminov")))
				.andExpect(content().string(containsString("[ROLE_ADMIN]")))
		        .andExpect(model().size(1))
		        .andExpect(model().attribute("account", any(AccountResponse.class)))
		        .andExpect(view().name("auth/success"));
		}*/
		
/*		@Test
		void correct_user_creds() throws Exception {
			
			Account account = null;
			if(accountService instanceof AccountServiceJPA) {
				account = jpa.findByPhone(PHONE).get();
				account.setPassword(BCrypt.hashpw(account.getPassword(), BCrypt.gensalt(4)));
				jpa.saveAndFlush(account);
			}
			else if(accountService instanceof AccountServiceDAO) {
				account = dao.getAccount(PHONE);
				account.setPassword(BCrypt.hashpw(account.getPassword(), BCrypt.gensalt(4)));
				dao.updateAccount(account);
			}

			mockMVC.perform(formLogin("/auth").user("phone", PHONE).password("superadmin"))
				.andExpect(authenticated())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/success"));
		}*/
		
/*		@Test
		void wrong_user_creds() throws Exception {
			
			mockMVC.perform(formLogin("/auth").user("phone", "9999999999").password("localadmin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error=true"));
		}*/
		
/*		@WithMockUser(username = "9999999999")
		@Test
		void entity_not_found() throws Exception {
			
			String phone = authentication().getName();			
			assertThat(phone).isEqualTo("9999999999");
			
			mockMVC.perform(get("/success"))
				.andExpect(authenticated())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/home"));
		}*/
		
/*		@Test
		void unauthorized_denied() throws Exception {
			mockMVC.perform(get("/success")).andExpect(status().isUnauthorized());
			mockMVC.perform(post("/confirm")).andExpect(status().isForbidden());			
		}*/
		
		@Test
		void accepted_registration() throws Exception {
			
			greenMail.purgeEmailFromAllMailboxes();
			
			AccountRequest accountRequest = buildCorrectAccountRequest();			
			mockMVC.perform(post("/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(accountRequest)))
				.andExpect(status().isOk())
/*		        .andExpect(model().size(2))
		        .andExpect(model().attribute("account", any(AccountRequest.class)))
		        .andExpect(model().attribute("success", "Your account has been created"))
				.andExpect(view().name("auth/login"));*/
				.andExpect(content().string("Check your email"));
			
			Awaitility.await().untilAsserted(() -> {
				assertEquals(true, greenMail.isRunning());
				MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
				assertEquals(1, receivedMessage.getAllRecipients().length);
				assertEquals(accountRequest.getSurname().toLowerCase() + mailbox, receivedMessage.getAllRecipients()[0].toString());
				assertEquals("noreply@bankdemo.com", receivedMessage.getFrom()[0].toString());
				assertEquals("Confirm your registration", receivedMessage.getSubject());
				assertFalse(GreenMailUtil.getBody(receivedMessage).isEmpty());
				assertTrue(GreenMailUtil.getBody(receivedMessage)
						.startsWith("<a href='http://" + uri + ":" + port + path + "/activate/"));
				assertTrue(GreenMailUtil.getBody(receivedMessage)
						.endsWith("' target=\"_blank\">Activate your account</a>"));
				assertFalse(GreenMailUtil.getBody(receivedMessage).contains("Shit happens"));
			});
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
				.andExpect(jsonPath("$", hasItem("Please select your date of birth")))
				.andExpect(jsonPath("$.length()", equalTo(8)))
				.andExpect(jsonPath("$").isArray());
/*				.andExpect(model().size(1))
				.andExpect(model().attribute("account", any(AccountRequest.class)))
				.andExpect(model().hasErrors())
				.andExpect(view().name("auth/register"));*/

			accountRequest.setBirthday(LocalDate.now().plusYears(10L));			
			mockMVC.perform(post("/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(accountRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", hasItem("Birthday can't be future time")))
				.andExpect(jsonPath("$.length()", equalTo(8)))
				.andExpect(jsonPath("$").isArray());
/*				.andExpect(model().size(1))
				.andExpect(model().attribute("account", any(AccountRequest.class)))
				.andExpect(model().hasErrors())
				.andExpect(view().name("auth/register"));*/
			
			accountRequest = buildCorrectAccountRequest();
//			accountRequestDTO.setBirthday("2001-01-01");
			accountRequest.setBirthday(LocalDate.now().minusYears(10L));
			accountRequest.setPhone("0000000000");
			
			mockMVC.perform(post("/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(accountRequest)))
				.andExpect(status().isBadRequest())
//				.andExpect(model().size(1))
//				.andExpect(model().attribute("account", any(AccountRequest.class)))
				.andExpect(jsonPath("$", hasItem("You must be 18+ to register")))
				.andExpect(jsonPath("$", hasItem("This number is already in use")))
				.andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$").isArray());
//				.andExpect(view().name("auth/register"));
		}

		@Test
		void successful_activation() throws Exception {
			
			ReflectionTestUtils.setField(context.getBean(AuthController.class), "accounts", accounts);
			
			AccountRequest accountRequest = buildCorrectAccountRequest();
			mockMVC.perform(post("/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(accountRequest)))
				.andExpect(status().isOk())
				.andExpect(content().string("Check your email"));
			
			List<String> keys = new ArrayList<>(accounts.keySet());
			String tail = keys.get(0);
			
			mockMVC.perform(get("/activate/{tail}", tail).header(HttpHeaders.ORIGIN, externalURL))
				.andExpect(status().isCreated())
				.andExpect(content().string("Your account has been created"));
		}
		
		@Test
		void failed_activation() throws Exception {
			
			ReflectionTestUtils.setField(context.getBean(AuthController.class), "accounts", accounts);
			
			AccountRequest accountRequest = buildCorrectAccountRequest();
			mockMVC.perform(post("/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(accountRequest)))
				.andExpect(status().isOk())
				.andExpect(content().string("Check your email"));
			
			List<String> keys = new ArrayList<>(accounts.keySet());
			String tail = keys.get(0);
			accountRequest = accounts.get(tail);
			accountRequest.setPhone(PHONE);
			
			accounts.put(tail, accountRequest);
			mockMVC.perform(get("/activate/{tail}", tail).header(HttpHeaders.ORIGIN, externalURL))
				.andExpect(status().isConflict())
//				.andExpect(content().string("This number is already in use"));
				.andExpect(content().contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)));
			
			mockMVC.perform(get("/activate/{tail}", tail).header(HttpHeaders.ORIGIN, externalURL))
				.andExpect(status().isGone())
				.andExpect(content().string("Link has been expired, try to register again"));
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
			
			greenMail.purgeEmailFromAllMailboxes();
			
			hashPassword(PHONE, "superadmin");			
			LoginRequest loginRequest = new LoginRequest(PHONE, "superadmin");			
			mockMVC.perform(post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isString())
				.andExpect(content().string("Check your email"));
			
			Map<String, LoginRequest> map = cache.asMap();
			List<String> keys = new ArrayList<>(map.keySet());
			String code = keys.get(0);
			AccountDetails details = accountDetailsService.loadUserByUsername(loginRequest.getPhone());
//			cache.invalidateAll();
			
			Awaitility.await().untilAsserted(() -> {
				assertEquals(true, greenMail.isRunning());
				MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
				assertEquals(1, receivedMessage.getAllRecipients().length);
				assertEquals(details.getAccount().getEmail(), receivedMessage.getAllRecipients()[0].toString());
				assertEquals("noreply@bankdemo.com", receivedMessage.getFrom()[0].toString());
				assertEquals("Login verification code", receivedMessage.getSubject());
				assertFalse(GreenMailUtil.getBody(receivedMessage).isEmpty());
				assertTrue(GreenMailUtil.getBody(receivedMessage).equals(code));
			});
		}
		
		@Test
		void wrong_password() throws Exception {
			
			hashPassword(PHONE, "superadmin");
			LoginRequest loginRequest = new LoginRequest(PHONE, "localadmin");
			
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
					("Bad credentials", result.getResolvedException().getMessage()));
			}
			
			loginRequest = new LoginRequest(PHONE, "superadmin");
			mockMVC.perform(post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(loginRequest)))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string(containsString("User is disabled")))
				.andExpect(jsonPath("$").isString())
				.andExpect(result -> assertThat
					(result.getResolvedException() instanceof DisabledException).isTrue())
				.andExpect(result -> assertEquals
					("User is disabled", result.getResolvedException().getMessage()));
		}
		
		@Test
		void absent_creds() throws Exception {
			
			LoginRequest loginRequest = new LoginRequest("4444444444", "blackmamba");
			mockMVC.perform(post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(loginRequest)))
				.andExpect(status().isNotFound())
				.andExpect(content().string(containsString("User 4444444444 not found")))
				.andExpect(jsonPath("$").isString())
				.andExpect(result -> assertThat
					(result.getResolvedException() instanceof UsernameNotFoundException).isTrue())
				.andExpect(result -> assertEquals
					("User 4444444444 not found", result.getResolvedException().getMessage()));
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
		
	}
	
	@WithMockUser(username = "0000000000", roles = "ADMIN")
	@Nested
//	@Sql("/test-operations-h2.sql")
	@TestInstance(Lifecycle.PER_CLASS)
	class AdminControllerIT{
		
		@Autowired
		private TestRestTemplate restTemplate;
		
		@Autowired
		private DataSource dataSource;
		private ResourceDatabasePopulator populator;
		
		@BeforeAll
		void prepare() {		
			populator = new ResourceDatabasePopulator();
			populator.addScripts(new ClassPathResource("test-operations-h2.sql"));
			populator.addScripts(new ClassPathResource("test-auth-h2.sql"));
			populator.execute(dataSource);
		}
		
		@Value("${server.address}")
		private String uri;
		@Value("${local.server.port}")
		private int port;
		@Value("${server.servlet.context-path}")
		private String path;
		
	    @Autowired
		private Cache<String, LoginRequest> cache;	    
	    
		@RegisterExtension
		GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
		        .withConfiguration(GreenMailConfiguration.aConfig().withUser("irybov", "bankdemo"))
		        .withPerMethodLifecycle(false);
		
//		private final LoginRequest loginRequest = new LoginRequest("0000000000", "superadmin");
		
		private String generateJWT() throws Exception {
			
			final String PHONE = "0000000000";
			final LoginRequest loginRequest = new LoginRequest(PHONE, "superadmin");
			hashPassword(PHONE, "superadmin");
			cache.invalidateAll();
/*						
			hashPassword(loginRequest.getPhone(), loginRequest.getPassword());
			mockMVC.perform(post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isString())
				.andExpect(content().string("Check your email"));
*/			
			ResponseEntity<String> response = 
					restTemplate.postForEntity("/login", loginRequest, String.class);
			assertEquals(response.getStatusCode(), org.springframework.http.HttpStatus.OK);
			assertEquals(response.getBody(), "Check your email");
			
			Map<String, LoginRequest> map = cache.asMap();
			List<String> keys = new ArrayList<>(map.keySet());
			String code = keys.get(0);
//			MvcResult result = mockMVC.perform(get("/token")
//					.header(HttpHeaders.AUTHORIZATION, "OTP " + code))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$").isString())
//				.andReturn();
			
		    HttpHeaders headers = new HttpHeaders();
		    headers.add(HttpHeaders.AUTHORIZATION, "OTP " + code);
	        String url = "http://" + uri + ":" + port + path + "/token";
	        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
			response = restTemplate.exchange(
					uriBuilder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
			assertEquals(response.getStatusCode(), org.springframework.http.HttpStatus.OK);
		    assertEquals(response.getBody().getClass(), String.class);
			
//			return result.getResponse().getContentAsString();
			return response.getBody();
		}
		
		@Test
		void can_get_clients_list() throws Exception {
			
			MvcResult result = mockMVC.perform(get("/accounts"))
				.andExpect(request().asyncStarted())
				.andReturn();
					
			mockMVC.perform(asyncDispatch(result))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE));
		}
		
		@Disabled
		@Test
		void clients_not_found_exception() throws Exception {
			
			MvcResult result = mockMVC.perform(get("/accounts"))
				.andExpect(request().asyncStarted())
				.andReturn();
					
			mockMVC.perform(asyncDispatch(result))
				.andExpect(status().isInternalServerError());
		}
	    
		@Test
		void can_change_account_status() throws Exception {
			
			mockMVC.perform(patch("/accounts/{id}/status", "1").with(csrf()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("false")));
		}

		@Test
		void can_change_bill_status() throws Exception {
			
			mockMVC.perform(patch("/bills/{id}/status", "1").with(csrf()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("false")));
		}
				
		@Test
		void input_mismatch_exception() throws Exception {
			
/*			assertThatThrownBy(() -> mockMVC.perform(get("/accounts/search/{phone}", "XXL"))
					.andExpect(status().isInternalServerError()))
					.hasCause(new InputMismatchException("Phone number should be of 10 digits"));*/
			
			mockMVC.perform(get("/accounts/{phone}/search", "XXL"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.report").value("Phone number should be of 10 digits"));
		}
		
		@Test
		void account_not_found_exception() throws Exception {
			
			String wrong = "4444444444";
			mockMVC.perform(get("/accounts/{phone}/search", wrong))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.report").value("Account with phone " + wrong + " not found"));
		}
		
		@Test
		void can_get_account_info() throws Exception {
			
			String phone = authentication().getName();
			mockMVC.perform(get("/accounts/{phone}/search", phone))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andExpect(jsonPath("$.updatedAt").isNotEmpty())
				.andExpect(jsonPath("$.active").isBoolean())
				.andExpect(jsonPath("$.name").value("Admin"))
				.andExpect(jsonPath("$.surname").value("Adminov"))
				.andExpect(jsonPath("$.phone").value(phone))
				.andExpect(jsonPath("$.birthday").isNotEmpty())
				.andExpect(jsonPath("$.bills").isArray())
				.andExpect(jsonPath("$.bills.length()", is(2)));
		}

		@Test
		void can_get_operations_page() throws Exception {
/*			
			mockMVC.perform(get("/operations/{id}/pageable", "1")
							.param("minval", "99.99")
							.param("maxval", "500.01")
							.param("sort", "amount,asc")
							.param("sort", "id,desc")
							.param("page", "1")
							.param("size", "2")
					)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pageable").exists())
				.andExpect(jsonPath("$.sort").exists())
//				.andExpect(jsonPath("$['sort']['sorted']").value("true"))
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content.length()", is(1)))
				.andDo(print());
*/
		    HttpHeaders headers = new HttpHeaders();
		    String token = generateJWT();
		    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
	        String url = "http://" + uri + ":" + port + path + "/operations/1/pageable";
	        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url)
	          .queryParam("sort", "amount,asc")
	          .queryParam("sort", "id,desc")
	          .queryParam("page", 0)
	          .queryParam("size", 2);
			
	        ResponseEntity<Page<OperationResponse>> response = 
	        		restTemplate.exchange(uriBuilder.toUriString(),
	        				HttpMethod.GET, new HttpEntity<>(headers), 
	        				new ParameterizedTypeReference<Page<OperationResponse>>(){});
	        assertEquals(response.getStatusCode(), org.springframework.http.HttpStatus.OK);
	        assertEquals(response.getBody().getContent().size(), 2);
		}
		
		@Test
		void can_export_data_2_csv_file() throws Exception {
			
			MvcResult result = mockMVC.perform(get("/operations/{id}/print", "1"))
				.andExpect(request().asyncStarted())
				.andReturn();
				
			mockMVC.perform(asyncDispatch(result))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE));
		}
		
		@Test
		void data_on_fetch_exception() throws Exception {
			
			MvcResult result = mockMVC.perform(get("/operations/{id}/print", "0"))
				.andExpect(request().asyncStarted())
				.andReturn();
					
			mockMVC.perform(asyncDispatch(result))
				.andExpect(status().isInternalServerError())
				.andExpect(content().bytes(new byte[0]));
		}
		
		@AfterAll void clear() {
			populator.setScripts(new ClassPathResource("test-clean-data-h2.sql"));
			populator.execute(dataSource);
			populator = null;
		}
		
	}
	
//	@Sql(statements = "INSERT INTO bankdemo.bills(is_active, balance, currency, account_id)"
//				 +" "+"VALUES('1', '10.00', 'USD', '1');")
	@WithMockUser(username = "1111111111", roles = "CLIENT")
	@Nested
	class BankControllerIT{
		
		@Autowired
		private ObjectMapper mapper;
		@Autowired
		private TestRestTemplate testRestTemplate;
		
		private static final String PHONE = "1111111111";
		
		@Value("${server.address}")
		private String uri;
		@Value("${local.server.port}")
		private int port;
		@Value("${external.payment-service}")
		private String externalURL;
		
		@Test
		void can_get_client_info() throws Exception {
			
			mockMVC.perform(get("/accounts/{phone}", PHONE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.createdAt").isNotEmpty())
			.andExpect(jsonPath("$.updatedAt").isNotEmpty())
			.andExpect(jsonPath("$.active").isBoolean())
			.andExpect(jsonPath("$.name").value("Kae"))
			.andExpect(jsonPath("$.surname").value("Yukawa"))
			.andExpect(jsonPath("$.phone").value(PHONE))
			.andExpect(jsonPath("$.birthday").isNotEmpty())
			.andExpect(jsonPath("$.bills").isArray())
			.andExpect(jsonPath("$.bills.length()", is(0)));
		}
		
		@Test
		void check_security_restriction() throws Exception {
			
			mockMVC.perform(get("/accounts/{phone}", "5555555555"))
			.andExpect(status().isForbidden())
			.andExpect(result -> assertEquals("Security restricted information", 
					   result.getResolvedException().getMessage()));
		}
		
		@Test
		void can_create_new_bill() throws Exception {
			
			mockMVC.perform(post("/bills")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(new BillRequest(PHONE, "RUB"))))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.id").isNumber())
					.andExpect(jsonPath("$.createdAt").exists())
					.andExpect(jsonPath("$.updatedAt").isEmpty())
					.andExpect(jsonPath("$.active").value(true))
					.andExpect(jsonPath("$.balance").value("0.0"))
					.andExpect(jsonPath("$.currency").value("RUB"));
//					.andExpect(jsonPath("$.owner").exists());
		}
		
		@Test
		void bill_creation_failed() throws Exception {
			
			Assertions.assertThatThrownBy(() ->
			mockMVC.perform(post("/bills")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(new BillRequest(PHONE, "SCR"))))
				.andExpect(status().isBadRequest()))
				.hasCause(new RegistrationException("Wrong currency provided"));
		}	
		
		@Test
		void can_delete_own_bill() throws Exception {
			
			mockMVC.perform(delete("/bills/{id}", "1"))
					.andExpect(status().isOk());
		}
/*		
		@Test
		void can_get_payment_html() throws Exception {
			
			mockMVC.perform(post("/bills/operate").with(csrf())
											   .param("id", "1")
											   .param("action", "deposit")
											   .param("balance", "0.00")
						)
				.andExpect(status().isOk())
				.andExpect(model().size(3))
				.andExpect(model().attribute("id", "1"))
				.andExpect(model().attribute("action", "deposit"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(view().name("bill/payment"));
			
			mockMVC.perform(post("/bills/operate").with(csrf())
											   .param("id", "1")
											   .param("action", "withdraw")
											   .param("balance", "0.00")
						)
				.andExpect(status().isOk())
				.andExpect(model().size(3))
				.andExpect(model().attribute("id", "1"))
				.andExpect(model().attribute("action", "withdraw"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(view().name("bill/payment"));
		}

		@Test
		void can_get_transfer_html() throws Exception {
			
			mockMVC.perform(post("/bills/operate").with(csrf())
											   .param("id", "1")
											   .param("action", "transfer")
											   .param("balance", "0.00")
						)
				.andExpect(status().isOk())
				.andExpect(model().size(3))
				.andExpect(model().attribute("id", "1"))
				.andExpect(model().attribute("action", "transfer"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(view().name("bill/transfer"));
		}
		*/
		@Test
		void check_bill_owner() throws Exception {
			
			mockMVC.perform(get("/bills/{id}", "1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
				.andExpect(content().string(equalTo("Admin Adminov")));
			
/*		    HttpHeaders headers = new HttpHeaders();
		    headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
			String response = testRestTemplate.withBasicAuth(PHONE, "supervixen")
					.getForObject("/bills/validate/{id}", String.class, "1");
			assertThat(response).contains("Admin Adminov");*/
		}
		
		@Test
		void owner_not_found() throws Exception {
			
			mockMVC.perform(get("/bills/{id}", "4"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
				.andExpect(content().string(containsString("Target bill with id: 4 not found")));
			
			mockMVC.perform(patch("/bills/{id}", "1")
										.param("recipient", String.valueOf(777))
//										.param("id", "1")
										.param("action", "transfer")
										.param("balance", "0.00")
										.param("amount", "0.00")
					)
					.andExpect(status().isNotFound())
					.andExpect(content().string(containsString
							  ("Target bill with id: " + 777 + " not found")));
/*					.andExpect(model().size(4))
					.andExpect(model().attribute("id", 1))
					.andExpect(model().attribute("action", "transfer"))
					.andExpect(model().attribute("balance", "0.00"))
					.andExpect(model().attribute("message", 
							"Target bill with id: " + 777 + " not found"))
					.andExpect(view().name("bill/transfer"));*/
		}
		
		@Disabled
		@Test
		void wrong_format_input() throws Exception {
			
			mockMVC.perform(patch("/bills/{id}", "1")
										.param("recipient", "XXX")
//									    .param("id", "1")
									    .param("action", "transfer")
									    .param("balance", "10.00")
					)
					.andExpect(status().isBadRequest())
					.andExpect(model().size(4))
					.andExpect(model().attribute("id", 1))
					.andExpect(model().attribute("action", "transfer"))
					.andExpect(model().attribute("balance", "10.00"))
					.andExpect(model().attribute("message", "Please provide correct bill number"))
					.andExpect(view().name("bill/transfer"));
		}

/*		@Test
		void can_get_password_html() throws Exception {
			
			mockMVC.perform(get("/accounts/password/{phone}", PHONE))
			.andExpect(status().isOk())
			.andExpect(model().size(1))
			.andExpect(model().attribute("password", any(PasswordRequest.class)))
			.andExpect(view().name("account/password"));
		}*/
		
		@Test
		void success_password_change() throws Exception {
			
			PasswordRequest pwDTO = new PasswordRequest("supervixen", "japanrocks");			
			hashPassword(PHONE, "supervixen");
			
			mockMVC.perform(patch("/accounts/{phone}", PHONE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(pwDTO)))
					.andExpect(status().isOk())
					.andExpect(content().string(containsString("Password changed")));
/*					.andExpect(model().size(2))
					.andExpect(model().attributeExists("password"))
					.andExpect(model().attribute("success", "Password changed"))
					.andExpect(view().name("account/password"));*/
		}
		
		@Test
		void failure_password_change() throws Exception {
			
			PasswordRequest pwDTO = new PasswordRequest("superjapan", "vixenrocks");
			hashPassword(PHONE, "supervixen");
			
			mockMVC.perform(patch("/accounts/{phone}", PHONE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(pwDTO)))
					.andExpect(status().isConflict())
					.andExpect(content().string(containsString("Current password mismatch")));
/*					.andExpect(model().size(2))
					.andExpect(model().attributeExists("password"))
					.andExpect(model().attribute("message", "Current password mismatch"))
					.andExpect(view().name("account/password"));*/			
		}
		
		@Test
		void password_binding_errors() throws Exception {
			
			PasswordRequest pwDTO = new PasswordRequest("black", "white");
			
			mockMVC.perform(patch("/accounts/{phone}", PHONE)
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(pwDTO)))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(containsString
							("Old password should be 10-60 symbols length")))
					.andExpect(content().string(containsString
							("New password should be 10-60 symbols length")))
				    .andExpect(result -> 
				      	assertTrue(result.getResolvedException() 
				      		instanceof MethodArgumentNotValidException).isTrue());
/*					.andExpect(model().size(1))
					.andExpect(model().errorCount(2))
					.andExpect(model().attributeExists("password"))
					.andExpect(view().name("account/password"));*/			
		}
		
		@Test
		void wrong_bill_serial() throws Exception {
			
			wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/verify"))
					.willReturn(WireMock.aResponse()
					.withStatus(HttpStatus.NOT_FOUND_404)
					.withBody("No bill with serial 33 found")));
			
			mockMVC.perform(patch("/bills/{id}", "1")
//										.param("id", "1")
									    .param("action", "external")
									    .param("balance", "5.00")
									    .param("amount", "5.00")
									    .param("bank", "Penkov")
									    .param("recipient", "33")
					)
					.andExpect(status().isNotFound())
					.andExpect(content().string(containsString("No bill with serial 33 found")));
/*					.andExpect(model().size(4))
					.andExpect(model().attribute("id", 1))
					.andExpect(model().attribute("action", "external"))
					.andExpect(model().attribute("balance", "5.00"))
					.andExpect(model().attribute("message", "No bill with serial 33 found"))
					.andExpect(view().name("bill/external"));*/
			
			wireMockServer.verify(WireMock.exactly(1), 
					WireMock.postRequestedFor(WireMock.urlPathEqualTo("/verify")));			
		}
		
		@Test
		void wrong_bank_name() throws Exception {
			
			wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/verify"))
					.willReturn(WireMock.aResponse()
					.withStatus(HttpStatus.NOT_FOUND_404)
					.withBody("No bank with name Demo found")));
			
			mockMVC.perform(patch("/bills/{id}", "1")
//										.param("id", "1")
									    .param("action", "external")
									    .param("balance", "5.00")
									    .param("amount", "5.00")
									    .param("bank", "Demo")
									    .param("recipient", "22")
					)
					.andExpect(status().isNotFound())
					.andExpect(content().string(containsString("No bank with name Demo found")));
/*					.andExpect(model().size(4))
					.andExpect(model().attribute("id", 1))
					.andExpect(model().attribute("action", "external"))
					.andExpect(model().attribute("balance", "5.00"))
					.andExpect(model().attribute("message", "No bank with name Demo found"))
					.andExpect(view().name("bill/external"));*/
			
			wireMockServer.verify(WireMock.exactly(1), 
					WireMock.postRequestedFor(WireMock.urlPathEqualTo("/verify")));				
		}
		
		@Test
		void connection_failure() throws Exception {
			
			wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/verify"))
					.willReturn(WireMock.aResponse()
					.withStatus(HttpStatus.SERVICE_UNAVAILABLE_503)
					.withBody("Service is temporary unavailable")));
			
			mockMVC.perform(patch("/bills/{id}", "1")
//										.param("id", "1")
									    .param("action", "external")
									    .param("balance", "10.00")
									    .param("amount", "10.00")
									    .param("bank", "Penkov")
									    .param("recipient", "22")
					)
					.andExpect(status().isServiceUnavailable())
					.andExpect(content().string(containsString("Service is temporary unavailable")));
/*					.andExpect(model().size(4))
					.andExpect(model().attribute("id", 1))
					.andExpect(model().attribute("action", "external"))
					.andExpect(model().attribute("balance", "10.00"))
					.andExpect(model().attribute("message", "Service is temporary unavailable"))
					.andExpect(view().name("bill/external"));*/
			
			wireMockServer.verify(WireMock.exactly(1), 
					WireMock.postRequestedFor(WireMock.urlPathEqualTo("/verify")));				
		}
		
		@Test
		void successful_payment() throws Exception {
			
			wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/verify"))
					.willReturn(WireMock.aResponse()
					.withStatus(HttpStatus.OK_200)
					.withBody("Data has been verified")));
			
			mockMVC.perform(patch("/bills/{id}", "1")
//									    .param("id", "1")
									    .param("action", "deposit")
									    .param("balance", "100.00")
									    .param("amount", "100.00")
					)
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Done!")));
//					.andExpect(redirectedUrl("/accounts/show/" + PHONE));
			
			mockMVC.perform(patch("/bills/{id}", "1")
//									    .param("id", "1")
									    .param("action", "withdraw")
									    .param("balance", "10.00")
									    .param("amount", "10.00")
					)
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Done!")));
//					.andExpect(redirectedUrl("/accounts/show/" + PHONE));
			
			mockMVC.perform(patch("/bills/{id}", "1")
//										.param("id", "0")
										.param("action", "transfer")
										.param("balance", "10.00")
										.param("amount", "10.00")
										.param("recipient", "3")
					)
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Done!")));
//					.andExpect(redirectedUrl("/accounts/show/" + PHONE));
			
			mockMVC.perform(patch("/bills/{id}", "1")
//				    					.param("id", "1")
									    .param("action", "external")
									    .param("balance", "10.00")
									    .param("amount", "10.00")
									    .param("bank", "Penkov")
									    .param("recipient", "22")
					)
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Done!")));
//					.andExpect(flash().attribute("message", "Data has been verified"))
//					.andExpect(redirectedUrl("/accounts/show/" + PHONE));
			
			OperationRequest dto = new OperationRequest(777, 3, "USD", 0.01, "Demo");
			mockMVC.perform(post("/bills/external")
										.contentType(MediaType.APPLICATION_JSON)
										.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Successfully received")));
			
			dto = new OperationRequest(777, 2, "NOK", 0.01, "Demo");
		    HttpHeaders headers = new HttpHeaders();
		    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(dto), headers);
			ResponseEntity<String> response = testRestTemplate.postForEntity("/bills/external", 
					request, String.class);
			assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK_200);
			assertThat(response.getBody()).isEqualTo("Successfully received");
			
			wireMockServer.verify(WireMock.exactly(1), 
					WireMock.postRequestedFor(WireMock.urlPathEqualTo("/verify")));
		}
		
		@Test
		void zero_amount_exception() throws Exception {
			
			wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/verify"))
					.willReturn(WireMock.aResponse()
					.withStatus(HttpStatus.OK_200)
					.withBody("Data has been verified")));
			
			mockMVC.perform(patch("/bills/{id}", "1")
//								    .param("id", "1")
								    .param("action", "deposit")
								    .param("balance", "10.00")
								    .param("amount", "0.00")
				)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Amount of money should be higher than zero")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 1))
				.andExpect(model().attribute("action", "deposit"))
				.andExpect(model().attribute("balance", "10.00"))
				.andExpect(model().attribute("message", "Amount of money should be higher than zero"))
				.andExpect(view().name("bill/payment"));*/
			
			mockMVC.perform(patch("/bills/{id}", "1")
//								    .param("id", "1")
								    .param("action", "withdraw")
								    .param("balance", "10.00")
								    .param("amount", "0.00")
				)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Amount of money should be higher than zero")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 1))
				.andExpect(model().attribute("action", "withdraw"))
				.andExpect(model().attribute("balance", "10.00"))
				.andExpect(model().attribute("message", "Amount of money should be higher than zero"))
				.andExpect(view().name("bill/payment"));*/
			
			mockMVC.perform(patch("/bills/{id}", "1")
//									.param("id", "1")
								    .param("action", "external")
								    .param("balance", "10.00")
								    .param("amount", "0.00")
								    .param("bank", "Penkov")
								    .param("recipient", "22")
				)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Amount of money should be higher than zero")));
/*				.andExpect(model().attribute("id", 1))
				.andExpect(model().attribute("action", "external"))
				.andExpect(model().attribute("balance", "10.00"))
				.andExpect(model().attribute("message", "Amount of money should be higher than zero"))
				.andExpect(view().name("bill/external"));*/
			
			OperationRequest dto = new OperationRequest(777, 2, "SEA", 0.00, "Demo");
			mockMVC.perform(post("/bills/external").header("Origin", externalURL)
													.contentType(MediaType.APPLICATION_JSON)
													.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString
						("Amount of money should be higher than zero")));
			
			wireMockServer.verify(WireMock.exactly(1), 
					WireMock.postRequestedFor(WireMock.urlPathEqualTo("/verify")));
		}
		
		@Test
		void negative_balance_exception() throws Exception {
			
			wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/verify"))
					.willReturn(WireMock.aResponse()
					.withStatus(HttpStatus.OK_200)
					.withBody("Data has been verified")));
			
			mockMVC.perform(patch("/bills/{id}", "1")
//								    .param("id", "1")
								    .param("action", "withdraw")
								    .param("balance", "10.00")
								    .param("amount", "10.01")
				)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Not enough money to complete operation")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 1))
				.andExpect(model().attribute("action", "withdraw"))
				.andExpect(model().attribute("balance", "10.00"))
				.andExpect(model().attribute("message", "Not enough money to complete operation"))
				.andExpect(view().name("bill/payment"));*/
			
			mockMVC.perform(patch("/bills/{id}", "1")
//									.param("id", "1")
								    .param("action", "external")
								    .param("balance", "10.00")
								    .param("amount", "10.01")
								    .param("bank", "Penkov")
								    .param("recipient", "22")
				)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Not enough money to complete operation")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 1))
				.andExpect(model().attribute("action", "external"))
				.andExpect(model().attribute("balance", "10.00"))
				.andExpect(model().attribute("message", "Not enough money to complete operation"))
				.andExpect(view().name("bill/external"));*/
			
			wireMockServer.verify(WireMock.exactly(1), 
					WireMock.postRequestedFor(WireMock.urlPathEqualTo("/verify")));
		}
		
		@Test
		void same_bill_id_exception() throws Exception {
			
			wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/verify"))
					.willReturn(WireMock.aResponse()
					.withStatus(HttpStatus.OK_200)
					.withBody("Data has been verified")));
			
			mockMVC.perform(patch("/bills/{id}", "1")
				    			.param("recipient", "1")
//							    .param("id", "1")
							    .param("action", "transfer")
							    .param("balance", "10.00")
							    .param("amount", "10.00")
			)
			.andExpect(status().isInternalServerError())
			.andExpect(content().string(containsString
					("Source and target bills should not be the same")));
/*			.andExpect(model().size(4))
			.andExpect(model().attribute("id", 1))
			.andExpect(model().attribute("action", "transfer"))
			.andExpect(model().attribute("balance", "10.00"))
			.andExpect(model().attribute("message", "Source and target bills should not be the same"))
			.andExpect(view().name("bill/transfer"));*/
			
			mockMVC.perform(patch("/bills/{id}", "1")
//								.param("id", "1")
							    .param("action", "external")
							    .param("balance", "10.00")
							    .param("amount", "5.00")
							    .param("bank", "Penkov")
							    .param("recipient", "1")
			)
			.andExpect(status().isInternalServerError())
			.andExpect(content().string(containsString
					("Source and target bills should not be the same")));
/*			.andExpect(model().size(4))
			.andExpect(model().attribute("id", 1))
			.andExpect(model().attribute("action", "external"))
			.andExpect(model().attribute("balance", "10.00"))
			.andExpect(model().attribute("message", "Source and target bills should not be the same"))
			.andExpect(view().name("bill/external"));*/
			
			OperationRequest dto = new OperationRequest(777, 777, "SEA", 0.01, "Demo");
			mockMVC.perform(post("/bills/external").header("Origin", externalURL)
													.contentType(MediaType.APPLICATION_JSON)
													.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Source and target bills should not be the same")));
			
			wireMockServer.verify(WireMock.exactly(1), 
					WireMock.postRequestedFor(WireMock.urlPathEqualTo("/verify")));
		}
		
		@Test
		void currency_mismatch_exception() throws Exception {
			
			wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/verify"))
					.willReturn(WireMock.aResponse()
					.withStatus(HttpStatus.BAD_REQUEST_400)
					.withBody("Wrong currency type NOK for the target bill 22")));
			
			mockMVC.perform(patch("/bills/{id}", "1")
										.param("recipient", "2")
//										.param("id", "1")
										.param("action", "transfer")
										.param("balance", "10.00")
										.param("amount", "10.00")
					)
					.andExpect(status().isInternalServerError())
					.andExpect(content().string(containsString
							("Wrong currency type of the target bill")));
/*					.andExpect(model().size(4))
					.andExpect(model().attribute("id", 1))
					.andExpect(model().attribute("action", "transfer"))
					.andExpect(model().attribute("balance", "10.00"))
					.andExpect(model().attribute("message", "Wrong currency type of the target bill"))
					.andExpect(view().name("bill/transfer"));*/
			
			mockMVC.perform(patch("/bills/{id}", "1")
//										.param("id", "1")
									    .param("action", "external")
									    .param("balance", "10.00")
									    .param("amount", "0.01")
									    .param("bank", "Penkov")
									    .param("recipient", "22")
					)
					.andExpect(status().isBadRequest())
					.andExpect(content().string(containsString
							("Wrong currency type NOK for the target bill 22")));
/*					.andExpect(model().size(4))
					.andExpect(model().attribute("id", 1))
					.andExpect(model().attribute("action", "external"))
					.andExpect(model().attribute("balance", "10.00"))
					.andExpect(model().attribute("message", "Wrong currency type NOK for the target bill 22"))
					.andExpect(view().name("bill/external"));*/
			
			OperationRequest dto = new OperationRequest(777, 2, "AUD", 0.01, "Demo");
			mockMVC.perform(post("/bills/external").header("Origin", externalURL)
													.contentType(MediaType.APPLICATION_JSON)
													.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Wrong currency type of the target bill")));
			
			wireMockServer.verify(WireMock.exactly(1), 
					WireMock.postRequestedFor(WireMock.urlPathEqualTo("/verify")));
		}
		
		@Test
		void constraint_violation_exception() throws Exception {
			
			OperationRequest dto = new OperationRequest(1_000_000_000, -1, "yuan", -0.01, "Demo");
			mockMVC.perform(post("/bills/external").header("Origin", externalURL)
													.contentType(MediaType.APPLICATION_JSON)
													.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("Sender's bill number should be less than 10 digits length")))
				.andExpect(content().string(containsString("Recepient's bill number should be positive")))
				.andExpect(content().string(containsString("Currency code should be 3 capital characters length")))
				.andExpect(content().string(containsString("Amount of money should be higher than zero")));
			
			dto = new OperationRequest(null, null, " ", null, null);
			mockMVC.perform(post("/bills/external").header("Origin", externalURL)
													.contentType(MediaType.APPLICATION_JSON)
													.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("Sender must not be null")))
				.andExpect(content().string(containsString("Recepient must not be null")))
				.andExpect(content().string(containsString("Currency must not be blank")))
				.andExpect(content().string(containsString("Amount must not be null")))
				.andExpect(content().string(containsString("Bank name must not be blank")));
		}
		
		@Test
		void establish_emitter() throws Exception {
//			mockMVC.perform(get("/bills/notify")).andExpect(status().isCreated());
			mockMVC.perform(get("/bills/notify")).andExpect(status().isOk());
/*			ResponseBodyEmitter emitter = testRestTemplate.getForObject(("/bills/notify"), 
					ResponseBodyEmitter.class);
			assertThat(emitter).isNotNull();*/
		}
		
		@WithAnonymousUser
		@Test
		void check_cors_and_xml_support() throws Exception {
			
			mockMVC.perform(options("/bills/external")
					.header(HttpHeaders.ORIGIN, "http://localhorse:4567")
					.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, RequestMethod.OPTIONS.name())
					)
				.andExpect(status().isForbidden());
			
			mockMVC.perform(options("/bills/external")
					.header(HttpHeaders.ORIGIN, externalURL)
					.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, RequestMethod.OPTIONS.name())
					)
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, externalURL))
				.andExpect(header().stringValues(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, 
						RequestMethod.OPTIONS.name() + "," + RequestMethod.POST.name()))
				.andExpect(status().isOk());
			
			mockMVC.perform(post("/bills/external").header(HttpHeaders.ORIGIN, externalURL)
													.contentType(MediaType.APPLICATION_XML))
				.andExpect(status().isBadRequest());
			
			mockMVC.perform(post("/bills/external")
					.header(HttpHeaders.ORIGIN, "http://"+uri+":"+port)
					.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, RequestMethod.POST.name())
													.contentType(MediaType.APPLICATION_XML))
				.andExpect(status().isForbidden());
		
			mockMVC.perform(post("/bills/external").header(HttpHeaders.ORIGIN, externalURL))
				.andExpect(status().isUnsupportedMediaType());
			
			XmlMapper xmlMapper = new XmlMapper();
			OperationRequest dto = new OperationRequest(777, 3, "USD", 0.01, "Demo");
			mockMVC.perform(post("/bills/external").header(HttpHeaders.ORIGIN, externalURL)
													.accept(MediaType.APPLICATION_XML)
													.contentType(MediaType.APPLICATION_XML)
													.content(xmlMapper.writeValueAsString(dto))
							)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
				.andExpect(content().string(containsString("Successfully received")));
		}
				
		@AfterEach
		void tear_down() {
			wireMockServer.resetAll();
		}
		
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//	@WithUserDetails("0000000000")
	@Nested
	class MegaControllerIT{
		
	    @Autowired
	    ApplicationContext context;		
	    @Autowired
		private Cache<String, LoginRequest> cache;
	    
		@RegisterExtension
		GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
		        .withConfiguration(GreenMailConfiguration.aConfig().withUser("irybov", "bankdemo"))
		        .withPerMethodLifecycle(false);
		
		private static final String PHONE = "0000000000";
		
		private String generateJWT(LoginRequest loginRequest) throws Exception {
			
			cache.invalidateAll();
			
			hashPassword(loginRequest.getPhone(), loginRequest.getPassword());
			mockMVC.perform(post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isString())
				.andExpect(content().string("Check your email"));
			
			Map<String, LoginRequest> map = cache.asMap();
			List<String> keys = new ArrayList<>(map.keySet());
			String code = keys.get(0);
			MvcResult result = mockMVC.perform(get("/token")
					.header(HttpHeaders.AUTHORIZATION, "OTP " + code))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isString())
				.andReturn();
						
			return result.getResponse().getContentAsString();
		}
		
		@Test
		void can_change_implementation() throws Exception {
			
			String token = generateJWT(new LoginRequest(PHONE, "superadmin"));

			AccountDetailsService details = (AccountDetailsService) context.getBean("accountDetailsService");
			LoginListener listener = (LoginListener) context.getBean("loginListener");
			UnlockService unlock = (UnlockService) context.getBean("unlockService");
			
			String impl = "DAO";			
			mockMVC.perform(put("/control").header(
					HttpHeaders.AUTHORIZATION, "Bearer " + token).param("impl", impl))
				.andExpect(status().isOk())
				.andExpect(content()
					.string(containsString("Services impementation has been switched to " + impl)));			
			assertThat(context.getBean("accountServiceAlias")).isInstanceOf(AccountServiceDAO.class);
			assertThat(context.getBean("billServiceAlias")).isInstanceOf(BillServiceDAO.class);
			assertThat(context.getBean("operationServiceAlias")).isInstanceOf(OperationServiceDAO.class);
			assertThat(details.getImpl()).isEqualTo("DAO");
			assertThat(listener.getImpl()).isEqualTo("DAO");
			assertThat(unlock.getImpl()).isEqualTo("DAO");
			
			impl = "JPA";
			mockMVC.perform(put("/control").header(
					HttpHeaders.AUTHORIZATION, "Bearer " + token).param("impl", impl))
				.andExpect(status().isOk())
				.andExpect(content()
					.string(containsString("Services impementation has been switched to " + impl)));
			assertThat(context.getBean("accountServiceAlias")).isInstanceOf(AccountServiceJPA.class);
			assertThat(context.getBean("billServiceAlias")).isInstanceOf(BillServiceJPA.class);
			assertThat(context.getBean("operationServiceAlias")).isInstanceOf(OperationServiceJPA.class);
			assertThat(details.getImpl()).isEqualTo("JPA");
			assertThat(listener.getImpl()).isEqualTo("JPA");
			assertThat(unlock.getImpl()).isEqualTo("JPA");
		}
				
		@Test
		void wrong_implementation_type() throws Exception {
						
			String token = generateJWT(new LoginRequest(PHONE, "superadmin"));
			
			String impl = "XXX";
			mockMVC.perform(put("/control").header(
					HttpHeaders.AUTHORIZATION, "Bearer " + token).param("impl", impl))
				.andExpect(status().isBadRequest())
				.andExpect(content()
				.string(containsString("Wrong implementation type " + impl + " specified, retry")));
		}
		
//		@WithUserDetails("1111111111")
		@Test
		void credentials_forbidden() throws Exception {
			
			String token = generateJWT(new LoginRequest("1111111111", "supervixen"));
			
	        mockMVC.perform(put("/control").header(
	        		HttpHeaders.AUTHORIZATION, "Bearer " + token).param("impl", "XXX"))
				.andExpect(status().isForbidden());
		}
		
	}
		
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class CustomFiltersIT{
		
	    @Autowired
		private Cache<String, LoginRequest> cache;	    
	    
		@RegisterExtension
		GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
		        .withConfiguration(GreenMailConfiguration.aConfig().withUser("irybov", "bankdemo"))
		        .withPerMethodLifecycle(false);
		
		private static final String PHONE = "1111111111";
		private final LoginRequest loginRequest = new LoginRequest(PHONE, "supervixen");
		
		private String generateJWT() throws Exception {
			
			cache.invalidateAll();
						
			hashPassword(loginRequest.getPhone(), loginRequest.getPassword());
			mockMVC.perform(post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isString())
				.andExpect(content().string("Check your email"));
			
			Map<String, LoginRequest> map = cache.asMap();
			List<String> keys = new ArrayList<>(map.keySet());
			String code = keys.get(0);
			MvcResult result = mockMVC.perform(get("/token")
					.header(HttpHeaders.AUTHORIZATION, "OTP " + code))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isString())
				.andReturn();
			
			return result.getResponse().getContentAsString();
		}
		
		@Test
		void incorrect_or_abscent_bearer() throws Exception {
			
			String token = generateJWT();
	        mockMVC.perform(put("/control").header(HttpHeaders.AUTHORIZATION, "Bear " + token))
				.andExpect(status().isForbidden());
	        
	        mockMVC.perform(put("/control"))
				.andExpect(status().isForbidden());
		}

		@Test
		void corrupted_or_abscent_token() throws Exception {
			
			String token = generateJWT();
		    mockMVC.perform(put("/control").header(
		    		HttpHeaders.AUTHORIZATION, "Bearer " + token + "fake"))
//		    	.andExpect(jsonPath("$").isString())
		    	.andExpect(content().string(containsString("Invalid token provided")))
				.andExpect(error -> assertThat
						(error.getResolvedException() instanceof AuthenticationException).isTrue())
				.andExpect(status().isExpectationFailed());			
			
		    mockMVC.perform(put("/control").header(HttpHeaders.AUTHORIZATION, "Bearer "))
//	    		.andExpect(jsonPath("$").isString())
	    		.andExpect(content().string(containsString("No token provided with Bearer")))
				.andExpect(error -> assertThat
						(error.getResolvedException() instanceof AuthenticationException).isTrue())
				.andExpect(status().isExpectationFailed());
		}
		
		@Test
		void wrong_request_method() throws Exception {
			
			Assertions.assertThatThrownBy(() ->
			mockMVC.perform(put("/token").header(HttpHeaders.AUTHORIZATION, "OTP " + "1234"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(result -> assertThat
					(result.getResolvedException() instanceof AuthenticationServiceException).isTrue())
				.andExpect(result -> assertEquals
					("Authentication method not supported: " + result.getRequest().getMethod(), 
								result.getResolvedException().getMessage())));
		}
		
		@Test
		void absent_otp_header() throws Exception {
			
			Assertions.assertThatThrownBy(() ->
			mockMVC.perform(post("/token"))
				.andExpect(status().isExpectationFailed())
				.andExpect(result -> assertThat
					(result.getResolvedException() instanceof InsufficientAuthenticationException).isTrue())
				.andExpect(result -> assertEquals
					("No OTP header present", result.getResolvedException().getMessage())));
		}
		
		@Test
		void empty_code_provided() throws Exception {
			
			Assertions.assertThatThrownBy(() ->
			mockMVC.perform(post("/token").header(HttpHeaders.AUTHORIZATION, "OTP " + " "))
				.andExpect(status().isExpectationFailed())
				.andExpect(result -> assertThat
					(result.getResolvedException() instanceof InsufficientAuthenticationException).isTrue())
				.andExpect(result -> assertEquals
					("No code provided with header", result.getResolvedException().getMessage())));
		}
		
		@Test
		void invalid_code_format() throws Exception {
			
			Assertions.assertThatThrownBy(() ->
			mockMVC.perform(post("/token").header(HttpHeaders.AUTHORIZATION, "OTP " + "xyz"))
				.andExpect(status().isExpectationFailed())
				.andExpect(result -> assertThat
					(result.getResolvedException() instanceof BadCredentialsException).isTrue())
				.andExpect(result -> assertEquals
					("Invalid format of code", result.getResolvedException().getMessage())));
		}
		
		@Test
		void wrong_or_expired_code() throws Exception {
			
			Assertions.assertThatThrownBy(() ->
			mockMVC.perform(post("/token").header(HttpHeaders.AUTHORIZATION, "OTP " + "1234"))
				.andExpect(status().isExpectationFailed())
				.andExpect(result -> assertThat
					(result.getResolvedException() instanceof AuthenticationCredentialsNotFoundException).isTrue())
				.andExpect(result -> assertEquals
					("Wrong or expired code", result.getResolvedException().getMessage())));
			
			hashPassword(loginRequest.getPhone(), loginRequest.getPassword());
			mockMVC.perform(post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isString())
				.andExpect(content().string("Check your email"));
			
			Map<String, LoginRequest> map = cache.asMap();
			List<String> keys = new ArrayList<>(map.keySet());
			String code = keys.get(0);
			cache.invalidate(code);
			
			Assertions.assertThatThrownBy(() ->
			mockMVC.perform(post("/token").header(HttpHeaders.AUTHORIZATION, "OTP " + code))
				.andExpect(status().isExpectationFailed())
				.andExpect(result -> assertThat
					(result.getResolvedException() instanceof AuthenticationCredentialsNotFoundException).isTrue())
				.andExpect(result -> assertEquals
					("Wrong or expired code", result.getResolvedException().getMessage())));
		}
		
	}
	
	@AfterAll
	static void clear() {
		wireMockServer.stop();
	}
	
}
