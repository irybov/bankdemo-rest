package com.github.irybov.bankdemorest.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.github.irybov.bankdemorest.controller.dto.OperationResponse;
import com.github.irybov.bankdemorest.entity.Operation;

class OperationMapperTest {

	private OperationMapper mapStruct = Mappers.getMapper(OperationMapper.class);

	@Test
	void test() {
		
		String action = "UNKNOWN";
		Operation operation = Operation.builder().action(action).build();
		List<Operation> operations = new ArrayList<>();
		operations.add(operation);
		operations.add(new Operation());
		OperationResponse response = new OperationResponse();
		response.setAction(action);
		
		assertEquals(response.getAction(), mapStruct.toDTO(operation).getAction());
		assertThat(mapStruct.toDTO(operation).getClass()).hasSameClassAs(OperationResponse.class);
		assertEquals(operations.size(), mapStruct.toList(operations).size());
		assertThat(mapStruct.toList(operations)).hasSameClassAs(new ArrayList<OperationResponse>());
		
	}

}
