package com.github.irybov.bankdemoboot.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.dao.OperationDAO;
import com.github.irybov.bankdemoboot.entity.Operation;
import com.github.irybov.bankdemoboot.repository.OperationRepository;

@Service
@Transactional
public class OperationServiceImpl implements OperationService {

	@Autowired
	private OperationRepository operationAgent;
//	@Autowired
//	private OperationDAO operationAgent;
	
	public void transfer(double amount, String action, String currency, int sender, int recipient) {
		
		Operation operation = Operation.builder()
				.amount(amount)
				.action(action)
				.currency(currency)
				.sender(sender)
				.recipient(recipient)
				.build();
		operationAgent.save(operation);
	}
	
	public void deposit(double amount, String action, String currency, int recipient) {
		
		Operation operation = Operation.builder()
				.amount(amount)
				.action(action)
				.currency(currency)
				.recipient(recipient)
				.build();
		operationAgent.save(operation);
	}
	
	public void withdraw(double amount, String action, String currency, int sender) {
		
		Operation operation = Operation.builder()
				.amount(amount)
				.action(action)
				.currency(currency)
				.sender(sender)
				.build();
		operationAgent.save(operation);		
	}
	
	public Operation get(long id) {
		return operationAgent.getById(id);
	}
	
	public List<OperationResponseDTO> getAll(int id) {
		
/*	    Comparator<Operation> compareById = Comparator.comparing(Operation::getId);	    
		return operationAgent.getAll(id)
				.stream()
				.sorted(compareById)
				.map(OperationResponseDTO::new)
				.collect(Collectors.toList());*/
		return operationAgent.findBySenderOrRecipientOrderByIdAsc(id, id)
				.stream()
				.map(OperationResponseDTO::new)
				.collect(Collectors.toList());
	}
	
}
