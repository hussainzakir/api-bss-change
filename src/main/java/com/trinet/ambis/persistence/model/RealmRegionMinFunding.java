package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The persistent class for the XBSS_REALM_REGION_MIN_FUNDING database table.
 * 
 */
@Entity
@Table(name="XBSS_REALM_REGION_MIN_FUNDING")
@NamedQuery(name="RealmRegionMinFunding.findAll", query="SELECT x FROM RealmRegionMinFunding x")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class RealmRegionMinFunding implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private RealmRegionMinFundingPK id;

	@Column(name="MIN_FUNDING_PCT")
	private BigDecimal minFundingPct;

	@Column(name="ENDDT")
	private java.sql.Date enddt;

}
