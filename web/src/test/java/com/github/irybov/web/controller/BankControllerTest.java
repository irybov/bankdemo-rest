package com.github.irybov.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
//import java.util.ArrayList;
//import java.util.Currency;
//import java.util.HashSet;
//import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.persistence.EntityNotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
//import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
//import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.irybov.service.dto.AccountResponse;
import com.github.irybov.service.dto.BillRequest;
import com.github.irybov.service.dto.BillResponse;
import com.github.irybov.service.dto.OperationRequest;
import com.github.irybov.service.dto.PasswordRequest;
import com.github.irybov.database.entity.Account;
import com.github.irybov.database.entity.Bill;
import com.github.irybov.database.entity.Operation;
import com.github.irybov.service.exception.PaymentException;
import com.github.irybov.service.exception.RegistrationException;
import com.github.irybov.service.mapper.AccountMapper;
import com.github.irybov.service.mapper.AccountMapperImpl;
import com.github.irybov.service.mapper.BillMapper;
import com.github.irybov.service.mapper.BillMapperImpl;
import com.github.irybov.service.mapper.CycleAvoidingMappingContext;
import com.github.irybov.service.security.AccountDetailsService;
import com.github.irybov.service.service.AccountService;
import com.github.irybov.service.service.BillService;
import com.github.irybov.service.service.OperationService;
import com.github.irybov.web.config.CaffeineConfig;
import com.github.irybov.web.config.SecurityBeans;
import com.github.irybov.web.config.SecurityConfig;
import com.github.irybov.web.controller.BankController;
import com.github.irybov.web.security.JWTUtility;

@WithMockUser(username = "4444444444", roles = "CLIENT")
@WebMvcTest(BankController.class)
@Import(value = {SecurityConfig.class, SecurityBeans.class, CaffeineConfig.class, 
			BillMapperImpl.class, AccountMapperImpl.class})
class BankControllerTest {

	@MockBean
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	@MockBean
	@Qualifier("billServiceAlias")
	private BillService billService;
	@MockBean
	@Qualifier("operationServiceAlias")
	private OperationService operationService;
	@MockBean
	private UserDetailsService accountDetailsService;
	@MockBean
	private JWTUtility jwtUtility;
	@Autowired
	private MockMvc mockMVC;
	@Autowired
	private ObjectMapper mapper;
	@MockBean
	private Executor executorService;
	@MockBean
	private RestTemplate restTemplate;
	
	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	private String phone;
	
	private Set<Currency> currencies;
	private static Account entity;
	
	private static Operation operation;	
	private static Operation.OperationBuilder builder;
	private static Bill bill;
	
//	private static ModelMapper modelMapper;
	private static AccountMapper accountMapper;
	private static BillMapper billMapper;
	
	@Value("${server.address}")
	private String uri;
	@Value("${server.port}")
	private int port;
	@Value("${external.payment-service}")
	private String externalURL;
	
	@Autowired
	ApplicationContext context;

	@BeforeAll
	static void prepare() {
		
		entity = new Account
				("Kylie", "Bunbury", "4444444444", "bunbury@greenmail.io", LocalDate.of(1989, 01, 30), "blackmamba", true);
		entity.setCreatedAt(OffsetDateTime.now());
		entity.setUpdatedAt(OffsetDateTime.now());
		entity.setId(0);
		
		operation = new Operation();
		builder = mock(Operation.OperationBuilder.class, Mockito.RETURNS_SELF);
		bill = new Bill("SEA", true, entity);
		
//		modelMapper = new ModelMapper();
		accountMapper = Mappers.getMapper(AccountMapper.class);
		billMapper = Mappers.getMapper(BillMapper.class);
	}
	
	@BeforeEach
	void set_up() {
		
		currencies = new HashSet<>();
		Currency usd = Currency.getInstance("USD");
		currencies.add(usd);
		Currency eur = Currency.getInstance("EUR");
		currencies.add(eur);
		Currency gbp = Currency.getInstance("GBP");
		currencies.add(gbp);
		Currency rub = Currency.getInstance("RUB");
		currencies.add(rub);
		
		ReflectionTestUtils.setField(context.getBean(BankController.class), "currencies", currencies);
		
		phone = authentication().getName();
	}
	
