package com.github.irybov.bankdemoboot.service;

import java.time.OffsetDateTime;
//import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemoboot.controller.dto.OperationResponse;
import com.github.irybov.bankdemoboot.dao.OperationDAO;
import com.github.irybov.bankdemoboot.entity.Operation;
//import com.github.irybov.bankdemoboot.model.OperationPage;

@Service
@Transactional(readOnly = true, noRollbackFor = Exception.class)
public class OperationServiceDAO implements OperationService {

	@Autowired
	private OperationDAO operationDAO;
	
	public Operation getOne(long id) {
		return operationDAO.getById(id);
	}
	public List<OperationResponse> getAll(int id) {
		
//	    Comparator<Operation> compareById = Comparator.comparing(Operation::getId);	    
		return operationDAO.getAll(id)
				.stream()
//				.sorted(compareById)
				.map(OperationResponse::new)
				.collect(Collectors.toList());
	}
/*	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public Page<OperationResponseDTO> getPage(int id, OperationPage page) {		
		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize());
		return operationDAO.getPage(id, pageable).map(OperationResponseDTO::new);
	}*/
	public Page<OperationResponse> getPage(int id, String action, double minval, double maxval,
			OffsetDateTime mindate, OffsetDateTime maxdate, Pageable pageable) {
		
//		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
//											page.getSortDirection(), page.getSortBy());
		return operationDAO.getPage(id, action, minval, maxval, mindate, maxdate, pageable)
				.map(OperationResponse::new);
	}
	
}
