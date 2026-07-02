package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString
public class RealmPlanYearRuleId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "OE_QUARTER")
	private String oeQuarter;
	@Column(name = "EFFDT")
	private java.sql.Date effdt;
	@Column(name = "KEY")
	private String ruleKey;

	public RealmPlanYearRuleId( String oeQuarter, java.sql.Date effdt, String ruleKey ) {
		super();
		this.setOeQuarter( oeQuarter );
		this.setEffdt( effdt );
		this.setRuleKey( ruleKey );
	}

	public RealmPlanYearRuleId() {
	}

}
