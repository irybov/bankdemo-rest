package com.github.irybov.bankdemoboot.repository;

import org.springframework.data.repository.CrudRepository;

import com.github.irybov.bankdemoboot.entity.Bill;

public interface BillRepository extends CrudRepository<Bill, Integer> {

}