	@Test
	void can_get_client_info() throws Exception {

//		AccountResponse account = modelMapper.map(entity, AccountResponse.class);
		AccountResponse account = accountMapper.toDTO(entity, new CycleAvoidingMappingContext());
		List<BillResponse> bills = new ArrayList<>();
		Bill bill = new Bill("SEA", true, entity);
//		bill.setOwner(entity);
//		bills.add(modelMapper.map(bill, BillResponse.class));
		bills.add(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));
		account.setBills(bills);
		
//		when(accountService.getAccountDTO(phone)).thenReturn(account);
//		when(accountService.getBills(account.getId())).thenReturn(bills);
		when(accountService.getFullDTO(phone)).thenReturn(account);
		
		mockMVC.perform(get("/accounts/{phone}", phone))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.createdAt").isNotEmpty())
			.andExpect(jsonPath("$.updatedAt").isNotEmpty())
			.andExpect(jsonPath("$.active").isBoolean())
			.andExpect(jsonPath("$.name").value("Kylie"))
			.andExpect(jsonPath("$.surname").value("Bunbury"))
			.andExpect(jsonPath("$.phone").value(phone))
			.andExpect(jsonPath("$.birthday").isNotEmpty())
			.andExpect(jsonPath("$.bills").isArray())
			.andExpect(jsonPath("$.bills.length()", is(1)));
		
//		verify(accountService).getAccountDTO(phone);
//		verify(accountService).getBills(account.getId());
		verify(accountService).getFullDTO(phone);
	}
	
	@Test
	void check_security_restriction() throws Exception {
		
		mockMVC.perform(get("/accounts/{phone}", "5555555555"))
			.andExpect(status().isForbidden())
			.andExpect(result -> assertEquals("Security restricted information", 
					   result.getResolvedException().getMessage()));
		
		verify(accountService, never()).getFullDTO(any(String.class));
	}
	
	@Test
	void can_create_new_bill() throws Exception {
		
		Bill bill = new Bill("RUB", true, entity);
		bill.setBalance(BigDecimal.valueOf(0.00));
		bill.setCreatedAt(OffsetDateTime.now());
		bill.setUpdatedAt(OffsetDateTime.now());
		bill.setId(0);
		when(accountService.addBill(any(BillRequest.class)))
//			.thenReturn(modelMapper.map(bill, BillResponse.class));
			.thenReturn(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));
		
		mockMVC.perform(post("/bills")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new BillRequest(phone, "RUB"))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").exists())
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.updatedAt").exists())
			.andExpect(jsonPath("$.active").value(true))
			.andExpect(jsonPath("$.balance").value("0.0"))
			.andExpect(jsonPath("$.currency").value("RUB"));
//			.andExpect(jsonPath("$.owner").exists());
		
		verify(accountService).addBill(any(BillRequest.class));
	}
	
	@Test
	void bill_creation_failed() throws Exception {
		
		Assertions.assertThatThrownBy(() ->
		mockMVC.perform(post("/bills")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new BillRequest(phone, "NOK"))))
			.andExpect(status().isBadRequest()))
			.hasCause(new RegistrationException("Wrong currency provided"));
		
		verify(accountService, never()).addBill(any(BillRequest.class));
	}
	
	@Test
	void can_delete_own_bill() throws Exception {
		
		doNothing().when(billService).deleteBill(anyInt());
		
		mockMVC.perform(delete("/bills/{id}", "0"))
			.andExpect(status().isOk());
		
		verify(billService).deleteBill(anyInt());
	}
