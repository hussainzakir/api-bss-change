package com.trinet.ambis.rest.controllers.dto.planofferings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanOfferingsReportDetails extends ReportDetails {

	@JsonInclude(Include.NON_NULL)
	private PlanOfferingsData data;
}
