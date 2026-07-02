package com.trinet.ambis.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.rest.controllers.dto.outputs.AttributeDesc;
import com.trinet.ambis.rest.controllers.dto.outputs.AttributeValue;
import com.trinet.ambis.rest.controllers.dto.outputs.CompareCurrentTrinetPlans;
import com.trinet.ambis.rest.controllers.dto.outputs.CvgLvlPlanInfo;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAttribute;

public class AttributeBuilderHelper {
	
	public static final String IN_NETWORK = "In-Network";
	public static final String IN_NETWORK_LABEL = "- In Network";
	public static final String OUT_OF_NETWORK = "Out-of-Network";
	public static final String IN_OUT_OF_NETWORK = "(In-Network/OON)";
	public static final String OUT_OF_NETWORK_VALUE = "Out of Network";
	public static final String SINGLE_IN_OUT_OF_NETWORK = "Single (In-Network/OON)";
	public static final String FAMILY_IN_OUT_OF_NETWORK = "Family (In-Network/OON)";
	public static final String SINGLE = "Single";
	public static final String FAMILY = "Family";
	public static final String SEPARATOR = " / ";
	public static final String CO_INSURANCE_OUT_OF_NETWORK ="Co-Insurance Out-of-Network";
	public static final String CO_INSURANCE ="Co-Insurance";
	public static final String CO_INSURANCE_IN_OUT_NETWORK = "Coinsurance (In-Network/OON)";
	public static final String DOLLAR = "$";
	public static final char COMMA = ',';
	public static final String COST_FORMATER = "#,##0.00";
	public static final String UNIT_RATE_FORMATER = "#,##0.00########";
	public static final String NO_VALUE = "--";
	public static final String CATEGORY = "category";
	public static final String ATTRIBUTE = "attribute";
	public static final String NO_PLAN_NAME = "No Current Plan";
	public static final String NO_PLAN_ID = "No_plan";
	public static final String NO_TYPE = "No_type";
	public static final String COST_CONVERTER = "[$,]";

	//Attribute Ids
	public static final String PRIMARY_SPECIAL_LABEL = "Primary" + SEPARATOR + "Specialist";
	public static final String RXTIER_LABEL = "Prescriptions (Tier 1" + SEPARATOR + "2" + SEPARATOR + "3)";
	public static final String HOSPITAL_OUTPATIENT_LABEL = "Hospital Outpatient (Facility" + SEPARATOR + "Surgery)";
	public static final Integer COINSURANCE_ID = 10;
	public static final Integer SPECIALIST_VISIT = 12;
	public static final Integer BRAND_RX_TIER_2 = 16;
	public static final Integer NONFORMULARY_RX_TIER_3 = 17;
	public static final Integer OUTPATIENT_FACILITY = 19;
	public static final Integer ATTRIBUTE_CO_INSURANCE_ID = 1;
	public static final Integer ATTRIBUTE_CO_INSURANCE_OUT_ID = 10;
	public static final Integer ATTRIBUTE_PRIMARY_SPECIAL_ID = 11;
	public static final Integer ATTRIBUTE_RXTIER_ID = 15;
	public static final Integer ATTRIBUTE_HOSPITAL_ID = 23;


	
	private AttributeBuilderHelper() {
		throw new IllegalStateException(
				"Helper class " + AttributeBuilderHelper.class.getName() + " can not be instantiated.");
	}
	
	public static AttributeValue getAttributeValue(String type, String value, List<AttributeValue> children) {
		return AttributeValue.builder().type(type).value(value).children(children).build();
	}
	
	public static AttributeDesc getAttributeDesc(String name, String type, List<AttributeDesc> children) {
		return AttributeDesc.builder().name(name).type(type).children(children).build();
	}

	public static AttributeDesc getAttributeDesc(Integer id, String name, String type, List<AttributeDesc> children) {
		return AttributeDesc.builder().id(id).name(name).type(type).children(children).build();
	}

	public static CompareCurrentTrinetPlans getCompareCurrentTrinetPlans(PlanAttribute currentPlan, List<PlanAttribute> triNetPlans) {
		return CompareCurrentTrinetPlans.builder()
				.currentPlan(currentPlan)
				.triNetPlans(triNetPlans)
				.build();
	}
	