/*	
	@Test
	void can_get_payment_html() throws Exception {
		
		mockMVC.perform(post("/bills/operate").with(csrf())
										   .param("id", "0")
										   .param("action", "deposit")
										   .param("balance", "0.00")
					)
			.andExpect(status().isOk())
			.andExpect(model().size(3))
			.andExpect(model().attribute("id", "0"))
			.andExpect(model().attribute("action", "deposit"))
			.andExpect(model().attribute("balance", "0.00"))
			.andExpect(view().name("bill/payment"));
		
		mockMVC.perform(post("/bills/operate").with(csrf())
										   .param("id", "0")
										   .param("action", "withdraw")
										   .param("balance", "0.00")
					)
			.andExpect(status().isOk())
			.andExpect(model().size(3))
			.andExpect(model().attribute("id", "0"))
			.andExpect(model().attribute("action", "withdraw"))
			.andExpect(model().attribute("balance", "0.00"))
			.andExpect(view().name("bill/payment"));
	}

	@Test
	void can_get_transfer_html() throws Exception {
		
		mockMVC.perform(post("/bills/operate").with(csrf())
										   .param("id", "0")
										   .param("action", "transfer")
										   .param("balance", "0.00")
					)
			.andExpect(status().isOk())
			.andExpect(model().size(3))
			.andExpect(model().attribute("id", "0"))
			.andExpect(model().attribute("action", "transfer"))
			.andExpect(model().attribute("balance", "0.00"))
			.andExpect(view().name("bill/transfer"));
	}
	
	@Test
	void can_get_external_html() throws Exception {
		
		mockMVC.perform(post("/bills/operate").with(csrf())
										   .param("id", "0")
										   .param("action", "external")
										   .param("balance", "0.00")
					)
			.andExpect(status().isOk())
			.andExpect(model().size(3))
			.andExpect(model().attribute("id", "0"))
			.andExpect(model().attribute("action", "external"))
			.andExpect(model().attribute("balance", "0.00"))
			.andExpect(view().name("bill/external"));
	}
	*/
	@Test
	void check_bill_owner() throws Exception {
		
		Bill bill = new Bill("SEA", true, entity);
//		bill.setOwner(entity);
		
		when(billService.getBillDTO(anyInt()))
//			.thenReturn(modelMapper.map(bill, BillResponse.class));
			.thenReturn(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));
		
		mockMVC.perform(get("/bills/{id}", "4"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
			.andExpect(content().string(equalTo
					  (bill.getOwner().getName() + " " + bill.getOwner().getSurname())));
		
		verify(billService).getBillDTO(anyInt());
	}
	
	@Test
	void owner_not_found() throws Exception {
		
		Random random = new Random();
		int id = random.nextInt(Byte.MAX_VALUE);
		when(billService.getBillDTO(id)).thenThrow(new EntityNotFoundException
				("Target bill with id: " + id + " not found"));
		
		mockMVC.perform(get("/bills/{id}", String.valueOf(id)))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
			.andExpect(content().string(containsString
					  ("Target bill with id: " + id + " not found")));
		
		mockMVC.perform(patch("/bills/{id}", "0")
														.param("recipient", String.valueOf(id))
//														.param("id", "0")
														.param("action", "transfer")
														.param("balance", "0.00")
														.param("amount", "0.00")
					)
			.andExpect(status().isNotFound())
			.andExpect(content().string(containsString
					  ("Target bill with id: " + id + " not found")));
/*			.andExpect(model().size(4))
			.andExpect(model().attribute("id", 0))
			.andExpect(model().attribute("action", "transfer"))
			.andExpect(model().attribute("balance", "0.00"))
			.andExpect(model().attribute("message", "Target bill with id: " + id + " not found"))
			.andExpect(view().name("bill/transfer"));*/
		
		verify(billService, times(2)).getBillDTO(id);
	}
	
	@Disabled
	@Test
	void wrong_format_input() throws Exception {
		
		mockMVC.perform(patch("/bills/{id}", "0")
														.param("recipient", "XXX")
//													    .param("id", "0")
													    .param("action", "transfer")
													    .param("balance", "0.00")
					)
			.andExpect(status().isBadRequest())
			.andExpect(model().size(4))
			.andExpect(model().attribute("id", 0))
			.andExpect(model().attribute("action", "transfer"))
			.andExpect(model().attribute("balance", "0.00"))
			.andExpect(model().attribute("message", "Please provide correct bill number"))
			.andExpect(view().name("bill/transfer"));
	}
/*
	@Test
	void can_get_password_html() throws Exception {
		
		mockMVC.perform(get("/accounts/password/{phone}", phone))
			.andExpect(status().isOk())
			.andExpect(model().size(1))
			.andExpect(model().attributeExists("password"))
			.andExpect(model().attribute("password", any(PasswordRequest.class)))
			.andExpect(view().name("account/password"));
	}
	*/
	@Test
	void success_password_change() throws Exception {
		
		PasswordRequest pwDTO = new PasswordRequest("blackmamba", "whitecorba");
		
		when(accountService.comparePassword(pwDTO.getOldPassword(), phone)).thenReturn(true);
		
		mockMVC.perform(patch("/accounts/{phone}", phone)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(pwDTO)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Password changed")));
/*			.andExpect(model().size(2))
			.andExpect(model().attributeExists("password"))
			.andExpect(model().attribute("success", "Password changed"))
			.andExpect(view().name("account/password"));*/
		
		verify(accountService).comparePassword(pwDTO.getOldPassword(), phone);
	}
	
	@Test
	void failure_password_change() throws Exception {
		
		PasswordRequest pwDTO = new PasswordRequest("blackcobra", "whitemamba");
		
		when(accountService.comparePassword(pwDTO.getOldPassword(), phone)).thenReturn(false);
		
		mockMVC.perform(patch("/accounts/{phone}", phone)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(pwDTO)))
			.andExpect(status().isConflict())
			.andExpect(content().string(containsString("Current password mismatch")));
/*			.andExpect(model().size(2))
			.andExpect(model().attributeExists("password"))
			.andExpect(model().attribute("message", "Old password mismatch"))
			.andExpect(view().name("account/password"));*/
		
		verify(accountService).comparePassword(pwDTO.getOldPassword(), phone);
	}
	
	@Test
	void password_binding_errors() throws Exception {

		PasswordRequest pwDTO = new PasswordRequest("black", "white");
		
		mockMVC.perform(patch("/accounts/{phone}", phone)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(pwDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString
					("Old password should be 10-60 symbols length")))
			.andExpect(content().string(containsString
					("New password should be 10-60 symbols length")))
		    .andExpect(result -> 
		      	assertTrue(result.getResolvedException() 
		      		instanceof MethodArgumentNotValidException));
//		    .andExpect(result -> 
//		      	assertEquals("resource not found", result.getResolvedException().getMessage()));
/*			.andExpect(model().size(1))
			.andExpect(model().errorCount(2))
			.andExpect(model().attributeExists("password"))
			.andExpect(view().name("account/password"));*/
	}
	
	@Test
	void wrong_bill_serial() throws Exception {
		
		when(billService.getBillDTO(anyInt()))
//			.thenReturn(modelMapper.map(bill, BillResponse.class));
			.thenReturn(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));
		when(restTemplate.postForEntity(anyString(), anyString(), any(Class.class)))
			.thenReturn(new ResponseEntity<>("No bill with serial 33 found", HttpStatus.NOT_FOUND));
		
		mockMVC.perform(patch("/bills/{id}", "0")
//														.param("id", "0")
														.param("action", "external")
														.param("balance", "0.00")
														.param("amount", "0.00")
														.param("bank", "Demo")
													    .param("recipient", "33")
						)
			.andExpect(status().isNotFound())
			.andExpect(content().string(containsString("No bill with serial 33 found")));
/*			.andExpect(model().size(4))
			.andExpect(model().attribute("id", 0))
			.andExpect(model().attribute("action", "external"))
			.andExpect(model().attribute("balance", "0.00"))
			.andExpect(model().attribute("message", "No bill with serial 33 found"))
			.andExpect(view().name("bill/external"));*/
		
		verify(billService).getBillDTO(anyInt());
		verify(restTemplate).postForEntity(anyString(), anyString(), any(Class.class));
	}
	
	@Test
	void wrong_bank_name() throws Exception {
		
		when(billService.getBillDTO(anyInt()))
//			.thenReturn(modelMapper.map(bill, BillResponse.class));
			.thenReturn(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));
		when(restTemplate.postForEntity(anyString(), anyString(), any(Class.class)))
			.thenReturn(new ResponseEntity<>("No bank with name Demo found", HttpStatus.NOT_FOUND));
		
		mockMVC.perform(patch("/bills/{id}", "0")
//														.param("id", "0")
														.param("action", "external")
														.param("balance", "0.00")
														.param("amount", "0.00")
														.param("bank", "Demo")
													    .param("recipient", "22")
						)
			.andExpect(status().isNotFound())
			.andExpect(content().string(containsString("No bank with name Demo found")));
