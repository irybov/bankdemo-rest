package com.github.irybov.bankdemoboot.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.entity.Bill;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class BillRepositoryTest {
	
	@Autowired
	private BillRepository billRepository;
	
	private Bill bill;
	private Account account;
	
	@BeforeAll
	void prepare() {
		account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true);
		bill = new Bill("SEA", true, account);
		billRepository.save(bill);
	}

	@Test
	void multi_test() {
		
		int id = bill.getId();
		Optional<Bill> fromDB = billRepository.findById(id);
		assertThat(fromDB.get()).isEqualTo(bill);
		fromDB.get().setBalance(fromDB.get().getBalance().add(BigDecimal.valueOf(9.99)));
		billRepository.save(fromDB.get());
		Optional<Bill> updated = billRepository.findById(id);
		assertThat(updated.get().getBalance()).isEqualTo(BigDecimal.valueOf(9.99));
		assertThat(updated.get()).isEqualTo(fromDB.get());
		billRepository.deleteById(id);
		List<Bill> bills = (List<Bill>) billRepository.findAll();
		assertThat(bills.size()).isEqualTo(0);
	}

    @AfterAll
    void clear() {
    	billRepository.deleteAll();
    	bill = null;
    	account = null;
    }
	
}
