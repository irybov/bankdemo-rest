package com.github.irybov.bankdemoboot.model;

import org.springframework.data.domain.Sort;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationPage {
	
    private int number = 0;
    private int size = 10;
    private Sort.Direction direction = Sort.Direction.DESC;
    private String sortBy = "id";

}
