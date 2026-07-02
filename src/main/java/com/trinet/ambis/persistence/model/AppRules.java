package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Data;

@Entity
@Table(name = "XBSS_APP_RULES")
@Data
public class AppRules implements Serializable {

	private static final long serialVersionUID = 1L;

	public AppRules() {
		super();
	}

	@Id
	@Column(name = "KEY")
	private String key;
	@Column(name = "VALUE")
	@Type(type="boolean")
	private boolean value;
	@Column(name = "DESCRIPTION")
	private String desc;

}