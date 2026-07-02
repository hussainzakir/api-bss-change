package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.util.CommonUtils;

import lombok.Data;

@Data
public class CompanyData {

	// for some reason, this is called "id" but it's REALLY the company code
	private String id;

	private long legacyCompanyId;

	private boolean mbg;

	private String name;

	private String product;

	private boolean tibProspect;

	private String oeQuarter;

	private String vertical;

	private String region;
	
	private String hqState;

	private Set<String> locations;

	private Long realmPlanYearId;

	private Set<String> companyStates;

	private List<String> employeeHomeStates;
	
	private List<RegionalMinimumFunding> regionalMinimumFunding;

	private SelectionDate selectionDate;
	
	private String companyLiveDate;

	private NewCompany newCompany;

	private Map<String, Boolean> selectedBenefits;

	private boolean strategyHistoryAvailable;

	private String micrositeUrl;

	private int actualHeadCount;

	private BigDecimal aleAmount;
	
	private BigDecimal aleAmountHistory;
	
	private Long defaultMinFundingPct;

	private Set<MinimumFunding> minFundings;

	private String defaultFundingType;

	private boolean aleOptional;

	private boolean optionalPlanEligible;

	private boolean pickAndChooseFlag;

	private boolean k1Company;

	private boolean renewalCompany;

	private boolean activeServiceOrder;
	
	private String zipCode;

	private boolean onboardingCompany;
	
	private String benefitStartDate;
	
	private boolean prospectCompany;
	
	private boolean isContingentPricing;

	private String expiryDate;
	
	private String proposalId;
	
	@JsonProperty("isPlanYearChangeSyncExcuted")
	private boolean planYearChangeSyncExcuted;

	private String prospectId;
	
	private String bundleName;
	
	private boolean strategyAccessed;
	
	private boolean prospectConvertedClient;
	
	private String exchangeId;

	private String preferencesLinkStatus;

	public CompanyData() {
		super();
	}

	public CompanyData( Company company ) {
		this();
		this.setId( company.getCode() );
		this.setLegacyCompanyId(company.getId());
		this.setMbg( company.isMbg() );
		this.setName( company.getName() );
		this.setProduct( company.getRealm().getPeoid() );
		this.setTibProspect(CompanyServiceHelper.isTibProspect(company));
		this.setVertical( company.getIndustry().getIndustryType().getType() );
		this.setEmployeeHomeStates( company.getEmployeeRegions() );
		this.setRegionalMinimumFunding( company.getRegionalMinimumFundings() );
		this.setCompanyLiveDate( company.getLiveDate() );
		this.setStrategyHistoryAvailable( company.isStrategiesHistoryAvailable() );
		this.setActualHeadCount( company.getActualHeadCount() );
		this.setAleAmount( company.getAleAmount() );
		this.setDefaultMinFundingPct( company.getDefaultMinFundingPct() );
		this.setMinFundings( company.getMinFundings() );
		this.setAleOptional( 1 == company.getRealmPlanYear().getAcaFplOpt() && company.isEligAle() );
		this.setRenewalCompany( company.isRenewalCompany() );
		this.setProspectCompany(company.isProspectCompany());
		if( company.isRenewalCompany() ) {
			this.setMicrositeUrl( company.getRealmPlanYear().getMicrositeUrl() );
		}
		this.setK1Company( company.isK1Company() );
		this.setRealmPlanYearId( company.getRealmPlanYear().getId() );
		this.setActiveServiceOrder( company.isActiveServiceOrder() );
		this.setOeQuarter( company.getRealmPlanYear().getOeQuarter() );
		this.setZipCode( company.getZipCode() );
		this.setAleAmountHistory( company.getAleAmountHistory() );
		this.setBenefitStartDate(company.getBenefitStartDate());
		this.setExpiryDate(company.getExpiryDate());
		this.setProposalId(company.getProposalId());
		this.setHqState(company.getHeadQuatersState());
		this.setBundleName(company.getBundleName());	
		if(CommonUtils.checkIfDateIsInRangeInclusive(CommonUtils
				.formatStringToDate(company.getBenefitStartDate(), BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), 
				company.getRealmPlanYear().getPlanYearStart(), company.getRealmPlanYear().getPlanYearEnd())){
			this.setOnboardingCompany(true);
		}

		this.setProspectId(company.getProspectId());
		this.setProspectConvertedClient(company.isProspectConvertedClient());
		this.setStrategyAccessed(1 == company.getStrategyAccessed());
	}
}
