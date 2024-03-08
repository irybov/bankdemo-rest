package com.github.irybov.bankdemorest.mapper;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.github.irybov.bankdemorest.controller.dto.OperationResponse;
import com.github.irybov.bankdemorest.entity.Operation;

@Mapper(componentModel = "spring", 
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL, 
	injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OperationMapper {

	OperationResponse toDTO(Operation entity);
	
	List<OperationResponse> toList(List<Operation> entities);
	
}
