/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.trinet.ambis.service.model.BenefitPlan;

import lombok.Data;

/**
 * @author kpamulapati
 *
 */

@Entity
@Table(name="xbss_contribution")
@Data
public class Contribution implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
    @Id
    @SequenceGenerator( name = "contributionSeq", sequenceName = "XBSS_CONTRIB_SEQ", allocationSize = 128, initialValue = 1 )
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "contributionSeq" )
	private long id;
	@Column(name="plan_Selection_id")
	private long planSelectionId;
	@Column(name="coverage_level")
	private String coverageLevel;
	@Column(name="er_contrib_pct")
	private BigDecimal employerPercent;
	@Column(name="HEADCOUNT")
	private long headCount;
	@Column(name="bn_empl_rate")
	private BigDecimal employeeContribution;
	@Column(name="bn_emplr_rate")
	private BigDecimal employerContribution;
	@Column(name="override_type")
	private String overrideType;
	@Column(name="HSA_HEADCOUNT")
	private long hsaHeadCount;
	
	@Transient
	BenefitPlan benefitPlanAssociation;
	@Transient
	BigDecimal planCost;
	@Transient
	String benefitPlan;
	@Transient
	Long planCarrier;
	
	public Contribution() {
	}
	
	public Contribution(Contribution contrib) {
		this.id = 0;
		this.benefitPlan = contrib.getBenefitPlan();
		this.coverageLevel = contrib.getCoverageLevel();
		this.employeeContribution = contrib.getEmployeeContribution();
		this.employerContribution = contrib.getEmployerContribution();
		this.employerPercent = contrib.getEmployerPercent();
		this.headCount = 0;
		this.hsaHeadCount = 0;
		this.overrideType = contrib.getOverrideType();
	}
	
}
