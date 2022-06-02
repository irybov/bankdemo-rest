package com.github.irybov.bankdemoboot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.dao.OperationDAO;
import com.github.irybov.bankdemoboot.entity.Operation;

@Service
@Transactional
public class OperationService {

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
	
	public void undo() {
		
	}
	
	public Operation get(long id) {
		return operationDAO.get(id);
	}
	
	public List<Operation> getAll(int id){
		return operationDAO.getAll(id);
	}
	
}
