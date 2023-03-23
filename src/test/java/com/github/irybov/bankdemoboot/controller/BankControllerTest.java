package com.github.irybov.bankdemoboot.controller;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.PasswordRequestDTO;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.security.AccountDetailsService;
import com.github.irybov.bankdemoboot.service.AccountService;
import com.github.irybov.bankdemoboot.service.BillService;
import com.github.irybov.bankdemoboot.service.OperationService;

@WithMockUser(username = "4444444444", roles = "CLIENT")
@WebMvcTest(controllers = BankController.class)
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
	private AccountDetailsService accountDetailsService;
	@Autowired
	private MockMvc mockMVC;
	
	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	private String phone;
	
	private static Set<Currency> currencies;
	
	@BeforeAll
	static void prepare() {
		
		currencies = new HashSet<>();
		Currency usd = Currency.getInstance("USD");
		currencies.add(usd);
		Currency eur = Currency.getInstance("EUR");
		currencies.add(eur);
		Currency gbp = Currency.getInstance("GBP");
		currencies.add(gbp);
		Currency rub = Currency.getInstance("RUB");
		currencies.add(rub);
	}
	
	@BeforeEach
	void set_up() {
		phone = authentication().getName();		
	}
	
	@Test
	void can_get_client_html() throws Exception {
		
		Account entity = new Account
				("Nia", "Nacci", phone, LocalDate.of(1998, 12, 10), "blackmamba", true);
		entity.setCreatedAt(OffsetDateTime.now());
		entity.setUpdatedAt(OffsetDateTime.now());
		AccountResponseDTO account = new AccountResponseDTO(entity);		
		List<BillResponseDTO> bills = new ArrayList<>();
		
		when(accountService.getAccountDTO(phone)).thenReturn(account);
		when(accountService.getBills(account.getId())).thenReturn(bills);
		
		mockMVC.perform(get("/accounts/show/{phone}", phone))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Private area")))
			.andExpect(model().size(3))
			.andExpect(model().attribute("account", account))
			.andExpect(model().attribute("bills", bills))
			.andExpect(model().attribute("currencies", currencies))
			.andExpect(view().name("/account/private"));
		
		verify(accountService).getAccountDTO(phone);
		verify(accountService).getBills(account.getId());
	}
	
	@Test
	void check_security_restriction() throws Exception {
		
		mockMVC.perform(get("/accounts/show/{phone}", "5555555555"))
			.andExpect(status().isForbidden())
			.andExpect(model().size(1))
			.andExpect(model().attribute("message", "Security restricted information"))
			.andExpect(forwardedUrl("/accounts/show/" + phone));
	}
	
	@Test
	void can_create_new_bill() throws Exception {
		
		Bill bill = new Bill("SEA", true, new Account());
		bill.setBalance(BigDecimal.valueOf(9.99));
		bill.setCreatedAt(OffsetDateTime.now());
		bill.setUpdatedAt(OffsetDateTime.now());
		when(accountService.addBill(anyString(), anyString())).thenReturn(new BillResponseDTO(bill));
		
		mockMVC.perform(post("/bills/add").with(csrf())
									   .param("phone", anyString())
									   .param("currency", anyString())
					)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").exists())
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.updatedAt").exists())
			.andExpect(jsonPath("$.active").exists())
			.andExpect(jsonPath("$.balance").exists())
			.andExpect(jsonPath("$.currency").exists())
			.andExpect(jsonPath("$.owner").exists());
		
		verify(accountService).addBill(anyString(), anyString());
	}
	
	@Test
	void can_delete_own_bill() throws Exception {
		
		doNothing().when(billService).deleteBill(anyInt());
		
		mockMVC.perform(delete("/bills/delete/{id}", "0").with(csrf()))
			.andExpect(status().isOk());
		
		verify(billService).deleteBill(anyInt());
	}
	
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
			.andExpect(view().name("/bill/payment"));
		
		mockMVC.perform(post("/bills/operate").with(csrf())
										   .param("id", "4")
										   .param("action", "withdraw")
										   .param("balance", "4.44")
					)
			.andExpect(status().isOk())
			.andExpect(model().size(3))
			.andExpect(model().attribute("id", "4"))
			.andExpect(model().attribute("action", "withdraw"))
			.andExpect(model().attribute("balance", "4.44"))
			.andExpect(view().name("/bill/payment"));
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
			.andExpect(view().name("/bill/transfer"));
	}
	
	@Test
	void check_bill_owner() throws Exception {
		
		Account entity = new Account
				("Nia", "Nacci", phone, LocalDate.of(1998, 12, 10), "blackmamba", true);
		entity.setCreatedAt(OffsetDateTime.now());
		entity.setUpdatedAt(OffsetDateTime.now());
		
		Bill bill = new Bill();
		bill.setOwner(entity);
		
		when(billService.getBillDTO(anyInt())).thenReturn(new BillResponseDTO(bill));
		
		mockMVC.perform(get("/bills/validate/{id}", "4"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
			.andExpect(content().string(containsString
					  (bill.getOwner().getName() + " " + bill.getOwner().getSurname())));
		
		verify(billService).getBillDTO(anyInt());
	}
	
	@Test
	void recipient_not_found() throws Exception {
		
		Random random = new Random();
		int id = random.nextInt(Byte.MAX_VALUE);
		when(billService.getBillDTO(id)).thenThrow(new EntityNotFoundException
				("Target bill with id: " + id + " not found"));
		
		mockMVC.perform(get("/bills/validate/{id}", String.valueOf(id)))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
			.andExpect(content().string(containsString
					  ("Target bill with id: " + id + " not found")));
		
		verify(billService).getBillDTO(id);
	}
	
	@Test
	void can_get_password_html() throws Exception {
		
		mockMVC.perform(get("/accounts/password/{phone}", phone))
			.andExpect(status().isOk())
			.andExpect(model().size(1))
			.andExpect(model().attributeExists("password"))
			.andExpect(model().attribute("password", any(PasswordRequestDTO.class)))
			.andExpect(view().name("/account/password"));
	}
	
	@Test
	void success_password_change() throws Exception {
		
		PasswordRequestDTO pw = new PasswordRequestDTO();
		pw.setOldPassword("blackmamba");
		pw.setNewPassword("whitecorba");
		
		when(accountService.comparePassword(pw.getOldPassword(), phone)).thenReturn(true);
		
		mockMVC.perform(patch("/accounts/password/{phone}", phone).with(csrf())
														.param("oldPassword", pw.getOldPassword())
														.param("newPassword", pw.getNewPassword())
					)
			.andExpect(status().isOk())
			.andExpect(model().size(2))
			.andExpect(model().attributeExists("password"))
			.andExpect(model().attribute("success", "Password changed"))
			.andExpect(view().name("/account/password"));
		
		verify(accountService).comparePassword(pw.getOldPassword(), phone);
	}
	
	@Test
	void failure_password_change() throws Exception {
		
		PasswordRequestDTO pw = new PasswordRequestDTO();
		pw.setOldPassword("blackcorba");
		pw.setNewPassword("whitemamba");
		
		when(accountService.comparePassword(pw.getOldPassword(), phone)).thenReturn(false);
		
		mockMVC.perform(patch("/accounts/password/{phone}", phone).with(csrf())
														.param("oldPassword", pw.getOldPassword())
														.param("newPassword", pw.getNewPassword())
					)
			.andExpect(status().isOk())
			.andExpect(model().size(2))
			.andExpect(model().attributeExists("password"))
			.andExpect(model().attribute("message", "Old password mismatch"))
			.andExpect(view().name("/account/password"));
		
		verify(accountService).comparePassword(pw.getOldPassword(), phone);
	}
	
	@Test
	void password_binding_errors() throws Exception {
		
		PasswordRequestDTO pw = new PasswordRequestDTO();
		pw.setOldPassword("black");
		pw.setNewPassword("white");
		
		mockMVC.perform(patch("/accounts/password/{phone}", phone).with(csrf())
														.param("oldPassword", pw.getOldPassword())
														.param("newPassword", pw.getNewPassword())
				)
			.andExpect(status().isBadRequest())
			.andExpect(model().size(1))
			.andExpect(model().errorCount(2))
			.andExpect(model().attributeExists("password"))
			.andExpect(view().name("/account/password"));
	}
	
	@AfterEach
	void tear_down() {
		phone = null;
	}
	
	@AfterAll
	static void clear() {
		currencies = null;
	}
	
}
