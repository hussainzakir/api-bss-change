package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OutputData {

    private String companyName;
    private String strategyName;
    private String primaryCarrierName;
    private TitlePageData titlePage;
    private String isContingent;
    private boolean generatePlanAppendixOnly;
    private boolean tibCompany;

    private BenefitTypeEmployeeCostSummary employeeCostSummary;

    private Map<String, PlanAppendix> planAppendix;
    private BenefitCostSummary benefitCostSummary;
    private FundingSummary fundingSummary;
    Map<String, BasePlanComparison> planComparison;
	private List<String> templateNames;
	private Map<String, Boolean> currStrategyIsBenTypeOffered;
    private Map<String, Boolean> trinetStrategyIsBenTypeOffered;
    private List<String> includedPlanTypes;
    private boolean employeeComparePageBreakEnabled;
    private boolean lifeAndDiPageBreakEnabled;
    private boolean mdvPageBreakEnabled;
    private String planappendixfirstBentype;
}
