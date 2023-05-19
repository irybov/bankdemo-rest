package com.github.irybov.bankdemoboot.controller.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
//import java.util.Collection;
import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
//import com.github.irybov.bankdemoboot.Role;
import com.github.irybov.bankdemoboot.entity.Account;
//import com.github.irybov.bankdemoboot.entity.Bill;

import io.swagger.annotations.ApiModel;
import lombok.Getter;

@ApiModel
@Getter
public class AccountResponseDTO {
	
	private Integer id;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;	
	private boolean active;	
	private String name;	
	private String surname;
	private String phone;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthday;
//	private String password;
	@JsonManagedReference
	private List<BillResponseDTO> bills;
//	private Set<Role> roles;

	public AccountResponseDTO(Account account) {
		
		this.id = account.getId();
		this.createdAt = account.getCreatedAt();
		this.updatedAt = account.getUpdatedAt();
		this.active = account.isActive();
		this.name = account.getName();
		this.surname = account.getSurname();
		this.phone = account.getPhone();
		this.birthday = account.getBirthday();
//		this.password = account.getPassword();
/*		this.bills = account.getBills()
				.stream()
//				.filter(Bill::isActive)
				.map(BillResponseDTO::new)
				.collect(Collectors.toList());*/
//		this.roles = account.getRoles();
	}	
	public void setBills(List<BillResponseDTO> bills) {
		this.bills = bills;
	}
	
}
