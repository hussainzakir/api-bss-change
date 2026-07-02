package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The primary key class for the XBSS_REALM_REGION_MIN_FUNDING database table.
 * 
 */
@Embeddable
@ToString
@Getter
@Setter
public class RealmRegionMinFundingPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="OE_QUARTER")
	private String oeQuarter;

	@Column(name="REGION")
	private String region;

	@Column(name="EFFDT")
	private java.sql.Date effdt;

	/* default constructor required for DAO actions */
	public RealmRegionMinFundingPK() {
	}

	public RealmRegionMinFundingPK( String oeQuarter, String region, java.sql.Date effdt ) {
		this.setOeQuarter( oeQuarter );
		this.setRegion( region );
		this.setEffdt( effdt );
	}

}
