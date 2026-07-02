package com.trinet.ambis.helper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.OutputBenefitsTypeEnums;
import com.trinet.ambis.rest.controllers.dto.outputs.AttributeDesc;
import com.trinet.ambis.rest.controllers.dto.outputs.AttributeValue;
import com.trinet.ambis.rest.controllers.dto.outputs.CvgLvlPlanInfo;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAttribute;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.model.plancompare.Attribute;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.model.plancompare.PlanCompareTemplate;
import com.trinet.ambis.service.prospect.enums.RateTypeEnum;

import lombok.extern.log4j.Log4j2;
import static com.trinet.ambis.helper.AttributeBuilderHelper.getAttributeDesc;
import static com.trinet.ambis.helper.AttributeBuilderHelper.getAttributeValue;
import static com.trinet.ambis.helper.AttributeBuilderHelper.COINSURANCE_ID;
import static com.trinet.ambis.helper.AttributeBuilderHelper.ATTRIBUTE_CO_INSURANCE_ID;
import static com.trinet.ambis.helper.AttributeBuilderHelper.NO_VALUE;
import static com.trinet.ambis.helper.AttributeBuilderHelper.ATTRIBUTE_PRIMARY_SPECIAL_ID;
import static com.trinet.ambis.helper.AttributeBuilderHelper.ATTRIBUTE_RXTIER_ID;
import static com.trinet.ambis.helper.AttributeBuilderHelper.DOLLAR;
import static com.trinet.ambis.helper.AttributeBuilderHelper.ATTRIBUTE_HOSPITAL_ID;
import static com.trinet.ambis.helper.AttributeBuilderHelper.CO_INSURANCE_IN_OUT_NETWORK;
import static com.trinet.ambis.helper.AttributeBuilderHelper.PRIMARY_SPECIAL_LABEL;
import static com.trinet.ambis.helper.AttributeBuilderHelper.RXTIER_LABEL;
import static com.trinet.ambis.helper.AttributeBuilderHelper.HOSPITAL_OUTPATIENT_LABEL;
import static com.trinet.ambis.helper.AttributeBuilderHelper.ATTRIBUTE_CO_INSURANCE_OUT_ID;

@Log4j2
public class PlanCompareHelper {
	
	public static final String ATTRIBUTE = "attribute";
	
	public static final String CATEGORY = "category";
	
	public static final String HSA_ATTRIBUTE_VALUE_YES = "Yes";
	
	public static final String HSA_ATTRIBUTE_VALUE_NO = "No";
	
	public static final String HSA_ATTRIBUTE_NAME = "HSA";
	
	public static final String PLAN_TYPE_ATTRIBUTE_NAME = "Plan Type";
	
	public static final List<Integer> UNUTILIZED_REPORT_ATTRIBUTES = List.of(5,47,4);
	public static final List<Integer> EXCLUDE_REPORT_ATTRIBUTES = List.of(COINSURANCE_ID,
			AttributeBuilderHelper.SPECIALIST_VISIT,
			AttributeBuilderHelper.BRAND_RX_TIER_2,
			AttributeBuilderHelper.NONFORMULARY_RX_TIER_3,
			AttributeBuilderHelper.OUTPATIENT_FACILITY);

	public static final String MED_NO_PLAN_ID = "10_NO_PLAN_ID";
	public static final String DEN_NO_PLAN_ID = "11_NO_PLAN_ID";
	public static final String VIS_NO_PLAN_ID = "14_NO_PLAN_ID";
	
	public static final String NTL = "NTL";

	private PlanCompareHelper() {
		throw new IllegalStateException(
				"Helper class " + PlanCompareHelper.class.getName() + " can not be instantiated.");
	}

	private static PlanCompareService planCompareService;

	public static void setPlanCompareService(PlanCompareService planCompareServiceLocal) {
		planCompareService = planCompareServiceLocal;
	}
	
