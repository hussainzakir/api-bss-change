package com.trinet.ambis.service.impl.planofferings;


import static com.trinet.ambis.common.BSSApplicationConstants.DENTAL_PLAN_TYPE;
import static com.trinet.ambis.common.BSSApplicationConstants.VISION_PLAN_TYPE;
import static com.trinet.ambis.common.BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE;
import static com.trinet.ambis.common.BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE;
import static com.trinet.ambis.helper.PlanCompareHelper.getPlanAttribute;
import static com.trinet.ambis.helper.PlanCompareHelper.populateMDVAttributeLabels;
import static com.trinet.ambis.helper.PlanCompareHelper.sortPlanOfferingPlanAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.PlanOfferingReportConstants;
import com.trinet.ambis.enums.OutputBenefitsTypeEnums;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.outputs.AttributeDesc;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendix;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAttribute;
import com.trinet.ambis.rest.controllers.dto.planofferings.CarrierData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsBenefitPlanData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.service.BenefitsPlanViewService;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.planofferings.PlanOfferingsReportDataService;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author smaguluri
 *
 */
@Service
@Log4j2
public class PlanOfferingsReportDataServiceImpl implements PlanOfferingsReportDataService {
	
	@Autowired
	private BenefitPlanDao benefitPlanDao;

	@Autowired
	private RealmPlanYearDao realmPlanYearDao;

	@Autowired
	private BenefitsPlanViewService benefitsPlanViewService;

	@Autowired
	private PersonService personService;

	@Autowired
	private RealmDataDao realmDataDao;

	@Autowired
	private PlanAvailabilityService planAvailabilityService;

	@Autowired
	private PlanOfferingsServiceUtil planOfferingsServiceUtil;
	 
	@Autowired
	private PortfolioService portfolioService;
	
	@Autowired
	private BenefitsBundleService benefitsBundleService;
	
	@Autowired
	private PlanCompareService planCompareService;
	
	/**
	 * This method return PlanOfferingsData to generate plan offerings report.
	 */
	@Override
	public PlanOfferingsData preparePlanOfferingsData(PlanOfferingsRequest planOfferingsRequest,
			HttpServletRequest httpRequest) {

		return PlanOfferingsData.builder().exchange(planOfferingsRequest.getExchange())
				.loggedInUser(personService.getPersonFirstAndLastName(BSSSecurityUtils.getAuthenticatedPersonId()))
				.quarter(planOfferingsRequest.getQuarter())
				.hqState(planOfferingsRequest.getHqState())
				.hqZipCode(planOfferingsRequest.getHqZipCode())
				.planYearStartDate(planOfferingsRequest.getPlanYearStartDate())
				.planYearEndDate(planOfferingsRequest.getPlanYearEndDate())
				.carriers(planOfferingsRequest.getCarriers())
				.reportCode(planOfferingsRequest.getReportCode())
				.wseZipCode(planOfferingsRequest.getZipCode())
				.wseState(planOfferingsRequest.getState())
				.allRegions(planOfferingsRequest.isAllRegions())
				.regions(planOfferingsRequest.getRegions())
				.bundleName(getBundleName(planOfferingsRequest.getBundleId()))
				.benefitTypes(transformToBSSBenefitTypeDisplayNames(planOfferingsRequest.getBenefitTypes()))
				.planOfferings(buildPlanAttributes(planOfferingsRequest, httpRequest)).build();
	}
	
