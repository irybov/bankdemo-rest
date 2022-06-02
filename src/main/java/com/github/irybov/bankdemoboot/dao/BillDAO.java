package com.github.irybov.bankdemoboot.dao;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.github.irybov.bankdemoboot.entity.Bill;

@Repository
public class BillDAO {

	@Autowired
	private EntityManager entityManager;
	
	public void saveBill(Bill bill) {		
		entityManager.persist(bill);
	}
	
	public void updateBill(Bill bill) {		
		entityManager.merge(bill);
	}
	
	public void deleteBill(int id) {	
		Bill bill = getBill(id);
		entityManager.remove(bill);
	}
	
	public Bill getBill(int id) {
		return entityManager.find(Bill.class, id);
	}

	public String getPhone(int billId) {
		return (String) entityManager.createNativeQuery
		("SELECT phone FROM accounts WHERE id = (SELECT account_id FROM bills WHERE id=:billId)")
				.setParameter("billId", billId)				
				.getSingleResult();
	}
	
/*	public List<Integer> getAll(String billCurrency){
		return entityManager.createQuery("SELECT id FROM Bill WHERE currency=:billCurrency",
				Integer.class)
				.setParameter("billCurrency", billCurrency)
				.getResultList();
	}*/
	
}
