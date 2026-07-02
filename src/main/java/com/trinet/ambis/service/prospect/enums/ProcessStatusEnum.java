package com.trinet.ambis.service.prospect.enums;

import lombok.Getter;

@Getter
public enum ProcessStatusEnum {

	STRATEGY_SYNC_PLYR_CHANGE("STRATEGY_SYNC_PLYR_CHANGE", "PROSPECT_ID"),
	STRATEGY_CREATE_PROCESS("STRATEGY_CREATE", "COMPANY_CODE"),
	PRE_LOAD("PRE_LOAD", "QUARTER"),
	BAND_UPDATE_EVENT("BAND_UPDATE_EVENT", "PROSPECT_ID"),
	TERMED_CLIENT_DEFAULT_SUBMIT("TERM_DEFAULT", "CONF_NUMBER"),
	BAND_CODE_RESUBMIT_PROCESS("BANDCODE_RESUBMIT", "CONF_NUMBER"),
	RESUBMIT_PROCESS("RESUBMIT", "CONF_NUMBER"),
	SUBMIT_PROCESS("SUBMIT", "CONF_NUMBER"),
	QUARTER_CHANGE("QUARTER_CHANGE", "COMPANY_CODE");

	private final String processName;
	private final String identifierName;

	private ProcessStatusEnum(String processName, String identifierName) {
		this.processName = processName;
		this.identifierName = identifierName;
	}

}
