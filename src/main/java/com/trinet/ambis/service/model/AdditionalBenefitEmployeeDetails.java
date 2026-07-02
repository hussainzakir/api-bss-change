package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a data collection class that can be used to gather the data needed to calculate the costs
 * of additional benefits (life insurance and disability).
 * @author rvutukuri
 *
 */
public class AdditionalBenefitEmployeeDetails {

	private Map<String, AdditionalPlan> planToDetailsMap;

	/**
	 * Get the set of benefit plan keys saved to this object so far.
	 * @return
	 */
	public Set<String> getPlans() {
		return this.planToDetailsMap.keySet();
	}

	/**
	 * Set the formula properties for this benefit plan.
	 * @param props
	 */
	public void setPlanFormulaProps( FormulaProperties props ) {
		AdditionalPlan plan = planToDetailsMap.get( props.getBenefitPlan() );
		if( plan != null ) {
			plan.setAsOfDate( props.getCoverageAsOfDate() );
			plan.setCoverageMin( props.getMinCovrg() );
			plan.setCoverageMax( props.getMaxCovrg() );
			plan.setBaseSource( props.getBaseSource() );
		}
	}

	/**
	 * Add the provided benefitPlan parameter to the map of benefit plans.
	 * If the key already exists, do nothing.
	 * @param benefitPlan
	 */
	public void addBenefitPlan( String benefitPlan ) {
		// if the planToDetailsMap does not exist, then create it
		if( planToDetailsMap == null ) {
			planToDetailsMap = new HashMap<>();
		}
		// create a new AdditionalPlan object if one doesn't already exist for this benefit plan
		if( planToDetailsMap.containsKey( benefitPlan )) {
			// do nothing
		} else {
			planToDetailsMap.put( benefitPlan, new AdditionalPlan() );
		}

	}

	/**
	 * Add salary data for a plan and employee
	 * @param benefitPlan
	 * @param emplid
	 * @param rate
	 */
	public void addPlanEmployeeAndRate( String benefitPlan, String emplid, BigDecimal rate ) {
		this.addBenefitPlan( benefitPlan );
		AdditionalPlan planDetails = this.planToDetailsMap.get( benefitPlan );
		planDetails.addEmployeeAndRate( emplid, rate );
	}


	/**
	 * When matching employee salaries to a plan, often the as-of date is the critical value.
	 * This method takes an employee and the corresponding salary amounts and saves the correct
	 * value to each plan with a matching as-of date.
	 * @param emplid
	 * @param annualRate
	 * @param abbr
	 * @param asOfDate
	 */
	public void addPlanEmployeeAndRate( String emplid, BigDecimal annualRate, BigDecimal abbr, java.sql.Date asOfDate ) {
		for( Map.Entry<String,AdditionalPlan> entry : planToDetailsMap.entrySet() ) {
			if( asOfDate != null && asOfDate.equals( entry.getValue().getAsOfDate() )) {
				entry.getValue().addEmployeeAndChooseRate( emplid, annualRate, abbr );
			}
		}
	}


	/**
	 * Gets the emplid-to-salary mapping saved for this benefit plan.
	 * @param benefitPlan
	 * @return a mapping of EMPLID to base salary (as defined for this plan)
	 */
	public Map<String,BigDecimal> retrieveEmplSalaryMapForPlan( String benefitPlan ) {
		AdditionalPlan planDetails = this.planToDetailsMap.get( benefitPlan );
		return planDetails.getSalaryMap();
	}

	public int getEmplCountForPlan( String benefitPlan ) {
		AdditionalPlan planDetails = this.planToDetailsMap.get( benefitPlan );
		return planDetails.getSalaryMap().size();
	}

	/**
	 * Finds all the unique as-of dates stored in the collection of AdditionalPlan details in this object.
	 * @return a set of SQL Date values
	 */
	public Set<java.sql.Date> selectDistinctAsOfDate() {
		Set<java.sql.Date> set = new HashSet<>();
		for( Map.Entry<String,AdditionalPlan> entry : this.planToDetailsMap.entrySet() ) {
			set.add( entry.getValue().getAsOfDate() );
		}
		return set;
	}

	/**
	 * This inner class contains the properties required for calculating the plan cost
	 * as well as the list of eligible employees and the corresponding salary amounts.
	 * @author mbrothers
	 *
	 */
	class AdditionalPlan {
		private java.sql.Date asOfDate;
		private Long salaryFactor;
		private BigDecimal coverageMin;
		private BigDecimal coverageMax;
		//base source is ANRT or ABBR
		private String baseSource;
		private Map<String, BigDecimal> emplSalaryMap;

