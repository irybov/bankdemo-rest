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

import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.dao.OperationDAO;
import com.github.irybov.bankdemoboot.entity.Operation;
//import com.github.irybov.bankdemoboot.model.OperationPage;

@Service
//@Transactional
public class OperationServiceDAO implements OperationService {

	@Autowired
	private OperationDAO operationDAO;
	
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public Operation getOne(long id) {
		return operationDAO.getById(id);
	}
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public List<OperationResponseDTO> getAll(int id) {
		
//	    Comparator<Operation> compareById = Comparator.comparing(Operation::getId);	    
		return operationDAO.getAll(id)
				.stream()
//				.sorted(compareById)
				.map(OperationResponseDTO::new)
				.collect(Collectors.toList());
	}
/*	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public Page<OperationResponseDTO> getPage(int id, OperationPage page) {		
		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize());
		return operationDAO.getPage(id, pageable).map(OperationResponseDTO::new);
	}*/
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public Page<OperationResponseDTO> getPage(int id, String action, double minval, double maxval,
			OffsetDateTime mindate, OffsetDateTime maxdate, Pageable pageable) {
		
//		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
//											page.getSortDirection(), page.getSortBy());
		return operationDAO.getPage(id, action, minval, maxval, mindate, maxdate, pageable)
				.map(OperationResponseDTO::new);
	}
	
}
