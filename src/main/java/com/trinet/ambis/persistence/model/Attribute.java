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
@Table(name = "XBSS_ATTRIBUTE")
public class Attribute implements Serializable{

	private static final long serialVersionUID = 1L;

	public Attribute() {
		super();
	}
	
	@Id
	@SequenceGenerator(name = "attributeSeq", sequenceName = "XBSS_ATTRIBUTE_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attributeSeq")
	private long id;

	@Column(name = "NAME")
	private String attributeName;

}
