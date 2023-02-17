package com.github.irybov.bankdemoboot.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.exception.PaymentException;
import com.github.irybov.bankdemoboot.repository.BillRepository;

//@ExtendWith(MockitoExtension.class)
class BillServiceJPATest {

	@Mock
	BillServiceJPA billServiceJPA;
	@Mock
	private BillRepository billRepository;
	@InjectMocks
	private BillServiceJPA billService;
	
	private AutoCloseable autoClosable;
	
	private static Bill bill;
	private static Account owner;
	
	@BeforeAll
	static void prepare() {
		owner = new Account("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01),
								   BCrypt.hashpw("superadmin", BCrypt.gensalt(4)), true);
		bill = new Bill("SEA", true, owner);
	}
	
	@BeforeEach
	void set_up() {
		autoClosable = MockitoAnnotations.openMocks(this);
		billService = new BillServiceJPA();
		ReflectionTestUtils.setField(billService, "billRepository", billRepository);
		ReflectionTestUtils.setField(billService, "billService", billServiceJPA);
	}
	
	@Test
	void can_save_new_bill() {		
		billService.saveBill(bill);
		verify(billRepository).save(bill);
	}
	
	@Test
	void can_update_new_bill() {		
		billService.updateBill(bill);
		verify(billRepository).save(bill);
	}
	
	@Test
	void can_delete_new_bill() {		
		billService.deleteBill(anyInt());
		verify(billRepository).deleteById(anyInt());
	}
	
	@Test
	void can_change_status() {
		
		when(billServiceJPA.getBill(anyInt())).thenReturn(bill);
		try {
			assertThat(billService.changeStatus(anyInt())).isFalse();
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	@Test
	void can_get_billDTO() {
		
		when(billServiceJPA.getBill(anyInt())).thenReturn(bill);
		try {
			assertThat(billService.getBillDTO(anyInt()))
			.isExactlyInstanceOf(BillResponseDTO.class);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	@Test
	void can_get_bill_entity() {
		
		when(billRepository.findById(anyInt())).thenReturn(Optional.of(bill));
		try {
			assertThat(billService.getBill(anyInt())).isExactlyInstanceOf(Bill.class);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
		verify(billRepository).findById(anyInt());
	}
	
	@Test
	void catch_entity_not_found_exception() {
		
		when(billRepository.findById(anyInt())).thenReturn(Optional.ofNullable(null));
		assertThatThrownBy(() -> billService.getBill(anyInt()))
				  .isInstanceOf(EntityNotFoundException.class);
		verify(billRepository).findById(anyInt());
	}
	
	@Test
	void catch_amount_payment_exception() {
		
		assertThatThrownBy(() -> billService.deposit(0, 0.00))
			.isInstanceOf(PaymentException.class)
		  	.hasMessage("Amount of money should be higher than zero");
		assertThatThrownBy(() -> billService.withdraw(0, 0.00))
			.isInstanceOf(PaymentException.class)
		    .hasMessage("Amount of money should be higher than zero");
		assertThatThrownBy(() -> billService.transfer(0, 0.00, 0))
		    .isInstanceOf(PaymentException.class)
		    .hasMessage("Amount of money should be higher than zero");
	}
	
	@Test
	void can_deposit_money() {
		
		when(billServiceJPA.getBill(anyInt())).thenReturn(bill);
		try {
			billService.deposit(anyInt(), 0.01);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(bill.getBalance().setScale(2, RoundingMode.DOWN).doubleValue(), 0.01, 0.00);
		assertThat(bill.getBalance().setScale(2, RoundingMode.FLOOR).doubleValue()).isEqualTo(0.01);
	}
	
	@Test
	void withdraw_not_enough_money() {
		
		when(billServiceJPA.getBill(anyInt())).thenReturn(bill);
		assertThatThrownBy(() -> billService.withdraw(anyInt(), 0.05))
			.isInstanceOf(PaymentException.class)
			.hasMessage("Not enough money to complete operation");
	}
	
	@Test
	void can_withdraw_money() {
		
		bill.setBalance(new BigDecimal(1.00));
		when(billServiceJPA.getBill(anyInt())).thenReturn(bill);
		try {
			billService.withdraw(anyInt(), 0.5);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	@Test
	void transfer_source_matches_target() {
		assertThatThrownBy(() -> billService.transfer(0, 0.01, 0))
	    .isInstanceOf(PaymentException.class)
	    .hasMessage("Source and target bills should not be the same");
	}
	
	@Test
	void transfer_wrong_currency() {
		
		bill.setBalance(new BigDecimal(1.00));
		Bill target = new Bill("XXX", false, owner);		
		when(billServiceJPA.getBill(1)).thenReturn(target);
		when(billServiceJPA.getBill(0)).thenReturn(bill);
		
		assertThatThrownBy(() -> billService.transfer(0, 0.01, 1))
	    .isInstanceOf(PaymentException.class)
	    .hasMessage("Wrong currency type of the target bill");
	}
	
	@Test
	void can_transfer_money() {
		
		bill.setBalance(new BigDecimal(1.00));
		Bill target = new Bill("SEA", false, owner);		
		when(billServiceJPA.getBill(1)).thenReturn(target);
		when(billServiceJPA.getBill(0)).thenReturn(bill);
		
		try {
			billService.transfer(0, 0.01, 1);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	@AfterEach
	void tear_down() throws Exception {
		autoClosable.close();
		billService = null;
		bill.setBalance(new BigDecimal(0.00));
	}

	@AfterAll
	static void clear() {
		bill = null;
		owner = null;
	}
	
}
