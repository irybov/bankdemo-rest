package com.github.irybov.bankdemoboot.service;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import com.github.irybov.bankdemoboot.controller.dto.BillResponse;
import com.github.irybov.bankdemoboot.entity.Bill;
import com.github.irybov.bankdemoboot.entity.Operation;
import com.github.irybov.bankdemoboot.exception.PaymentException;

public interface BillService {

	public void saveBill(Bill bill);
//	public void updateBill(Bill bill);
	public void deleteBill(int id);
	public void deposit(Operation operation) throws PaymentException;
	public void withdraw(Operation operation) throws PaymentException;
	public void transfer(Operation operation) throws PaymentException;
	public void external(Operation operation) throws PaymentException;
	public void outward(Operation operation) throws PaymentException;
	public boolean changeStatus(int id);
	public BillResponse getBillDTO(int id) throws EntityNotFoundException;
	public List<BillResponse> getAll(int id);
}