/*			.andExpect(model().size(4))
			.andExpect(model().attribute("id", 0))
			.andExpect(model().attribute("action", "external"))
			.andExpect(model().attribute("balance", "0.00"))
			.andExpect(model().attribute("message", "No bank with name Demo found"))
			.andExpect(view().name("bill/external"));*/
		
		verify(billService).getBillDTO(anyInt());
		verify(restTemplate).postForEntity(anyString(), anyString(), any(Class.class));
	}
	
	@Test
	void connection_failure() throws Exception {
		
		when(billService.getBillDTO(anyInt()))
//			.thenReturn(modelMapper.map(bill, BillResponse.class));
			.thenReturn(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));
		when(restTemplate.postForEntity(anyString(), anyString(), any(Class.class)))
			.thenReturn(new ResponseEntity<>("Service is temporary unavailable", HttpStatus.SERVICE_UNAVAILABLE));
		
		mockMVC.perform(patch("/bills/{id}", "0")
//														.param("id", "0")
														.param("action", "external")
														.param("balance", "0.00")
														.param("amount", "0.00")
														.param("bank", "Demo")
													    .param("recipient", "22")
						)
			.andExpect(status().isServiceUnavailable())
			.andExpect(content().string(containsString("Service is temporary unavailable")));
/*			.andExpect(model().size(4))
			.andExpect(model().attribute("id", 0))
			.andExpect(model().attribute("action", "external"))
			.andExpect(model().attribute("balance", "0.00"))
			.andExpect(model().attribute("message", "Service is temporary unavailable"))
			.andExpect(view().name("bill/external"));*/
		
		verify(billService).getBillDTO(anyInt());
		verify(restTemplate).postForEntity(anyString(), anyString(), any(Class.class));		
	}
	
	@Test
	void successful_payment() throws Exception {
		
		when(builder.build()).thenReturn(operation);
		when(billService.getBillDTO(anyInt()))
//			.thenReturn(modelMapper.map(bill, BillResponse.class));
			.thenReturn(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));
		doNothing().when(executorService).execute(any(Runnable.class));
		
		doNothing().when(billService).deposit(operation);
		doNothing().when(billService).withdraw(operation);
		doNothing().when(billService).transfer(operation);
		doNothing().when(billService).external(operation);
		doNothing().when(billService).outward(operation);
		when(operationService.deposit(anyDouble(), anyString(), anyString(), anyInt(), anyString()))
			.thenReturn(operation);
		when(operationService.withdraw(anyDouble(), anyString(), anyString(), anyInt(), anyString()))
			.thenReturn(operation);
		when(operationService.transfer(anyDouble(), anyString(), anyString(), anyInt(), anyInt(), 
				anyString()))
			.thenReturn(operation);
		
		when(restTemplate.postForEntity(anyString(), anyString(), any(Class.class)))
			.thenReturn(new ResponseEntity<>("Data has been verified", HttpStatus.OK));
		
		mockMVC.perform(patch("/bills/{id}", "0")
//													    .param("id", "0")
													    .param("action", "deposit")
													    .param("balance", "0.00")
													    .param("amount", "0.00")
						)
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Done!")));
//			.andExpect(redirectedUrl("/accounts/show/" + phone));
				
		mockMVC.perform(patch("/bills/{id}", "0")
//													    .param("id", "0")
													    .param("action", "withdraw")
													    .param("balance", "0.00")
													    .param("amount", "0.00")
						)
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Done!")));
//			.andExpect(redirectedUrl("/accounts/show/" + phone));
				
		mockMVC.perform(patch("/bills/{id}", "0")
//														.param("id", "0")
														.param("action", "transfer")
														.param("balance", "0.00")
														.param("amount", "0.00")
														.param("recipient", "22")
						)
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Done!")));
//			.andExpect(redirectedUrl("/accounts/show/" + phone));
				
		mockMVC.perform(patch("/bills/{id}", "0")
//														.param("id", "0")
														.param("action", "external")
														.param("balance", "0.00")
														.param("amount", "0.00")
														.param("bank", "Demo")
													    .param("recipient", "22")
						)
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Done!")));
//			.andExpect(flash().attribute("message", "Data has been verified"))
//			.andExpect(redirectedUrl("/accounts/show/" + phone));
		
		OperationRequest dto = new OperationRequest(777, 3, "USD", 0.01, "Demo");
		mockMVC.perform(post("/bills/external").header("Origin", externalURL)
												.contentType(MediaType.APPLICATION_JSON)
												.content(mapper.writeValueAsString(dto))
						)
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Successfully received")));
		
		verify(billService, times(5)).getBillDTO(anyInt());
		verify(billService).deposit(operation);
		verify(billService).withdraw(operation);
		verify(billService).transfer(operation);
		verify(billService).external(operation);
		verify(billService).outward(operation);
		verify(operationService).deposit(anyDouble(), anyString(), anyString(), anyInt(), 
				anyString());
		verify(operationService).withdraw(anyDouble(), anyString(), anyString(), anyInt(), 
				anyString());
		verify(operationService, times(3)).transfer(anyDouble(), anyString(), anyString(), 
				anyInt(), anyInt(), anyString());
		verify(restTemplate).postForEntity(anyString(), anyString(), any(Class.class));
	}
	
	@Test
	void zero_amount_exception() throws Exception {
		
		when(builder.build()).thenReturn(operation);
		when(billService.getBillDTO(anyInt()))
//			.thenReturn(modelMapper.map(bill, BillResponse.class));
			.thenReturn(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));
		
		when(operationService.deposit(anyDouble(), anyString(), anyString(), anyInt(), anyString()))
			.thenReturn(operation);
		when(operationService.withdraw(anyDouble(), anyString(), anyString(), anyInt(), anyString()))
			.thenReturn(operation);
		when(operationService.transfer(anyDouble(), anyString(), anyString(), anyInt(), anyInt(), 
				anyString()))
			.thenReturn(operation);
		doThrow(new PaymentException("Amount of money should be higher than zero"))
			.when(billService).deposit(operation);
		doThrow(new PaymentException("Amount of money should be higher than zero"))
			.when(billService).withdraw(operation);
		doThrow(new PaymentException("Amount of money should be higher than zero"))
			.when(billService).transfer(operation);
		doThrow(new PaymentException("Amount of money should be higher than zero"))
			.when(billService).outward(operation);
		
		when(restTemplate.postForEntity(anyString(), anyString(), any(Class.class)))
			.thenReturn(new ResponseEntity<>(new String(), HttpStatus.OK));
		
		mockMVC.perform(patch("/bills/{id}", "0")
//													    .param("id", "0")
													    .param("action", "deposit")
													    .param("balance", "0.00")
													    .param("amount", "0.00")
						)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Amount of money should be higher than zero")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "deposit"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(model().attribute("message", "Amount of money should be higher than zero"))
				.andExpect(view().name("bill/payment"));*/
				
		mockMVC.perform(patch("/bills/{id}", "0")
//													    .param("id", "0")
													    .param("action", "withdraw")
													    .param("balance", "0.00")
													    .param("amount", "0.00")
						)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Amount of money should be higher than zero")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "withdraw"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(model().attribute("message", "Amount of money should be higher than zero"))
				.andExpect(view().name("bill/payment"));*/
				
		mockMVC.perform(patch("/bills/{id}", "0")
//														.param("id", "0")
														.param("action", "transfer")
														.param("balance", "0.00")
														.param("amount", "0.00")
														.param("recipient", "22")
						)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Amount of money should be higher than zero")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "transfer"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(model().attribute("message", "Amount of money should be higher than zero"))
				.andExpect(view().name("bill/transfer"));*/
		
		mockMVC.perform(patch("/bills/{id}", "0")
//														.param("id", "0")
														.param("action", "external")
														.param("balance", "0.00")
														.param("amount", "0.00")
														.param("bank", "Demo")
													    .param("recipient", "22")
						)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Amount of money should be higher than zero")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "external"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(model().attribute("message", "Amount of money should be higher than zero"))
				.andExpect(view().name("bill/external"));*/
		
		OperationRequest dto = new OperationRequest(777, 3, "USD", 0.00, "Demo");
		mockMVC.perform(post("/bills/external").header("Origin", externalURL)
												.contentType(MediaType.APPLICATION_JSON)
												.content(mapper.writeValueAsString(dto))
						)
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString
						("Amount of money should be higher than zero")));		

		verify(operationService).deposit(anyDouble(), anyString(), anyString(), anyInt(), 
				anyString());
		verify(operationService).withdraw(anyDouble(), anyString(), anyString(), anyInt(), 
				anyString());
		verify(operationService, times(2)).transfer(anyDouble(), anyString(), anyString(), 
				anyInt(), anyInt(), anyString());
		verify(billService, times(5)).getBillDTO(anyInt());
		verify(billService).deposit(operation);
		verify(billService).withdraw(operation);
		verify(billService).transfer(operation);
		verify(billService).outward(operation);
		verify(restTemplate).postForEntity(anyString(), anyString(), any(Class.class));
	}
	
	@Test
	void negative_balance_exception() throws Exception {
		
		when(builder.build()).thenReturn(operation);
		when(billService.getBillDTO(anyInt()))
//			.thenReturn(modelMapper.map(bill, BillResponse.class));
			.thenReturn(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));
		
		when(operationService.withdraw(anyDouble(), anyString(), anyString(), anyInt(), anyString()))
			.thenReturn(operation);
		when(operationService.transfer(anyDouble(), anyString(), anyString(), anyInt(), anyInt(), 
				anyString()))
			.thenReturn(operation);
		doThrow(new PaymentException("Not enough money to complete operation"))
			.when(billService).withdraw(operation);
		doThrow(new PaymentException("Not enough money to complete operation"))
			.when(billService).transfer(operation);
		doThrow(new PaymentException("Not enough money to complete operation"))
			.when(billService).outward(operation);
		
		when(restTemplate.postForEntity(anyString(), anyString(), any(Class.class)))
			.thenReturn(new ResponseEntity<>(new String(), HttpStatus.OK));
				
		mockMVC.perform(patch("/bills/{id}", "0")
//													    .param("id", "0")
													    .param("action", "withdraw")
													    .param("balance", "0.00")
													    .param("amount", "0.01")
						)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Not enough money to complete operation")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "withdraw"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(model().attribute("message", "Not enough money to complete operation"))
				.andExpect(view().name("bill/payment"));*/
		
		mockMVC.perform(patch("/bills/{id}", "0")
//														.param("id", "0")
														.param("action", "transfer")
														.param("balance", "0.00")
														.param("amount", "0.01")
														.param("recipient", "22")
						)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Not enough money to complete operation")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "transfer"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(model().attribute("message", "Not enough money to complete operation"))
				.andExpect(view().name("bill/transfer"));*/
		
		mockMVC.perform(patch("/bills/{id}", "0")
//														.param("id", "0")
														.param("action", "external")
														.param("balance", "0.00")
														.param("amount", "0.01")
														.param("bank", "Demo")
													    .param("recipient", "22")
						)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Not enough money to complete operation")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "external"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(model().attribute("message", "Not enough money to complete operation"))
				.andExpect(view().name("bill/external"));*/

		verify(operationService).withdraw(anyDouble(), anyString(), anyString(), anyInt(), 
				anyString());
		verify(operationService, times(2)).transfer(anyDouble(), anyString(), anyString(), anyInt(), anyInt(), 
				anyString());
		verify(billService, times(4)).getBillDTO(anyInt());
		verify(billService).withdraw(operation);
		verify(billService).transfer(operation);
		verify(billService).outward(operation);
		verify(restTemplate).postForEntity(anyString(), anyString(), any(Class.class));
	}

	@Test
	void same_bill_id_exception() throws Exception {
		
		when(builder.build()).thenReturn(operation);
		when(billService.getBillDTO(anyInt()))
//			.thenReturn(modelMapper.map(bill, BillResponse.class));
			.thenReturn(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));

		when(operationService.transfer(anyDouble(), anyString(), anyString(), anyInt(), anyInt(), 
				anyString()))
			.thenReturn(operation);
		doThrow(new PaymentException("Source and target bills should not be the same"))
			.when(billService).transfer(operation);
		doThrow(new PaymentException("Source and target bills should not be the same"))
			.when(billService).external(operation);
		doThrow(new PaymentException("Source and target bills should not be the same"))
			.when(billService).outward(operation);
		
		when(restTemplate.postForEntity(anyString(), anyString(), any(Class.class)))
			.thenReturn(new ResponseEntity<>(new String(), HttpStatus.OK));
		
		mockMVC.perform(patch("/bills/{id}", "0")
//													    .param("id", "0")
													    .param("action", "transfer")
													    .param("balance", "0.00")
													    .param("amount", "0.00")
													    .param("recipient", "0")
						)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Source and target bills should not be the same")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "transfer"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(model().attribute("message", "Source and target bills should not be the same"))
				.andExpect(view().name("bill/transfer"));*/
		
		mockMVC.perform(patch("/bills/{id}", "0")
//			    										.param("id", "0")
													    .param("action", "external")
													    .param("balance", "0.00")
													    .param("amount", "0.00")
														.param("bank", "Demo")
													    .param("recipient", "0")
						)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Source and target bills should not be the same")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "external"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(model().attribute("message", "Source and target bills should not be the same"))
				.andExpect(view().name("bill/external"));*/
		
		OperationRequest dto = new OperationRequest(777, 777, "USD", 0.01, "Demo");
		mockMVC.perform(post("/bills/external").header("Origin", externalURL)
												.contentType(MediaType.APPLICATION_JSON)
												.content(mapper.writeValueAsString(dto)))
			.andExpect(status().isInternalServerError())
			.andExpect(content().string(containsString
					("Source and target bills should not be the same")));	

		verify(operationService, times(3)).transfer(anyDouble(), anyString(), anyString(), 
				anyInt(), anyInt(), anyString());
		verify(billService, times(3)).getBillDTO(anyInt());
		verify(billService).transfer(operation);
		verify(billService).external(operation);
		verify(billService).outward(operation);
		verify(restTemplate).postForEntity(anyString(), anyString(), any(Class.class));
	}
	
	@Test
	void currency_mismatch_exception() throws Exception {
		
		when(builder.build()).thenReturn(operation);
		when(billService.getBillDTO(anyInt()))
//			.thenReturn(modelMapper.map(bill, BillResponse.class));
			.thenReturn(billMapper.toDTO(bill, new CycleAvoidingMappingContext()));

		when(operationService.transfer(anyDouble(), anyString(), anyString(), anyInt(), anyInt(), 
				anyString()))
			.thenReturn(operation);
		doThrow(new PaymentException("Wrong currency type of the target bill"))
			.when(billService).transfer(operation);
		doThrow(new PaymentException("Wrong currency type of the target bill"))
			.when(billService).external(operation);
		
		when(restTemplate.postForEntity(anyString(), anyString(), any(Class.class)))
			.thenReturn(new ResponseEntity<>(new String
					("Wrong currency type SEA for the target bill 22"), HttpStatus.BAD_REQUEST));
				
		mockMVC.perform(patch("/bills/{id}", "0")
														.param("recipient", "22")
//														.param("id", "0")
														.param("action", "transfer")
														.param("balance", "0.01")
														.param("amount", "0.01")
						)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString
						("Wrong currency type of the target bill")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "transfer"))
				.andExpect(model().attribute("balance", "0.01"))
				.andExpect(model().attribute("message", "Wrong currency type of the target bill"))
				.andExpect(view().name("bill/transfer"));*/
		
		mockMVC.perform(patch("/bills/{id}", "0")
														.param("recipient", "22")
														.param("bank", "Demo")
//														.param("id", "0")
														.param("action", "external")
														.param("balance", "0.01")
														.param("amount", "0.01")
						)
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString
						("Wrong currency type SEA for the target bill 22")));
