package com.trinet.ambis.service.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * @author tallam
 */
@Data
public class ModelCompareStrategy {

	private long id;

	private String name;

	@JsonIgnore
	private long companyId;

	@JsonIgnore
	private boolean submitted;
	
	@JsonIgnore
	private boolean history;

	private boolean activeStrategy = false;
	
	private boolean hasFundingOverrides;

	@JsonInclude(Include.NON_EMPTY)
	private List<GroupFunding> groupFundingList = new ArrayList<>();
	
	private List<StrategyGroupDetails> groups;
	

}