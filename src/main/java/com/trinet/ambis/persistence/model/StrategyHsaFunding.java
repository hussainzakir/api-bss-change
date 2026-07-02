/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hliddle
 *
 */

@Data
@Entity
@Table(name = "XBSS_STRATEGY_HSA_FUNDING")
@NoArgsConstructor
public class StrategyHsaFunding {

	@Id
	@Column(name = "STRATEGY_ID")
	private long strategyId;

	@Column(name = "OPTION_ID")
	private Integer optionId;

	@Column(name = "LUMP_SUM_FREQUENCY")
	private String lumpSumFrequency;

	@Column(name = "ANNUAL_EE_AMOUNT")
	private BigDecimal annualEeAmount;

	@Column(name = "ANNUAL_FAMILY_AMOUNT")
	private BigDecimal annualFamilyAmount;

	@Column(name = "ANNUAL_MONTH")
	private Integer annualMonth;

	@Column(name = "QUARTERLY_EE_AMOUNT")
	private BigDecimal quarterlyEeAmount;

	@Column(name = "QUARTERLY_FAMILY_AMOUNT")
	private BigDecimal quarterlyFamilyAmount;

	@Column(name = "Q1_MONTH")
	private Integer q1Month;

	@Column(name = "Q2_MONTH")
	private Integer q2Month;

	@Column(name = "Q3_MONTH")
	private Integer q3Month;

	@Column(name = "Q4_MONTH")
	private Integer q4Month;

	@Column(name = "CONTRIBUTION_FREQUENCY")
	private String contributionFrequency;

	@Column(name = "MONTHLY_EE_AMOUNT")
	private BigDecimal monthlyEeAmount;

	@Column(name = "MONTHLY_FAMILY_AMOUNT")
	private BigDecimal monthlyFamilyAmount;

}