	private Map<String, PlanAppendix> buildPlanAttributes(PlanOfferingsRequest planOfferingsRequest,
			HttpServletRequest httpRequest) {

		Map<String, PlanAppendix> planAppendixMap = new LinkedHashMap<>();
		Set<String> carrierIds = new HashSet<>();
		planOfferingsRequest.getCarriers().stream().forEach(carrier -> carrierIds.add(String.valueOf(carrier.getId())));
		Company company  = planOfferingsServiceUtil.buildCompany(planOfferingsRequest);

		Set<String> outOfRegionPlans = null;
		if(!isExchangeReport(planOfferingsRequest.getReportCode())) {
			Map<String, Set<PlanCarrier>> planCarrierMap = portfolioService.findPrimaryPlanCarriers(company);
			Set<String> primaryPlanCarriers = BenefitCategoriesHelper.getPlanCarriers(planCarrierMap);
			outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
					realmDataDao);
		}else {
			outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, carrierIds,
					realmDataDao);
		}
		
		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions(company);
		Long planyrId = company.getRealmPlanYear().getId();
		List<PlanOfferingsBenefitPlanData> benefitsPlans = benefitPlanDao
				.getBenefitsPlanOfferingsBy(planOfferingsRequest, planyrId, outOfRegionPlans, isPickChoose);
		RealmCloneProgram realmProgram = realmDataDao.getRealmCloneProgram(planyrId);
		List<String> availablePlansByZipCodeAndState;
		List<PlanOfferingsBenefitPlanData> filteredData = null;

		if (isWSEReport(planOfferingsRequest.getReportCode())) {

			List<String> plans = benefitsPlans.stream().map(PlanOfferingsBenefitPlanData::getBenefitPlan)
					.collect(Collectors.toList());

			// Get the list of plans the client offers by plan type
			if (!StringUtils.isEmpty(planOfferingsRequest.getCompanyCode())) {
				Map<String, List<String>> planSelectionsByPlanType = benefitPlanDao
						.getCompanyPlanSelectionsForPlanOfferingReport(planOfferingsRequest, planyrId);

				// Remove any plans from plans if they don't exist in planSelectionsByPlanType
				plans.retainAll(planSelectionsByPlanType.values().stream().flatMap(List::stream).collect(Collectors.toList()));
			}

			availablePlansByZipCodeAndState = getPlansByWSEZipCodeAndState(planOfferingsRequest, plans,
					realmProgram.getCloneProgram());
			// Get benefit plans from PlanOfferingsBenefitPlanData
			filteredData = benefitsPlans.stream()
					.filter(plan -> availablePlansByZipCodeAndState.contains(plan.getBenefitPlan()))
					.collect(Collectors.toList());
		} else {
			filteredData = benefitsPlans;
		}

		Map<String, List<PlanOfferingsBenefitPlanData>> plansByType = filteredData.stream()
				.collect(Collectors.groupingBy(PlanOfferingsBenefitPlanData::getPlanType));

		Map<String, List<BenefitPlanCompare>> planAttributes = new HashMap<>();

		planOfferingsRequest.getBenefitTypes().stream().forEach(planType ->

		constructPlanAttributes(planAttributes, planAppendixMap, planType, plansByType,
				Utils.convertStringToDate(
						Utils.convertDateFormat(planOfferingsRequest.getPlanYearEndDate(),
								BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY,
								BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD).get(),
						BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD),
				httpRequest).get()

		);
		mergeVoluntoryPlans().accept(planAppendixMap);
		return planAppendixMap;
	}
	

	private Supplier<Map<String, PlanAppendix>> constructPlanAttributes(
			Map<String, List<BenefitPlanCompare>> planAttributes, Map<String, PlanAppendix> planAppendixMap,
			String planType, Map<String, List<PlanOfferingsBenefitPlanData>> plansByType, Date effDate,
			HttpServletRequest httpRequest) {
		return () -> {
			List<BenefitPlanCompare> benefitPlanCompares;
			Set<String> planIds = null;
			if (Objects.nonNull(plansByType) && !plansByType.isEmpty() && Objects.nonNull(plansByType.get(planType))) {
				planIds = plansByType.get(planType).stream().map(PlanOfferingsBenefitPlanData::getBenefitPlan)
						.collect(Collectors.toSet());
			}
			try {
				benefitPlanCompares = planCompareService.getPlanAttributes(planIds, effDate,
						BSSApplicationConstants.databaseTemplatesMap.get(planType), httpRequest).get();
				List<BenefitPlanCompare> benefitPlans = benefitPlanCompares.stream()
						.filter(plan -> Objects.nonNull(plan.getTemplate()))
						.filter(plan -> !plan.getTemplate().isEmpty()).collect(Collectors.toList());
				planAttributes.put(planType, benefitPlans);
			} catch (Exception e) {
				log.error("Exception...");
				Thread.currentThread().interrupt();
				throw new BSSApplicationException(new BSSApplicationError(
						"Error occured while preparing the plan offerings report, Error : " + e.getMessage()));
			}
			List<AttributeDesc> attributeLabels = populateMDVAttributeLabels(planType, planAttributes);
			PlanAppendix planAppendix = planAppendixMap.get(planType);
			if (planAppendixMap.containsKey(planType)) {
				if (planAppendixMap.get(planType).getAttributeNames().isEmpty()) {
					planAppendix.setAttributeNames(attributeLabels);
				}
			} else {
				if (Objects.nonNull(attributeLabels)) {
					planAppendix = new PlanAppendix();
					planAppendix.setAttributeNames(attributeLabels);

					List<PlanAttribute> allAttributes = new LinkedList<>();
					List<BenefitPlanCompare> compPlans = planAttributes.get(planType);
					compPlans.stream().forEach(plan -> {
						PlanAttribute planAttribute = getPlanAttribute(plan, false);
						String basePlanName = plansByType.get(planType).stream().filter(planByType -> planByType.getBenefitPlan().equals(plan.getPlanId())).map(PlanOfferingsBenefitPlanData::getDescription).findFirst().orElse(null);
						planAttribute.setPlanName(basePlanName);
						allAttributes.add(planAttribute);
					});
					planAppendix.setPlanAttributes(allAttributes);
				}
			}

			planAppendix.setPlanAttributes(sortPlanOfferingPlanAttributes().apply(planAppendix.getPlanAttributes()));
			planAppendixMap.put(planType, planAppendix);
			return planAppendixMap;
		};
	}
	
	/**
	 * merger both benefits plans and voluntary benefit plans
	 * 
	 */
	private Consumer<Map<String, PlanAppendix>> mergeVoluntoryPlans() {

		return planAppendixMap -> {
			if (Objects.nonNull(planAppendixMap.get(DENTAL_PLAN_TYPE))
					&& Objects.nonNull(planAppendixMap.get(VOLUNTARY_DENTAL_PLAN_TYPE))) {
				planAppendixMap.get(DENTAL_PLAN_TYPE).getPlanAttributes()
						.addAll(planAppendixMap.get(VOLUNTARY_DENTAL_PLAN_TYPE).getPlanAttributes());
				planAppendixMap.keySet().remove(VOLUNTARY_DENTAL_PLAN_TYPE);
			}
			if (Objects.nonNull(planAppendixMap.get(VISION_PLAN_TYPE))
					&& Objects.nonNull(planAppendixMap.get(VOLUNTARY_VISION_PLAN_TYPE))) {
				planAppendixMap.get(VISION_PLAN_TYPE).getPlanAttributes()
						.addAll(planAppendixMap.get(VOLUNTARY_VISION_PLAN_TYPE).getPlanAttributes());
				planAppendixMap.keySet().remove(VOLUNTARY_VISION_PLAN_TYPE);
			}

		};
	}

	public List<CarrierData> getCarriersBy(String reportCode, String quarter, Date effDt, String hqState, Optional<String> hqZipCode, String benefitType){
		if (!PlanOfferingReportConstants.REPORT_CODE_EXCHANGE.equalsIgnoreCase(reportCode)
				&& ("CA".equals(hqState) || "NY".equals(hqState)) && !hqZipCode.isPresent()) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Zipcode is mandatory for CA and NY states", null, null));

		}
		if (PlanOfferingReportConstants.REPORT_CODE_EXCHANGE.equalsIgnoreCase(reportCode) && benefitType==null) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Benefit type is required for exchange selection", null, null));
		}
		String planType = PlanOfferingReportConstants.REPORT_CODE_EXCHANGE.equalsIgnoreCase(reportCode)?transformToBSSBenefitTypeCode(Arrays.asList(benefitType)).get(0):benefitType;
		RealmPlanYear realmPlanYear = realmPlanYearDao.findByOeQuarterAndPlanYearStart(quarter, effDt);
		if(Objects.nonNull(realmPlanYear)) {
			List<CarrierData> carriersList = realmDataDao.getCarriersBy(hqZipCode.orElse(""), realmPlanYear.getId(),
					hqState, reportCode, planType);
			return carriersList.stream().sorted(Comparator.comparing(CarrierData :: getCarrierName)).collect(Collectors.toList());
		} else {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_REALM_PLAN_YEAR_NOT_FOUND,
					BSSHttpStatusConstants.NOT_FOUND, "", String.format("Realm plan year not found for quarter %s and plan year start date %s",
							quarter, effDt), null, null));
		}
	}
	
	private boolean isWSEReport(String reportingCode) {
		return  PlanOfferingReportConstants.REPORT_CODE_WSE.equalsIgnoreCase(reportingCode);
	}
	
	private boolean isExchangeReport(String reportingCode) {
		return  PlanOfferingReportConstants.REPORT_CODE_EXCHANGE.equalsIgnoreCase(reportingCode);
	}
	
	private List<String> getPlansByWSEZipCodeAndState(PlanOfferingsRequest planOfferingsRequest, List<String> benPlans, String cloneProgramId){
		
		PlanAvailableRequest planAvailabilityReq = new PlanAvailableRequest();
		planAvailabilityReq.setCloneBenefitProgram(cloneProgramId);
		planAvailabilityReq
				.setEffectiveDate(Utils.convertStringToDate(
						Utils.convertDateFormat(planOfferingsRequest.getPlanYearStartDate(),
								BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY,
								BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY).get(),
						BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		List<PlanAvailableRequest.Location> locationList = new ArrayList<>();
		PlanAvailableRequest.Location location = new PlanAvailableRequest.Location();
		location.setState(planOfferingsRequest.getState());
		location.setPostalCode(planOfferingsRequest.getZipCode());
		locationList.add(location);
		planAvailabilityReq.setLocations(locationList);
		planAvailabilityReq.setPlans(benPlans);
		
		CompletableFuture<List<PlanAvailableResponse>> availablePlansCompletableFuture = planAvailabilityService
				.getBenefitPlanAvailability(planAvailabilityReq);
		List<PlanAvailableResponse> availablePlans = availablePlansCompletableFuture.join();

		List<String> availablePlansByZipCodeAndState = new ArrayList<>();
		for (PlanAvailableResponse planAvailableResponse : availablePlans) {
			planAvailableResponse.getPlansByBenType()
					.forEach(benTypePlans -> availablePlansByZipCodeAndState.addAll(benTypePlans.getPlanIds()));
		}
		return availablePlansByZipCodeAndState;
	}
	
	/**
	 * This method is to Transform to BSS BenefitType codes to Benefit Display Names
	 *  */
	private List<String> transformToBSSBenefitTypeDisplayNames(List<String> benTypeCodes) {
		return benTypeCodes.stream().map(OutputBenefitsTypeEnums::getDisplayNameByCode).collect(Collectors.toList());
	}
	
	/**
	 * This method is to Transform to BSS BenefitType codes
	 * 
	 * @param benFitTypeNames
	 */
	private List<String> transformToBSSBenefitTypeCode(List<String> benFitTypeNames) {
		return benFitTypeNames.stream()
				.map(OutputBenefitsTypeEnums::getBenTypeCodeByName).collect(Collectors.toList());
	}
	
	/**
	 * This method is to get the bundle name
	 * @param bundleId
	 * @return
	 */
	private String getBundleName(Long bundleId) {
		String bundleName = null;
		if (bundleId != null) {
			Bundle bundle = benefitsBundleService.getBundleById(bundleId);
			if (null != bundle) {
				bundleName = bundle.getName();
			}
		}
		return bundleName;
	}

}
