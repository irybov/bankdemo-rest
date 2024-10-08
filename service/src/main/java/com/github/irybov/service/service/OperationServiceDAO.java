package com.github.irybov.service.service;

import java.time.OffsetDateTime;
//import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.database.dao.OperationDAO;
import com.github.irybov.database.entity.Operation;
import com.github.irybov.service.dto.OperationResponse;
import com.github.irybov.service.mapper.OperationMapper;

@Service
@Transactional(readOnly = true, noRollbackFor = Exception.class)
public class OperationServiceDAO implements OperationService {
	
	@Autowired
	private OperationMapper mapStruct;
//	@Autowired
//	private ModelMapper modelMapper;

	@Autowired
	private OperationDAO operationDAO;
	
	public OperationResponse getOne(long id) {
//		return modelMapper.map(operationDAO.getById(id), OperationResponse.class);
		return mapStruct.toDTO(operationDAO.getById(id));
	}
	public List<OperationResponse> getAll(int id) {
		
//	    Comparator<Operation> compareById = Comparator.comparing(Operation::getId);	    
/*		return operationDAO.getAll(id)
				.stream()
//				.sorted(compareById)
				.map(source -> modelMapper.map(source, OperationResponse.class))
				.collect(Collectors.toList());*/
		return mapStruct.toList(operationDAO.getAll(id)); 
	}
/*	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	public Page<OperationResponseDTO> getPage(int id, OperationPage page) {		
		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize());
		return operationDAO.getPage(id, pageable).map(OperationResponseDTO::new);
	}*/
	public Page<OperationResponse> getPage(int id, String action, Double minval, Double maxval,
			OffsetDateTime mindate, OffsetDateTime maxdate, Pageable pageable) {
		
//		Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(),
//											page.getSortDirection(), page.getSortBy());
		return operationDAO.getPage(id, action, minval, maxval, mindate, maxdate, pageable)
//				.map(source -> modelMapper.map(source, OperationResponse.class));
				.map(source -> mapStruct.toDTO(source));
	}
	
	@Transactional(readOnly = false, propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
	public void save(Operation operation) {operationDAO.save(operation);}
	
}
