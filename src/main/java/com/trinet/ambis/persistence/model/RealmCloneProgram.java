package com.trinet.ambis.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EmbeddedId;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trinet.ambis.util.JsonDateDeserializer;
import com.trinet.ambis.util.JsonDateSerializer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xbss_realm_clone_pgm")
public class RealmCloneProgram {
	
	@EmbeddedId
	private RealmCloneProgramId id;

	@Column (name = "CLONE_BENPGM")	
	private String cloneProgram;

	@Column (name="CLONE_K1_BENPGM")	
	private String cloneK1Program;

	@Column (name="CLONE_COMPANY")	
	private String cloneCompany;

	@JsonDeserialize(using = JsonDateDeserializer.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	@Column(name = "ENDDT")
	private java.sql.Date enddt;

}
