package com.github.irybov.bankdemoboot.service;

import java.text.SimpleDateFormat;

import javax.persistence.EntityExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.Role;
import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.dao.AccountDAO;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.entity.Account;

@Service
@Transactional
public class AccountService {

	@Autowired
	private AccountDAO accountDAO;
	
	public void saveAccount(AccountRequestDTO accountRequestDTO) throws Exception {
		Account account = new Account(accountRequestDTO.getName(), accountRequestDTO.getSurname(),
				accountRequestDTO.getPhone(),
				new SimpleDateFormat("yyyy-MM-dd").parse(accountRequestDTO.getBirthday()),
				BCrypt.hashpw(accountRequestDTO.getPassword(), BCrypt.gensalt(4)));
		account.addRole(Role.CLIENT);
		try {
			accountDAO.saveAccount(account);
		} catch (EntityExistsException exc) {
			exc.printStackTrace();
		}
	}
	
	public AccountResponseDTO getAccount(String phone) {
		return new AccountResponseDTO(accountDAO.getAccount(phone));
	}
	
	public void updateAccount(Account account) {
		accountDAO.updateAccount(account);		
	}
	
	public boolean verifyAccount(String phone, String current){
		if(accountDAO.checkPhone(phone) == null || !phone.equals(current)) {
			return false;
		}
		return true;
	}
	
	public void addBill(String phone, String currency) {
		Account account = accountDAO.getAccount(phone);
		account.addBill(new Bill(currency));
		updateAccount(account);
	}
	
	public void changeStatus(String phone) {
		Account account = accountDAO.getAccount(phone);
		if(account.isActive()) {
			account.setActive(false);
		}
		else {
			account.setActive(true);			
		}
		updateAccount(account);
	}
	
	public void changePassword(String phone, String password) {
		Account account = accountDAO.getAccount(phone);
		account.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(4)));
		updateAccount(account);
	}
	
}
