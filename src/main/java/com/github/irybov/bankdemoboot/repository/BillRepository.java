package com.github.irybov.bankdemoboot.repository;

import java.util.List;

//import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.github.irybov.bankdemoboot.entity.Bill;

public interface BillRepository extends CrudRepository<Bill, Integer> {

//	@Query("SELECT b FROM Bill b WHERE b.owner.id=:id")
//	List<Bill> getAll(int id);
	
	List<Bill> findByOwnerId(int id);
}
