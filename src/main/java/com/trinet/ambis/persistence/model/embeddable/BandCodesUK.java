package com.trinet.ambis.persistence.model.embeddable;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BandCodesUK implements Serializable {

	private static final long serialVersionUID = 4308380831188090762L;

	@Column(name = "COMPANY_ID")
	@NotNull
	private long companyId;

	@Column(name = "BAND_CODE_TYPE")
	@NotNull
	private String bandCodeType;

	@Column(name = "EFFECTIVE_DT")
	@NotNull
	private Date effectiveDt;

}