	public static Map<String, PlanAttribute> planAttributeMapByBenefitType(){
		Map<String, PlanAttribute> emptyPlans = new HashMap<>();
		emptyPlans.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, emptyMedicalPlanAttributes());
		emptyPlans.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, emptyDentalPlanAttributes());
		emptyPlans.put(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE, emptyDentalPlanAttributes());
		emptyPlans.put(BSSApplicationConstants.VISION_PLAN_TYPE, emptyVisionPlanAttributes());
		emptyPlans.put(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE, emptyVisionPlanAttributes());
		return emptyPlans;
	}
	
	public static PlanAttribute emptyMedicalPlanAttributes() {
		return PlanAttribute.builder()
				.planName(NO_PLAN_NAME)
				.carrierName(NO_VALUE)
				.planId(NO_PLAN_ID)
				.attributeValues(dummyMedicalAttributeValue())
				.headCount(AttributeBuilderHelper.dummyCvgLvlPlan())
				.planRates(AttributeBuilderHelper.dummyCvgLvlPlan())
				.build();
	}
	
	public static PlanAttribute emptyDentalPlanAttributes() {
		return PlanAttribute.builder()
				.planName(NO_PLAN_NAME)
				.carrierName(NO_VALUE)
				.planId(NO_PLAN_ID)
				.attributeValues(dummyDentalAttributeValue())
				.headCount(AttributeBuilderHelper.dummyCvgLvlPlan())
				.planRates(AttributeBuilderHelper.dummyCvgLvlPlan())
				.build();
	}
	
	public static PlanAttribute emptyVisionPlanAttributes() {
		return PlanAttribute.builder()
				.planName(NO_PLAN_NAME)
				.carrierName(NO_VALUE)
				.planId(NO_PLAN_ID)
				.attributeValues(dummyVisionAttributeValue())
				.headCount(AttributeBuilderHelper.dummyCvgLvlPlan())
				.planRates(AttributeBuilderHelper.dummyCvgLvlPlan())
				.build();
	}

	public static List<AttributeValue> dummyMedicalAttributeValue() {
		List<AttributeValue> medicalAttributeValues = new LinkedList<>();

		List<AttributeValue> children = new ArrayList<>();
		AttributeValue attributeValue = getAttributeValue(ATTRIBUTE, NO_VALUE, null);
		medicalAttributeValues.add(attributeValue);//Net Work Name 
		children.add(attributeValue);
		children.add(attributeValue);
		AttributeValue deductable = getAttributeValue(CATEGORY, NO_VALUE, children);
		AttributeValue outOfPacketMax = getAttributeValue(CATEGORY, NO_VALUE, children);
		medicalAttributeValues.add(deductable);
		medicalAttributeValues.add(outOfPacketMax);
		medicalAttributeValues.add(attributeValue);//Coinsurance
		medicalAttributeValues.add(attributeValue);//Primary Care Visit
		medicalAttributeValues.add(attributeValue);//Specialist Visit
		medicalAttributeValues.add(attributeValue);//Lab & X-Ray
		medicalAttributeValues.add(attributeValue);//Urgent Care Visit
		medicalAttributeValues.add(attributeValue);//Emergency Room Visit
		medicalAttributeValues.add(attributeValue);//Rx Deductible (Non-Generic)
		medicalAttributeValues.add(attributeValue);//Generic Rx (Tier 1)
		medicalAttributeValues.add(attributeValue);//Brand Rx (Tier 2)
		medicalAttributeValues.add(attributeValue);//Non-Formulary Rx (Tier 3) 
		return medicalAttributeValues;
	}
	
	public static List<AttributeValue> dummyDentalAttributeValue(){
		List<AttributeValue> dentalAttributeValues = new LinkedList<>();
		List<AttributeValue> children = new ArrayList<>();
		AttributeValue attributeValue = getAttributeValue(ATTRIBUTE, NO_VALUE, null);
		children.add(attributeValue);
		children.add(attributeValue);
		AttributeValue deductable = getAttributeValue(CATEGORY, NO_VALUE, children);
		dentalAttributeValues.add(deductable);
		dentalAttributeValues.add(attributeValue);//Preventative
		dentalAttributeValues.add(attributeValue);//Basic
		dentalAttributeValues.add(attributeValue);//Major
		dentalAttributeValues.add(attributeValue);//Annual Maximum
		dentalAttributeValues.add(attributeValue);//Orthodontia Lifetime Maximum
		dentalAttributeValues.add(attributeValue);//Orthodontia
		dentalAttributeValues.add(attributeValue);//Waiting Period - Major
		dentalAttributeValues.add(attributeValue);//Endo/Perio/Oral Surgery 
		dentalAttributeValues.add(attributeValue);
		return dentalAttributeValues;
	}
	
	public static List<AttributeValue> dummyVisionAttributeValue(){
		List<AttributeValue> visionAttributeValues = new LinkedList<>();
		AttributeValue attributeValue = getAttributeValue(ATTRIBUTE, NO_VALUE, null);
		visionAttributeValues.add(attributeValue);//Examination Copay
		visionAttributeValues.add(attributeValue);//Materials Copay
		visionAttributeValues.add(attributeValue);//Frames Allowance
		visionAttributeValues.add(attributeValue);//Examination Frequency 
		visionAttributeValues.add(attributeValue);//Frames Frequency
		visionAttributeValues.add(attributeValue);//Lenses or Contact Lens Frequency
		return visionAttributeValues;
	}
	
	public static CvgLvlPlanInfo dummyCvgLvlPlan() {
		CvgLvlPlanInfo rate = new CvgLvlPlanInfo();
		rate.setEmployeeChildren(NO_VALUE);
		rate.setEmployeeOnly(NO_VALUE);
		rate.setFamily(NO_VALUE);
		rate.setEmployeeSpouse(NO_VALUE);
		return rate;
	}
}
