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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.persistence.EntityNotFoundException;

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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
	private MockMvc mock;

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
	
	@Test
	void can_get_admin_html() throws Exception {

		AccountResponseDTO admin = new AccountResponseDTO(new Account());		
		when(accountService.getAccountDTO(anyString())).thenReturn(admin);
		
		mock.perform(get("/accounts/search"))
			.andExpect(status().isOk())
			.andExpect(model().size(1))
	        .andExpect(model().attribute("admin", admin))
			.andExpect(content().string(containsString("Admin's area")))
	        .andExpect(view().name("/account/search"));
		
	    verify(accountService).getAccountDTO(anyString());
	}

	@Test
	void can_get_history_html() throws Exception {
		
		mock.perform(get("/operations/list"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Operations history")))
			.andExpect(view().name("/account/history"));
	}
	
	@Test
	void can_get_clients_list() throws Exception {
		
		List<AccountResponseDTO> clients = new ArrayList<>();
		when(accountService.getAll()).thenReturn(clients);
		
		mock.perform(get("/accounts/list"))
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
		
		mock.perform(get("/accounts/status/{id}", "0"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("false")));
		
	    verify(accountService).changeStatus(anyInt());
	}
	
	@Test
	void can_change_bill_status() throws Exception {
		
		when(billService.changeStatus(anyInt())).thenReturn(false);
		
		mock.perform(get("/bills/status/{id}", "0"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("false")));
		
	    verify(billService).changeStatus(anyInt());
	}
	
	@Test
	void input_mismatch_exception() throws Exception {
		
		assertThatThrownBy(() -> mock.perform(get("/accounts/search/{phone}", "00000"))
				.andExpect(status().isInternalServerError()))
				.hasCause(new InputMismatchException("Phone number should be of 10 digits"));
	}
	
	@Test
	void entity_not_found_exception() throws Exception {
		
		String phone = "9999999999";
		when(accountService.getAccountDTO(anyString()))
			.thenThrow(new EntityNotFoundException("Account with phone " + phone + " not found"));
		
		mock.perform(get("/accounts/search/{phone}", phone))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.report").value("Account with phone " + phone + " not found"));
		
	    verify(accountService).getAccountDTO(anyString());
	}
	
	@Test
	void can_get_account_info() throws Exception {
		
		Account entity = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true);
		entity.setCreatedAt(OffsetDateTime.now());
		entity.setUpdatedAt(OffsetDateTime.now());
		
		AccountResponseDTO account = new AccountResponseDTO(entity);
		when(accountService.getAccountDTO(anyString())).thenReturn(account);
		List<BillResponseDTO> bills = new ArrayList<>();
		Bill bill = new Bill();
		bill.setOwner(entity);
		bills.add(new BillResponseDTO(bill));
		when(accountService.getBills(anyInt())).thenReturn(bills);
		
		mock.perform(get("/accounts/search/{phone}", "0000000000"))
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
		
	    verify(accountService).getAccountDTO(anyString());
	    verify(accountService).getBills(anyInt());
	}
	
	@Test
	void can_get_operations_page() throws Exception {
		
		List<OperationResponseDTO> operations = new ArrayList<>();
		operations.add(new OperationResponseDTO(new Operation()));
		OperationPage page = new OperationPage();
		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
										   page.getSortDirection(), page.getSortBy());
		Page<OperationResponseDTO> operationPage = new PageImpl<>
						(operations, pageable, operations.size());
		
		when(operationService.getPage(anyInt(), anyString(), anyDouble(), anyDouble(),
				any(OffsetDateTime.class), any(OffsetDateTime.class),refEq(page)))
				.thenReturn(operationPage);
		
		mock.perform(get("/operations/list/{id}", "0"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").exists())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content").isNotEmpty());
		
		verify(operationService).getPage(anyInt(), anyString(), anyDouble(), anyDouble(),
				any(OffsetDateTime.class), any(OffsetDateTime.class),refEq(page));
	}
	
	@Test
	void can_export_data_2_csv_file() throws Exception {
		
		Account entity = new Account
				("Nia", "Nacci", "4444444444", LocalDate.of(1998, 12, 10), "blackmamba", true);
		entity.setCreatedAt(OffsetDateTime.now());
		entity.setUpdatedAt(OffsetDateTime.now());
		
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
		
		mock.perform(get("/operations/print/{id}", "0"))
			.andDo(print())
			.andExpect(status().isOk());
		
		verify(operationService).getAll(anyInt());
		verify(billService).getBillDTO(anyInt());
	}
	
}
