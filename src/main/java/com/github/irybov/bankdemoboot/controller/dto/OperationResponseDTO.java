package com.github.irybov.bankdemoboot.controller.dto;

import java.time.OffsetDateTime;

import com.github.irybov.bankdemoboot.entity.Operation;

import lombok.Getter;

@Getter
public class OperationResponseDTO {

	private Long id;
	private OffsetDateTime createdAt;
	private Double amount;
	private String action;
	private String currency;	
	private Integer sender;		
	private Integer recipient;
	
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
