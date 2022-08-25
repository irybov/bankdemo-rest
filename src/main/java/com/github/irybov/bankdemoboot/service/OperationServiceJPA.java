package com.github.irybov.bankdemoboot.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.entity.Operation;
import com.github.irybov.bankdemoboot.repository.OperationRepository;

@Service
@Transactional
public class OperationServiceJPA implements OperationService {

	@Autowired
	private OperationRepository operationRepository;
	
	public void transfer(double amount, String action, String currency, int sender, int recipient) {
		
		Operation operation = Operation.builder()
				.amount(amount)
				.action(action)
				.currency(currency)
				.sender(sender)
				.recipient(recipient)
				.build();
		operation.setCreatedAt(OffsetDateTime.now());
		operationRepository.save(operation);
	}
	
	public void deposit(double amount, String action, String currency, int recipient) {
		
		Operation operation = Operation.builder()
				.amount(amount)
				.action(action)
				.currency(currency)
				.recipient(recipient)
				.build();
		operation.setCreatedAt(OffsetDateTime.now());
		operationRepository.save(operation);
	}
	
	public void withdraw(double amount, String action, String currency, int sender) {
		
		Operation operation = Operation.builder()
				.amount(amount)
				.action(action)
				.currency(currency)
				.sender(sender)
				.build();
		operation.setCreatedAt(OffsetDateTime.now());
		operationRepository.save(operation);
	}
	
	@Transactional(readOnly = true)
	public Operation get(long id) {
		return operationRepository.getById(id);
	}
	@Transactional(readOnly = true)
	public List<OperationResponseDTO> getAll(int id) {

		return operationRepository.findBySenderOrRecipientOrderByIdAsc(id, id)
				.stream()
				.map(OperationResponseDTO::new)
				.collect(Collectors.toList());
	}
	
}
