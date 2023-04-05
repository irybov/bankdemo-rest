package com.github.irybov.bankdemoboot.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.dao.BillDAO;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.exception.PaymentException;

class BillServiceDAOTest {
	
//	@Mock
//	BillServiceDAO billServiceDAO;
	@Mock
	private BillDAO billDAO;
	@InjectMocks
	private BillServiceDAO billService;
	
	private AutoCloseable autoClosable;
	
	private static Bill bill;
	private static Account owner;
	
	@BeforeAll
	static void prepare() {
		owner = new Account("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01),
				"superadmin", true);
		bill = new Bill("SEA", true, owner);
	}
	
	@BeforeEach
	void set_up() {
		autoClosable = MockitoAnnotations.openMocks(this);
		billService = new BillServiceDAO();
		ReflectionTestUtils.setField(billService, "billDAO", billDAO);
//		ReflectionTestUtils.setField(billService, "billService", billServiceDAO);
	}

	@Test
	void can_save_new_bill() {		
		billService.saveBill(bill);
		verify(billDAO).saveBill(bill);
	}
	
	@Test
	void can_update_new_bill() {		
		billService.updateBill(bill);
		verify(billDAO).updateBill(bill);
	}
	
	@Test
	void can_delete_new_bill() {		
		billService.deleteBill(anyInt());
		verify(billDAO).deleteBill(anyInt());
	}
	
	@Test
	void can_change_status() {
		
		when(billDAO.getBill(anyInt())).thenReturn(bill);
		try {
			assertThat(billService.changeStatus(anyInt())).isFalse();
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
		verify(billDAO).getBill(anyInt());
	}
	
	@Test
	void can_get_billDTO() {
		
		when(billDAO.getBill(anyInt())).thenReturn(bill);
		try {
			assertThat(billService.getBillDTO(anyInt()))
			.isExactlyInstanceOf(BillResponseDTO.class);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
		verify(billDAO).getBill(anyInt());
	}
	
	@Test
	void can_get_bill_entity() {
		
		when(billDAO.getBill(anyInt())).thenReturn(bill);
		try {
			assertThat(billService.getBill(anyInt())).isExactlyInstanceOf(Bill.class);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
		verify(billDAO).getBill(anyInt());
	}
	
	@Test
	void catch_entity_not_found_exception() {
		
		when(billDAO.getBill(anyInt())).thenReturn(null);
		assertThatThrownBy(() -> billService.getBill(anyInt()))
				  .isInstanceOf(EntityNotFoundException.class);
		verify(billDAO).getBill(anyInt());
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
	
	@ParameterizedTest
	@CsvSource({"0.01", "0.02", "0.03"})
	void can_deposit_money(double amount) {
		
		when(billDAO.getBill(anyInt())).thenReturn(bill);
		try {
			billService.deposit(anyInt(), amount);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(bill.getBalance().setScale(2, RoundingMode.DOWN).doubleValue(), amount, 0.00);
		verify(billDAO).getBill(anyInt());
	}
	
	@Test
	void withdraw_not_enough_money() {
		
		when(billDAO.getBill(anyInt())).thenReturn(bill);
		assertThatThrownBy(() -> billService.withdraw(anyInt(), 0.05))
			.isInstanceOf(PaymentException.class)
			.hasMessage("Not enough money to complete operation");
		verify(billDAO).getBill(anyInt());
	}
	
	@Test
	void transfer_not_enough_money() {
		
		when(billDAO.getBill(anyInt())).thenReturn(bill);
		assertThatThrownBy(() -> billService.transfer(0, 0.05, 1))
			.isInstanceOf(PaymentException.class)
			.hasMessage("Not enough money to complete operation");
		verify(billDAO, times(2)).getBill(anyInt());
	}
	
	@Test
	void can_withdraw_money() {
		
		bill.setBalance(new BigDecimal(1.00));
		when(billDAO.getBill(anyInt())).thenReturn(bill);
		try {
			billService.withdraw(anyInt(), 0.5);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(bill.getBalance().setScale(2, RoundingMode.DOWN).doubleValue(), 0.5, 0.00);
		assertThat(bill.getBalance().setScale(2, RoundingMode.FLOOR).doubleValue()).isEqualTo(0.5);
		verify(billDAO).getBill(anyInt());
	}
	
	@Test
	void transfer_source_matches_target() {
		assertThatThrownBy(() -> billService.transfer(0, 0.01, 0))
	    .isInstanceOf(PaymentException.class)
	    .hasMessage("Source and target bills should not be the same");
	}
	
	@Test
	void transfer_wrong_currency_type() {
		
		bill.setBalance(new BigDecimal(1.00));
		Bill target = new Bill("XXX", false, owner);		
		when(billDAO.getBill(1)).thenReturn(target);
		when(billDAO.getBill(0)).thenReturn(bill);
		
		assertThatThrownBy(() -> billService.transfer(0, 0.01, 1))
	    	.isInstanceOf(PaymentException.class)
	    	.hasMessage("Wrong currency type of the target bill");
		verify(billDAO, times(2)).getBill(anyInt());
	}
	
	@Test
	void can_transfer_money() {
		
		bill.setBalance(new BigDecimal(1.00));
		Bill target = new Bill("SEA", false, owner);		
		when(billDAO.getBill(1)).thenReturn(target);
		when(billDAO.getBill(0)).thenReturn(bill);
		
		try {
			billService.transfer(0, 0.01, 1);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
		verify(billDAO, times(4)).getBill(anyInt());
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
