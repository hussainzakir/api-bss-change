package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.ToString;

@Embeddable
@ToString
public class RealmConfigurationId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "REALM_ID")
	private long realmId;
	@Column(name = "KEY")
	private String configKey;

	public RealmConfigurationId(long realmId, String configKey) {
		super();
		this.realmId = realmId;
		this.configKey = configKey;
	}

	public RealmConfigurationId() {
	}

	public long getRealmId() {
		return realmId;
	}

	public void setRealmId(long realmId) {
		this.realmId = realmId;
	}

	public String getConfigKey() {
		return configKey;
	}

	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}

}