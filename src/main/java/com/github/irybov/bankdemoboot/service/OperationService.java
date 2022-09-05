package com.github.irybov.bankdemoboot.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.entity.Operation;

public interface OperationService {

	public void transfer(double amount, String action, String currency, int sender, int recipient);
	public void deposit(double amount, String action, String currency, int recipient);
	public void withdraw(double amount, String action, String currency, int sender);
	public Operation get(long id);
	public List<OperationResponseDTO> getAll(int id);
	public Page<OperationResponseDTO> getPage(int id, Pageable pageable);
}