	public static CompletableFuture<List<BenefitPlanCompare>> getFuturePlansAttributes(
			Map<String, Map<String, Set<String>>> plansIdsToCompareByBenefitType,
			Date effDt, String template, HttpServletRequest httpRequest) {

		List<CompletableFuture<List<BenefitPlanCompare>>> planAttributeFutures = new ArrayList<>();
		for (Map.Entry<String, Map<String, Set<String>>> entry : plansIdsToCompareByBenefitType.entrySet()) {
			String templateName = getTemplateName(template, entry.getKey());
			Map<String, Set<String>> planIdsByBenefitType = entry.getValue();
			Set<String> futurePlanIds = planIdsByBenefitType.values().stream().flatMap(Set::stream)
					.collect(Collectors.toSet());
			if (CollectionUtils.isNotEmpty(futurePlanIds)) {
				planAttributeFutures
						.add(planCompareService.getPlanAttributes(futurePlanIds, effDt, templateName, httpRequest));
			}

		}
		return aggregatePlanAttributeFutures(planAttributeFutures);
	}

	private static String getTemplateName(String template, String benefitType) {
		return BSSApplicationConstants.BSS_EXPORT_TEMPLATE.equalsIgnoreCase(template)
				? BSSApplicationConstants.exportTemplatesMap.get(benefitType)
				: BSSApplicationConstants.prospectTemplatesMap.get(benefitType);
	}
	
	private static CompletableFuture<List<BenefitPlanCompare>> aggregatePlanAttributeFutures(
			List<CompletableFuture<List<BenefitPlanCompare>>> planAttributeFutures) {
		return CompletableFuture.allOf(planAttributeFutures.toArray(new CompletableFuture[0]))
		        .thenApply(v -> planAttributeFutures.stream()
		                .map(CompletableFuture::join)
		                .flatMap(List::stream)
		                .collect(Collectors.toList())
		        );
	}

	public static CompletableFuture<List<BenefitPlanCompare>> getCurrentPlansAttributes(
			Map<String, Map<String, Set<String>>> plansIdsToCompareByBenefitType, Date effDt, String template,
			HttpServletRequest httpRequest) {

		List<CompletableFuture<List<BenefitPlanCompare>>> planAttributeFutures = new ArrayList<>();
		for (Map.Entry<String, Map<String, Set<String>>> entry : plansIdsToCompareByBenefitType.entrySet()) {
			String templateName = getTemplateName(template, entry.getKey());
			Map<String, Set<String>> planIdsByBenefitType = entry.getValue();
			Set<String> currentPlanIds = new LinkedHashSet<>(planIdsByBenefitType.keySet());
			currentPlanIds.removeAll(Arrays.asList(MED_NO_PLAN_ID, DEN_NO_PLAN_ID, VIS_NO_PLAN_ID));
			if (!currentPlanIds.isEmpty()) {
				planAttributeFutures
						.add(planCompareService.getPlanAttributes(currentPlanIds, effDt, templateName, httpRequest));
			}
		}
		return aggregatePlanAttributeFutures(planAttributeFutures);
	}

	public static Map<String, BenefitPlanCompare> mapPlanIdToObject(List<BenefitPlanCompare> planCompareData) {
		return planCompareData.stream().collect(Collectors.toMap(BenefitPlanCompare::getPlanId, Function.identity()));
	}
	
