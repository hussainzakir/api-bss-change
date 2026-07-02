package com.trinet.ambis.persistence.template.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The persistent class for the XBSS_REALM_TEMPLATE database table.
 * 
 */
@Entity
@Table(name="XBSS_REALM_TEMPLATE")
@NamedQuery(name="XbssRealmTemplate.findAll", query="SELECT x FROM XbssRealmTemplate x")
@ToString
@NoArgsConstructor
public class XbssRealmTemplate implements Serializable {
    
	private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "tbl-bss-gen")
    @TableGenerator(name = "tbl-bss-gen", allocationSize = 1, table = "XBSS_SEQUENCES_GENERATORS")
	@Column(unique=true, nullable=false, precision=19)
	private long id;

	@Column(name="REALM_YEAR_ID", precision=19)
	private BigDecimal realmYearId;

	//bi-directional many-to-one association to XbssTemplate
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="TEMPLATE_ID")
	private XbssTemplate xbssTemplate;

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public BigDecimal getRealmYearId() {
		return this.realmYearId;
	}

	public void setRealmYearId(BigDecimal realmYearId) {
		this.realmYearId = realmYearId;
	}

	public XbssTemplate getXbssTemplate() {
		return this.xbssTemplate;
	}

	public void setXbssTemplate(XbssTemplate xbssTemplate) {
		this.xbssTemplate = xbssTemplate;
	}

}
