package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "XBSS_REALM_CONFIGURATIONS")
@ToString
public class RealmConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public RealmConfiguration() {
		super();
	}

	@EmbeddedId
	private RealmConfigurationId id;
	@Column(name = "VALUE")
	private String configValue;
	@Column(name = "DESCRIPTION")
	private String configDesc;

	public RealmConfigurationId getId() {
		return id;
	}

	public void setId(RealmConfigurationId id) {
		this.id = id;
	}

	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}

	public String getConfigDesc() {
		return configDesc;
	}

	public void setConfigDesc(String configDesc) {
		this.configDesc = configDesc;
	}

}