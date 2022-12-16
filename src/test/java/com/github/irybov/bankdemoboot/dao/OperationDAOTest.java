package com.github.irybov.bankdemoboot.dao;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.entity.Operation;
import com.github.irybov.bankdemoboot.model.OperationPage;

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

	@ParameterizedTest
	@MethodSource("numbers")
	void test_getAll(int id, int quantity) {
		List<Operation> operations = operationDAO.getAll(id);
		assertThat(operations.size()).isEqualTo(quantity);
	}
	private static Stream<Arguments> numbers() {
		return Stream.of(Arguments.of(1, 3), Arguments.of(2, 2), Arguments.of(3, 3));
	}
	
	@ParameterizedTest
	@MethodSource("params")
	void test_getPage(int id, String action, double minval, double maxval,
			OffsetDateTime mindate, OffsetDateTime maxdate, int quantity) {
		
		OperationPage page = new OperationPage();
		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
				page.getSortDirection(), page.getSortBy());
		
		Page<Operation> resultPage = operationDAO.getPage(id, action, minval, maxval,
														  mindate, maxdate, pageable);
		
		assertThat(resultPage.getContent().size()).isEqualTo(quantity);
	}
	private static Stream<Arguments> params() {
		return Stream.of(Arguments.of(1, "deposit",  100.00, 700.00,
				OffsetDateTime.now().minusDays(1L), OffsetDateTime.now().plusDays(1L), 1),
				Arguments.of(2, "withdraw",  300.00, 500.00,
						OffsetDateTime.now().minusDays(1L), OffsetDateTime.now().plusDays(1L), 0));
	}
	
}
