package com.trinet.ambis.util;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.rest.controllers.dto.outputs.AttributeDesc;
import com.trinet.ambis.rest.controllers.dto.outputs.AttributeValue;
import com.trinet.ambis.rest.controllers.dto.outputs.CvgLvlPlanInfo;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAttribute;
import com.trinet.ambis.rest.controllers.dto.outputs.EmployeeCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeTotal;
import com.trinet.ambis.common.BSSApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import static com.trinet.ambis.common.PlanAttributeConstants.*;

public class PlanWordWrapUtil {

	private PlanWordWrapUtil() {
		// Utility class should not be instantiated
	}

	public static String getBenTypeName(String benType) {
		if (StringUtils.isBlank(benType)) return "";
		if (BSSApplicationConstants.MEDICAL_PLAN_TYPES.contains(benType)) {
			return ProspectConstants.MEDICAL_PLAN_TYPE_DESC.toUpperCase();
		} else if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(benType)) {
			return ProspectConstants.DENTAL_PLAN_TYPE_DESC.toUpperCase();
		} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(benType)) {
			return ProspectConstants.VISION_PLAN_TYPE_DESC.toUpperCase();
		}
		return benType;
	}

	public static int calculateMaxLinesForPlan(PlanAttribute plan, List<AttributeDesc> attributeLabels, Map<String, Double> limits,String benTypeName) {
		int planNameMax = 1;
		if (StringUtils.isNotBlank(plan.getPlanName())) {
			planNameMax = updateMaxLines(planNameMax, plan.getPlanName().length(),limits.get(KEY_PLAN_NAME));
		}
		int ratesMax = 1;
		if (plan.getPlanRates() != null) {
			ratesMax = calculateMaxLinesForPlanRates(plan.getPlanRates(), plan.getHeadCount(), limits.get(KEY_PLAN_RATES));
		}

		int attrMax = 1;
		if (plan.getAttributeValues() != null && attributeLabels != null) {
			Map<String, StringBuilder> groupedValueBuilders = new HashMap<>();
			groupedValueBuilders.put(KEY_VISIT, new StringBuilder());
			groupedValueBuilders.put(KEY_DEDUCTIBLE, new StringBuilder());
			groupedValueBuilders.put(KEY_OOP, new StringBuilder());
			attrMax = traverseAttributesMaxLines(plan.getAttributeValues(), attributeLabels, groupedValueBuilders, null, limits,benTypeName);
			int accuMax = updateMaxLinesFromAccumulators(groupedValueBuilders, limits);
			attrMax = Math.max(attrMax, accuMax);
		}

		int maxLines = Math.max(1, Math.max(Math.max(planNameMax, ratesMax), attrMax));

		plan.setWordWrap(maxLines > 1);
		return maxLines;
	}

	private static int calculateMaxLinesForPlanRates(CvgLvlPlanInfo rates, CvgLvlPlanInfo headCount, Double rateLengthLimit) {
		if (rates == null) return 1;
		int maxLines = 1;

		String[] rateValues = {
			rates.getEmployeeOnly(),
			rates.getEmployeeSpouse(),
			rates.getEmployeeChildren(),
			rates.getFamily()
		};
		String[] hcValues = null;
		if (headCount != null) {
			hcValues = new String[] {
				headCount.getEmployeeOnly(),
				headCount.getEmployeeSpouse(),
				headCount.getEmployeeChildren(),
				headCount.getFamily()
			};
		}

		for (int i = 0; i < rateValues.length; i++) {
			String rate = rateValues[i];
			if (StringUtils.isNotBlank(rate)) {
				int length = rate.length();
				if (hcValues != null && hcValues[i] != null) {
					String hc = hcValues[i];
					if ("0".equals(hc)) {
						length += 3; // "(-)"
					} else {
						length += hc.length() + 2; // "(XX)"
					}
				}
				maxLines = updateMaxLines(maxLines, length, rateLengthLimit);
			}
		}

		return maxLines;
	}

	private static int traverseAttributesMaxLines(List<AttributeValue> values, List<AttributeDesc> labels, Map<String, StringBuilder> groupedValueBuilders, Integer parentId, Map<String, Double> limits, String benTypeName) {
		int maxLines = 1;
		if (values == null || labels == null) return maxLines;

		int size = Math.min(values.size(), labels.size());
		for (int i = 0; i < size; i++) {
			AttributeValue val = values.get(i);
			AttributeDesc desc = labels.get(i);

			if (ATTR_TYPE_CATEGORY.equals(val.getType()) && val.getChildren() != null && desc.getChildren() != null) {
				Integer newParentId = desc.getId() != null ? desc.getId() : parentId;
				int childLines = traverseAttributesMaxLines(val.getChildren(), desc.getChildren(), groupedValueBuilders, newParentId, limits, benTypeName);
				maxLines = Math.max(maxLines, childLines);
			} else if (ATTR_TYPE_ATTRIBUTE.equals(val.getType())) {
				maxLines = processAttributeMaxLines(val, desc, groupedValueBuilders, parentId, limits, maxLines, benTypeName);
			}
		}
		return maxLines;
	}

	private static int updateMaxLinesFromAccumulators(Map<String, StringBuilder> groupedValueBuilders, Map<String, Double> limits) {
		if (groupedValueBuilders == null || limits == null) return 1;

		int visitLines = 1;
		StringBuilder visit = groupedValueBuilders.get(KEY_VISIT);
		if (StringUtils.isNotBlank(visit)) {
			visitLines = updateMaxLines(visitLines, visit.length(), limits.get(KEY_VISIT));
		}

		int dedLines = 1;
		StringBuilder ded = groupedValueBuilders.get(KEY_DEDUCTIBLE);
		if (StringUtils.isNotBlank(ded)) {
			dedLines = updateMaxLines(dedLines, ded.length(), limits.get(KEY_DEDUCTIBLE));
		}

		int oopLines = 1;
		StringBuilder oop = groupedValueBuilders.get(KEY_OOP);
		if (StringUtils.isNotBlank(oop)) {
			oopLines = updateMaxLines(oopLines, oop.length(), limits.get(KEY_OOP));
		}

		return Math.max(visitLines, Math.max(dedLines, oopLines));
	}

	private static int processAttributeMaxLines(AttributeValue val, AttributeDesc desc, Map<String, StringBuilder> groupedValueBuilders, Integer parentId, Map<String, Double> limits, int maxLines, String benTypeName) {
		String originalValue = val.getValue();
		if (StringUtils.isBlank(originalValue)) return maxLines;

		Integer id = desc.getId();
		if (shouldSkipAttributeCheck(id) || StringUtils.isBlank(desc.getName())) {
			return maxLines;
		}

		String processedValue = replaceDeductibleMarkers(originalValue);
		String inNetworkPart = extractInNetworkPart(desc.getName(), processedValue);

		appendToGroupedValueBuilders(id, parentId, inNetworkPart, groupedValueBuilders, benTypeName);

		return updateMaxLines(maxLines, inNetworkPart.length(), limits.get(id != null ? String.valueOf(id) : desc.getName()));
	}

	private static String extractInNetworkPart(String name, String processedValue) {
		if (name.contains(SUFFIX_IN_NETWORK_OON)) {
			int splitIdx = processedValue.indexOf(SEPARATOR_SLASH);
			if (splitIdx != -1) {
				return processedValue.substring(0, splitIdx);
			}
			splitIdx = processedValue.indexOf("/");
			if (splitIdx != -1) {
				return processedValue.substring(0, splitIdx);
			}
		}
		return processedValue;
	}

	private static void appendToGroupedValueBuilders(Integer id, Integer parentId, String inNetworkPart, Map<String, StringBuilder> groupedValueBuilders, String benTypeName) {
		if (id == null) return;
		if (ProspectConstants.MEDICAL_PLAN_TYPE_DESC.equalsIgnoreCase(benTypeName) &&
				(id == ID_PRIMARY_SPECIALIST ||
				id == ID_URGENT_CARE ||
				id == ID_EMERGENCY_ROOM)) {
			appendWithSeparator(groupedValueBuilders.get(KEY_VISIT), inNetworkPart);
		}
		if (ProspectConstants.MEDICAL_PLAN_TYPE_DESC.equalsIgnoreCase(benTypeName) &&
				parentId != null && (id == ID_DEDUCTIBLE_SINGLE_MED || id == ID_DEDUCTIBLE_FAMILY_MED)) {
			appendWithSeparator(groupedValueBuilders.get(KEY_DEDUCTIBLE), inNetworkPart);
		}
		if (ProspectConstants.DENTAL_PLAN_TYPE_DESC.equalsIgnoreCase(benTypeName) &&
				parentId != null && (id == ID_DEDUCTIBLE_SINGLE_DEN || id == ID_DEDUCTIBLE_FAMILY_DEN)) {
			appendWithSeparator(groupedValueBuilders.get(KEY_DEDUCTIBLE), inNetworkPart);
		}
		if (ProspectConstants.MEDICAL_PLAN_TYPE_DESC.equalsIgnoreCase(benTypeName) &&
				parentId != null && (id == ID_OOP_SINGLE || id == ID_OOP_FAMILY)) {
			appendWithSeparator(groupedValueBuilders.get(KEY_OOP), inNetworkPart);
		}
	}
	private static void appendWithSeparator(StringBuilder sb, String value) {
		if (sb == null || StringUtils.isBlank(value)) return;
		if (sb.length() > 0) sb.append(SEPARATOR_SLASH);
		sb.append(value);
	}
	private static int updateMaxLines(int currentMaxLines, int valueLength, Double limit) {
		if (limit != null) {
			return Math.max(currentMaxLines, (int) Math.ceil(valueLength / limit));
		}
		return currentMaxLines;
	}

	private static boolean shouldSkipAttributeCheck(Integer id) {
		if (id == null) return false;

		return  id == SKIP_ATTR_NAT_NETWORK ||
			    id == SKIP_ATTR_RX_DED_NON_GENERIC ||
			    id == SKIP_ATTR_ORTHO_OR_HOSP_OUTPATIENT ||
		        id == SKIP_ATTR_HOSP_INPATIENT ||
				id == SKIP_ATTR_WAITING_PERIOD_MAJOR;
	}

	private static String replaceDeductibleMarkers(String value) {
		if (StringUtils.isBlank(value)) return value;

		value = value.replace(" after ded", "*");
		value = value.replace("after ded", "*");
		value = value.replace(" after Rx ded", "*");
		value = value.replace("after Rx ded", "*");
		value = value.replace("Kaiser", "**");
		value = value.replace("(Kaiser Pharmacy)", "**");

		return value;
	}
	public static int calculateMaxLinesForEmployeeCostSummary(EmployeeCostSummary emp, Map<String, Double> limits) {
		int[] lineRequirements = {
				calculateMaxLines(getPlanName(emp.getCurrentPlan()), limits.get(KEY_EMP_CURR_PLAN_NAME)),
				calculateMaxLines(getPlanName(emp.getTriNetPlan()), limits.get(KEY_EMP_PROP_PLAN_NAME)),
				calculateMaxLines(getEmployeeFullName(emp), limits.get(KEY_EMP_NAME)),
				calculateMaxLines(getEmployeeState(emp), limits.get(KEY_EMP_STATE)),
				calculateMaxLines(getEmployeeCoverageCode(emp), limits.get(KEY_EMP_TIERS)),
				calculateMaxLinesForPlanCosts(emp.getCurrentPlan(), limits.get(KEY_EMP_COST)),
				calculateMaxLinesForPlanCosts(emp.getTriNetPlan(), limits.get(KEY_EMP_COST)),
				calculateMaxLines(String.valueOf(emp.getCostDiff()), limits.get(KEY_EMP_COST_DIFF))
		};

		int empLines = java.util.stream.IntStream.of(lineRequirements).max().orElse(1);

		emp.setWordWrap(empLines > 1);
		return empLines;
	}

	private static String getPlanName(BenefitTypeTotal plan) {
		return plan != null ? plan.getPlanName() : null;
	}

	private static String getEmployeeFullName(EmployeeCostSummary emp) {
		if (emp.getEmployee() == null) return "";
		return (StringUtils.defaultString(emp.getEmployee().getFirstName()) + StringUtils.defaultString(emp.getEmployee().getLastName())).trim();
	}

	private static String getEmployeeState(EmployeeCostSummary emp) {
		return emp.getEmployee() != null ? emp.getEmployee().getState() : null;
	}

	private static String getEmployeeCoverageCode(EmployeeCostSummary emp) {
		return emp.getEmployee() != null ? emp.getEmployee().getCoverageCode() : null;
	}

	private static int calculateMaxLines(String value, Double limit) {
		return StringUtils.isNotBlank(value) ? updateMaxLines(1, value.length(), limit) : 1;
	}

	public static int calculateMaxLinesForPlanCosts(BenefitTypeTotal plan, Double costLimit) {
		if (plan == null) return 1;
		int lines = 1;
		if (plan.getErAmount() != null) lines = updateMaxLines(lines, plan.getErAmount().toString().length(), costLimit);
		if (plan.getEeAmount() != null) lines = updateMaxLines(lines, plan.getEeAmount().toString().length(), costLimit);
		if (plan.getTotal() != null) lines = updateMaxLines(lines, plan.getTotal().toString().length(), costLimit);
		return lines;
	}
}
