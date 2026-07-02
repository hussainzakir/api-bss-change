package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BenDefnOptnHSA extends BenDefnOptn {
	
	private static final long serialVersionUID = 5959086827520101373L;
	
	private static final String DEACTIVATE_HSA = "236Q";
	private static final String TURN_OFF_HSA = "2009";
	private static final BigDecimal OPTION_LVL_INACTIVE = new BigDecimal("60");
	private static final String HSA_PLAN_TYPE = "67";
	private String pfClient;

	public BenDefnOptnHSA() {
		super();
	}

	/**
	 * Determine whether this is an HSA option with Level 0
	 * 
	 * @return true if this is a Level 0 HSA option, otherwise false
	 */
	public boolean isOptnHSALevel0() {
		return this.getPlanType().equals(HSA_PLAN_TYPE) && this.getOptionCd() != null
				&& this.getOptionCd().endsWith("0");
	}

	/**
	 * Determine whether this is an HSA option with Level 5
	 * 
	 * @return true if this is a Level 5 HSA option, otherwise false
	 */
	public boolean isOptnHSALevel5() {
		return this.getPlanType().equals(HSA_PLAN_TYPE) && this.getOptionCd() != null
				&& this.getOptionCd().endsWith("5");
	}

	public void turnOffHSA() {
		this.setOptionLvl(OPTION_LVL_INACTIVE);
		this.setEligRulesId(TURN_OFF_HSA);
	}

	public void deactivateHSA() {
		this.setOptionLvl(OPTION_LVL_INACTIVE);
		this.setEligRulesId(DEACTIVATE_HSA);
	}

}
