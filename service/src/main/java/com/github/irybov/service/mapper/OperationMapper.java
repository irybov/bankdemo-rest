package com.github.irybov.service.mapper;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.github.irybov.database.entity.Operation;
import com.github.irybov.service.dto.OperationResponse;

@Mapper(componentModel = "spring", 
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL, 
	injectionStrategy = InjectionStrategy.FIELD)
public interface OperationMapper {

	OperationResponse toDTO(Operation entity);
	
	List<OperationResponse> toList(List<Operation> entities);
	
}
