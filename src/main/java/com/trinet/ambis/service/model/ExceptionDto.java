package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trinet.ambis.util.JsonDateDeserializer;
import com.trinet.ambis.util.JsonDateSerializer;

import lombok.Data;

/**
 * @author schaudhari
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ExceptionDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	@NotNull
	private String companyCode;
	private String companyName;
	@NotNull
	private String planType;

	@JsonDeserialize(using = JsonDateDeserializer.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	private Date startDate;

	@JsonDeserialize(using = JsonDateDeserializer.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	private Date endDate;

	private boolean active;
	@NotNull
	private String approverId;
	private String approverName;
	private Date createTime;
	private String createdById;
	private String createdByName;
	private String lastUpdatedById;
	private String lastUpdatedByName;
	private String exchange;
	@NotNull
	private long realmId;
	@NotNull
	private String quarter;

}
