package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;

import lombok.Data;

@Data
public class StrategyHsaFundingDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long strategyId;
	private Integer optionId;
	private String lumpSumFrequency;
	private BigDecimal annualEeAmount;
	private BigDecimal annualFamilyAmount;
	private Integer annualMonth;
	private BigDecimal quarterlyEeAmount;
	private BigDecimal quarterlyFamilyAmount;
	private Integer q1Month;
	private Integer q2Month;
	private Integer q3Month;
	private Integer q4Month;
	private String contributionFrequency;
	private BigDecimal monthlyEeAmount;
	private BigDecimal monthlyFamilyAmount;
	private boolean customLevel;
	
	public StrategyHsaFundingDto() {
	}
	
	public StrategyHsaFundingDto(StrategyHsaFundingDto strategyHsaFundingDto, long strategyId) {
		this.strategyId = strategyId;
		this.optionId = strategyHsaFundingDto.getOptionId();
		this.lumpSumFrequency = strategyHsaFundingDto.getLumpSumFrequency();
		this.annualEeAmount = strategyHsaFundingDto.getAnnualEeAmount();
		this.annualFamilyAmount = strategyHsaFundingDto.getAnnualFamilyAmount();
		this.annualMonth = strategyHsaFundingDto.getAnnualMonth();
		this.quarterlyEeAmount = strategyHsaFundingDto.getQuarterlyEeAmount();
		this.quarterlyFamilyAmount = strategyHsaFundingDto.getQuarterlyFamilyAmount();
		this.q1Month = strategyHsaFundingDto.getQ1Month();
		this.q2Month = strategyHsaFundingDto.getQ2Month();
		this.q3Month = strategyHsaFundingDto.getQ3Month();
		this.q4Month = strategyHsaFundingDto.getQ4Month();
		this.contributionFrequency = strategyHsaFundingDto.getContributionFrequency();
		this.monthlyEeAmount = strategyHsaFundingDto.getMonthlyEeAmount();
		this.monthlyFamilyAmount = strategyHsaFundingDto.getMonthlyFamilyAmount();
	}	


	/**
	 * Returns true when the client has chosen to offer customized HSA plans.
	 * Currently, the HSA levels 5, 6, and 7 are the custom levels.
	 * @return true if the client has chosen a custom HSA level, otherwise false
	 */
	public boolean isCustomLevel() {
		if( this.getOptionId() == null ) {
			return false;
		}
		if( Arrays.asList( 5, 6, 7 ).contains( this.getOptionId() )) {
			return true;
		} else {
			return false;
		}
	}

}
