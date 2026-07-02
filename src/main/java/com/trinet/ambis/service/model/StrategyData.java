package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Data;

@Data
public class StrategyData implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonUnwrapped
    private StrategySummary strategySummary;

    private List<StrategyBenefitGroup> benefitGroups = new ArrayList<>();
    
    private StrategyHsaFundingDto strategyHsaFunding;

    private boolean isCached;
}
