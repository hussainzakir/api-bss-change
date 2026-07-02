package com.trinet.ambis.rest.controllers.dto.outputs;

import lombok.Data;

@Data
@lombok.ToString
public class VariableInstructions {

	private Page css;
	private Page mainPage;
	private Page coverPage;
	private Page totalCostComparePage;
	private Page eeCostComparePage;
	private Page appendixCoverPage;
    private Page appendixCoverPageDuplicate;
    private Page appendixCoverOMS;
    private Page appendixDisclaimerOMS;
    private Page appendixMedical;
	private Page appendixDental;
	private Page appendixVision;
	private Page compareMedical;
	private Page compareDental;
	private Page compareVision;
	private Page compareLife;
	private Page compareDisable;
	private Page compareDisableAndLife;
	private Page fundingSummaryPage;
	private Page disclaimerPage;
	private Page medicalPlanOffer;
	private Page dentalPlanOffer;
	private Page visionPlanOffer;
	private Page appendixDisabilityAllPlans;
	private Page appendixLifeADAndDAllPlans;
	private Page eeCostComparePageMedical;
	private Page eeCostComparePageDental;
	private Page eeCostComparePageVision;

}
