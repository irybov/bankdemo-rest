package com.github.irybov.service.mapper;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import com.github.irybov.database.entity.Account;
import com.github.irybov.database.entity.Bill;
import com.github.irybov.service.dto.AccountRequest;
import com.github.irybov.service.dto.AccountResponse;
import com.github.irybov.service.dto.BillResponse;

@Mapper(componentModel = "spring", 
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL, 
	injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AccountMapper {
	
	default List<BillResponse> toListOfBills(List<Bill> entities,
            @Context CycleAvoidingMappingContext cycleAvoidingMappingContext){
		return Mappers.getMapper(BillMapper.class).toList(entities, cycleAvoidingMappingContext);		
	}
	
	Account toModel(AccountRequest input,
            @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

	AccountResponse toDTO(Account entity,
            @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);
	
	List<AccountResponse> toList(List<Account> entities,
            @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);
	
}
