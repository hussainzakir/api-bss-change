package com.trinet.ambis.service.model;

import lombok.Getter;

@Getter
public class PlanTypePortfolio {
    private final Long portfolioId;
    private final String planType;

    public PlanTypePortfolio(Long portfolioId, String planType) {
        this.portfolioId = portfolioId;
        this.planType = planType;
    }

}