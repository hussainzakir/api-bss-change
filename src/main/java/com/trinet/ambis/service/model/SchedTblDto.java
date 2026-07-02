package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.trinet.ambis.persistence.model.SchedTblId;

import lombok.Data;

/**
 * @author schaudhari
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchedTblDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonUnwrapped
	private SchedTblId sched;

	private String oeQuarter;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Los_Angeles")
	private Date openDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Los_Angeles")
	private Date closeDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Los_Angeles")
	private Date internalOpenDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Los_Angeles")
	private Date internalCloseDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Los_Angeles")
	private Date extensionEndDate;

	@JsonIgnore
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Los_Angeles")
	private Date updateTime;

	@JsonIgnore
	private String lastUpdatedBy;

}
