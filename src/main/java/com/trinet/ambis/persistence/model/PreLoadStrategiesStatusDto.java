package com.trinet.ambis.persistence.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreLoadStrategiesStatusDto {

	private String userId;
	private String preloadDate;
	private String status;
	private String value;
	private String type;
 

}
