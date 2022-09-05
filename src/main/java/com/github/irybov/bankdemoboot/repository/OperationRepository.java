package com.github.irybov.bankdemoboot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.irybov.bankdemoboot.entity.Operation;

public interface OperationRepository extends JpaRepository<Operation, Long> {

	List<Operation> findBySenderOrRecipientOrderByIdDesc(int sender, int recipient);
	Page<Operation> findBySenderOrRecipient(int sender, int recipient, Pageable pageable);
}
