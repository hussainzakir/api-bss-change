package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "XBSS_APP_CONFIGURATIONS")
@Data
public class AppConfigurations implements Serializable {

	private static final long serialVersionUID = 1L;

	public AppConfigurations() {
		super();
	}

	@Id
	@Column(name = "KEY")
	private String key;
	@Column(name = "VALUE")
	private String value;
	@Column(name = "DESCRIPTION")
	private String desc;

}