	public static PlanAttribute getPlanAttribute(BenefitPlanCompare plan, boolean forProspect) {
		String planTypeAttribute  = null;
		boolean hsa = false;
		List<AttributeValue> attributeValues = populateMDVAttributeValues(plan);
		if(Objects.nonNull(plan) && Objects.nonNull(plan.getBenefitType()))
			planTypeAttribute  =  plan.getTemplate().stream()
					.flatMap(template -> template.getChildren().stream()
							.filter(child -> child.getName().equalsIgnoreCase(PLAN_TYPE_ATTRIBUTE_NAME))
							)
					.findFirst()
					.map(Attribute::getValue)
					.orElse(null);
		if (forProspect && Objects.nonNull(plan) && Objects.nonNull(plan.getBenefitType()) && plan.getBenefitType().equalsIgnoreCase(BSSApplicationConstants.MEDICAL)) {

			hsa = plan.getTemplate().stream()
					.flatMap(template -> template.getChildren().stream()
							.filter(child -> child.getName().equalsIgnoreCase(HSA_ATTRIBUTE_NAME)))
					.findFirst().map(attribute -> HSA_ATTRIBUTE_VALUE_YES.equalsIgnoreCase(attribute.getValue())).orElse(false);
		}
		if(Objects.nonNull(plan) && Objects.nonNull(plan.getBenefitType()) && plan.getBenefitType().equalsIgnoreCase(BSSApplicationConstants.DENTAL)) {
			
			boolean hasCategory = false;
			for(AttributeValue value : attributeValues) {
				if(value.getType().equalsIgnoreCase(CATEGORY)) {
					hasCategory = true;
				}
			}
			//Adding dummy category for dental
			if(!hasCategory) {
				List<AttributeValue> childAttributeValues = new LinkedList<>();
				List<AttributeValue> categoryalues = new LinkedList<>();
				childAttributeValues.add(getAttributeValue(ATTRIBUTE, NO_VALUE, null));
				childAttributeValues.add(getAttributeValue(ATTRIBUTE, NO_VALUE, null));
				AttributeValue childValue = getAttributeValue(CATEGORY, NO_VALUE, childAttributeValues);
				attributeValues.add(getAttributeValue(ATTRIBUTE, NO_VALUE, null));
				attributeValues.add(getAttributeValue(ATTRIBUTE, NO_VALUE, null));
				attributeValues.add(getAttributeValue(ATTRIBUTE, NO_VALUE, null));
				categoryalues.add(childValue);
				categoryalues.addAll(attributeValues);
				return PlanAttribute.builder()
						.planName(plan.getName() != null ? plan.getName() : plan.getPlanName())
						.planId(plan.getPlanId())
						.carrierName(plan.getCarrier())
						.planType(planTypeAttribute)
						.attributeValues(categoryalues)
						.build();
			}
		}
		return PlanAttribute.builder()
				.planName(plan.getName() != null ? plan.getName() : plan.getPlanName())
				.planId(plan.getPlanId())
				.carrierName(plan.getCarrier())
				.carrierLogoUrl(plan.getCarrierLogoUrl())
				.planType(planTypeAttribute)
				.attributeValues(attributeValues)
				.hsa(hsa)
				.build();
	}
	
	public static List<AttributeDesc> populateMDVAttributeLabels(String benType, Map<String, List<BenefitPlanCompare>> benefitTypeCurrentPlansAttributes) {
		if(benefitTypeCurrentPlansAttributes.containsKey(benType)) {
			BenefitPlanCompare benefitPlanCompare = benefitTypeCurrentPlansAttributes.get(benType).stream().findFirst().orElse(new BenefitPlanCompare());
			if(Objects.nonNull(benefitPlanCompare.getTemplate())) {
				PlanCompareTemplate planCompareTemplate = benefitPlanCompare.getTemplate().stream().findFirst().orElse(new PlanCompareTemplate());
				List<Attribute> children = planCompareTemplate.getChildren();
				if(planCompareTemplate.getType().equalsIgnoreCase(CATEGORY)) {
					return children.stream().map(prepareAttribute()).filter(Objects::nonNull)
							.collect(Collectors.toList());
				}
			}
		}
		return Collections.emptyList();
	}
	
	public static List<AttributeValue> populateMDVAttributeValues(BenefitPlanCompare benefitPlanCompare) {

		if (Objects.nonNull(benefitPlanCompare) && Objects.nonNull(benefitPlanCompare.getTemplate())) {
			PlanCompareTemplate planCompareTemplate = benefitPlanCompare.getTemplate().stream().findFirst()
					.orElse(new PlanCompareTemplate());
			if (planCompareTemplate.getType().equalsIgnoreCase(CATEGORY)) {
				List<Attribute> children = planCompareTemplate.getChildren();
				return prepareAttributeValue().apply(children);
			}
		}

		return Collections.emptyList();
	}

