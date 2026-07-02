package com.trinet.ambis.service.model;
import lombok.Data;


@Data
public class MappedHeadCount {
	
    private String planType;
    private String currentBenefitPlanId;
    private String futureBenefitPlanId;
    private String coverageCode;
    private int headCount;

}
