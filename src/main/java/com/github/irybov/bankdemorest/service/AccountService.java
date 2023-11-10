package com.github.irybov.bankdemorest.service;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import com.github.irybov.bankdemorest.controller.dto.AccountRequest;
import com.github.irybov.bankdemorest.controller.dto.AccountResponse;
import com.github.irybov.bankdemorest.controller.dto.BillResponse;

public interface AccountService {

	public void saveAccount(AccountRequest accountRequestDTO) throws RuntimeException;
	public AccountResponse getAccountDTO(String phone) throws PersistenceException;
//	public void updateAccount(Account account);
	public boolean verifyAccount(String phone, String current) throws EntityNotFoundException;
	public BillResponse addBill(String phone, String currency) throws RuntimeException;
//	public void changeStatus(String phone) throws Exception;
	public void changePassword(String phone, String password) throws EntityNotFoundException;
	public boolean comparePassword(String oldPassword, String phone) throws EntityNotFoundException;
//	public AccountResponseDTO getById(int id);
	public Boolean changeStatus(int id);
	public Optional<String> getPhone(String phone);
//	public List<BillResponse> getBills(int id);
	public List<AccountResponse> getAll();
	public AccountResponse getFullDTO(String phone) throws PersistenceException;
}
