package com.github.irybov.bankdemoboot.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import com.github.irybov.bankdemoboot.entity.Operation;

@Sql("/test-data-h2.sql")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class OperationRepositoryTest {
	
	@Autowired
	private OperationRepository operationRepository;

	@Test
	void test_findBySenderOrRecipientOrderByIdDesc() {
		List<Operation> operations = operationRepository.findBySenderOrRecipientOrderByIdDesc(1, 1);
		assertThat(operations.size()).isEqualTo(3);
	}

}
