package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trinet.ambis.util.JsonDateDeserializer;
import com.trinet.ambis.util.JsonDateTimeDeserializer;
import com.trinet.ambis.util.JsonDateTimeSerializer;

import lombok.Data;

/**
 * @author schaudhari
 *
 */
@Data
public class SchedMidYearFundingDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;

	private long companyId;

	private String serviceOrderNumber;

	@JsonDeserialize(using = JsonDateDeserializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date midYearFundingEffDate;

	private boolean active;

	@JsonIgnore
	private String lastUpdatedBy;

	@JsonIgnore
	private Date updtdtm;

	private String companyCode;

	private String lastUpdatedByName;

	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	private Date updatedTime;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date planYearStartDate;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date planYearEndDate;

}
