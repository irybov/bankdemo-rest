package com.github.irybov.bankdemoboot.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.github.irybov.bankdemoboot.entity.Operation;

@Repository
public class OperationDAO {
	
	@Autowired
	private EntityManager entityManager;
	
	public void save(Operation operation) {
		entityManager.persist(operation);
	}
	
	public Operation get(long id) {
		return entityManager.find(Operation.class, id);
	}
	
	public void undo() {
		
	}
	
	public List<Operation> getAll(int id){
		return entityManager.createQuery
				("SELECT o FROM Operation o WHERE o.sender=:id OR o.recipient=:id",
				Operation.class)
				.setParameter("id", id)					
				.getResultList();
	}
	
}
