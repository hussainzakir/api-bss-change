package com.trinet.ambis.persistence.dao.ps.impl;

import lombok.Data;

@Data
public class BnRateTblForInsert {
	private String rateTblId;
	private String effdtStr;
	private String rateType;
	private String groupDescr;
	private String descrShort;
	private String pfClient;
}