	public static Function<Attribute, AttributeDesc> prepareAttribute() {
		return attribute -> {
			if (attribute.isDisplay() && attribute.getType().equalsIgnoreCase(CATEGORY)) {
				String categoryLabel = (!ObjectUtils.anyNull(attribute.getDisplayName())
						&& !ObjectUtils.isEmpty(attribute.getDisplayName().trim())) ? attribute.getDisplayName()
								: attribute.getName();
				return getAttributeDesc(attribute.getId(), categoryLabel, CATEGORY,
						prepareChildAttribute().apply(attribute.getChildren()));
			}
			if (attribute.isDisplay() && attribute.getType().equalsIgnoreCase(ATTRIBUTE)) {
				String categoryLabel = (!ObjectUtils.anyNull(attribute.getDisplayName())
						&& !ObjectUtils.isEmpty(attribute.getDisplayName().trim())) ? attribute.getDisplayName()
								: attribute.getName();
				return getAttributeDesc(attribute.getId(), categoryLabel, ATTRIBUTE, null);
			}
			return null;
		};
	}
	
	public static String modifyName(String name, String displayName) {
		if(!StringUtils.isBlank(name)) {
			if(name.contains(AttributeBuilderHelper.IN_NETWORK)) {
				String regexTarget = "("+ AttributeBuilderHelper.IN_NETWORK +")$";
				return name.replaceAll(regexTarget, AttributeBuilderHelper.IN_OUT_OF_NETWORK);
				
			}
			if(displayName.equalsIgnoreCase(AttributeBuilderHelper.IN_NETWORK)) {
				return  name.concat(" " + AttributeBuilderHelper.IN_OUT_OF_NETWORK);
			}
		}
		return null;
	}

	public static Function<List<Attribute>, List<AttributeDesc>> prepareChildAttribute() {
		return attributes -> {
			List<AttributeDesc> attributeDescs = new LinkedList<>();
			for (Attribute attribute : attributes) {
				if (attribute.isDisplay()) {
					attributeDescs.add(getAttributeDesc(attribute.getId(), attribute.getDisplayName(), ATTRIBUTE, null));
				}
			}
			return attributeDescs;
		};
	}

	public static Function<List<Attribute>, List<AttributeValue>> prepareAttributeValue() {
		return attributes -> {
			List<AttributeValue> attributeValues = new ArrayList<>();
			for (Attribute attribute : attributes) {
				processCategoryAttribute(attributeValues, attribute);
				processAttribute(attributes, attributeValues, attribute);
			}
			return attributeValues;
		};
	}

	private static void processCategoryAttribute(List<AttributeValue> attributeValues, Attribute attribute) {
		if (attribute.getType().equalsIgnoreCase(CATEGORY) && attribute.isDisplay()) {
			attributeValues.add(
					getAttributeValue(CATEGORY, NO_VALUE, prepareChildAttributeValue().apply(attribute.getChildren())));
		}
	}

	public static Function<List<Attribute>, List<AttributeValue>> prepareChildAttributeValue() {
		return attributes -> {
			List<AttributeValue> attributeDescs = new LinkedList<>();
			for (Attribute attribute : attributes) {
				if (attribute.getType().equalsIgnoreCase(ATTRIBUTE) && attribute.isDisplay()) {
					attributeDescs.add(getAttributeValue(ATTRIBUTE, doubleDashIfValueIsNull(attribute), null));
				}
			}

			return attributeDescs;
		};
	}

	private static void processAttribute(List<Attribute> attributes, List<AttributeValue> attributeValues,
			Attribute attribute) {
		if (attribute.getType().equalsIgnoreCase(ATTRIBUTE) && attribute.isDisplay()) {
			attributeValues.add(getAttributeValue(ATTRIBUTE, doubleDashIfValueIsNull(attribute), null));
		}
	}
	
