package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.trinet.ambis.persistence.model.StrategyHsaFunding;

import lombok.Data;

/**
 * @author hliddle
 */
@Data
public class ModelCompareStrategyHsaFunding {
	
	private Integer optionId;
	private String lumpSumFrequency;
	private BigDecimal annualEeAmount;
	private BigDecimal annualFamilyAmount;
	private String annualMonth;
	private BigDecimal quarterlyEeAmount;
	private BigDecimal quarterlyFamilyAmount;
	private String contributionFrequency;
	private BigDecimal monthlyEeAmount;
	private BigDecimal monthlyFamilyAmount;
	private Map<String, String> quarters = new HashMap<>();

	public ModelCompareStrategyHsaFunding() {
	}

	public ModelCompareStrategyHsaFunding(StrategyHsaFunding strategyHsaFunding) {
		this.optionId = strategyHsaFunding.getOptionId();
		this.lumpSumFrequency = strategyHsaFunding.getLumpSumFrequency();
		this.annualEeAmount = strategyHsaFunding.getAnnualEeAmount();
		this.annualFamilyAmount = strategyHsaFunding.getAnnualFamilyAmount();
		this.annualMonth = strategyHsaFunding.getAnnualMonth() == null ? ""
				: StringUtils.capitalize(StringUtils.lowerCase(Month.of(strategyHsaFunding.getAnnualMonth().intValue()).name()));
		this.quarterlyEeAmount = strategyHsaFunding.getQuarterlyEeAmount();
		this.quarterlyFamilyAmount = strategyHsaFunding.getQuarterlyFamilyAmount();
		this.contributionFrequency = strategyHsaFunding.getContributionFrequency();
		this.monthlyEeAmount = strategyHsaFunding.getMonthlyEeAmount();
		this.monthlyFamilyAmount = strategyHsaFunding.getMonthlyFamilyAmount();
		this.quarters.put("q1", strategyHsaFunding.getQ1Month() == null ? ""
				: StringUtils.capitalize(StringUtils.lowerCase(Month.of(strategyHsaFunding.getQ1Month().intValue()).name())));
		this.quarters.put("q2", strategyHsaFunding.getQ2Month() == null ? ""
				: StringUtils.capitalize(StringUtils.lowerCase(Month.of(strategyHsaFunding.getQ2Month().intValue()).name())));
		this.quarters.put("q3", strategyHsaFunding.getQ3Month() == null ? ""
				: StringUtils.capitalize(StringUtils.lowerCase(Month.of(strategyHsaFunding.getQ3Month().intValue()).name())));
		this.quarters.put("q4", strategyHsaFunding.getQ4Month() == null ? ""
				: StringUtils.capitalize(StringUtils.lowerCase(Month.of(strategyHsaFunding.getQ4Month().intValue()).name())));
	}

}