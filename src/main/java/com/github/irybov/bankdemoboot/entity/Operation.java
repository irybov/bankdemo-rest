package com.github.irybov.bankdemoboot.entity;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
	@Column(updatable = false)
	private Long id;
	
//	@NotNull
//	@Builder.Default
	@Setter
	@Column(updatable = false)
	private OffsetDateTime createdAt;
	
	@NotNull
	@Column(updatable = false)
	private Double amount;

	@NotNull
	@Column(updatable = false)
	private String action;
	
	@NotNull
	@Size(min=3, max=3)
	@Column(length=3, updatable = false)
	private String currency;
	
	@Column(updatable = false)
	private Integer sender;
	@Column(updatable = false)
	private Integer recipient;
	
}
