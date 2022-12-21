package com.github.irybov.bankdemoboot.repository;

import java.time.LocalDate;

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

	@Test
	void test_insertion() {
		Account account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true);
		Bill bill = new Bill("GBP", true, account);
		billRepository.save(bill);
	}

}
