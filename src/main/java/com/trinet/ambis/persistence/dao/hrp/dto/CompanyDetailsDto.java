package com.trinet.ambis.persistence.dao.hrp.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for company details extracted by company ID
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDetailsDto {

	private String code;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date planYearStart;

	private String cloneBenpgm;

	private Long bundleSeq;

	private String oeQuarter;

	private Integer naicsCode;

	private Integer largeDealProspect;

	private Long naicsBundleId;

	private String exchangeId;

}


