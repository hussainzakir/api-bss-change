package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "xbss_realm")
@NoArgsConstructor
public class Realm implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	private long id;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "DESCR")
	private String description;

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name = "REALM_TYPE")
	private String realmType;
	
	public String getRealmType() {
		return realmType;
	}
	public void setRealmType(String realmType) {
		this.realmType = realmType;
	}
	@Column(name = "PEO_ID")
	private String peoid;

	public String getPeoid() {
		return peoid;
	}
	public void setPeoid(String peoid) {
		this.peoid = peoid;
	}
	@Column(name = "VERTICAL_CD")
	private String verticalCode;
	
	public String getVerticalCode() {
		return verticalCode;
	}

	public void setVerticalCode(String verticalCode) {
		this.verticalCode = verticalCode;
	}
	@Column(name = "BEN_EXCHNG")
	private String benExchange;

	public String getBenExchange() {
		return benExchange;
	}

	public void setBenExchange(String benExchange) {
		this.benExchange = benExchange;
	}
	
}
