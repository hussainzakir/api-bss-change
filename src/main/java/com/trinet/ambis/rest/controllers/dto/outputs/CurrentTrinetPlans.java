package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.service.dto.CarrierAssetDto;
import com.trinet.ambis.service.dto.CmsLogoDto;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.prospect.dto.EmployeeHeadCountRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@ToString
public class CurrentTrinetPlans {
	private Optional<List<EmployeePlansRes>> prospectEmployees;
	private List<String> requestBenefitTypes;
	private Company company;
	private String strategyId;
	private Map<String, XbssRealmPlyrPlan> plyrPlanMap;
	private Map<String,Set<XbssRealmPlyrPlan>> realmPlyrPlans;
	
	private List<String> currentPlanIds;
	private List<String> trinetPlanIds;
	private Map<String,List<BenefitPlanCompare>> benefitTypeCurrentPlansAttributes;
	private Map<String,List<BenefitPlanCompare>> benefitTypeTrinetPlansAttributes;
	private List<EmployeeHeadCountRes> currentPlanHeadCounts;
	private List<EmployeeHeadCountRes> trinetPlanHeadCounts;
	private Map<String, RateDetail> currentPlanRates;
	private Map<String, RateDetail> trinetPlanRates;
	private Map<String, RateDetail> tibPlanRates;
	private Map<String, List<BenefitPlanRate>> trinetPlanRatesByCompany;
	// Current Plans : Map Key PlanId, Key coverageCode Value EmployeeId
	private Map<String, Map<String, List<String>>> currentPlanCoverageEmployeeMapping;
	private Map<String, Map<String, List<String>>> trinetPlanCoverageEmployeeMapping;
	
	//Map<BenefitPlanType,Map<CurrentPlanId,List<TrdnetPlanID>>>  
	private Map<String,Map<String,List<String>>> currentTrinetPlansMapping;
	
	//BenefitType Key
	private Map<String,BasePlanComparison> planComparison;
	
	//No current Plan Employees
	private List<EmployeePlansRes> noCurrentPlanEmployees;
	
	private HttpServletRequest httpRequest;

	@Builder.Default
	private Map<List<String>, String> cmsLogoDetailMap = new HashMap<>();

}