		AdditionalPlan() {
			this.emplSalaryMap = new HashMap<>();
		}

		public void setAsOfDate( java.sql.Date newAsOfDate ) {
			if( newAsOfDate != null ) {
				this.asOfDate = newAsOfDate;
			}
		}
		public java.sql.Date getAsOfDate() {
			return this.asOfDate;
		}

		public void setSalaryFactor( Long newSalaryFactor ) {
			if( newSalaryFactor != null ) {
				this.salaryFactor = newSalaryFactor;
			}
		}
		public Long getSalaryFactor() {
			return this.salaryFactor;
		}

		public void setCoverageMin( BigDecimal newCoverageMin ) {
			if( newCoverageMin != null ) {
				this.coverageMin = newCoverageMin;
			}
		}
		public BigDecimal getCoverageMin() {
			return this.coverageMin;
		}

		public void setCoverageMax( BigDecimal newCoverageMax ) {
			if( newCoverageMax != null ) {
				this.coverageMax = newCoverageMax;
			}
		}
		public BigDecimal getCoverageMax() {
			return this.coverageMax;
		}

		public void setBaseSource( String newBaseSource ) {
			if( newBaseSource != null ) {
				this.baseSource = newBaseSource;
			}
		}
		public String getBaseSource() {
			return this.baseSource;
		}

		public Map<String,BigDecimal> getSalaryMap() {
			return this.emplSalaryMap;
		}

		public BigDecimal getAverageCoverage() {
			BigDecimal total = BigDecimal.ZERO.setScale( 2, RoundingMode.HALF_UP );
			for( BigDecimal rate : this.emplSalaryMap.values() ) {
				BigDecimal covrg = rate;
				if( this.getSalaryFactor() == null || this.getSalaryFactor() == 0L ) {
					// there is no salary factor; do not multiply
				} else {
					covrg = covrg.multiply( BigDecimal.valueOf( this.getSalaryFactor() ) );
				}

				covrg = ( covrg.compareTo( this.getCoverageMin() ) < 0 ) ? this.getCoverageMin() : covrg;

				if( this.getCoverageMax() == null || this.getCoverageMax().equals( BigDecimal.ZERO ) ) {
					// no coverage max; do not apply a limit
				} else {
					covrg = ( covrg.compareTo( this.getCoverageMax() ) > 0 ? this.coverageMax : covrg );
				}

				total = total.add( covrg );
			}
			return total.divide( BigDecimal.valueOf( this.emplSalaryMap.size() ), RoundingMode.HALF_UP );
		}

		public BigDecimal getAverageSalary() {
			BigDecimal total = BigDecimal.ZERO.setScale( 2, RoundingMode.HALF_UP );
			for( BigDecimal rate : this.emplSalaryMap.values() ) {
				total = total.add( rate );
			}
			return total.divide( BigDecimal.valueOf( this.emplSalaryMap.size() ), RoundingMode.HALF_UP );
		}


		/**
		 * Add a mapping for employee ID to a rate.
		 * If a mapping already exists for this key, the old value will be replaced.  This allows the caller to 
		 * replace old data if better data can be obtained later.
		 * @param emplid
		 * @param rate
		 */
		public void addEmployeeAndRate( String emplid, BigDecimal rate ) {
			if( emplSalaryMap == null ) {
				emplSalaryMap = new HashMap<>();
			}
			emplSalaryMap.put( emplid, rate );
		}

		/**
		 * Add a mapping for employee ID to a rate.  The caller passes both annual rate and annual-benefits-base-rate
		 * and this method will choose the correct one by examining the plan properties previously saved.
		 * @param emplid
		 * @param annualRate
		 * @param abbr
		 */
		public void addEmployeeAndChooseRate( String emplid, BigDecimal annualRate, BigDecimal abbr ) {
			if( emplSalaryMap == null ) {
				emplSalaryMap = new HashMap<>();
			}
			if( "ABBR".equals( this.getBaseSource() )) {
				emplSalaryMap.put( emplid, ( abbr.equals( BigDecimal.ZERO ) ? annualRate : abbr ));
			} else {
				emplSalaryMap.put( emplid, annualRate );
			}
		}
	}

}
