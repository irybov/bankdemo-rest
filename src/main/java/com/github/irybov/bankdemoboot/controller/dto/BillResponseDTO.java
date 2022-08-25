package com.github.irybov.bankdemoboot.controller.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.github.irybov.bankdemoboot.entity.Bill;

import lombok.Getter;

@Getter
public class BillResponseDTO {

	private int id;
	private OffsetDateTime createdAt;
	private boolean active;
	private BigDecimal balance;
	private String currency;
	
	public BillResponseDTO(Bill bill) {
		this.id = bill.getId();
		this.createdAt = bill.getCreatedAt();
		this.active = bill.isActive();
		this.balance = bill.getBalance();
		this.currency = bill.getCurrency();
	}
		
}
