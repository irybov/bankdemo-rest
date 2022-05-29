package com.github.irybov.bankdemoboot.service;

import javax.persistence.EntityExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.Role;
import com.github.irybov.bankdemoboot.dao.AccountDAO;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.entity.Account;

@Service
@Transactional
public class AccountService {

	@Autowired
	private AccountDAO accountDAO;
	
	public void saveAccount(Account account) throws Exception {
		account.setPassword(BCrypt.hashpw(account.getPassword(), BCrypt.gensalt(4)));
		account.addRole(Role.CLIENT);
		try {
			accountDAO.saveAccount(account);
		} catch (EntityExistsException exc) {
			exc.printStackTrace();
		}
	}
	
	public Account getAccount(String phone) {
		return accountDAO.getAccount(phone);
	}
	
	public boolean verifyAccount(String phone, String current){
		if(accountDAO.checkPhone(phone) == null || !phone.equals(current)) {
			return false;
		}
		return true;
	}
	
	public void addBill(Account account, String currency) {
		account.addBill(new Bill(currency));
		accountDAO.updateAccount(account);
	}
	
	public void changeStatus(String phone) {
		Account account = accountDAO.getAccount(phone);
		if(account.isActive()) {
			account.setActive(false);
		}
		else {
			account.setActive(true);			
		}
		accountDAO.updateAccount(account);
	}
	
}
