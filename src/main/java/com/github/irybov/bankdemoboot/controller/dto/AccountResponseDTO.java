package com.github.irybov.bankdemoboot.controller.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.github.irybov.bankdemoboot.Role;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.entity.Bill;

import lombok.Getter;

@Getter
public class AccountResponseDTO {

	private int id;
	private boolean active;	
	private String name;	
	private String surname;
	private String phone;	
	private LocalDate birthday;	
	private List<Bill> bills;
	private Set<Role> roles;

	public AccountResponseDTO(Account account) {
		this.id = account.getId();
		this.active = account.isActive();
		this.name = account.getName();
		this.surname = account.getSurname();
		this.phone = account.getPhone();
		this.birthday = account.getBirthday();
		this.bills = account.getBills();
		this.roles = account.getRoles();
	}
	
}
