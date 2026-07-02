package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "XBSS_REALM_PLYR_RULES")
@Getter
@Setter
@ToString
public class RealmPlanYearRule implements Serializable {

	private static final long serialVersionUID = 1L;

	public RealmPlanYearRule() {
		super();
	}

	@EmbeddedId
	private RealmPlanYearRuleId id;
	@Column(name = "VALUE")
	private boolean ruleValue;
	@Column(name = "DESCRIPTION")
	private String ruleDesc;
	@Column(name = "ENDDT")
	private java.sql.Date enddt;

}
