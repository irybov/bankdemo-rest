package com.github.irybov.service.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.irybov.database.entity.Account;
import com.github.irybov.database.entity.Bill;
import com.github.irybov.service.dto.AccountResponse;
import com.github.irybov.service.dto.BillResponse;
import com.github.irybov.service.mapper.BillMapper;
import com.github.irybov.service.mapper.CycleAvoidingMappingContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BillMapperImpl.class, AccountMapperImpl.class})
class BillMapperTest {
	
	@Autowired
	private BillMapper mapStruct;
	private static Account entity = new Account
			("Admin", "Adminov", "0000000000", "@greenmail.io", LocalDate.of(2001, 01, 01), "superadmin", true);

	@Test
	void test() {
		
		CycleAvoidingMappingContext cycleAvoidingMappingContext = new CycleAvoidingMappingContext();
		
		Bill bill = new Bill("SEA", true, entity);
		List<Bill> bills = new ArrayList<>();
		bills.add(bill);
		bills.add(new Bill("SEA", true, entity));
		BillResponse response = new BillResponse();
		response.setCurrency(bill.getCurrency());
		response.setActive(bill.isActive());
		response.setOwner(new AccountResponse());
		
		assertAll(
			() -> assertEquals(response.getCurrency(), mapStruct.toDTO(bill, cycleAvoidingMappingContext).getCurrency()), 
			() -> assertEquals(response.isActive(), mapStruct.toDTO(bill, cycleAvoidingMappingContext).isActive()), 
			() -> assertThat(mapStruct.toDTO(bill, cycleAvoidingMappingContext).getOwner()).hasSameClassAs(response.getOwner()), 
			() -> assertEquals(bills.size(),  mapStruct.toList(bills, cycleAvoidingMappingContext).size()), 
			() -> assertThat(mapStruct.toList(bills, cycleAvoidingMappingContext)).hasSameClassAs(new ArrayList<BillResponse>())
		);
		
	}

}
