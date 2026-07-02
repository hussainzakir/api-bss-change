/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.List;

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Data
public class AdditionalBenefitPlanRates {

	private long groupId;
	private String groupName;
	private String groupType;
	private String benefitProgram;
	private List<AdditionalBenefitsCategoryOffer> additionalBenefitOffers;

}