/*				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 0))
				.andExpect(model().attribute("action", "external"))
				.andExpect(model().attribute("balance", "0.01"))
				.andExpect(model().attribute("message", 
						"Wrong currency type SEA for the target bill 22"))
				.andExpect(view().name("bill/external"));*/
		
		OperationRequest dto = new OperationRequest(777, 3, "SEA", 0.01, "Demo");
		mockMVC.perform(post("/bills/external").header("Origin", externalURL)
												.contentType(MediaType.APPLICATION_JSON)
												.content(mapper.writeValueAsString(dto)))
			.andExpect(status().isInternalServerError())
			.andExpect(content().string(containsString
					("Wrong currency type of the target bill")));	

		verify(operationService, times(2)).transfer(anyDouble(), anyString(), anyString(), 
				anyInt(), anyInt(), anyString());
		verify(billService, times(3)).getBillDTO(anyInt());
		verify(billService).transfer(operation);
		verify(billService).external(operation);
		verify(restTemplate).postForEntity(anyString(), anyString(), any(Class.class));
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
//		mockMVC.perform(get("/bills/notify")).andExpect(status().isCreated());
		mockMVC.perform(get("/bills/notify")).andExpect(status().isOk());
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
		
		
		when(builder.build()).thenReturn(operation);
		doNothing().when(executorService).execute(any(Runnable.class));
		doNothing().when(billService).external(operation);
		when(operationService.transfer(anyDouble(), anyString(), anyString(), anyInt(), anyInt(), 
				anyString()))
		.thenReturn(operation);
		
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
		
		verify(billService).external(operation);
		verify(operationService).transfer(anyDouble(), anyString(), anyString(), 
				anyInt(), anyInt(), anyString());
	}	
	
	@AfterEach
	void tear_down() {
		currencies = null;
		phone = null;
	}
	
	@AfterAll
	static void clear() {
		entity = null;
    	operation = null;
    	builder = null;
		bill = null;
		
//		modelMapper = null;
		accountMapper = null;
		billMapper = null;
	}
	
}
