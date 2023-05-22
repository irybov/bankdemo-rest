package com.github.irybov.bankdemoboot.controller.dto;

import java.time.OffsetDateTime;

import com.github.irybov.bankdemoboot.entity.Operation;

import io.swagger.annotations.ApiModel;
import lombok.Getter;

@ApiModel
@Getter
public class OperationResponse {

//	private Long id;
	private OffsetDateTime createdAt;
	private Double amount;
	private String action;
	private String currency;	
	private Integer sender;		
	private Integer recipient;
	
	public OperationResponse(Operation operation) {
		
//		this.id = operation.getId();
		this.createdAt = operation.getCreatedAt();
		this.amount = operation.getAmount();
		this.action = operation.getAction();
		this.currency = operation.getCurrency();
		this.sender = operation.getSender();
		this.recipient = operation.getRecipient();
	}
	
}
