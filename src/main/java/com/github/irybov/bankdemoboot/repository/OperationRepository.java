package com.github.irybov.bankdemoboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.irybov.bankdemoboot.entity.Operation;

public interface OperationRepository extends JpaRepository<Operation, Long> {

}
