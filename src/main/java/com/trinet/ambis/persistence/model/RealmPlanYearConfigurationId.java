package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trinet.ambis.util.JsonDateDeserializer;
import com.trinet.ambis.util.JsonDateSerializer;

import lombok.ToString;

@Embeddable
@ToString
public class RealmPlanYearConfigurationId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "OE_QUARTER")
	private String oeQuarter;

	@Column(name = "KEY")
	private String configKey;

	@JsonDeserialize(using = JsonDateDeserializer.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	@Column(name = "EFFDT")
	private java.sql.Date effdt;

	public RealmPlanYearConfigurationId( String oeQuarter, String configKey, java.sql.Date effdt ) {
		super();
		this.setOeQuarter( oeQuarter );
		this.setConfigKey( configKey );
		this.setEffdt( effdt );
	}

	public RealmPlanYearConfigurationId() {
	}

	public String getOeQuarter() {
		return this.oeQuarter;
	}
	public void setOeQuarter( String oeQuarter ) {
		this.oeQuarter = oeQuarter;
	}

	public String getConfigKey() {
		return configKey;
	}
	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}

	public java.sql.Date getEffdt() {
		return this.effdt;
	}
	public void setEffdt( java.sql.Date effdt ) {
		this.effdt = effdt;
	}

}
