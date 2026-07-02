package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;
import lombok.Setter;

@JsonAutoDetect
@Getter
@Setter
@lombok.Generated
public class BenefitDocument implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

	private Integer qtrPlanYearDateId;

	private String title;
	private String description;
	private String url;
	private Date startDate;
	private Date endDate;
}
