package com.github.irybov.bankdemoboot.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.entity.Operation;
import com.github.irybov.bankdemoboot.model.OperationPage;

public interface OperationService {

	public void transfer(double amount, String action, String currency, int sender, int recipient);
	public void deposit(double amount, String action, String currency, int recipient);
	public void withdraw(double amount, String action, String currency, int sender);
	public Operation getOne(long id);
	public List<OperationResponseDTO> getAll(int id);
//	public Page<OperationResponseDTO> getPage(int id, OperationPage page);
	public Page<OperationResponseDTO> getPage(int id, String action, double minval, double maxval,
			OperationPage page);
}
