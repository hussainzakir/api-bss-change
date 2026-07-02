package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trinet.ambis.util.JsonDateDeserializer;
import com.trinet.ambis.util.JsonDateSerializer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@ToString
@Getter
@Setter
public class RealmCloneProgramId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "OE_QUARTER")
	private String oeQuarter;

	@JsonDeserialize(using = JsonDateDeserializer.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	@Column(name = "EFFDT")
	private java.sql.Date effdt;

	public RealmCloneProgramId( String oeQuarter, java.sql.Date effdt ) {
		super();
		this.setOeQuarter( oeQuarter );
		this.setEffdt( effdt );
	}

	public RealmCloneProgramId() {
	}


	public void setEffdt( String string ) {
		this.effdt = java.sql.Date.valueOf( string );
	}
	public void setEffdt( java.sql.Date effdt ) {
		this.effdt = effdt;
	}

}
