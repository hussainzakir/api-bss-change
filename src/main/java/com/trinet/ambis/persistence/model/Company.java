package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.Industry;
import com.trinet.ambis.service.model.MinimumFunding;
import com.trinet.ambis.service.model.RegionalMinimumFunding;

import lombok.Builder;
import lombok.Data;

/**
 * @author jshuali
 */
@Entity
@Table(name = "xbss_company")
@Data
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator( name = "companySeq", sequenceName = "XBSS_COMPANY_SEQ", allocationSize = 1, initialValue = 1 )
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "companySeq" )	
    private long id;
    @Column(name = "CURRENT_YEAR_TOTAL_COST")
    private BigDecimal currentYearTotalCost;
    @Column(name = "TOTAL_BENEFIT_GROUPS")
    private int totalBenefitGroups;
    @Column(name = "HEAD_COUNT")
    private int headcount;
    @Column(name = "PERCENT_CHANGE")
	private BigDecimal percentChange;
    @Column(name = "TOTAL_EMPLOYEES")
    private int totalEmployees;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "COMPANY_NAME")
    private String name;
    @Column(name = "CODE", unique = true)
    private String code;
    @Transient
    private String headQuatersState;
    @Temporal(TemporalType.TIMESTAMP)
    @Column ( name = "UPDATETIME")
    private Date updateTime;
    @Column( name = "REALM_YEAR_ID")
	private long realmPlanYearId;
    @Column( name = "ACA_LARGE_EMPLR")
   	private boolean acaLargeEmplr;
    @Column( name = "PROSPECT_ID")
   	private String prospectId;
    @Column( name = "BUNDLE_ID")
    private Long bundleId;
    @Column( name = "BUNDLE_SEQ")
    private int bundleSeq;
    @Column( name = "AUTH_BROKER")
    private String authBroker;
    @Column( name = "OMS_OFFERING")
    private String omsOffering;
    @Column( name = "PLYR_CHANGE_SYNC_EXCUTED")
    private Integer plYrChangeSyncExcuted;
    @Column( name = "STRATEGY_ACCESSED")
    private int strategyAccessed;
    @Column( name = "ALE_UPDATED")
    private Integer aleUpdated;
    @Column(name = "NAICS_CODE")
    private Integer bssNaicsCode;
    @Column(name = "RISK_TYPE")
    @Enumerated(EnumType.STRING)
    private RiskTypeEnum riskType;
    @Column(name = "RATE_GROUP_ID")
    private String rateGroupId;
    @Column(name = "LARGE_DEAL_PROSPECT")
    private int largeDealProspect;

    @Transient
    private String benefitProgram;
    @Transient
	private Realm realm;
    @Transient
    private BigDecimal aleAmount;
    @Transient
    private BigDecimal aleAmountHistory;
    @Transient
    private Integer naicsCode;
    @Transient
    private String headQuatersCity;
    @Transient
    private boolean newCompany = false;
    @Transient
	private RealmPlanYear realmPlanYear;
    @Transient
    private boolean isPayrollProcessed;
    @Transient
    private int actualHeadCount;
    @Transient
	private String quater;
    @Transient
    private String planEndDate;
    @Transient
    private String planStartDate;
    @Transient
    private String benefitStartDate;
    @Transient
    private String companySetupDate;
    @Transient    
    private boolean strategiesHistoryAvailable;
    @Transient
    private String liveDate;
    @Transient
    private Industry industry;
    @Transient
    private String pfClient;
    @Transient
    private BandCodes bandCodes;
    @Transient
    private boolean isBMGUser;
    @Transient
    private boolean isCSAUser;
    @Transient
    private boolean isTMTUser;
    @Transient
    private boolean isBenCorpAdUser;
    @Transient
    private boolean isBenAdvisorUser;
    @Transient
    private boolean mbg;
    @Transient
    private boolean isRenewalOpen;
    @Transient
    private boolean isRenewalCompany;
    @Transient
    private boolean isProspectCompany;
    @Transient
    private boolean isContingentPricing;
    @Transient
    private String proposalId;
    @Transient
    private boolean isTransitionPeriod;
    @Transient
    private SchedTbl schedTbl;
    @Transient
    private boolean isEligAle;
    @Transient
    private boolean isTexasSitus;
	@Transient
    private boolean isK1Company;
	@Transient
    private String defaultFundingType;
	@Transient
    private boolean isBandCodeUpdated;
	@Transient
    private boolean activeServiceOrder;
	@Transient
    private String serviceOrderNumber;
	@Transient
	private List<RegionalMinimumFunding> regionalMinimumFundings;
	@Transient
	private String exclusiveMedPlan;
	@Transient
	private long defaultMinFundingPct;
	@Transient
	private Set<MinimumFunding> minFundings;
	@Transient
	private String zipCode;
	@Transient
	private String emplId;
	@Transient
	private Set<String> sdiStates;
	@Transient
	private Set<String> companyRegions;
	@Transient
	private Set<String> fundingRegions;
	@Transient
	private List<String> employeeRegions;
	@Transient
	private boolean regionsUpdated;
	@Transient
	private boolean onboardingCompany;
	@Transient
	private boolean isAcaLargeEmplrStatusUpdated;
	@Transient
    private String expiryDate;
	@Transient
	private String commonOwnerCompanyCode;
	@Transient
	private boolean benefitsQuarterException;
	@Transient
    ProcessInfo processInfo;
	@Transient
	private String bundleName;
	@Transient
	private boolean prospectConvertedClient;
    @Transient
    private boolean isAleUpdatedNewClient;
    @Transient
    private boolean prospectConvertedOnboardingClient;
    @Transient
    private boolean planYearChanged;
    @Transient
    private boolean isRatesUpdated;
    @Transient
    private String quarterEffectiveDate;
    @Transient
    private String rateType;

    @Transient
    public boolean isLargeDealProspect() {
        return this.largeDealProspect == 1;
    }
	@Data
	@Builder
	public static class ProcessInfo {
		String processName;
		Long oldRealmPlanYear;
		Long oldCompanyId;
	}

    @Transient
    public RiskTypeEnum getRiskType() {
        return riskType != null ? riskType : RiskTypeEnum.BANDS;
    }

}

