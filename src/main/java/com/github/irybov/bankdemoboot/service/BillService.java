package com.github.irybov.bankdemoboot.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.dao.BillDAO;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.exception.BillNotFoundException;
import com.github.irybov.bankdemoboot.exception.NotEnoughMoneyException;
import com.github.irybov.bankdemoboot.exception.SameBillException;
import com.github.irybov.bankdemoboot.exception.WrongCurrencyTypeException;

@Service
@Transactional
public class BillService {

	@Autowired
	private BillDAO billDAO;
	
	public void saveBill(Bill bill) {
		billDAO.saveBill(bill);
	}
	
	public void updateBill(Bill bill) {
		billDAO.updateBill(bill);
	}
	
	public void deleteBill(int id) {
		billDAO.deleteBill(id);
	}
	
	public Bill getBill(int id) {
		return billDAO.getBill(id);
	}
	
	public String getPhone(int id) {
		return billDAO.getPhone(id);
	}
	
	public String deposit(int id, double amount) {
		Bill bill = getBill(id);
		bill.setBalance(bill.getBalance().add(new BigDecimal(amount)));
		updateBill(bill);
		return bill.getCurrency();
	}
	
	public String withdraw(int id, double amount) throws Exception {
		
		Bill bill = getBill(id);
		if(bill.getBalance().compareTo(BigDecimal.valueOf(amount)) == -1) {
			throw new NotEnoughMoneyException("Not enough money to complete operation");
		}
		bill.setBalance(bill.getBalance().subtract(new BigDecimal(amount)));
		updateBill(bill);
		return bill.getCurrency();
	}
	
	public String transfer(int id, double amount, int to) throws Exception {

		if(id == to) {
			throw new SameBillException("Source and target bills should not be the same");
		}		
		Bill target = getBill(to);
		if(target == null) {
			throw new BillNotFoundException("Target bill with id: " + to + " not found");
		}
		
		Bill bill = getBill(id);
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
	
}
