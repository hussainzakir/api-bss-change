package com.trinet.ambis.service.model;

import java.util.Date;

import lombok.Data;

@Data
public class OLPProcessStatus {

	private long olpId;
	private int status;
	private Date updateDate;

}
