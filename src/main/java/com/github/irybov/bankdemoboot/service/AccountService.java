package com.github.irybov.bankdemoboot.service;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.entity.Account;

public interface AccountService {

	public void saveAccount(AccountRequestDTO accountRequestDTO) throws Exception;
	public AccountResponseDTO getAccountDTO(String phone);
	public void updateAccount(Account account);
	public boolean verifyAccount(String phone, String current);
	public BillResponseDTO addBill(String phone, String currency);
	public void changeStatus(String phone);
	public void changePassword(String phone, String password);
	public boolean comparePassword(String oldPassword, String phone);
	public AccountResponseDTO getById(int id);
	public Boolean changeStatus(int id);
	public String getPhone(String phone);
	
}
