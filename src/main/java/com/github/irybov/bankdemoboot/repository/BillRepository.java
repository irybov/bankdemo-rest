package com.github.irybov.bankdemoboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.irybov.bankdemoboot.entity.Bill;

public interface BillRepository extends JpaRepository<Bill, Integer> {

}
