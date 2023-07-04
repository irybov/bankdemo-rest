package com.github.irybov.bankdemoboot.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.controller.dto.BillResponse;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.entity.Operation;
import com.github.irybov.bankdemoboot.exception.PaymentException;
import com.github.irybov.bankdemoboot.repository.BillRepository;
import com.github.irybov.bankdemoboot.repository.OperationRepository;

@Service
@Transactional
public class BillServiceJPA implements BillService {
	
//	@Autowired
//	BillServiceJPA billService;
	@Autowired
	private BillRepository billRepository;
	@Autowired
	private OperationRepository operationRepository;
	
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveBill(Bill bill) {
		billRepository.save(bill);
	}
	
	@Transactional(propagation = Propagation.MANDATORY)
	void updateBill(Bill bill) {
		billRepository.save(bill);
	}
	
	public void deleteBill(int id) {
		billRepository.deleteById(id);
	}
	
	@Transactional(propagation = Propagation.MANDATORY, readOnly = true)
	Bill getBill(int id) throws EntityNotFoundException {
		return billRepository.findById(id).orElseThrow
				(()-> new EntityNotFoundException("Target bill with id: " + id + " not found"));
	}
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public BillResponse getBillDTO(int id) throws EntityNotFoundException {
		return new BillResponse(getBill(id));
	}
	
	public void deposit(Operation operation) throws Exception {
		
		double amount = operation.getAmount();
		if(amount < 0.01) throw new PaymentException("Amount of money should be higher than zero");		

		int id = operation.getRecipient();
		Bill bill = getBill(id);
		bill.setBalance(bill.getBalance().add(BigDecimal.valueOf(amount)));
		updateBill(bill);
		operationRepository.save(operation);
	}
	
	public void withdraw(Operation operation) throws Exception {
		
		double amount = operation.getAmount();
		if(amount < 0.01) throw new PaymentException("Amount of money should be higher than zero");

		int id = operation.getSender();
		Bill bill = getBill(id);
		if(bill.getBalance().compareTo(BigDecimal.valueOf(amount)) == -1) {
			throw new PaymentException("Not enough money to complete operation");
		}
		bill.setBalance(bill.getBalance().subtract(BigDecimal.valueOf(amount)));
		updateBill(bill);
		operationRepository.save(operation);
	}
	
	public void transfer(Operation operation) throws Exception {

		double amount = operation.getAmount();
		if(amount < 0.01) throw new PaymentException("Amount of money should be higher than zero");
		
		int from = operation.getSender();
		int to = operation.getRecipient();
		if(from == to) throw new PaymentException("Source and target bills should not be the same");		
		Bill target = getBill(to);
/*		if(target == null) {
			throw new PaymentException("Target bill with id: " + to + " not found");
		}*/
		
		Bill sender = getBill(from);
		if(!sender.getCurrency().equals(target.getCurrency())){
			throw new PaymentException("Wrong currency type of the target bill");
		}
		if(sender.getBalance().compareTo(BigDecimal.valueOf(amount)) == -1) {
			throw new PaymentException("Not enough money to complete operation");
		}
		
		target.setBalance(target.getBalance().add(BigDecimal.valueOf(amount)));
		sender.setBalance(sender.getBalance().subtract(BigDecimal.valueOf(amount)));
		updateBill(target);
		updateBill(sender);
		operationRepository.save(operation);
	}
	
	public void external(Operation operation) throws Exception {
		
		double amount = operation.getAmount();
		if(amount < 0.01) throw new PaymentException("Amount of money should be higher than zero");
		
		int from = operation.getSender();
		int to = operation.getRecipient();
		if(from == to) throw new PaymentException("Source and target bills should not be the same");
		
		Bill target = getBill(to);
		if(!operation.getCurrency().equals(target.getCurrency())){
			throw new PaymentException("Wrong currency type of the target bill");
		}
		target.setBalance(target.getBalance().add(BigDecimal.valueOf(amount)));
		updateBill(target);
		operationRepository.save(operation);
	}
	
	public void outward(Operation operation) throws Exception {
		
		double amount = operation.getAmount();
		if(amount < 0.01) throw new PaymentException("Amount of money should be higher than zero");
		
		int from = operation.getSender();
		int to = operation.getRecipient();
		if(from == to) throw new PaymentException("Source and target bills should not be the same");
		
		Bill sender = getBill(from);
		sender.setBalance(sender.getBalance().subtract(BigDecimal.valueOf(amount)));
		updateBill(sender);
		operationRepository.save(operation);
	}
	
	public boolean changeStatus(int id) {
		
		Bill bill = getBill(id);
		if(bill.isActive()) {
			bill.setActive(false);
		}
		else {
			bill.setActive(true);
		}
		updateBill(bill);
		return bill.isActive();
	}
	
	@Transactional(propagation = Propagation.MANDATORY, readOnly = true, 
			noRollbackFor = Exception.class)
	public List<BillResponse> getAll(int id) {		
//		List<Bill> bills = billRepository.getAll(id);
		List<Bill> bills = billRepository.findByOwnerId(id);
		return bills.stream().map(BillResponse::new).collect(Collectors.toList());		
	}
	
}
