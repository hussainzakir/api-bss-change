package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author schaudhari
 *
 */
@Data
@Entity
@Table(name = "XBSS_HQ_EXCEPTION")
public class HQException implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private HQExceptionsId id;
	@Column(name = "STATE")
	private String hqState;
	@Column(name = "POSTAL_CODE")
	private String postalCode;

}
