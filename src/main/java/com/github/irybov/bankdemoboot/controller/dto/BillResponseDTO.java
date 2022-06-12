package com.github.irybov.bankdemoboot.controller.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.github.irybov.bankdemoboot.entity.Bill;

import lombok.Getter;

@Getter
public class BillResponseDTO {

	private int id;
	private final OffsetDateTime timestamp;
	private boolean active;
	private BigDecimal balance;
	private String currency;
	
	public BillResponseDTO(Bill bill) {
		this.id = bill.getId();
		this.timestamp = bill.getTimestamp();
		this.active = bill.isActive();
		this.balance = bill.getBalance();
		this.currency = bill.getCurrency();
	}
		
}
