package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BenefitOffer implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonUnwrapped
    private BenefitOfferSummary summary;
    private Set<PlanCarrier> planCarriers;
    private PlanPackage planPackage;
    private List<BenefitPlan> benefitPlans = new ArrayList<>();
    private List<AdditionalBenefitOffer> additionalBenefitOffers = new ArrayList<>();
    
    public List<BenefitPlan> getBenefitPlans() {
        return benefitPlans;
    }

    public void setBenefitPlans(List<BenefitPlan> benefitPlans) {
        this.benefitPlans = benefitPlans;
    }

    public BenefitOfferSummary getSummary() {
        return summary;
    }

    public void setSummary(BenefitOfferSummary summary) {
        this.summary = summary;
    }

    public Set<PlanCarrier> getPlanCarriers() {
        return planCarriers;
    }

    public void setPlanCarriers(Set<PlanCarrier> planCarriers) {
        this.planCarriers = planCarriers;
    }

    public PlanPackage getPlanPackage() {
        return planPackage;
    }

    public void setPlanPackage(PlanPackage planPackage) {
        this.planPackage = planPackage;
    }

    public List<AdditionalBenefitOffer> getAdditionalBenefitOffers() {
        return additionalBenefitOffers;
    }

    public void setAdditionalBenefitOffers(List<AdditionalBenefitOffer> additionalBenefitOffers) {
        this.additionalBenefitOffers = additionalBenefitOffers;
    }

}
