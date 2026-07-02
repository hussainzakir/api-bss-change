package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trinet.ambis.util.JsonDateDeserializer;
import com.trinet.ambis.util.JsonDateSerializer;

import lombok.ToString;

@Entity
@Table(name = "XBSS_REALM_PLYR_CONFIGURATIONS")
@ToString
public class RealmPlanYearConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public RealmPlanYearConfiguration() {
		super();
	}

	@EmbeddedId
	private RealmPlanYearConfigurationId id;

	@Column(name = "VALUE")
	private String configValue;

	@Column(name = "DESCRIPTION")
	private String configDesc;

	@JsonDeserialize(using = JsonDateDeserializer.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	@Column(name = "ENDDT")
	private java.sql.Date enddt;


	public RealmPlanYearConfigurationId getId() {
		return id;
	}
	public void setId(RealmPlanYearConfigurationId id) {
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
	public void setConfigDesc( String configDesc ) {
		this.configDesc = configDesc;
	}

	public java.sql.Date getEnddt() {
		return this.enddt;
	}
	public void setEnddt( java.sql.Date enddt ) {
		this.enddt = enddt;
	}

}
