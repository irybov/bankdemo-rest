package com.github.irybov.bankdemoboot.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.persistence.PersistenceException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.entity.Operation;
import com.github.irybov.bankdemoboot.model.OperationPage;
import com.github.irybov.bankdemoboot.security.AccountDetailsService;
import com.github.irybov.bankdemoboot.service.AccountService;
import com.github.irybov.bankdemoboot.service.BillService;
import com.github.irybov.bankdemoboot.service.OperationService;
import com.opencsv.CSVWriter;

@WithMockUser(username = "0000000000", roles = "ADMIN")
@WebMvcTest(controllers = AdminController.class)
class AdminControllerTest {

	@Autowired
	@Qualifier("asyncExecutor")
	private Executor executorService;
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
	private AccountDetailsService accountDetailsService;
	@Autowired
	private MockMvc mockMVC;
	
	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	private String phone;
	
	private static Account entity;
	
	@TestConfiguration
	static class TestConfig {
		
		@Bean
	    @Primary
	    public Executor asyncExecutor() {
	    	final int cores = Runtime.getRuntime().availableProcessors();
	    	final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	    	executor.setCorePoolSize(cores);
	        executor.setMaxPoolSize(cores * 2);
	        executor.setQueueCapacity(cores * 10);
	    	executor.initialize();
	    	return executor;
	    }
	}

