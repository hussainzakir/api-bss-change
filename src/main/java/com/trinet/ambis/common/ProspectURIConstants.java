package com.trinet.ambis.common;

public class ProspectURIConstants {

	private ProspectURIConstants() {

	}

	public static final String PROSPECT_API_URI = "prospectApiUri";
	public static final String PROSPECT_STRATEGY_URI = "prospectStrategyUri";
	public static final String PROSPECT_EMPLOYEE_COSTS_BY_TYPES_URI = "prospectEmplLevelCostsByTypes";
	public static final String PROSPECT_INFO_URI = "/prospect-info";
	public static final String PROSPECT_UPDATE_EXPIRY_DATE = PROSPECT_INFO_URI + "/expiry-date";

	public static final String PROSPECT_CENSUS_URI = "/census-info";

	public static final String PROPERTY_EMPLOYEE_PLAN_ASSIGNMENT_API_URI = "prospectEmplPlanAssignmentUri";
	public static final String PROPERTY_PROSPECT_BENEFITS_PLANS_RATES_API_URI = "prospectBenefitsPlansRatesApiUri";
	public static final String PROPERTY_PATH_PARAM_PLANIDS = "planIds";

	public static final String BENEFIT_GROUP = "/benefit-group";
	public static final String EMPLOYEE_GROUP_ASSIGNMENT = BENEFIT_GROUP + "/employee-assignment";
	public static final String BENEFIT_GROUP_ID_PARAM = BENEFIT_GROUP + "/{groupId}";
	public static final String BENEFIT_GROUP_NAME = BENEFIT_GROUP_ID_PARAM + "/name";

	public static final String GROUP_FUNDING = BENEFIT_GROUP + "/{groupId}/funding";
	public static final String BENEFIT_PLANS = "/benefits-plans";

}