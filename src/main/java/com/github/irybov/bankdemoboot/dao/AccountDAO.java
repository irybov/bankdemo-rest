package com.github.irybov.bankdemoboot.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.github.irybov.bankdemoboot.Role;
import com.github.irybov.bankdemoboot.entity.Account;

@Repository
public class AccountDAO {

	@Autowired
	private EntityManager entityManager;

	public void saveAccount(Account account) {
		entityManager.persist(account);
	}
	
	public void updateAccount(Account account) {
		entityManager.merge(account);
	}
	
	public void deleteAccount(Account account) {
		entityManager.remove(account);
	}

	public Account getAccount(String phone) {
		return entityManager.createQuery("SELECT a FROM Account a WHERE a.phone=:phone",
				Account.class)
				.setParameter("phone", phone)
				.getSingleResult();
	}
	public Account getById(int id) {
		return entityManager.find(Account.class, id);
	}
	
	public String getPhone(String check) {
		return entityManager.createQuery("SELECT phone FROM Account WHERE phone=:check",
				String.class)
				.setParameter("check", check)
				.getSingleResult();
//				.getResultStream().findFirst().orElse(null);
	}
	
	public List<Account> getAll(){
		return entityManager.createQuery
				("SELECT DISTINCT a FROM Account a JOIN a.roles r WHERE r=:role ORDER BY a.id ASC",
				Account.class)
				.setParameter("role", Role.CLIENT)
				.getResultList();
	}
	
}
