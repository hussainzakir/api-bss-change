package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.trinet.ambis.persistence.model.embeddable.BandCodesUK;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "XBSS_BAND_CODES")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BandCodes implements Serializable {

	private static final long serialVersionUID = -1574090329711985251L;

	@EmbeddedId
	private BandCodesUK bandCodesUK;

	@Column(name = "BAND_CODE_VAL")
	@NotNull
	private String bandCodeVal;

}