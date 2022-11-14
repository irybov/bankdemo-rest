package com.github.irybov.bankdemoboot.model;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.github.irybov.bankdemoboot.entity.Operation;

@Component
public class OperationSpecifications {
	
	public static Specification<Operation> hasOperation(String action){
		
		if(action == null) return null;		
		return(root, query, builder) -> {return builder.equal(root.get("action"), action);};
	}
	
}
