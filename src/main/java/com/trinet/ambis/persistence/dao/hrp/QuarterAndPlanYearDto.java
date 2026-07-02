package com.trinet.ambis.persistence.dao.hrp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuarterAndPlanYearDto {

	private String benExchng;
	private String peoId;
	private String oeQuarter;
	private String planYearStart;

}
