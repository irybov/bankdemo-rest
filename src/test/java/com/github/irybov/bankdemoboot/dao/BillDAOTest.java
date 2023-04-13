package com.github.irybov.bankdemoboot.dao;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.entity.Bill;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class BillDAOTest {
	
	@PersistenceContext
	private EntityManager entityManager;
	@InjectMocks
	private BillDAO billDAO;
	
	private Bill bill;
	private Account account;
	
	@BeforeAll
	void prepare() {
		ReflectionTestUtils.setField(billDAO, "entityManager", entityManager);
	}
	
	@BeforeEach
	void set_up() {
		account = new Account
				("Nia", "Nacci", "4444444444", LocalDate.of(1998, 12, 10), "blackmamba", true);
		bill = new Bill("SEA", true, account);
		billDAO.saveBill(bill);
	}

	@Test
	void multi_test() {
		
		int id = bill.getId();
		Bill fromDB = billDAO.getBill(id);
		assertThat(fromDB).isEqualTo(bill);
		fromDB.setBalance(fromDB.getBalance().add(BigDecimal.valueOf(9.99)));
		billDAO.updateBill(fromDB);
		Bill updated = billDAO.getBill(id);
		assertThat(updated.getBalance()).isEqualTo(BigDecimal.valueOf(9.99));
		assertThat(updated).isEqualTo(fromDB);
		billDAO.deleteBill(id);
		List<Bill> bills = billDAO.getAll();
		assertThat(bills.size()).isEqualTo(1);
		
		bills = billDAO.getByOwner(1);
		assertThat(bills.size()).isEqualTo(1);
	}
	
    @AfterEach
    void tear_down() {
    	entityManager.clear();
    	bill = null;
    	account = null;
    }

}
