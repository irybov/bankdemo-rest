package com.github.irybov.bankdemorest.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
//import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;

import com.github.irybov.bankdemorest.controller.dto.OperationResponse;
import com.github.irybov.bankdemorest.entity.Operation;
import com.github.irybov.bankdemorest.entity.QOperation;
import com.github.irybov.bankdemorest.jpa.OperationJPA;
import com.github.irybov.bankdemorest.mapper.OperationMapper;
import com.github.irybov.bankdemorest.misc.Action;
import com.github.irybov.bankdemorest.page.OperationPage;
import com.github.irybov.bankdemorest.util.QPredicate;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("/test-operations-h2.sql")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class OperationJPATest {
	
	@Autowired
	private OperationJPA operationJPA;

	//@Execution(ExecutionMode.CONCURRENT)
	@ParameterizedTest
	@CsvSource({"1, 1, 3", "2, 2, 2", "3, 3, 3"})
	void test_findBySenderOrRecipientOrderByIdDesc(int sender, int recipient, int quantity) {
		
	    Comparator<Operation> compareById = Comparator.comparing(Operation::getId).reversed();	
		List<Operation> operations =
				operationJPA.findBySenderOrRecipientOrderByIdDesc(sender, recipient);
		assertThat(operations.size()).isEqualTo(quantity);
		assertThat(operations).isSortedAccordingTo((compareById));
	}
	
	//@Execution(ExecutionMode.CONCURRENT)
	@ParameterizedTest
	@MethodSource("params")
	void test_findAllBySpecs(int id, String action, double minval, double maxval,
			OffsetDateTime mindate, OffsetDateTime maxdate, int quantity) {
		
		OperationPage page = new OperationPage();
		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
				page.getSortDirection(), page.getSortBy());
		
//		ModelMapper modelMapper = new ModelMapper();
		OperationMapper mapStruct = Mappers.getMapper(OperationMapper.class);
//		List<Predicate> predicates = new ArrayList<>();
//		predicates.add(QOperation.operation.action.like(action));
//		predicates.add(QOperation.operation.sender.eq(id).or
//				(QOperation.operation.recipient.eq(id)));
//		predicates.add(QOperation.operation.amount.between(minval, maxval));
//		predicates.add(QOperation.operation.createdAt.between(mindate, maxdate));
		Predicate or = QPredicate.builder()
				.add(id, QOperation.operation.sender::eq)
				.add(id, QOperation.operation.recipient::eq)
				.buildOr();
		Predicate and = QPredicate.builder()
				.add(action, QOperation.operation.action::like)
				.add(minval, maxval, QOperation.operation.amount::between)
				.add(mindate, maxdate, QOperation.operation.createdAt::between)
				.buildAnd();
		Predicate where = ExpressionUtils.allOf(or, and);
		
		Page<OperationResponse> resultPage = operationJPA.findAll
//				(ExpressionUtils.allOf(predicates), pageable)
				(where, pageable)
//				.map(source -> modelMapper.map(source, OperationResponse.class));
				.map(source -> mapStruct.toDTO(source));
		
		assertThat(resultPage.getContent().size()).isEqualTo(quantity);
	}
	private static Stream<Arguments> params() {
		return Stream.of(Arguments.of(1, "deposit",  100.00, 700.00,
				OffsetDateTime.now().minusDays(1L), OffsetDateTime.now().plusDays(1L), 1),
				Arguments.of(2, "transfer",  200.00, 900.00,
						OffsetDateTime.now().minusDays(1L), OffsetDateTime.now().plusDays(1L), 2));
	}
	
	@Test
	void test_saveAndFlush() {
		
		Operation.OperationBuilder builder = Operation.builder();
		Operation operation = builder
			.amount(0.00)
			.action(Action.EXTERNAL.name().toLowerCase())
			.currency("SEA")
			.createdAt(OffsetDateTime.now())
			.bank("Demo")
			.build();
		
		Operation entity = operationJPA.saveAndFlush(operation);
		assertThat(entity.getId() == 1);
	}
	
}
