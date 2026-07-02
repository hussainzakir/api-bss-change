package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "XBSS_EXCEPTION_TYPE")
public class ExceptionType implements Serializable{

	private static final long serialVersionUID = 1L;

	public ExceptionType() {
		super();
	}
	
	@Id
	@SequenceGenerator(name = "exceptionTypeSeq", sequenceName = "XBSS_EXCEPTION_TYPE_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exceptionTypeSeq")
	private long id;

	@Column(name = "NAME")
	private String exceptionName;
}
