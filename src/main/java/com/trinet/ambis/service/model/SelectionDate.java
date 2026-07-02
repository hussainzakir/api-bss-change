package com.trinet.ambis.service.model;

import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class SelectionDate {

	public SelectionDate(Date internalOpenDate, Date internalCloseDate, Date externalOpenDate, Date externalCloseDate) {
		super();
		this.internalOpenDate = internalOpenDate;
		this.internalCloseDate = internalCloseDate;
		this.externalOpenDate = externalOpenDate;
		this.externalCloseDate = externalCloseDate;

	}

	public SelectionDate() {
		super();
	}

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date internalOpenDate;

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date internalCloseDate;

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date externalOpenDate;

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date externalCloseDate;

}