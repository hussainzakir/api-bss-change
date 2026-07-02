package com.trinet.ambis.rest.controllers.dto.outputs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.trinet.ambis.rest.controllers.dto.planofferings.ReportDetails;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class BSSReportDetails extends ReportDetails {

	@JsonInclude(Include.NON_NULL)
	private OutputData data;

}
