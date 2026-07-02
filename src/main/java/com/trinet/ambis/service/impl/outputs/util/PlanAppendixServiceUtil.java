package com.trinet.ambis.service.impl.outputs.util;

import java.util.Optional;
import java.util.function.Function;

import com.trinet.ambis.enums.MedicalPlanAttributes;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixFilters;
import com.trinet.ambis.service.model.plancompare.Attribute;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.util.RangeUtils;

public class PlanAppendixServiceUtil {

	private PlanAppendixServiceUtil() {
	}

	private static Function<BenefitPlanCompare, Optional<Attribute>> getSingleINDeductibleAttribute = plan -> plan
			.getTemplate().stream()
			.flatMap(template -> template.getChildren().stream()
					.filter(child -> child.getName().equalsIgnoreCase(MedicalPlanAttributes.DEDUCTIBLE.getName())))
			.flatMap(template -> template.getChildren().stream())
			.filter(child -> child.getName().equalsIgnoreCase(MedicalPlanAttributes.MEDICAL_SINGLE_DEDUCTIBLE.getName())).findFirst();

	/**
	 * Checks if plan to be added to appendix report based on the single deductible
	 * filter
	 * 
	 * @param plan
	 * @param planAppendixFilters
	 * @return <true> if single deductible attribute is present and value matches
	 *         given filter values <false> if single deductible attribute is present
	 *         and value does not match given filter values <false> deductible
	 *         attribute is not present
	 */
	public static boolean isSingleINDeductibleFilterApplicable(BenefitPlanCompare plan,
			PlanAppendixFilters planAppendixFilters) {
		Optional<Attribute> singleINDeductibleAttribute = getSingleINDeductibleAttribute.apply(plan);
		return (singleINDeductibleAttribute.isPresent()
				&& RangeUtils.isInRange(singleINDeductibleAttribute.get().getValue(),
						planAppendixFilters.getDeductibleMin(), planAppendixFilters.getDeductibleMax()));
	}

}
