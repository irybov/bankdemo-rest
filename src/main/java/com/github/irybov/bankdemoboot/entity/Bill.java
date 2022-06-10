package com.github.irybov.bankdemoboot.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="bills")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Bill {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private final OffsetDateTime timestamp = OffsetDateTime.now();
	
	@NotNull
	private boolean active = true;
	
	@NotNull
	@Column(columnDefinition = "Decimal(19,2) default '0.00'")
	@JsonFormat(shape=JsonFormat.Shape.NUMBER_FLOAT)
	private BigDecimal balance = new BigDecimal(0.00);

	@NotNull
	@Size(min=3, max=3)
	private String currency;
	
	public Bill(String currency) {
		this.currency = currency;
	}
	
}
