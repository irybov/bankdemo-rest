package com.github.irybov.bankdemoboot.model;

import org.springframework.data.domain.Sort;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountPage {

    private int number = 0;
    private int size = 20;
    private Sort.Direction direction = Sort.Direction.ASC;
    private String sortBy = "id";
	
}
