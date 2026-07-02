package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.trinet.ambis.common.BSSApplicationConstants;

import lombok.Data;

/**
 * Java DTO representation of the PS_BEN_DEFN_OPTN record in PeopleSoft
 * @author mbrothers
 *
 */
@Data
public class BenDefnOptn implements Serializable {
	
	private static final long serialVersionUID = 4954187142666414L;
	
	private String benefitProgram;
	private String effdt;
	private String planType;
	private BigDecimal optionId;
	private BigDecimal displayOptSeq;
	private String optionType;
	private String benefitPlan;
	private String covrgCd;
	private String optionCd;
	private BigDecimal optionLvl;
	private String dedcd;
	private String dfltOptionInd;
	private String eligRulesId;
	private String locationTblId;
	private String crossPlanType;
	private String crossBenefPlan;
	private BigDecimal coverageLimitPct;
	private String crossPlnDpndChk;
	private BenDefnCost benDefnCost;

	/**
	 * Quick method for determining active options
	 * 
	 * @param elig
	 * @return true if option is eligible; false if not eligible
	 */
	public boolean isPlanActive() {
		// trap null to avoid null pointer exception on List.contains
		if( this.getEligRulesId() == null ) {
			return false;
		}
		return !BSSApplicationConstants.ELIG_INACTIVE.contains(this.getEligRulesId());
	}


	public boolean isWaive() {
		return "W".equals( this.getOptionCd() );
	}
}
