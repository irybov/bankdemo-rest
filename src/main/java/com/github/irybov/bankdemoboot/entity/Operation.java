package com.github.irybov.bankdemoboot.entity;

import java.time.OffsetDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name="operations", schema = "bankdemo")
@ToString
public class Operation {

	@EqualsAndHashCode.Exclude
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@NotNull
	@Builder.Default
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private final OffsetDateTime timestamp = OffsetDateTime.now();
	
	@NotNull
	private double amount;

	@NotNull
	private String action;
	
	@NotNull
	@Size(min=3, max=3)
	private String currency;
	
	private int sender;
		
	private int recipient;
	
}
