package com.github.irybov.bankdemoboot.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.irybov.bankdemoboot.security.Role;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="accounts", schema = "bankdemo",
uniqueConstraints={@UniqueConstraint(columnNames={"phone"})})
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "phone", cacheStrategy=CacheStrategy.LAZY)
public class Account{

//	@EqualsAndHashCode.Exclude
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

//	@EqualsAndHashCode.Exclude
//	@NotNull
	@ToString.Exclude
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private OffsetDateTime createdAt;
	
//	@EqualsAndHashCode.Exclude
//	@NotNull
	@ToString.Exclude
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private OffsetDateTime updatedAt;
	
	@NotNull
	private boolean isActive;
	
	@NotBlank(message = "Name must not be empty")
	@Size(min=2, max=20, message = "Name should be 2-20 chars length")
	@Pattern(regexp = "^[A-Z][a-z]{1,19}", message = "Please input name like Xx")
	@Column(nullable = false)
	private String name;
	
	@NotBlank(message = "Surname must not be empty")
	@Size(min=2, max=40, message = "Surname should be 2-40 chars length")
	@Pattern(regexp = "^[A-Z][a-z]{1,19}([-][A-Z][a-z]{1,19})?",
			message = "Please input surname like Xx or Xx-Xx")
	@Column(nullable = false)
	private String surname;
	
	@NotBlank(message = "Phone number must not be empty")
	@Size(min=10, max=10, message = "Phone number should be 10 digits length")
	@Pattern(regexp = "^\\d{10}$", message = "Please input phone like XXXXXXXXXX")
	@Column(unique=true, nullable = false, length=10)
	private String phone;
	
	@Past(message = "Birthday cant be future time")
	@NotNull(message = "Please select your date of birth")
	@Column(columnDefinition = "date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthday;
	
	@NotBlank(message = "Password must not be empty")
	@Size(min=10, max=100, message = "Password should be 10-50 symbols length")
	@Column(nullable = false)
	private String password;
	
//	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(mappedBy="owner", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
//	@JoinColumn(name="account_id")
	private List<Bill> bills;

//	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ElementCollection(targetClass = Role.class, fetch=FetchType.EAGER)
	@CollectionTable(name="roles", joinColumns = @JoinColumn(name="account_id"))
	@Enumerated(EnumType.STRING)
	private Set<Role> roles;
	
	public Account(String name, String surname, String phone, LocalDate birthday, String password,
			boolean isActive) {
		this.name = name;
		this.surname = surname;
		this.phone = phone;
		this.birthday = birthday;
		this.password = password;
		this.isActive = isActive;
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
	
	@PrePersist
	protected void onCreate() {
		createdAt = OffsetDateTime.now();
	}
	@PreUpdate
	protected void onUpdate() {
		updatedAt = OffsetDateTime.now();
	}

}
