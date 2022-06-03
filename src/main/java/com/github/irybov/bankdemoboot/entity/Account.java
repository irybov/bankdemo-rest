package com.github.irybov.bankdemoboot.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.irybov.bankdemoboot.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="accounts")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Account implements UserDetails{

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@NotNull
	private boolean active = true;
	
	@NotBlank(message = "Name must not be empty")
	@Size(min=2, max=20, message = "Name should be 2-20 chars length")
	@Pattern(regexp = "^[A-Z][a-z]{1,19}", message = "Please input name like Xx")
	private String name;
	
	@NotBlank(message = "Surname must not be empty")
	@Size(min=2, max=40, message = "Surname should be 2-40 chars length")
	@Pattern(regexp = "^[A-Z][a-z]{1,19}([-][A-Z][a-z]{1,19})?",
			message = "Please input surname like Xx or Xx-Xx")
	private String surname;
	
	@NotBlank(message = "Phone number must not be empty")
	@Size(min=10, max=10, message = "Phone number should be 10 digits length")
	@Pattern(regexp = "^\\d{10}", message = "Please input phone like XXXXXXXXXX")
	@Column(unique=true)
	private String phone;
	
	@NotNull(message = "Please select your date of birth")
	@Column(columnDefinition = "date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;
	
	@NotBlank(message = "Password must not be empty")
	@Size(min=10, message = "Password should be 10-50 symbols length")
	private String password;
	
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="account_id")
	private List<Bill> bills;
	
	@ElementCollection(targetClass = Role.class)
	@CollectionTable(name="roles", joinColumns = @JoinColumn(name="account_id"))
	@Enumerated(EnumType.STRING)
	private Set<Role> roles;
	
	public Account(String name, String surname, String phone, Date birthday, String password) {
		this.name = name;
		this.surname = surname;
		this.phone = phone;
		this.birthday = birthday;
		this.password = password;
	}	
	
	public void addBill(Bill bill) {
		if(bills == null) {
			bills = new ArrayList<>();
		}
		bills.add(bill);
	}

	public void addRole(Role role) {
		if(roles == null) {
			roles = new HashSet<>();
		}
		roles.add(role);
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles;
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
		return active;
	}

}
