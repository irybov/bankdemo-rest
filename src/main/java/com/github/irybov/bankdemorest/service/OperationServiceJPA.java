package com.github.irybov.bankdemorest.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemorest.controller.dto.OperationResponse;
import com.github.irybov.bankdemorest.entity.Operation;
import com.github.irybov.bankdemorest.entity.QOperation;
import com.github.irybov.bankdemorest.jpa.OperationJPA;
import com.github.irybov.bankdemorest.util.OperationSpecifications;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

@Service
@Transactional(readOnly = true, noRollbackFor = Exception.class)
public class OperationServiceJPA implements OperationService {

	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private OperationJPA operationJPA;
	
	public OperationResponse getOne(long id) {
		return modelMapper.map(operationJPA.getById(id), OperationResponse.class);
	}
	public List<OperationResponse> getAll(int id) {

		return operationJPA.findBySenderOrRecipientOrderByIdDesc(id, id)
				.stream()
				.map(source -> modelMapper.map(source, OperationResponse.class))
				.collect(Collectors.toList());
	}
/*	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public Page<OperationResponseDTO> getPage(int id, OperationPage page){
		
		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
				Sort.by("id").descending());
		return operationRepository.findBySenderOrRecipient(id, id, pageable)
				.map(OperationResponseDTO::new);
	}*/
	public Page<OperationResponse> getPage(int id, String action, double minval, double maxval,
			OffsetDateTime mindate, OffsetDateTime maxdate, Pageable pageable){
		
//		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
//											page.getSortDirection(), page.getSortBy());
/*		
		return operationJPA.findAll(OperationSpecifications.orderBy
					(OperationSpecifications.hasAction(action)
				.and(OperationSpecifications.hasOwner(id))
				.and(OperationSpecifications.amountBetween(minval, maxval))
				.and(OperationSpecifications.dateBetween(mindate, maxdate))), pageable)
					.map(source -> modelMapper.map(source, OperationResponse.class));
*/		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(QOperation.operation.action.like("%"+action+"%"));
		predicates.add(QOperation.operation.sender.eq(id).or
				(QOperation.operation.recipient.eq(id)));
		predicates.add(QOperation.operation.amount.between(minval, maxval));
		predicates.add(QOperation.operation.createdAt.between(mindate, maxdate));
		
		return operationJPA.findAll(ExpressionUtils.allOf(predicates), pageable)
				.map(source -> modelMapper.map(source, OperationResponse.class));
	}
	
}
