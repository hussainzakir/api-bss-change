package com.trinet.ambis.service.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author tallam
 */

@Data
public class GroupFunding {
    private Long id;
    private String benefitProgram;
    private String name;
    private String waitTime;
    
    private Map<String, BenefitOfferFunding> offerTypeFunding = new HashMap<>();
    private ModelCompareStrategyHsaFunding hsaFunding = new ModelCompareStrategyHsaFunding();
    
}