	public static Function<List<Attribute>,List<AttributeValue>> prepareDentalVisionChildAttributeValue(){
		return attributes ->{
			List<AttributeValue> attributeValues = new LinkedList<>();
			List<Attribute> outNetWorkAttributes = attributes.stream().filter(attib -> Objects.nonNull(attib.getDisplayName())).filter(attib -> attib.getDisplayName().equalsIgnoreCase(AttributeBuilderHelper.OUT_OF_NETWORK)).collect(Collectors.toList());
			for(Attribute attib : attributes) {
				prepareDVCategoryAttribute(attributeValues, attib);
				
				if(attib.getType().equalsIgnoreCase(ATTRIBUTE) && attib.getDisplayName().equalsIgnoreCase(AttributeBuilderHelper.IN_NETWORK)) {
					String regexTarget = "("+ AttributeBuilderHelper.IN_NETWORK +")$";
					String name = attib.getName().replaceAll(regexTarget, "");
					Optional<Attribute> outNetWorkAttrib = outNetWorkAttributes.stream().filter(attrib -> attrib.getName().contains(name.trim())).findAny();
				
					if(outNetWorkAttrib.isPresent()) {
						String inValue = doubleDashIfValueIsNull(attib);
						String outValue = doubleDashIfValueIsNull(outNetWorkAttrib.get());
						String inAndOutNetworkValue = inValue+AttributeBuilderHelper.SEPARATOR+outValue;
						outNetWorkAttributes = outNetWorkAttributes.stream().filter(attrib -> !attrib.getName().equalsIgnoreCase(outNetWorkAttrib.get().getName())).collect(Collectors.toList());
						AttributeValue childAttrib = getAttributeValue(ATTRIBUTE, inAndOutNetworkValue, null);
						attributeValues.add(childAttrib);
					}
				}
				
				prepareNonInOutUtilizedAttributes(attributeValues, attib);
			}
			return attributeValues;
		};
	}
	
	private static void prepareDVCategoryAttribute(List<AttributeValue> attributeValues, Attribute attib) {
		if(attib.getType().equalsIgnoreCase(CATEGORY)) {
			AttributeValue childAttrib = getAttributeValue(CATEGORY, NO_VALUE, prepareDentalChildCategoryAttributeValue().apply(attib.getChildren()));
			attributeValues.add(childAttrib);
		}
	}
	
	private static void prepareNonInOutUtilizedAttributes(List<AttributeValue> attributeValues, Attribute attib) {
		if(attib.getType().equalsIgnoreCase(ATTRIBUTE) && (!attib.getDisplayName().equalsIgnoreCase(AttributeBuilderHelper.IN_NETWORK) && !attib.getDisplayName().equalsIgnoreCase(AttributeBuilderHelper.OUT_OF_NETWORK)) && !UNUTILIZED_REPORT_ATTRIBUTES.contains(attib.getId())) {
			AttributeValue childAttrib = getAttributeValue(ATTRIBUTE, StringUtils.isBlank(attib.getValue()) ? NO_VALUE : attib.getValue(), null);
			attributeValues.add(childAttrib);
		}
	}
	
