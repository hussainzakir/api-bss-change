package com.trinet.ambis.persistence.template.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;

/**
 * The persistent class for the XBSS_REALM_PLYR_PLAN database table.
 */
@Data
@Entity
@Table(name = "XBSS_REALM_PLYR_PLAN")
@NamedQuery(name = "XbssRealmPlyrPlan.findAll", query = "SELECT x FROM XbssRealmPlyrPlan x")
public class XbssRealmPlyrPlan implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    private long id;

    @Column(name = "BENEFIT_PLAN")
    private String benefitPlan;

    @Column(name = "PLAN_TYPE")
    private String planType;

    @Column(name = "PORTFOLIO_ID")
    private BigDecimal portfolioId;

    @Column(name = "REALM_YEAR_ID")
    private BigDecimal realmYearId;

	@Column(name = "SITUS")
	private String situs;

	@Column(name = "BAND_LOCATOR")
	private String bandLocator;

	@Column(name = "HDHP")
	private boolean highDeductible;
	
	@Column(name = "PLAN_CATEGORY")
	private String planCategory;
	
	@Column(name = "WIDELY_AVAILABLE_FLAG")
	private boolean widelyAvailable;

	public XbssRealmPlyrPlan() {
		super();
	}

}
