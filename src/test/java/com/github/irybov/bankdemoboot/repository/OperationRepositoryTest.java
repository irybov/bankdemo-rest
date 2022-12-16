package com.github.irybov.bankdemoboot.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;

import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.entity.Operation;
import com.github.irybov.bankdemoboot.model.OperationPage;
import com.github.irybov.bankdemoboot.model.OperationSpecs;

@Sql("/test-data-h2.sql")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class OperationRepositoryTest {
	
	@Autowired
	private OperationRepository operationRepository;

	@ParameterizedTest
	@MethodSource("numbers")
	void test_findBySenderOrRecipientOrderByIdDesc(int sender, int recipient, int quantity) {
		List<Operation> operations =
				operationRepository.findBySenderOrRecipientOrderByIdDesc(sender, recipient);
		assertThat(operations.size()).isEqualTo(quantity);
	}
	private static Stream<Arguments> numbers() {
		return Stream.of(Arguments.of(1, 1, 3), Arguments.of(2, 2, 2), Arguments.of(3, 3, 3));
	}
	
	@ParameterizedTest
	@MethodSource("params")
	void test_findAllBySpecs(int id, String action, double minval, double maxval,
			OffsetDateTime mindate, OffsetDateTime maxdate, int quantity) {
		
		OperationPage page = new OperationPage();
		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
				page.getSortDirection(), page.getSortBy());
		
		Page<OperationResponseDTO> resultPage = operationRepository.findAll
				(Specification.where(OperationSpecs.hasAction(action)
				.and(OperationSpecs.hasOwner(id))
				.and(OperationSpecs.amountBetween(minval, maxval))
				.and(OperationSpecs.dateBetween(mindate, maxdate))), pageable)
				.map(OperationResponseDTO::new);
		
		assertThat(resultPage.getContent().size()).isEqualTo(quantity);
	}
	private static Stream<Arguments> params() {
		return Stream.of(Arguments.of(1, "deposit",  100.00, 700.00,
				OffsetDateTime.now().minusDays(1L), OffsetDateTime.now().plusDays(1L), 1),
				Arguments.of(2, "withdraw",  300.00, 500.00,
						OffsetDateTime.now().minusDays(1L), OffsetDateTime.now().plusDays(1L), 0));
	}
	
}
