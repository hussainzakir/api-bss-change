package com.trinet.ambis.service.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BenConfirmationStatement {

	@JsonIgnore
	long strategyId;
	@JsonIgnore
	long realmYrId;
	@JsonIgnore
	Date planYrStartDate;
	@JsonIgnore
	Date planYrEndDate;
	@JsonIgnore
	Date effectiveDate;
	String submitType;
	String submitUser;
	String confirmationNumber;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss.SSSZ", timezone = JsonFormat.DEFAULT_TIMEZONE)
	Date submittedDate;
	
	@JsonProperty("startDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
	Date statementStartDate;
	
	@JsonProperty("endDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
	Date statementEndDate;
	String url;
}
