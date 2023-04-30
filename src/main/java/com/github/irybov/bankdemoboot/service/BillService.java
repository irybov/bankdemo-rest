package com.github.irybov.bankdemoboot.service;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.entity.Operation;

public interface BillService {

	public void saveBill(Bill bill);
//	public void updateBill(Bill bill);
	public void deleteBill(int id);
	public void deposit(Operation operation) throws Exception;
	public void withdraw(Operation operation) throws Exception;
	public void transfer(Operation operation) throws Exception;
	public void external(Operation operation) throws Exception;
	public boolean changeStatus(int id);
	public BillResponseDTO getBillDTO(int id) throws EntityNotFoundException;
	public List<BillResponseDTO> getAll(int id);
}
