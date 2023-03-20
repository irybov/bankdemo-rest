package com.github.irybov.bankdemoboot.controller.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.github.irybov.bankdemoboot.entity.Bill;

import lombok.Getter;

@Getter
public class BillResponseDTO {

	private int id;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	private boolean active;
	private BigDecimal balance;
	private String currency;
	private AccountResponseDTO owner;
	
	public BillResponseDTO(Bill bill) {
		
		this.id = bill.getId();
		this.createdAt = bill.getCreatedAt();
		this.updatedAt = bill.getUpdatedAt();		
		this.active = bill.isActive();
		this.balance = bill.getBalance();
		this.currency = bill.getCurrency();
		this.owner = new AccountResponseDTO(bill.getOwner());
	}
		
}
