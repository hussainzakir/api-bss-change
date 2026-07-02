/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.service.model.BenefitOffer;

import lombok.Data;

/**
 * @author kpamulapati
 *
 */

@Data
@Entity
@Table(name = "xbss_group")
public class BenefitGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "groupSeq", sequenceName = "XBSS_GROUP_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "groupSeq")
	private long id;

	@Column(name = "BENEFIT_PROGRAM")
	private String benefitProgram;

	@Column(name = "DESCR")
	private String name;

	@Column(name = "default_group")
	@JsonProperty("isDefault")
	private boolean defaultGroup;

	@Column(name = "GROUP_TYPE")
	private String type;

	@Column(name = "HEADCOUNT")
	private long headcount;

	@Column(name = "COMPANY_ID")
	private long companyId;

	@Column(name = " WAITING_PERIOD ")
	private String waitingPeriod;

	@Column(name = "RATE_TBL_ID")
	private String rateTblId;

	@Column(name = "ELIG_RULES_ID")
	private String eligRuleId;

	@Column(name = "ELIG_CONFIG1")
	private String eligConfig1;

	@Column(name = "STATUS")
	private String status;

	public boolean isActive() {
		return "A".equals( this.getStatus() );
	}

	@Column(name = "STATE")
	private String state;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "benefitGroup")
	private Set<GroupRate> groupRate = new HashSet<>();

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "benefitGroup")
	private Set<BenefitGroupStrategy> benefitGroupStrategy = new HashSet<>();

	@Transient
	private BigDecimal estimatedTotalCost;
	@Transient
	private long strategyId;
	@Transient
	private BigDecimal percentChange;
	@Transient
	private List<BenefitOffer> benefitOffers = new ArrayList<>();
	@Transient
	private Map<String, Integer> coverageLevelHeadCounts;
	@Transient
	private boolean systemCreated;

	/**
	 * @return the groupRate
	 */
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "benefitGroup")
	public Set<GroupRate> getGroupRate() {
		return groupRate;
	}

	/**
	 * @return the benefitGroupStrategy
	 */
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "benefitGroup")
	public Set<BenefitGroupStrategy> getBenefitGroupStrategy() {
		return benefitGroupStrategy;
	}

	public boolean isK1Group() {
        return BSSApplicationConstants.K1_GROUP_TYPE.equals( this.getType() );
	}
}