	public static Function<List<Attribute>,List<AttributeValue>> prepareDentalChildCategoryAttributeValue(){
		return attributes ->{
			List<AttributeValue> attributeValues = new LinkedList<>();
			List<Attribute> outNetWorkAttributes = attributes.stream().filter(attib -> Objects.nonNull(attib.getDisplayName())).filter(attib -> attib.getDisplayName().contains(AttributeBuilderHelper.OUT_OF_NETWORK_VALUE)).collect(Collectors.toList());
			for(Attribute attib : attributes) {
				if(attib.getType().equalsIgnoreCase(ATTRIBUTE) && attib.getName().contains(AttributeBuilderHelper.IN_NETWORK_LABEL)) {
					String regexTarget = "("+AttributeBuilderHelper.IN_NETWORK_LABEL+")$";
					String name = attib.getName().replaceAll(regexTarget, "");
					Optional<Attribute> outNetWorkAttrib = outNetWorkAttributes.stream().filter(attrib -> attrib.getName().contains(name.trim())).findAny();
				
					if(outNetWorkAttrib.isPresent()) {
						String inAndOutNetworkValue = doubleDashIfValueIsNull(attib)+AttributeBuilderHelper.SEPARATOR+doubleDashIfValueIsNull(outNetWorkAttrib.get());
						AttributeValue childAttrib = getAttributeValue(ATTRIBUTE, inAndOutNetworkValue, null);
						attributeValues.add(childAttrib);
					}
				}
			}
			return attributeValues;
		};
		
	}
	public static CvgLvlPlanInfo getCvgLvlPlanInfo(Map<String, String> mapOfCovgCodeToCount) {
		 return CvgLvlPlanInfo.builder()
				.employeeOnly(formatPlanCost().apply(mapOfCovgCodeToCount.get(CoverageCodesEnums.COV_EMPLOYEE.getCode())))
				.employeeChildren(
						formatPlanCost().apply(mapOfCovgCodeToCount.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode())))
				.employeeSpouse(formatPlanCost().apply(mapOfCovgCodeToCount.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode())))
				.family(formatPlanCost().apply(mapOfCovgCodeToCount.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode()))).build();
	}
	
	public static CvgLvlPlanInfo getCvgLvlPlanHeadCount(Map<String, String> mapOfCovgCodeToCount) {
		 return CvgLvlPlanInfo.builder()
				.employeeOnly(planHeadCount().apply(mapOfCovgCodeToCount.get(CoverageCodesEnums.COV_EMPLOYEE.getCode())))
				.employeeChildren(
						planHeadCount().apply(mapOfCovgCodeToCount.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode())))
				.employeeSpouse(planHeadCount().apply(mapOfCovgCodeToCount.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode())))
				.family(planHeadCount().apply(mapOfCovgCodeToCount.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode()))).build();
	}
	
	public static CvgLvlPlanInfo getAgeBandCvgLvlInfo() {
		return CvgLvlPlanInfo.builder().employeeOnly(RateTypeEnum.AGE_BANDED.getType())
				.employeeChildren(RateTypeEnum.AGE_BANDED.getType())
				.employeeSpouse(RateTypeEnum.AGE_BANDED.getType())
				.family(RateTypeEnum.AGE_BANDED.getType()).build();
	}
	
	public static Function<String,String> formatPlanCost(){
		return planCost -> {
			if (Objects.nonNull(planCost)) {
				BigDecimal formatCost = new BigDecimal(planCost);
				DecimalFormatSymbols formatSymbol = new DecimalFormatSymbols();
				formatSymbol.setGroupingSeparator(AttributeBuilderHelper.COMMA);
				DecimalFormat format = new DecimalFormat(AttributeBuilderHelper.COST_FORMATER, formatSymbol);
				return DOLLAR.concat(format.format(formatCost));
			}
			return null;
		};
	}

	public static Function<String,String> formatUnitRate(){
		return planCost -> {
			if (Objects.nonNull(planCost)) {
				BigDecimal formatCost = new BigDecimal(planCost);
				DecimalFormatSymbols formatSymbol = new DecimalFormatSymbols();
				formatSymbol.setGroupingSeparator(AttributeBuilderHelper.COMMA);
				DecimalFormat format = new DecimalFormat(AttributeBuilderHelper.UNIT_RATE_FORMATER, formatSymbol);
				return DOLLAR.concat(format.format(formatCost));
			}
			return null;
		};
	}
	
	public static Function<String,String> planHeadCount(){
		return headCount -> {
			return Objects.nonNull(headCount) ? headCount : String.valueOf(0);
		};
	}
	
	public static Function<List<PlanAttribute>, List<PlanAttribute>> sortPlanAttributes(boolean includeCarrierSorting) {
		return allAttributes -> {
			if (CollectionUtils.isNotEmpty(allAttributes)) {
				Comparator<PlanAttribute> sortingCriteria = buildSortingCriteria(includeCarrierSorting);
				Map<Boolean, List<PlanAttribute>> partitionedPlans = allAttributes.stream().filter(Objects::nonNull)
						.collect(Collectors.partitioningBy(plan -> plan.getPlanName().contains(NTL)));

				partitionedPlans.forEach((key, planList) -> {
					if (planList != null)
						planList.sort(sortingCriteria);
				});

				List<PlanAttribute> sortedPlans = new ArrayList<>();
				sortedPlans.addAll(partitionedPlans.getOrDefault(false, Collections.emptyList()));
				sortedPlans.addAll(partitionedPlans.getOrDefault(true, Collections.emptyList()));
				return sortedPlans;
			}
			return allAttributes;
		};
	}
	
	private static Comparator<PlanAttribute> buildSortingCriteria(boolean includeCarrierSorting) {
		Comparator<PlanAttribute> sortingCriteria;
		
		sortingCriteria = Comparator.comparing(PlanAttribute::getRegion,
				Comparator.nullsLast(Comparator.naturalOrder()));
		if (includeCarrierSorting)
			sortingCriteria = sortingCriteria.thenComparing(PlanAttribute::getCarrierName,
					Comparator.nullsLast(Comparator.naturalOrder()));
		sortingCriteria = sortingCriteria
				.thenComparing(planRateNullCheckAndReplaceDoller(), Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(PlanAttribute::getPlanName, Comparator.nullsLast(Comparator.naturalOrder()));
		return sortingCriteria;
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}
	
	public static BiFunction<OutputRequest, List<PlanAttribute>, List<PlanAttribute>> filterMedPlanByAttributes(String benType) {
		return (prospectRequest, allAttributes) -> {
			if (OutputBenefitsTypeEnums.MEDICAL.getCode().equalsIgnoreCase(benType)
					&& CollectionUtils.isNotEmpty(allAttributes)) {
				if(CollectionUtils.isNotEmpty(prospectRequest.getPlanAppendixFilters()
						.getMedicalPlanCategories())) {
					allAttributes = allAttributes.stream().filter(plan -> prospectRequest.getPlanAppendixFilters()
							.getMedicalPlanCategories().contains(plan.getPlanType())).collect(Collectors.toList());
				}
				if (StringUtils.isNoneBlank(prospectRequest.getPlanAppendixFilters().getHsa())) {
					allAttributes = getFilteredHsaAttributes(prospectRequest.getPlanAppendixFilters().getHsa(), allAttributes, filter());
				}
				
			}
			return allAttributes;
		};
	}
	
	public static UnaryOperator<List<PlanAttribute>> sortPlanOfferingPlanAttributes() {
		return allAttributes -> {
			if (CollectionUtils.isNotEmpty(allAttributes)) {
				Comparator<PlanAttribute> sortingCriteria = Comparator
						.comparing(PlanAttribute::getCarrierName, Comparator.nullsLast(String::compareToIgnoreCase))
						.thenComparing(PlanAttribute::getPlanName, Comparator.nullsLast(String::compareToIgnoreCase));
				Collections.sort(allAttributes, sortingCriteria);
			}
			return allAttributes;
		};
	}
	
	private static List<PlanAttribute> getFilteredHsaAttributes(String hsa, List<PlanAttribute> allAttributes,
			BiFunction<String, List<PlanAttribute>, List<PlanAttribute>> filterHsa) {
		return filterHsa.apply(hsa, allAttributes);
	}
	
	public static BiFunction<String, List<PlanAttribute>, List<PlanAttribute>> filter(){
		return (hasHsa, attributes) -> {
			if(planHsaPredicate(HSA_ATTRIBUTE_VALUE_NO).test(hasHsa)) {
				return getFilteredHasPlans(() -> attributes.stream().filter(Predicate.not(PlanAttribute::isHsa)).collect(Collectors.toList()));
			}else if(planHsaPredicate(HSA_ATTRIBUTE_VALUE_YES).test(hasHsa)) {
				return getFilteredHasPlans(() -> attributes.stream().filter(PlanAttribute::isHsa).collect(Collectors.toList()));
			}else {
				return getFilteredHasPlans(() -> attributes);
			}
		};
	}
	
	public static List<PlanAttribute> getFilteredHasPlans(Supplier<List<PlanAttribute>> plans) {
		return plans.get();
	}
	
	public static Predicate<String> planHsaPredicate(String hsaOption) {
		return option -> option.equalsIgnoreCase(hsaOption);
	}
	
	public static Predicate<String> hasBenType(Collection<String> prospectBenTypes, Collection<String> dentalBenTypes, Collection<String> visionBenTypes){
		return type -> containsBenType(prospectBenTypes)
				.or(containsBenType(dentalBenTypes))
				.or(containsBenType(visionBenTypes))
				.test(type);
	}
	
	public static Predicate<String> containsBenType(Collection<String> types){
		return types::contains;
	}
	
	public static UnaryOperator<String> prepareDentalBenType(){
		return type -> BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE.equals(type) ? BSSApplicationConstants.DENTAL_PLAN_TYPE : type;
	}
	
	public static UnaryOperator<String> prepareVisionBenType(){
		return type -> BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE.equals(type) ? BSSApplicationConstants.VISION_PLAN_TYPE : type;
	}
	
	public static Function<PlanAttribute, BigDecimal> planRateNullCheckAndReplaceDoller(){
		return plan -> {
			if (Objects.nonNull(plan.getEeErPlanRates()) && Objects.nonNull(plan.getEeErPlanRates().getEmployeeOnly())) {
				return plan.getEeErPlanRates().getEmployeeOnly().getTotal();
			}
			else if (Objects.nonNull(plan.getPlanRates()) && (Objects.nonNull(plan.getPlanRates().getEmployeeOnly()))) {
				if (RateTypeEnum.AGE_BANDED.getType().equals(plan.getPlanRates().getEmployeeOnly())) {
                    return null;
				} else {
					return new BigDecimal(plan.getPlanRates().getEmployeeOnly()
							.replaceAll(AttributeBuilderHelper.COST_CONVERTER, ""));
				}
			}
			else {
				log.error("Plan {} rate is null", plan.getPlanName());
			}
			return null;
		};
	}

	private static String doubleDashIfValueIsNull(Attribute attribute){
		return StringUtils.isBlank(attribute.getValue()) ? NO_VALUE : attribute.getValue();
	}

	private static BiFunction<Integer,Integer,String> getMergedAttributeValue(BiPredicate<Integer,Integer> condition, List<AttributeValue> attributeValues, Attribute... attributes){
		return (operator, comOperator) -> {
            if(condition.test(operator, comOperator)){
				String mergedValue =  Arrays.stream(attributes).filter(Objects::nonNull).map(PlanCompareHelper::doubleDashIfValueIsNull).collect(Collectors.joining(AttributeBuilderHelper.SEPARATOR));
				attributeValues.add(getAttributeValue(ATTRIBUTE, mergedValue, null));
			}
            return null;
		};
	}

	private static Function<Integer, AttributeDesc> getMergedAttributeLabel(BiPredicate<Integer, Integer> condition) {
		return operator -> {
			Optional<Integer> id = Optional.of(operator);
			Optional<AttributeDesc> attributeDesc = id.filter(oper -> oper.equals(ATTRIBUTE_CO_INSURANCE_ID))
					.map(opera -> getAttributeDesc(CO_INSURANCE_IN_OUT_NETWORK, ATTRIBUTE, null));
			attributeDesc = attributeDesc.or(() -> id.filter(oper -> oper.equals(ATTRIBUTE_PRIMARY_SPECIAL_ID))
					.map(opera -> getAttributeDesc(PRIMARY_SPECIAL_LABEL, ATTRIBUTE, null)));
			attributeDesc = attributeDesc.or(() -> id.filter(oper -> oper.equals(ATTRIBUTE_RXTIER_ID))
					.map(opera -> getAttributeDesc(RXTIER_LABEL, ATTRIBUTE, null)));
			return attributeDesc.or(() -> id.filter(oper -> oper.equals(ATTRIBUTE_HOSPITAL_ID))
					.map(oper -> getAttributeDesc(HOSPITAL_OUTPATIENT_LABEL, ATTRIBUTE, null))).orElse(null);
		};
	}

	private static Function<List<Attribute>, Attribute> getAttributeById(Integer id){
		return attributes -> attributes.stream().filter(attrib -> id.equals(attrib.getId())).findFirst().orElse(null);
	}

	private static Predicate<Integer> excludeAttributes(){
		return id ->  !id.equals(ATTRIBUTE_CO_INSURANCE_ID)
				&& !id.equals(ATTRIBUTE_CO_INSURANCE_OUT_ID)
				&& !id.equals(ATTRIBUTE_PRIMARY_SPECIAL_ID)
				&& !id.equals(ATTRIBUTE_RXTIER_ID)
				&& !id.equals(ATTRIBUTE_HOSPITAL_ID);
	}
}