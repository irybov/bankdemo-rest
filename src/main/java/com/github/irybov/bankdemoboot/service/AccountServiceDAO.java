package com.github.irybov.bankdemoboot.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.dao.AccountDAO;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.exception.RegistrationException;
import com.github.irybov.bankdemoboot.security.Role;
import com.github.irybov.bankdemoboot.entity.Account;

@Service
@Transactional
public class AccountServiceDAO implements AccountService {

//	@Autowired
//	AccountServiceDAO accountService;
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
				accountRequestDTO.getPhone(), birthday,
				bCryptPasswordEncoder.encode(accountRequestDTO.getPassword()), true);
		account.addRole(Role.CLIENT);
		try {
			accountDAO.saveAccount(account);
		}
		catch (RuntimeException exc) {
			throw new PersistenceException("This number is already in use.");
		}
	}
	
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public AccountResponseDTO getFullDTO(String phone) throws NoResultException {
		Account account = accountDAO.getWithBills(phone);
		if(account == null) {
			throw new NoResultException("Account with phone " + phone + " not found");
		}
		AccountResponseDTO dto = new AccountResponseDTO(account);
		dto.setBills(account.getBills().stream().map(BillResponseDTO::new).collect(Collectors.toList()));
		return dto;
	}
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public AccountResponseDTO getAccountDTO(String phone) throws NoResultException {
		return new AccountResponseDTO(getAccount(phone));
	}
	@Transactional(propagation = Propagation.MANDATORY, readOnly = true)
	Account getAccount(String phone) throws NoResultException {
		Account account = accountDAO.getAccount(phone);
		if(account == null) {
			throw new NoResultException("Account with phone " + phone + " not found");
		}
		return account;
	}
/*	@Transactional(readOnly = true)
	public AccountResponseDTO getById(int id) {
		return new AccountResponseDTO(accountDAO.getById(id));
	}*/
	@Transactional(propagation = Propagation.MANDATORY)
	void updateAccount(Account account) {
		accountDAO.updateAccount(account);
	}
	
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public boolean verifyAccount(String phone, String current) throws NoResultException{
		if(getAccount(phone).getPhone() == null || !phone.equals(current)) {
			return false;
		}
		return true;
	}
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public String getPhone(String phone){
		return accountDAO.getPhone(phone);
	}
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public List<BillResponseDTO> getBills(int id) {
//		List<Bill> bills = accountDAO.getById(id).getBills();
//		return bills.stream().map(BillResponseDTO::new).collect(Collectors.toList());
		return billService.getAll(id);
	}
	
	public BillResponseDTO addBill(String phone, String currency) throws Exception {
		Account account = getAccount(phone);
		Bill bill = new Bill(currency, true, account);
		billService.saveBill(bill);
		account.addBill(bill);
		updateAccount(account);
		return new BillResponseDTO(bill);
	}
	
/*	public void changeStatus(String phone) throws Exception {
		
		Account account = accountService.getAccount(phone);
		if(account.isActive()) {
			account.setActive(false);
		}
		else {
			account.setActive(true);
		}
		accountService.updateAccount(account);
	}*/
	
	public void changePassword(String phone, String password) throws NoResultException {
		Account account = getAccount(phone);
		account.setPassword(bCryptPasswordEncoder.encode(password));
		updateAccount(account);
	}
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public boolean comparePassword(String oldPassword, String phone) throws NoResultException {
		Account account = getAccount(phone);
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
		updateAccount(account);
		return account.isActive();
	}

	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public List<AccountResponseDTO> getAll() {
		return accountDAO.getAll()
				.stream()
//				.sorted((a1, a2) -> a1.getId() - a2.getId())
				.map(AccountResponseDTO::new)
				.collect(Collectors.toList());
	}
	
}
