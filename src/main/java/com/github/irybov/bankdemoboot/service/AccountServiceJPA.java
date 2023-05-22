package com.github.irybov.bankdemoboot.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequest;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponse;
import com.github.irybov.bankdemoboot.controller.dto.BillResponse;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.exception.RegistrationException;
import com.github.irybov.bankdemoboot.repository.AccountRepository;
import com.github.irybov.bankdemoboot.security.Role;
import com.github.irybov.bankdemoboot.entity.Account;

@Primary
@Service
@Transactional
public class AccountServiceJPA implements AccountService {

//	@Autowired
//	AccountServiceJPA accountService;
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	@Qualifier("billServiceAlias")
	private BillService billService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public void saveAccount(AccountRequest accountRequestDTO) throws Exception {
		
//		LocalDate birthday = LocalDate.parse(accountRequestDTO.getBirthday());
		if (accountRequestDTO.getBirthday().until(LocalDate.now(), ChronoUnit.YEARS) < 18) {
			throw new RegistrationException("You must be 18+ to register");
		}
		
		Account account = new Account(accountRequestDTO.getName(), accountRequestDTO.getSurname(),
				accountRequestDTO.getPhone(), accountRequestDTO.getBirthday(),
				bCryptPasswordEncoder.encode(accountRequestDTO.getPassword()), true);
		account.addRole(Role.CLIENT);
		try {
			accountRepository.save(account);
		}
		catch (RuntimeException exc) {
			throw new DataIntegrityViolationException("Number " + account.getPhone() + " is already in use.");
		}
	}
	
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public AccountResponse getFullDTO(String phone) throws EntityNotFoundException {
		Account account = accountRepository.getWithBills(phone);
		if(account == null) {
			throw new EntityNotFoundException("Account with phone " + phone + " not found");
		}
		AccountResponse dto = new AccountResponse(account);
		dto.setBills(account.getBills().stream().map(BillResponse::new).collect(Collectors.toList()));
		return dto;
	}	
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public AccountResponse getAccountDTO(String phone) throws EntityNotFoundException {
		return new AccountResponse(getAccount(phone));
	}
	@Transactional(propagation = Propagation.MANDATORY, readOnly = true)
	Account getAccount(String phone) throws EntityNotFoundException {
		Account account = accountRepository.findByPhone(phone);
		if(account == null) {
			throw new EntityNotFoundException("Account with phone " + phone + " not found");
		}
		return account;
//		return accountRepository.getByPhone(phone);
	}
/*	@Transactional(readOnly = true)
	public AccountResponseDTO getById(int id) {
//		Optional<Account> account = accountRepository.findById(id);
//		return new AccountResponseDTO(account.get());
		Account account = accountRepository.getById(id);
		return new AccountResponseDTO(account);
	}*/
	@Transactional(propagation = Propagation.MANDATORY)
	void updateAccount(Account account) {
		accountRepository.save(account);
	}
	
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public boolean verifyAccount(String phone, String current) throws EntityNotFoundException{
		if(getAccount(phone).getPhone() == null || !phone.equals(current)) {
			return false;
		}
		return true;
	}
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public String getPhone(String phone){
		return accountRepository.getPhone(phone);
	}
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public List<BillResponse> getBills(int id){
//		List<Bill> bills = accountRepository.getById(id).getBills();
//		return bills.stream().map(BillResponseDTO::new).collect(Collectors.toList());		
		return billService.getAll(id);
		
	}
	
	public BillResponse addBill(String phone, String currency) throws Exception {
		Account account = getAccount(phone);
		Bill bill = new Bill(currency, true, account);
		billService.saveBill(bill);
		account.addBill(bill);
		updateAccount(account);
		return new BillResponse(bill);
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
	
	public void changePassword(String phone, String password) throws EntityNotFoundException {
		Account account = getAccount(phone);
		account.setPassword(bCryptPasswordEncoder.encode(password));
		updateAccount(account);
	}
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public boolean comparePassword(String oldPassword, String phone) throws EntityNotFoundException {
		Account account = getAccount(phone);
		return bCryptPasswordEncoder.matches(oldPassword, account.getPassword());
	}

	public Boolean changeStatus(int id) {

		Optional<Account> optional = accountRepository.findById(id);
		Account account = optional.get();
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
	public List<AccountResponse> getAll() {		
		return accountRepository.getAll()
				.stream()
				.sorted((a1, a2) -> a1.getId() - a2.getId())
				.map(AccountResponse::new)
				.collect(Collectors.toList());
	}
	
}
