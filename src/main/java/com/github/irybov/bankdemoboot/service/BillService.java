package com.github.irybov.bankdemoboot.service;

import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.entity.Bill;

public interface BillService {

	public void saveBill(Bill bill);
	public void updateBill(Bill bill);
	public void deleteBill(int id);
	public String deposit(int id, double valueOf) throws Exception;
	public String withdraw(int id, double valueOf) throws Exception;
	public String transfer(int id, double valueOf, int target) throws Exception;
	public boolean changeStatus(int id) throws Exception;
	public BillResponseDTO getBillDTO(int id) throws Exception;
}
