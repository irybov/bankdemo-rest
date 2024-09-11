package com.github.irybov.service.mapper;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import com.github.irybov.database.entity.Account;
import com.github.irybov.database.entity.Bill;
import com.github.irybov.service.dto.AccountResponse;
import com.github.irybov.service.dto.BillResponse;

@Mapper(componentModel = "spring", 
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL, 
	injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BillMapper {
	
	default AccountResponse toDTOfromAccount(Account entity,
            @Context CycleAvoidingMappingContext cycleAvoidingMappingContext) {
		return Mappers.getMapper(AccountMapper.class).toDTO(entity, cycleAvoidingMappingContext);
	}
	
	BillResponse toDTO(Bill entity,
            @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

	List<BillResponse> toList(List<Bill> entities,
            @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);
	
}
