package com.github.irybov.bankdemorest.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.irybov.bankdemorest.controller.dto.OperationResponse;
import com.github.irybov.bankdemorest.entity.Operation;

//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = {OperationMapperImpl.class})
class OperationMapperTest {
	
//	@Autowired
//	private OperationMapper mapStruct;
	private OperationMapper mapStruct = Mappers.getMapper(OperationMapper.class);

	@Test
	void test() {
		
		String action = "UNKNOWN";
		Operation operation = Operation.builder().action(action).build();
		OperationResponse response = new OperationResponse();
		response.setAction(action);
		assertEquals(response.getAction(), mapStruct.toDTO(operation).getAction());
		
	}

}
