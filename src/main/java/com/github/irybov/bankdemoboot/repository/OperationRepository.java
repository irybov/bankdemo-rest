package com.github.irybov.bankdemoboot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.irybov.bankdemoboot.entity.Operation;
import com.github.irybov.bankdemoboot.service.OperationAgent;

public interface OperationRepository extends JpaRepository<Operation, Long>, OperationAgent {

	List<Operation> findBySenderOrRecipientOrderByIdAsc(int sender, int recipient);
}
