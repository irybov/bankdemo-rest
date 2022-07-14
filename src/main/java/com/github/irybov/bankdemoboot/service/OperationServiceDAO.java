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

@Service
@Transactional
public class OperationServiceDAO implements OperationService {

	@Autowired
	private OperationDAO operationDAO;
	
	public void transfer(double amount, String action, String currency, int sender, int recipient) {
		
		Operation operation = Operation.builder()
				.amount(amount)
				.action(action)
				.currency(currency)
				.sender(sender)
				.recipient(recipient)
				.build();
		operationDAO.save(operation);
	}
	
	public void deposit(double amount, String action, String currency, int recipient) {
		
		Operation operation = Operation.builder()
				.amount(amount)
				.action(action)
				.currency(currency)
				.recipient(recipient)
				.build();
		operationDAO.save(operation);
	}
	
	public void withdraw(double amount, String action, String currency, int sender) {
		
		Operation operation = Operation.builder()
				.amount(amount)
				.action(action)
				.currency(currency)
				.sender(sender)
				.build();
		operationDAO.save(operation);
	}
	
	public Operation get(long id) {
		return operationDAO.getById(id);
	}
	
	public List<OperationResponseDTO> getAll(int id) {
		
	    Comparator<Operation> compareById = Comparator.comparing(Operation::getId);	    
		return operationDAO.getAll(id)
				.stream()
				.sorted(compareById)
				.map(OperationResponseDTO::new)
				.collect(Collectors.toList());
	}
	
}
