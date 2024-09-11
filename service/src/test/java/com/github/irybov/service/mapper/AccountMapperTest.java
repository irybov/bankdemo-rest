package com.github.irybov.service.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.irybov.database.entity.Account;
import com.github.irybov.database.entity.Bill;
import com.github.irybov.service.dto.AccountRequest;
import com.github.irybov.service.dto.AccountResponse;
import com.github.irybov.service.mapper.AccountMapper;
import com.github.irybov.service.mapper.CycleAvoidingMappingContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BillMapperImpl.class, AccountMapperImpl.class})
class AccountMapperTest {
	
	@Autowired
	private AccountMapper mapStruct;
	private static Account entity = new Account
			("Admin", "Adminov", "0000000000", "@greenmail.io", LocalDate.of(2001, 01, 01), "superadmin", true);

	@Test
	void test() {
		
		CycleAvoidingMappingContext cycleAvoidingMappingContext = new CycleAvoidingMappingContext();
		
		AccountRequest request = new AccountRequest();
		request.setName("Ivan");
		request.setSurname("Rybov");
		request.setPhone("7777777777");
		request.setBirthday(LocalDate.of(1980, Month.SEPTEMBER, 21));
		
		Account account = mapStruct.toModel(request, cycleAvoidingMappingContext);
		assertTrue(new ReflectionEquals(mapStruct.toModel(request, cycleAvoidingMappingContext)).matches(account));

		Bill bill = new Bill("SEA", true, entity);
		bill.setOwner(account);
		List<Bill> bills = new ArrayList<>();
		bills.add(bill);
		bills.add(new Bill("SEA", true, entity));
		account.setBills(bills);		
		AccountResponse response = mapStruct.toDTO(account, cycleAvoidingMappingContext);
		
		assertTrue(new ReflectionEquals(mapStruct.toDTO(account, cycleAvoidingMappingContext), "bills").matches(response));
		assertTrue(new ReflectionEquals(mapStruct.toDTO(account, cycleAvoidingMappingContext).getBills()).matches(response.getBills()));
		assertEquals(mapStruct.toDTO(account, cycleAvoidingMappingContext).getBills().size(), bills.size());
				
		List<Account> accounts = new ArrayList<>();
		accounts.add(account);
		accounts.add(entity);
		
		assertEquals(accounts.size(), mapStruct.toList(accounts, cycleAvoidingMappingContext).size());
		assertThat(mapStruct.toList(accounts, cycleAvoidingMappingContext)).hasSameClassAs(new ArrayList<AccountResponse>());
		
	}

}
