package com.github.irybov.bankdemoboot.service;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
//import com.github.irybov.bankdemoboot.entity.Account;

public interface AccountService {

	public void saveAccount(AccountRequestDTO accountRequestDTO) throws Exception;
	public AccountResponseDTO getAccountDTO(String phone) throws EntityNotFoundException;
//	public void updateAccount(Account account);
	public boolean verifyAccount(String phone, String current) throws EntityNotFoundException;
	public BillResponseDTO addBill(String phone, String currency) throws Exception;
//	public void changeStatus(String phone) throws Exception;
	public void changePassword(String phone, String password) throws EntityNotFoundException;
	public boolean comparePassword(String oldPassword, String phone) throws EntityNotFoundException;
//	public AccountResponseDTO getById(int id);
	public Boolean changeStatus(int id);
	public String getPhone(String phone);
	public List<BillResponseDTO> getBills(int id);
	public List<AccountResponseDTO> getAll();
}
