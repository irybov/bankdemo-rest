package com.github.irybov.bankdemoboot.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.Role;
import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.dao.AccountDAO;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.exception.RegistrationException;
import com.github.irybov.bankdemoboot.entity.Account;

@Service
@Transactional
public class AccountServiceDAO implements AccountService {

	@Autowired
	AccountServiceDAO accountService;
	@Autowired
	private AccountDAO accountDAO;
	
	@Autowired
	@Qualifier("billServiceAlias")
	private BillService billService;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public void saveAccount(AccountRequestDTO accountRequestDTO) throws Exception {
		
		LocalDate birthday = LocalDate.parse(accountRequestDTO.getBirthday());
		if (LocalDate.from(birthday).until(LocalDate.now(), ChronoUnit.YEARS) < 18) {
			throw new RegistrationException("You must be 18+ to register");
		}
		
		Account account = new Account(accountRequestDTO.getName(), accountRequestDTO.getSurname(),
				accountRequestDTO.getPhone(), birthday, BCrypt.hashpw
				(accountRequestDTO.getPassword(), BCrypt.gensalt(4)), true);
		account.addRole(Role.CLIENT);
		try {
			accountDAO.saveAccount(account);
		}
		catch (RuntimeException exc) {
			throw new PersistenceException("Database exception: this number is already in use.");
		}
	}
	
	@Transactional(readOnly = true)
	public AccountResponseDTO getAccountDTO(String phone) {
		return new AccountResponseDTO(accountService.getAccount(phone));
	}
	@Transactional(readOnly = true)
	Account getAccount(String phone) {
		return accountDAO.getAccount(phone);
	}
	@Transactional(readOnly = true)
	public AccountResponseDTO getById(int id) {
		return new AccountResponseDTO(accountDAO.getById(id));
	}
	
	public void updateAccount(Account account) {
		accountDAO.updateAccount(account);
	}
	
	@Transactional(readOnly = true)
	public boolean verifyAccount(String phone, String current){
		if(getAccount(phone).getPhone() == null || !phone.equals(current)) {
			return false;
		}
		return true;
	}
	@Transactional(readOnly = true)
	public String getPhone(String phone){
		return accountService.getAccount(phone).getPhone();
	}
	@Transactional(readOnly = true)
	public List<BillResponseDTO> getBills(int id) {
		List<Bill> bills = accountDAO.getById(id).getBills();
		return bills.stream().map(BillResponseDTO::new).collect(Collectors.toList());
	}
	
	public BillResponseDTO addBill(String phone, String currency) {
		Account account = accountService.getAccount(phone);
		Bill bill = new Bill(currency, true, account);
		billService.saveBill(bill);
		account.addBill(bill);
		accountService.updateAccount(account);
		return new BillResponseDTO(bill);
	}
	
	public void changeStatus(String phone) {
		
		Account account = accountService.getAccount(phone);
		if(account.isActive()) {
			account.setActive(false);
		}
		else {
			account.setActive(true);
		}
		accountService.updateAccount(account);
	}
	
	public void changePassword(String phone, String password) {
		Account account = accountService.getAccount(phone);
		account.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(4)));
		accountService.updateAccount(account);
	}
	@Transactional(readOnly = true)
	public boolean comparePassword(String oldPassword, String phone) {
		Account account = accountService.getAccount(phone);
		return bCryptPasswordEncoder.matches(oldPassword, account.getPassword());
	}

	public Boolean changeStatus(int id) {
		
		Account account = accountDAO.getById(id);
		if(account.isActive()) {
			account.setActive(false);
		}
		else {
			account.setActive(true);
		}
		accountService.updateAccount(account);
		return account.isActive();
	}

	@Transactional(readOnly = true)
	public List<AccountResponseDTO> getAll() {
		return accountDAO.getAll()
				.stream()
				.map(AccountResponseDTO::new)
				.collect(Collectors.toList());
	}
	
}
