package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * @author schaudhari
 *
 */
@Data
@Embeddable
public class HQExceptionsId implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "REALM_YEAR_ID")
	private long realmYrId;
	@Column(name = "COMPANY")
	private String company;

}
