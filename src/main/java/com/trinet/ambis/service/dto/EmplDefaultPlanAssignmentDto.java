package com.trinet.ambis.service.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author schaudhari
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmplDefaultPlanAssignmentDto implements Serializable {

	private static final long serialVersionUID = -1817456323378644924L;

	@NotNull
	private long companyId;

	@NotNull
	private String emplId;

	@NotNull
	private String planType;

	@NotNull
	private int portfolioId;

	@NotNull
	private String benefitPlanId;
	
	@NotNull
	private String coverageCode;

}
