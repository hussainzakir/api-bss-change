package com.trinet.ambis.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Model representing pay-in rate information for a specific coverage level/tier.
 * Used to retrieve pay-in rates by benefit plan ID.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PayInRateInfo implements Serializable {

    private static final long serialVersionUID = 2025013012345678901L;
    
    /**
     * Plan type identifier (e.g., "10" for medical, "15" for DP medical)
     */
    private String planType;
    
    /**
     * Coverage level/tier code (e.g., "1", "2", "C", "4" for regional plans;
     * "5", "6", "7", "8" for DP regional plans)
     */
    private String coverageLevel;
    
    /**
     * Pay-in rate amount for this coverage level
     */
    private double payInRate;
}
