package com.github.irybov.bankdemoboot.dao;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.entity.Operation;

@Sql("/test-data-h2.sql")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class OperationDAOTest {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@InjectMocks
	private OperationDAO operationDAO;
	
	@BeforeAll
	void prepare() {
		ReflectionTestUtils.setField(operationDAO, "entityManager", entityManager);
	}

	@Test
	void test_getAll() {
		List<Operation> operations = operationDAO.getAll(1);
		assertThat(operations.size()).isEqualTo(3);
	}

}
