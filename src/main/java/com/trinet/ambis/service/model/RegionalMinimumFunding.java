package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.ToString;

@ToString
public class RegionalMinimumFunding {
    
    private String region;
    
    private BigDecimal fundingPct;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public BigDecimal getFundingPct() {
        return fundingPct;
    }

    public void setFundingPct(BigDecimal fundingPct) {
        this.fundingPct = fundingPct;
    }

}
