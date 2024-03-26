package com.github.irybov.bankdemorest.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
//import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemorest.controller.dto.OperationResponse;
import com.github.irybov.bankdemorest.entity.Operation;
import com.github.irybov.bankdemorest.entity.QOperation;
import com.github.irybov.bankdemorest.jpa.OperationJPA;
import com.github.irybov.bankdemorest.mapper.OperationMapper;
import com.github.irybov.bankdemorest.page.OperationPage;
import com.github.irybov.bankdemorest.util.QPredicate;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

@Service
@Transactional(readOnly = true, noRollbackFor = Exception.class)
public class OperationServiceJPA implements OperationService {
	
	@Autowired
	private OperationMapper mapStruct;
//	@Autowired
//	private ModelMapper modelMapper;
	
	@Autowired
	private OperationJPA operationJPA;
	
	public OperationResponse getOne(long id) {
//		return modelMapper.map(operationJPA.getReferenceById(id), OperationResponse.class);
		return mapStruct.toDTO(operationJPA.getReferenceById(id));
	}
	public List<OperationResponse> getAll(int id) {

//		return operationJPA.findBySenderOrRecipientOrderByIdDesc(id, id)
//				.stream()
//				.map(source -> modelMapper.map(source, OperationResponse.class))
//				.collect(Collectors.toList());
		return mapStruct.toList(operationJPA.findBySenderOrRecipientOrderByIdDesc(id, id));
	}
/*	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public Page<OperationResponseDTO> getPage(int id, OperationPage page){
		
		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
				Sort.by("id").descending());
		return operationRepository.findBySenderOrRecipient(id, id, pageable)
				.map(OperationResponseDTO::new);
	}*/
	public Page<OperationResponse> getPage(int id, String action, Double minval, Double maxval,
			OffsetDateTime mindate, OffsetDateTime maxdate, Pageable pageable){
		
//		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
//											page.getSortDirection(), page.getSortBy());
/*	
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(QOperation.operation.action.like("%"+action+"%"));
		predicates.add(QOperation.operation.sender.eq(id).or
				(QOperation.operation.recipient.eq(id)));
		predicates.add(QOperation.operation.amount.between(minval, maxval));
		predicates.add(QOperation.operation.createdAt.between(mindate, maxdate));
*/
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
//		Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
//				Sort.Direction.DESC, new OperationPage().getSortBy());
		
//		return operationJPA.findAll(ExpressionUtils.allOf(predicates), pageable)
		return operationJPA.findAll(where, pageable)				
//				.map(source -> modelMapper.map(source, OperationResponse.class));
				.map(source -> mapStruct.toDTO(source));
	}
	
	@Transactional(readOnly = false, propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
	public void save(Operation operation) {operationJPA.saveAndFlush(operation);}
	
}
