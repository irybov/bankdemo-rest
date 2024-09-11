package com.github.irybov.service.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

//import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.database.entity.Account;
import com.github.irybov.database.entity.Bill;
import com.github.irybov.database.entity.Role;
import com.github.irybov.database.jpa.AccountJPA;
import com.github.irybov.service.dto.AccountRequest;
import com.github.irybov.service.dto.AccountResponse;
import com.github.irybov.service.dto.BillRequest;
import com.github.irybov.service.dto.BillResponse;
import com.github.irybov.service.exception.RegistrationException;
import com.github.irybov.service.mapper.AccountMapper;
import com.github.irybov.service.mapper.BillMapper;
import com.github.irybov.service.mapper.CycleAvoidingMappingContext;

@Service
@Transactional
public class AccountServiceJPA implements AccountService {
	
	@Autowired
	private AccountMapper accountMapper;
	@Autowired
//	@Lazy
	private BillMapper billMapper;
//	@Autowired
//	private ModelMapper modelMapper;
//	@Autowired
//	AccountServiceJPA accountService;
	@Autowired
	private AccountJPA accountJPA;
	
	@Autowired
	@Qualifier("billServiceAlias")
	private BillService billService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public void saveAccount(AccountRequest accountRequest) throws RuntimeException {
		
//		LocalDate birthday = LocalDate.parse(accountRequestDTO.getBirthday());
		if (accountRequest.getBirthday().until(LocalDate.now(), ChronoUnit.YEARS) < 18) {
			throw new RegistrationException("You must be 18+ to register");
		}
		
//		Account account = modelMapper.map(accountRequest, Account.class);
		Account account = accountMapper.toModel(accountRequest, new CycleAvoidingMappingContext());
		bCryptPasswordEncoder.encode(accountRequest.getPassword());
		account.addRole(Role.CLIENT);
		
		try {
			accountJPA.saveAndFlush(account);
		}
		catch (RuntimeException exc) {
			throw new DataIntegrityViolationException(exc.getMessage());
		}
	}
	
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public AccountResponse getFullDTO(String phone) throws EntityNotFoundException {
		Optional<Account> optional = accountJPA.getWithBills(phone);
		Account account = optional.orElseThrow(() -> 
				new EntityNotFoundException("Account with phone " + phone + " not found"));
//		AccountResponse dto = modelMapper.map(account, AccountResponse.class);
		AccountResponse dto = accountMapper.toDTO(account, new CycleAvoidingMappingContext());
/*		dto.setBills(account.getBills().stream()
				.map(source -> modelMapper.map(source, BillResponse.class))
				.collect(Collectors.toList()));*/
		return dto;
	}	
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public AccountResponse getAccountDTO(String phone) throws EntityNotFoundException {
//		return modelMapper.map(getAccount(phone), AccountResponse.class);
		return accountMapper.toDTO(getAccount(phone), new CycleAvoidingMappingContext());
	}
//	@Transactional(propagation = Propagation.MANDATORY, readOnly = true)
	Account getAccount(String phone) throws EntityNotFoundException {
		Optional<Account> optional = accountJPA.findByPhone(phone);
		Account account = optional.orElseThrow(() -> 
				new EntityNotFoundException("Account with phone " + phone + " not found"));
		return account;
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
		accountJPA.save(account);
	}
	
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public boolean verifyAccount(String phone, String current) throws EntityNotFoundException{
		if(getAccount(phone).getPhone() == null || !phone.equals(current)) {
			return false;
		}
		return true;
	}
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public Optional<String> getPhone(String phone){
		return accountJPA.getPhone(phone);
	}
/*	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public List<BillResponse> getBills(int id){
//		List<Bill> bills = accountRepository.getById(id).getBills();
//		return bills.stream().map(BillResponseDTO::new).collect(Collectors.toList());		
		return billService.getAll(id);
		
	}*/
	
	public BillResponse addBill(BillRequest billRequest) throws RuntimeException {
		Account account = getAccount(billRequest.getPhone());
		Bill bill = new Bill(billRequest.getCurrency(), true, account);
		billService.saveBill(bill);
		account.addBill(bill);
		updateAccount(account);
//		return modelMapper.map(bill, BillResponse.class);
		return billMapper.toDTO(bill, new CycleAvoidingMappingContext());
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

		Optional<Account> optional = accountJPA.findById(id);
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
/*		return accountJPA.getAll()
				.stream()
//				.sorted((a1, a2) -> a1.getId() - a2.getId())
				.map(source -> modelMapper.map(source, AccountResponse.class))
				.collect(Collectors.toList());*/
		return accountMapper.toList(accountJPA.getAll(), new CycleAvoidingMappingContext());
	}
	
}
