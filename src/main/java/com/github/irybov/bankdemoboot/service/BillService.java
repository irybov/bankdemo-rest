package com.github.irybov.bankdemoboot.service;

import com.github.irybov.bankdemoboot.entity.Bill;

public interface BillService {

	public void saveBill(Bill bill);
	public void updateBill(Bill bill);
	public void deleteBill(int id);
	public Bill getBill(int id) throws Exception;
	public String deposit(int id, double valueOf) throws Exception;
	public String withdraw(int id, double valueOf) throws Exception;
	public String transfer(int id, double valueOf, int target) throws Exception;
	public void changeStatus(int id) throws Exception;
}
