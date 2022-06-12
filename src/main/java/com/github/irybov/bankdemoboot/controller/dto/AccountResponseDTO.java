package com.github.irybov.bankdemoboot.controller.dto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.irybov.bankdemoboot.Role;
import com.github.irybov.bankdemoboot.entity.Account;

import lombok.Getter;

@Getter
public class AccountResponseDTO implements UserDetails{

	private static final long serialVersionUID = 1L;
	
	private int id;
	private boolean active;	
	private String name;	
	private String surname;
	private String phone;
	private LocalDate birthday;
	private String password;
	private List<BillResponseDTO> bills;
	private Set<Role> roles;

	public AccountResponseDTO(Account account) {
		this.id = account.getId();
		this.active = account.isActive();
		this.name = account.getName();
		this.surname = account.getSurname();
		this.phone = account.getPhone();
		this.birthday = account.getBirthday();
		this.password = account.getPassword();
		this.bills = account.getBills()
				.stream()
//				.filter(Bill::isActive)
				.map(BillResponseDTO::new)
				.collect(Collectors.toList());
		this.roles = account.getRoles();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.phone;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.active;
	}
	
}
