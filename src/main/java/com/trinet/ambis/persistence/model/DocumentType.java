package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
/**
 * 
 * @author svemulapalli
 *
 */
@Entity
@Table(name="DM_DOCUMENT_TYPE",schema = "benefits_docmgmt")
@Data
@lombok.Generated
public class DocumentType implements Serializable {
	private static final long serialVersionUID = 475771723289396353L;
	
	@Id
	@Column(name="document_type_id")
	private Integer documentTypeId;
	
	@Column(name="document_type")
	private String docTypeName;
	
	private String description;
	
	@Column(name="exchange_id")
	private Integer exchangeId;

	@Column(name="effective_date_type")
	@JsonIgnore
	private String dateType;
}
