package com.github.irybov.bankdemoboot.controller.dto;

import java.time.OffsetDateTime;

import com.github.irybov.bankdemoboot.entity.Operation;

import lombok.Getter;

@Getter
public class OperationResponseDTO {

	private long id;
	private OffsetDateTime createdAt;
	private double amount;
	private String action;
	private String currency;	
	private int sender;		
	private int recipient;
	
	public OperationResponseDTO(Operation operation) {
		
		this.id = operation.getId();
		this.createdAt = operation.getCreatedAt();
		this.amount = operation.getAmount();
		this.action = operation.getAction();
		this.currency = operation.getCurrency();
		this.sender = operation.getSender();
		this.recipient = operation.getRecipient();
	}
	
}
