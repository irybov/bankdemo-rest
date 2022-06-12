package com.github.irybov.bankdemoboot.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.persistence.EntityExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.Role;
import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.dao.AccountDAO;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.exception.NotAdultAgeException;
import com.github.irybov.bankdemoboot.repository.AccountRepository;
import com.github.irybov.bankdemoboot.entity.Account;

@Service
@Transactional
public class AccountService {

	@Autowired
	private AccountRepository accountRepository;	
	@Autowired
	private AccountDAO accountDAO;	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public void saveAccount(AccountRequestDTO accountRequestDTO) throws Exception {
		
		LocalDate birthday = LocalDate.parse(accountRequestDTO.getBirthday());
		if (LocalDate.from(birthday).until(LocalDate.now(), ChronoUnit.YEARS) < 18) {			
			throw new NotAdultAgeException("You must be 18+ to register");
		}
		
		Account account = new Account(accountRequestDTO.getName(), accountRequestDTO.getSurname(),
				accountRequestDTO.getPhone(), birthday, BCrypt.hashpw
				(accountRequestDTO.getPassword(), BCrypt.gensalt(4)));
		account.addRole(Role.CLIENT);		
		try {
			accountDAO.saveAccount(account);
		} catch (EntityExistsException exc) {
			exc.printStackTrace();
		}
	}
	
	public AccountResponseDTO getAccountDTO(String phone) {
		return new AccountResponseDTO(getAccount(phone));
	}
	private Account getAccount(String phone) {
//		return accountDAO.getAccount(phone);
		return accountRepository.findByPhone(phone);
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
		Account account = getAccount(phone);
		account.addBill(new Bill(currency));
		updateAccount(account);
	}
	
	public void changeStatus(String phone) {
		Account account = getAccount(phone);
		if(account.isActive()) {
			account.setActive(false);
		}
		else {
			account.setActive(true);			
		}
		updateAccount(account);
	}
	
	public void changePassword(String phone, String password) {
		Account account = getAccount(phone);
		account.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(4)));
		updateAccount(account);
	}
	
	public boolean comparePassword(String oldPassword, String phone) {
		Account account = getAccount(phone);
		return bCryptPasswordEncoder.matches(oldPassword, account.getPassword());
	}
	
}
