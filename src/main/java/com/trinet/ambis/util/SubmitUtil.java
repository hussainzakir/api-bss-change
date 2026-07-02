package com.trinet.ambis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitPlan;

public class SubmitUtil {
	private static final Logger logger = LoggerFactory.getLogger(SubmitUtil.class);

	private SubmitUtil() {
		throw new IllegalStateException(
				"Utility class " + SubmitUtil.class.getName() + " can not be instantiated.");
	}
	
	public static String getCoverageCode(String coverCdStr) {
		if ("employeePlusOne".equals(coverCdStr)) {
			return "81";
		} else if ("employee".equals(coverCdStr)) {
			return "1";
		} else if ("family".equals(coverCdStr)) {
			return "82";
		} else if ("employeePlusSpouse".equals(coverCdStr)) {
			return "2";
		} else if ("employeePlusChild".equals(coverCdStr)) {
			return "C";
		} else if ("employeePlusFamily".equals(coverCdStr)) {
			return "4";
		} else if ("all".equals(coverCdStr)) {
			return "Z";
		} else {
			logger.info("INVALID COVERAGE CODE : {} " , coverCdStr);
		}
		return null;
	}

	public static Set<String> getSelectedBenefitPlans(BenefitOffer benefitOfferDto) {
		List<BenefitPlan> list = new ArrayList<>();
		if (benefitOfferDto.getSummary().getType().equals(Constants.MEDICAL)
				|| benefitOfferDto.getSummary().getType().equals(Constants.DENTAL)
				|| benefitOfferDto.getSummary().getType().equals(Constants.VISION)) {
			list.addAll(benefitOfferDto.getBenefitPlans());
		}
		Set<String> benefitsPlansList = new TreeSet<>();
		for (BenefitPlan plan : list) {
			benefitsPlansList.add(plan.getId());
		}

		return benefitsPlansList;
	}

	public static boolean isEmployeePaid(BenefitOffer benefitOffer) {
		boolean result = false;
		List<BenefitPlan> list = new ArrayList<>(benefitOffer.getBenefitPlans());
		for (BenefitPlan plan : list) {
			if(plan.isEmployeePaid()) {
				result = true;
				break;
			}
		}
		return result;
	}
}
