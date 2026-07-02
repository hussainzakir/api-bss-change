package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The persistent class for the XBSS_MANDATORY_REGIONS database table.
 * 
 */
@Entity
@Table(name = "xbss_mandatory_regions")
@Getter
@Setter
@ToString
public class MandatoryRegion implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private MandatoryRegionPK id;

	@Column(name="ENDDT")
	private java.sql.Date enddt;

}