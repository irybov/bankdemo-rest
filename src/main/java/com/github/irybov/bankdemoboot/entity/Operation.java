package com.github.irybov.bankdemoboot.entity;

import java.time.OffsetDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="operations")
@ToString
public class Operation {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@NotNull
	private double amount;

	@NotNull
	private String action;
	
	@NotNull
	private String currency;
	
	private int sender;
		
	private int recipient;

	@Builder.Default
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private final OffsetDateTime timestamp = OffsetDateTime.now();
	
}
