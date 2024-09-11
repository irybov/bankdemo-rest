package com.github.irybov.service.service;

import java.util.List;
import java.util.Optional;

import javax.persistence.PersistenceException;

import com.github.irybov.service.dto.AccountRequest;
import com.github.irybov.service.dto.AccountResponse;
import com.github.irybov.service.dto.BillRequest;
import com.github.irybov.service.dto.BillResponse;

public interface AccountService {

	public void saveAccount(AccountRequest accountRequestDTO) throws PersistenceException;
	public AccountResponse getAccountDTO(String phone) throws PersistenceException;
//	public void updateAccount(Account account);
	public boolean verifyAccount(String phone, String current) throws PersistenceException;
	public BillResponse addBill(BillRequest billRequest) throws PersistenceException;
//	public void changeStatus(String phone) throws Exception;
	public void changePassword(String phone, String password) throws PersistenceException;
	public boolean comparePassword(String oldPassword, String phone) throws PersistenceException;
//	public AccountResponseDTO getById(int id);
	public Boolean changeStatus(int id);
	public Optional<String> getPhone(String phone);
//	public List<BillResponse> getBills(int id);
	public List<AccountResponse> getAll();
	public AccountResponse getFullDTO(String phone) throws PersistenceException;
}
