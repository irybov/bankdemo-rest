package com.github.irybov.bankdemoboot.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.dao.BillDAO;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.exception.BillNotFoundException;
import com.github.irybov.bankdemoboot.exception.NegativeAmountException;
import com.github.irybov.bankdemoboot.exception.NotEnoughMoneyException;
import com.github.irybov.bankdemoboot.exception.SameBillException;
import com.github.irybov.bankdemoboot.exception.WrongCurrencyTypeException;
import com.github.irybov.bankdemoboot.repository.BillRepository;

@Service
@Transactional
public class BillService {

	@Autowired
	private BillRepository billRepository;	
	@Autowired
	private BillDAO billDAO;
	
	public void saveBill(Bill bill) {
		billDAO.saveBill(bill);
	}
	
	public void updateBill(Bill bill) {
		billDAO.updateBill(bill);
	}
	
	public void deleteBill(int id) {
//		billDAO.deleteBill(id);
		billRepository.deleteById(id);
	}
	
	public Bill getBill(int id) {
//		return billDAO.getBill(id);
		return billRepository.getById(id);
	}
	
	public String getPhone(int id) {
		return billDAO.getPhone(id);
	}
	
	public String deposit(int id, double amount) throws Exception {
		
		if(amount < 0.01) {
			throw new NegativeAmountException("Amount of money should be higher than zero");
		}		
		
		Bill bill = getBill(id);
		bill.setBalance(bill.getBalance().add(new BigDecimal(amount)));
		updateBill(bill);
		return bill.getCurrency();
	}
	
	public String withdraw(int id, double amount) throws Exception {
		
		if(amount < 0.01) {
			throw new NegativeAmountException("Amount of money should be higher than zero");
		}
		
		Bill bill = getBill(id);
		if(bill.getBalance().compareTo(BigDecimal.valueOf(amount)) == -1) {
			throw new NotEnoughMoneyException("Not enough money to complete operation");
		}
		bill.setBalance(bill.getBalance().subtract(new BigDecimal(amount)));
		updateBill(bill);
		return bill.getCurrency();
	}
	
	public String transfer(int from, double amount, int to) throws Exception {

		if(amount < 0.01) {
			throw new NegativeAmountException("Amount of money should be higher than zero");
		}
		
		if(from == to) {
			throw new SameBillException("Source and target bills should not be the same");
		}		
		Bill target = getBill(to);
		if(target == null) {
			throw new BillNotFoundException("Target bill with id: " + to + " not found");
		}
		
		Bill bill = getBill(from);
		if(!bill.getCurrency().equals(target.getCurrency())){
			throw new WrongCurrencyTypeException("Wrong currency type of the target bill");
		}		
		if(bill.getBalance().compareTo(BigDecimal.valueOf(amount)) == -1) {
			throw new NotEnoughMoneyException("Not enough money to complete the operation");
		}
		
		bill.setBalance(bill.getBalance().subtract(new BigDecimal(amount)));
		updateBill(bill);
		target.setBalance(target.getBalance().add(new BigDecimal(amount)));
		updateBill(target);
		return bill.getCurrency();
	}
	
	public void changeStatus(int id) {
		Bill bill = getBill(id);
		if(bill.isActive()) {
			bill.setActive(false);
		}
		else {
			bill.setActive(true);
		}
		updateBill(bill);
	}
	
}
