package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "XBSS_EXCEPTION_TYPE_ATTRIBUTE")
public class ExceptionTypeAttribute implements Serializable{

	private static final long serialVersionUID = 1L;

	public ExceptionTypeAttribute() {
		super();
	}
	
	@Id
	@SequenceGenerator(name = "exceptionTypeAttributeSeq", sequenceName = "XBSS_EXCEP_TYPE_ATTRI_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exceptionTypeAttributeSeq")
	private long id;

	@ManyToOne
	@JoinColumn(name = "EXCEPTION_TYPE_ID")
	private ExceptionType exceptionType;

	@ManyToOne
	@JoinColumn(name = "ATTRIBUTE_ID")
	private Attribute attribute;

	@Column(name = "ATTRIBUTE_VALUE")
	private String attributeValue;
}
