package com.github.irybov.bankdemoboot.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

//import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name="bills", schema = "bankdemo")
@Data
@NoArgsConstructor
public class Bill {

	@EqualsAndHashCode.Exclude
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

//	@NotNull
//	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private OffsetDateTime createdAt;
	
//	@NotNull
//	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private OffsetDateTime updatedAt;
	
	@NotNull
	private boolean isActive;
	
	@NotNull
	@Column(columnDefinition = "Decimal(19,2) default '0.00'", precision = 2)
//	@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
	private BigDecimal balance = BigDecimal.valueOf(0.00);

	@NotNull
	@Size(min=3, max=3)
	@Column(length=3)
	private String currency;
	
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH,
			CascadeType.REFRESH}, fetch=FetchType.EAGER)
	@JoinColumn(name="account_id", updatable = false)
	private Account owner;
	
	public Bill(String currency, boolean isActive, Account owner) {		
		this.currency = currency;
		this.isActive = isActive;
		this.owner = owner;
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
