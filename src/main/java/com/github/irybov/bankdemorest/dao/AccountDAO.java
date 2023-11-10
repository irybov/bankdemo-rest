package com.github.irybov.bankdemorest.dao;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.github.irybov.bankdemorest.entity.Account;
import com.github.irybov.bankdemorest.security.Role;

@Repository
public class AccountDAO {

	@Autowired
	private EntityManager entityManager;

	public void saveAccount(Account account) {
		entityManager.persist(account);
	}
	
	public void updateAccount(Account account) {
		if(entityManager.contains(account)) entityManager.merge(account);
		else saveAccount(account);
	}
	
	public void deleteAccount(Account account) {
		entityManager.remove(account);
	}

	public Account getAccount(String phone) {
		return entityManager.createQuery("SELECT a FROM Account a WHERE a.phone=:phone",
				Account.class)
				.setParameter("phone", phone)
				.getResultStream().findFirst().orElse(null);
//				.getSingleResult();
	}
	public Account getById(int id) {
		return entityManager.find(Account.class, id);
	}
	
	public Optional<String> getPhone(String check) {
		return entityManager.createQuery("SELECT phone FROM Account WHERE phone=:check",
				String.class)
				.setParameter("check", check)
				.getResultStream().findFirst();
//				.getSingleResult();
	}
	
	public List<Account> getAll() {
		return entityManager.createQuery
				("SELECT DISTINCT a FROM Account a JOIN a.roles r WHERE r=:role ORDER BY a.id ASC",
				Account.class)
				.setParameter("role", Role.CLIENT)
				.getResultList();
	}
	
	public Account getWithBills(String phone) {
		return entityManager.createQuery("SELECT a FROM Account a LEFT JOIN FETCH a.bills WHERE a.phone=:phone",
				Account.class)
				.setParameter("phone", phone)
				.getResultStream().findFirst().orElse(null);
//				.getSingleResult();
	}
	
	public Account getWithRoles(String phone) {
		Account account = entityManager.createQuery("SELECT a FROM Account a WHERE a.phone=:phone",
				Account.class)
				.setParameter("phone", phone)
				.getResultStream().findFirst().orElse(null);
//				.getSingleResult();
		Hibernate.initialize(account.getRoles());		
		return account;
	}
		
}