	@BeforeAll
	static void prepare() {
		
		entity = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true);
		entity.setCreatedAt(OffsetDateTime.now());
		entity.setUpdatedAt(OffsetDateTime.now());
		entity.setId(0);
	}
	
	@BeforeEach
	void set_up() {
		phone = authentication().getName();		
	}

    @Test
	void can_get_admin_html() throws Exception {

		AccountResponseDTO admin = new AccountResponseDTO(new Account());		
		when(accountService.getAccountDTO(anyString())).thenReturn(admin);
		
		mockMVC.perform(get("/accounts/search"))
			.andExpect(status().isOk())
			.andExpect(model().size(1))
	        .andExpect(model().attribute("admin", admin))
			.andExpect(content().string(containsString("Admin's area")))
	        .andExpect(view().name("/account/search"));
		
	    verify(accountService).getAccountDTO(anyString());
	}

	@Test
	void can_get_history_html() throws Exception {
		
		mockMVC.perform(get("/operations/list"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Operations history")))
			.andExpect(view().name("/account/history"));
	}
	
	@Test
	void can_get_clients_list() throws Exception {
		
		List<AccountResponseDTO> clients = new ArrayList<>();
		when(accountService.getAll()).thenReturn(clients);
		
		mockMVC.perform(get("/accounts/list"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Clients list")))
			.andExpect(model().size(1))
	        .andExpect(model().attribute("clients", clients))
	        .andExpect(view().name("/account/clients"));
		
	    verify(accountService).getAll();
	}
	
	@Test
	void can_change_account_status() throws Exception {
		
		when(accountService.changeStatus(anyInt())).thenReturn(false);		
		
		mockMVC.perform(get("/accounts/status/{id}", "0"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("false")));
		
	    verify(accountService).changeStatus(anyInt());
	}
	
	@Test
	void can_change_bill_status() throws Exception {
		
		when(billService.changeStatus(anyInt())).thenReturn(false);
		
		mockMVC.perform(get("/bills/status/{id}", "0"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("false")));
		
	    verify(billService).changeStatus(anyInt());
	}
	
	@Test
	void input_mismatch_exception() throws Exception {
		
		assertThatThrownBy(() -> mockMVC.perform(get("/accounts/search/{phone}", "XXL"))
				.andExpect(status().isInternalServerError()))
				.hasCause(new InputMismatchException("Phone number should be of 10 digits"));
	}
	
	@Test
	void entity_not_found_exception() throws Exception {
		
		String wrong = "9999999999";
//		when(accountService.getAccountDTO(wrong))
		when(accountService.getFullDTO(wrong))
			.thenThrow(new PersistenceException("Account with phone " + wrong + " not found"));
		
		mockMVC.perform(get("/accounts/search/{phone}", wrong))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.report").value("Account with phone " + wrong + " not found"));
		
//	    verify(accountService).getAccountDTO(wrong);
	    verify(accountService).getFullDTO(wrong);
	}
	
	@Test
	void can_get_account_info() throws Exception {
		
		AccountResponseDTO account = new AccountResponseDTO(entity);
		List<BillResponseDTO> bills = new ArrayList<>();
		Bill bill = new Bill();
		bill.setOwner(entity);
		bills.add(new BillResponseDTO(bill));
		account.setBills(bills);
//		when(accountService.getAccountDTO(phone)).thenReturn(account);
		when(accountService.getFullDTO(phone)).thenReturn(account);
//		when(accountService.getBills(anyInt())).thenReturn(bills);
		
		mockMVC.perform(get("/accounts/search/{phone}", phone))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").exists())
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.updatedAt").exists())
			.andExpect(jsonPath("$.active").exists())
			.andExpect(jsonPath("$.name").exists())
			.andExpect(jsonPath("$.surname").exists())
			.andExpect(jsonPath("$.phone").exists())
			.andExpect(jsonPath("$.birthday").exists())
			.andExpect(jsonPath("$.bills").exists())
			.andExpect(jsonPath("$.bills").isArray())
			.andExpect(jsonPath("$.bills").isNotEmpty());
		
//	    verify(accountService).getAccountDTO(phone);
//	    verify(accountService).getBills(anyInt());
	    verify(accountService).getFullDTO(phone);
	}
	
	@Test
	void can_get_operations_page() throws Exception {
		
		List<OperationResponseDTO> operations = new ArrayList<>();
		operations.add(new OperationResponseDTO(new Operation()));
//		OperationPage page = new OperationPage();
//		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
//										   page.getSortDirection(), page.getSortBy());
		Page<OperationResponseDTO> operationPage = new PageImpl<>(operations);
		
		when(operationService.getPage(anyInt(), anyString(), anyDouble(), anyDouble(),
				any(OffsetDateTime.class), any(OffsetDateTime.class), any(Pageable.class)))
				.thenReturn(operationPage);
		
		mockMVC.perform(get("/operations/list/{id}", "0"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").exists())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content").isNotEmpty());
		
		verify(operationService).getPage(anyInt(), anyString(), anyDouble(), anyDouble(),
				any(OffsetDateTime.class), any(OffsetDateTime.class), any(Pageable.class));
	}
	
	@Test
	void can_export_data_2_csv_file() throws Exception {
		
		List<OperationResponseDTO> operations = new ArrayList<>();
		Operation operation = Operation.builder()
				.amount(0.01)
				.action("deposit")
				.currency("SEA")
				.recipient(1)
				.createdAt(OffsetDateTime.now())
				.build();
		operations.add(new OperationResponseDTO(operation));

		Bill bill = new Bill("SEA", true, entity);
		bill.setBalance(BigDecimal.valueOf(9.99));
		bill.setCreatedAt(OffsetDateTime.now());
		BillResponseDTO fake = new BillResponseDTO(bill);
		
		CompletableFuture<List<OperationResponseDTO>> futureOperations = CompletableFuture.completedFuture(operations);
		when(operationService.getAll(anyInt())).thenReturn(futureOperations.join());
		CompletableFuture<BillResponseDTO> futureBill = CompletableFuture.completedFuture(fake);
		when(billService.getBillDTO(anyInt())).thenReturn(futureBill.join());
		
		byte[] output = data_2_csv_converter(entity, bill, operations);
		
		mockMVC.perform(get("/operations/print/{id}", "0"))
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
			.andExpect(content().bytes(output));
		
		verify(operationService).getAll(anyInt());
		verify(billService).getBillDTO(anyInt());
	}
	
	private byte[] data_2_csv_converter(Account account, Bill bill, List<OperationResponseDTO> operations) {

		String[] owner = {account.getName(), account.getSurname(), account.getPhone()};		
		List<String[]> data = new ArrayList<>();
		data.add(owner);
		data.add(new String[0]);
		
		String[] info = {bill.getCurrency(), String.valueOf(bill.getBalance()), bill.getCreatedAt()
				.toString()};
		data.add(info);
		data.add(new String[0]);
		
		String[] header = {"Action", "Amount", "When", "Recipient", "Sender"};
		data.add(header);
		data.add(new String[0]);		
		
		for(OperationResponseDTO operation : operations) {
			String[] row = {operation.getAction(),
							String.valueOf(operation.getAmount()),
							operation.getCreatedAt().toString(),
							String.valueOf(operation.getRecipient()),
							String.valueOf(operation.getSender())};
			data.add(row);
		}
		
		byte[] byteArray = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(baos);
				PrintWriter pw = new PrintWriter(osw);
				BufferedWriter bw = new BufferedWriter(pw);
				CSVWriter writer = new CSVWriter(bw);) {
			writer.writeAll(data);
			writer.flush();
			byteArray = baos.toByteArray();
		}
        catch (IOException exc) {
//        	log.error(exc.getMessage(), exc);
		}		
		return byteArray;		
	}
	
	@AfterEach
	void tear_down() {
		phone = null;
	}
	
	@AfterAll
	static void clear() {
		entity = null;
	}
	
}
