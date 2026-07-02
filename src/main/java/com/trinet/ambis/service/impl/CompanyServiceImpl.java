package com.trinet.ambis.service.impl;

import static com.trinet.ambis.common.BSSApplicationConstants.ONE_DAY;
import static com.trinet.ambis.common.BSSApplicationConstants.PREFERENCES_LINK_STATUS_DISABLED;
import static com.trinet.ambis.common.BSSApplicationConstants.PREFERENCES_LINK_STATUS_HIDE;
import static com.trinet.ambis.common.BSSApplicationConstants.PREFERENCES_LINK_STATUS_READ_ONLY;
import static com.trinet.ambis.common.BSSApplicationConstants.PREFERENCES_LINK_STATUS_SHOW;

import com.trinet.ambis.persistence.dao.hrp.*;
import com.trinet.ambis.persistence.model.*;
import com.trinet.ambis.service.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.trinet.ambis.enums.*;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.service.dto.ProcessInfoDto;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.RealmTypeService;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.CompanyBandCodesDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDataDao;
import com.trinet.ambis.persistence.dao.hrp.HQExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmCloneProgramDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDataDao;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyDetailsDto;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.service.model.CommonData;
import com.trinet.ambis.service.model.CompanyBandCodes;
import com.trinet.ambis.service.model.CompanyData;
import com.trinet.ambis.service.model.CompanyRealmData;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.FundingType;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.service.model.NewCompany;
import com.trinet.ambis.service.model.OLPProcessStatus;
import com.trinet.ambis.service.model.SelectionDate;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;

@Service
public class CompanyServiceImpl implements CompanyService {

	private static final Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);

	private static final String CLASS_NAME = CompanyServiceImpl.class.toString();

	@Autowired
	CompanyDao companyDao;

	@Autowired
	ProspectCompanyService prospectCompanyService;

	@Autowired
	GroupRuleService groupRuleService;

	@Autowired
	PsCompanyDao psCompanyDao;

	@Autowired
	HrpDao hrpDao;

	@Autowired
	SchedTblService schedTblService;

	@Autowired
	StrategyService strategyService;

	@Autowired
	private ProcessStatusService processStatusService;

	@Autowired
	RealmPlanYearRuleService realmPlanYearRuleService;

	@Autowired
	RealmPlanYearService realmPlanYearService;

	@Autowired
	RealmDataDao realmDataDao;

	@Autowired
	RealmRegionMinFundingService realmRegionMinFundingService;

	@Autowired
	RealmTypeService realmTypeService;

	@Autowired
	RealmWaitPeriodService realmWaitPeriodService;

	@Autowired
	EmployerEmployeePlansMappingService employerEmployeePlansMappingService;

	@Autowired
	RealmConfigurationService realmConfigurationService;

	@Autowired
	BenefitOfferExceptionService benOfferExceptionService;

	@Autowired
	MinFundExceptionService minFundExceptionService;

	@Autowired
	RealmCloneProgramDao realmCloneProgramDao;

	@Autowired
	CompanyBandCodesDao companyBandCodesDao;

	@Autowired
	SchedMidYearFundingDataDao schedMidYearFundingDataDao;

	@Autowired
	HQExceptionDao hqExceptionDao;

	@Autowired
	CacheService cacheService;

	@Autowired
	MandatoryRegionDao mandatoryRegionDao;

	@Autowired
	CompanyDataDao companyDataDao;
	
	@Autowired
	BenefitsBundleService benefitsBundleService;
	
     @Autowired
     BssCoreServiceClient bssCoreServiceClient;
	 
	 @Autowired
	private CompanyService companyService;

	@Autowired
	private Executor executor;

	@Autowired
	private BandCodesService bandCodesService;

	@Autowired
	FlexRateService flexRateService;

	@Autowired
	RealmPlanYearDao realmPlanYearDao;

	@Autowired
    RateSystemService rateSystemService;

	@Override
	public CommonData getCompanyCommonData(String code, String emplid, BenExchngEnums benExchange, boolean strategyAccessed) {
		Company company = getCompanyDetails(code, false, emplid, benExchange);
		if (strategyAccessed) {
			updateStrategyAccessed(company);
		}
		if(Objects.nonNull(company.getRiskType()) && RiskTypeEnum.DIFFERENTIALS.equals(company.getRiskType())) {
			flexRateService.syncRateGroupWhenUpdated(company);
		}
		return getCommonData(company);
	}

	@Override
	public Company getCompanyDetails(String code) {
		return getCompanyDetails(code, false, null, null);
	}

	@Override
	public List<Long> getIdsByCodeAndExchange(String code, BenExchngEnums benExchange) {
		return companyDao.findCompaniesBy(code, benExchange.getBenExchng()).stream()
				.map(Company::getId)
				.collect(Collectors.toList());
	}

	@Override
	public Company getCompanyDetails(String code, boolean history, String emplId, BenExchngEnums benExchange) {
		return getCompanyDetails(code, history, emplId, benExchange, false);
	}

	@Override
	public Company getCompanyDetails(String code, boolean history, String emplId, BenExchngEnums benExchange,
			boolean fromPlanYearSync) {
		if (CompanyServiceHelper.isClientCompanyPattern(code)) {
			return getClientCompanyDetails(code, history, emplId, fromPlanYearSync);
		} else {
			return prospectCompanyService.getProspectCompanyDetails(code, benExchange);
		}
	}

	private void updateStrategyAccessed(Company company) {
		Company companyByRealmYearId = companyDao.findByCodeAndRealmPlanYearId(company.getCode(), company.getRealmPlanYearId());
		if (companyByRealmYearId != null) {
			companyByRealmYearId.setStrategyAccessed(1);
			company.setStrategyAccessed(1);
			companyDao.save(companyByRealmYearId);
		}
	}

	private Company getClientCompanyDetails(String code, boolean history, String emplId, boolean fromPlanYearSync) {
		// retrieving the company details from PS
		Company basicCompany = psCompanyDao.getBasicCompanyDetails(code);

		// setting user role details
		setUser(basicCompany, emplId);

		SchedMidYearFunding smf = null;
		if (basicCompany.isTMTUser()) {
			smf = schedMidYearFundingDataDao.getMidYearFundingScheduleForCompany(code);
		}

		boolean isRenewalCompany = isRenewalCompany(basicCompany);
		boolean getLastRealmYear = false;
		if ((smf != null && !smf.isFuturePlanYear() && smf.isFutureExisting()) || history) {
			getLastRealmYear = true;
			// If the caller is band change processing for current realm plan year and
			// company has not crossed over into next plan year then reset the history flag
			// to
			// false because the only intent of passing in history as true to get the
			// correct
			// realm plan year.
			if (history && BSSApplicationConstants.BANDCHANGE_USER_ID.equals(emplId)) {
				history = false;
			}
		}

		RealmPlanYear rpy = populateRealmPlanYear(basicCompany, getLastRealmYear, isRenewalCompany);
		boolean isEventDrivenSyncEnabled = AppRulesAndConfigsUtils.isEventDrivenSyncEnabled();
		boolean autoRefreshCensus = RulesAndConfigsUtils.isAutoRefreshCensusOn(rpy.getId());
		if (autoRefreshCensus && !isEventDrivenSyncEnabled) {
			this.refreshCompanyCensus(basicCompany.getCode(), rpy.getId());
		}

		RealmPlanYear prevRealmPlanYear = null;
		if (history) {
			prevRealmPlanYear = rpy;
		} else {
			prevRealmPlanYear = realmPlanYearService.getPreviousRealmPlanYear(rpy);
			autoRefreshCensus = RulesAndConfigsUtils.isAutoRefreshCensusOn(prevRealmPlanYear.getId());
			if (autoRefreshCensus && !isEventDrivenSyncEnabled) {
				this.refreshCompanyCensus(basicCompany.getCode(), prevRealmPlanYear.getId());
			}
		}

		Date effdt = findEffectiveDate(history, basicCompany.getPlanStartDate(), isRenewalCompany, rpy, smf, basicCompany);
		Company psCompany = psCompanyDao.getCompanyDetailsByEffdt(basicCompany, effdt);

		// getting company actual head counts
		int actualHC = psCompanyDao.getCompanyActualHeadCount(code);

		Company bssCompany = getOrCreateBssCompany(code, basicCompany, psCompany, actualHC, isRenewalCompany,
				fromPlanYearSync);
		
		psCompany.setRenewalCompany(isRenewalCompany);
		psCompany.setActualHeadCount(actualHC);
		psCompany.setRenewalOpen(basicCompany.isRenewalOpen());
		psCompany.setMbg(basicCompany.isMbg());
		psCompany.setRealmPlanYearId(rpy.getId());
		psCompany.setRealmPlanYear(rpy);
		psCompany.setK1Company(basicCompany.isK1Company());
		psCompany.setEmplId(basicCompany.getEmplId());
		psCompany.setAleAmountHistory(prevRealmPlanYear.getAleAmount());
		setBandCodes(psCompany, bssCompany, effdt);
		populateCloneProgram(rpy);
		populateSchedTblInfo(psCompany);
		psCompany.setPlanStartDate(
				CommonUtils.formatDateToString(effdt, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		psCompany.setProspectId(bssCompany.getProspectId());
		
		// identifying if the client is prospect converted client.
		setProspectConvertedClient(psCompany);
		
		if (psCompany.isProspectConvertedClient() && !isRenewalCompany) {
			psCompany.setEligAle(bssCoreServiceClient.getAleStatus(bssCompany.getId()));

			if (Objects.isNull(bssCompany.getAleUpdated()) || bssCompany.getAleUpdated() == 1) {
				psCompany.setAleUpdatedNewClient(true);
			}
		}

		// override the hqState and postal code for Exceptions to HQ State Vendor Rules
		overrideHQStateAndPostalCode(psCompany);

		// setting industry
		psCompany.setIndustry(CompanyServiceHelper.getIndustry(psCompany));

		// setting regions for the Company
		setRegions(psCompany, bssCompany, rpy, history, prevRealmPlanYear);

		List<RealmRegionMinFunding> realmRegionMinFundings = realmRegionMinFundingService
				.findByid_realmYearId(psCompany.getRealmPlanYearId());

		Set<MinFundExceptionDto> minFundExceptions = minFundExceptionService
				.findActiveByCompanyCodeAndQuarter(psCompany);

		// setting the minimum funding for exchange.
		CompanyServiceHelper.updateMinimumFunding(psCompany, realmRegionMinFundings, minFundExceptions);

		// updating the BSS company with PS data.
		CompanyServiceHelper.mapPSCompanyDataToBSSCompany(psCompany, bssCompany);

		if (history) {
			bssCompany = this.populateAleFlagForHistory(bssCompany, rpy);
		}

		bssCompany.setSdiStates(RulesAndConfigsUtils.getSDIStates(bssCompany.getRealmPlanYearId()));
		
		// setting bundle name
		setBundleName(bssCompany);
		
		if (null != smf) {
			bssCompany.setPlanStartDate(CommonServiceHelper.formatDateToString(smf.getMidYearFundingEffDate(),
					BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
			bssCompany.setActiveServiceOrder(true);
			bssCompany.setServiceOrderNumber(smf.getServiceOrderNumber());
			logger.info("COMPANY PLAN START DATE : {} ", bssCompany.getPlanStartDate());
			logger.info("SERVICE ORDER NUMBER : {} ", smf.getServiceOrderNumber());
		} else {
			bssCompany.setActiveServiceOrder(false);
		}
		logger.info("COMPANY ID : {} ", bssCompany.getId());
		processBanCodeUpdate(bssCompany);

		String rateType="REGIONAL";
		boolean phase2OutputsEnabled = AppRulesAndConfigsUtils.isBssOutputPhase2Enabled();
		if(phase2OutputsEnabled){
			rateType = rateSystemService.getRateSystemRateType(bssCompany);
		}
		bssCompany.setRateType(rateType.toUpperCase());

		cacheService.invalidateOutofDateCache(bssCompany);
		return bssCompany;
	}

	private void setBandCodes(Company psCompany, Company bssCompany, Date effdt) {
		// Custom logic for riskType DIFFERENTIAL
		if (Objects.equals(RiskTypeEnum.DIFFERENTIALS, bssCompany.getRiskType())) {
			String bssNaicsCode = bssCompany.getBssNaicsCode() == null ? null : String.valueOf(bssCompany.getBssNaicsCode());
			BenExchngEnums exchangeEnum = BenExchngEnums.getByBenExchange(psCompany.getRealm().getBenExchange());
			if (exchangeEnum != null) {
				String lifeBandCode = bandCodesService.getBandCodeByType(
						bssNaicsCode, effdt, BSSApplicationConstants.LIFE, exchangeEnum
				);
				String disBandCode = bandCodesService.getBandCodeByType(
						bssNaicsCode, effdt, BSSApplicationConstants.DISABILITY, exchangeEnum
				);
				BandCodes bandCodes = psCompany.getBandCodes();
				bandCodes.setLifeBandCode(lifeBandCode);
				bandCodes.setDisBandCode(disBandCode);
			}
		}
	}

	private Company getOrCreateBssCompany(String code, Company basicCompany, Company psCompany, int actualHC,
			boolean isRenewalCompany, boolean fromPlanYearSync) {
        // retrieving BSS company details
        Company bssCompany = companyDao.findByCodeAndRealmPlanYearId(code, basicCompany.getRealmPlanYearId());
        // if company not available in BSS creating the BSS company
        if (bssCompany == null) {
            String companySetupDate = basicCompany.getCompanySetupDate();
            String prospectConversionCutOffDate = AppRulesAndConfigsUtils.getProspectConversionCutOffDate();
            Date cutOffDate = Utils.convertStringToDate(prospectConversionCutOffDate, Constants.DATE_FORMAT);
            Date setupDate = Utils.convertStringToDate(companySetupDate, Constants.DATE_FORMAT);
			if (setupDate.compareTo(cutOffDate) >= 0 && !isRenewalCompany && !fromPlanYearSync) {
				throw new BSSApplicationException(
						new BSSApplicationError(BSSErrorResponseCodes.BSS_PROSPECT_TO_CLIENT_CONVERSION_ERROR,
								BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, "CompanyServiceImpl",
								"Prospect to client conversion is pending for company: " + code, null, null));
			}
            basicCompany.setActualHeadCount(actualHC);
            basicCompany.setAcaLargeEmplr(psCompany.isEligAle());
            bssCompany = CompanyServiceHelper.createBssCompany(basicCompany);
            bssCompany.setBssNaicsCode(psCompany.getNaicsCode());

            // derive risk type from rules/configs instead of realm plan year id
            RiskTypeEnum riskType = RulesAndConfigsUtils.getRenewalRiskType(basicCompany.getRealmPlanYearId());
            bssCompany.setRiskType(riskType);
            bssCompany = createUpdateCompany(bssCompany);
		}
        return bssCompany;
    }

	private void processBanCodeUpdate(Company bssCompany) {
		List<CompanyBandCodes> psBandcodes = CompanyServiceHelper.getBssBandCodeList(bssCompany.getId(),
				bssCompany.getBandCodes());
		// retrieve the band code from the DB
		List<CompanyBandCodes> bssBandcodes = companyBandCodesDao.getBandCodesByCompanyId(bssCompany.getId());
		if (null != bssBandcodes && !bssBandcodes.isEmpty()) {
			bssCompany.setBandCodeUpdated(!CompanyServiceHelper.compareBandCodes(bssBandcodes, psBandcodes));
		} else {
			companyBandCodesDao.insertUpdateCompanyBandCodes(bssCompany.getId(), psBandcodes);
			bssCompany.setBandCodeUpdated(false);
		}

		logger.info("Is BandCode updated for company id :: {} -->> {}", bssCompany.getId(),
				bssCompany.isBandCodeUpdated());
		logger.info("Is BandCode updated for company id :: {} -->> {}", bssCompany.getId(),
				bssCompany.isRegionsUpdated());
	}

	private void setRegions(Company psCompany, Company bssCompany, RealmPlanYear rpy, boolean history,
			RealmPlanYear prevRealmPlanYear) {
		RealmPlanYear realmPlanYear = null;
		if (null != rpy && null != prevRealmPlanYear) {
			realmPlanYear = history ? rpy : prevRealmPlanYear;
		}
		// getting company and employee regions and setting it to company object
		getLocations(psCompany, realmPlanYear);
		Set<String> bssCompanyRegions = companyDataDao.getRegionsByCompanyId(bssCompany.getId());

		if (CollectionUtils.isNotEmpty(bssCompanyRegions)) {
			Set<String> latestCompanyRegions = new HashSet<>();
			latestCompanyRegions.addAll(psCompany.getCompanyRegions());
			latestCompanyRegions.addAll(psCompany.getFundingRegions());
			latestCompanyRegions.addAll(psCompany.getEmployeeRegions());
			if (CollectionUtils.isNotEmpty(latestCompanyRegions)) {
				psCompany
						.setRegionsUpdated(!CollectionUtils.isEqualCollection(bssCompanyRegions, latestCompanyRegions));
			}
		} else {
			Set<String> allRegions = new HashSet<>();
			allRegions.addAll(psCompany.getCompanyRegions());
			allRegions.addAll(psCompany.getFundingRegions());
			allRegions.addAll(psCompany.getEmployeeRegions());
			companyDataDao.insertUpdateCompanyRegions(bssCompany.getId(), allRegions);
			psCompany.setRegionsUpdated(false);
		}
	}

	@Override
	public void refreshCompanyCensus(String companyCode, long realmYearId) {
		try {
			boolean isRenewalCompany = companyService.isRenewalCompany(companyCode);
			boolean isTerminatedCompany = companyService.isTermedCompany(companyCode);
			if (isRenewalCompany && !isTerminatedCompany) {
				// Check current census data and only launch new refresh thread if stale
				Timestamp createdDate = psCompanyDao.getCompanyCensusCreateDt(companyCode, realmYearId);
				if (createdDate == null || (createdDate.getTime() + ONE_DAY) < System.currentTimeMillis()) {
					Executors.newCachedThreadPool()
							.submit(() -> psCompanyDao.refreshCompanyCensus(companyCode, realmYearId));
				}
			}

		} catch( Exception throwable ) {
			Exception ex = new BSSApplicationException(throwable, new BSSApplicationError(BSSErrorResponseCodes.BSS_CENSUS_REFRESH_ERROR,
					0, "CompanyServiceImpl#refreshCompanyCensus", "An exception occurred creating or running the thread for BSS Census refresh",
					null, null));
			CommonUtils.logExceptions(ex, logger, companyCode, "");
		}
	}

	/**
	 * This method refreshes company census
	 * 
	 * @param companyCode
	 * @param realmYearId
	 */
	@Override
	public void refreshCompanyCensusSynchronously(String companyCode, long realmYearId) {
		psCompanyDao.refreshCompanyCensus(companyCode, realmYearId);
	}
	
	private CommonData getCommonData(Company company) {
		CommonData commonData = new CommonData();
		commonData.setCompanyCommonData(populateCompanyCommonData(company));
		commonData.setPlanYearCommonData(CompanyServiceHelper.populatePlanYearCommonData(company));
		commonData.setCurrentplanYear(CompanyServiceHelper.populateCurrentPlanYearData(company, realmPlanYearService));
		commonData.setUserCommonData(CompanyServiceHelper.populateUserCommonData(company));
		commonData.setWaitPeriods(realmWaitPeriodService.getWaitPeriodsByRelamPlanYear(company.getRealmPlanYearId()));
		commonData.setFundingTypes(realmDataDao.getRealmFundingTypes(company.getRealmPlanYearId()));
		
		List<FundingType> prospectFundingTypes = new ArrayList<>();
		for (FundingType fType : commonData.getFundingTypes()) {
			if (fType.isDefaultFunding()) {
				commonData.getCompanyCommonData().setDefaultFundingType(fType.getId());
			}
			if(BSSApplicationConstants.PROSPECT_FUND_TYPES.contains(fType.getId())) {
				prospectFundingTypes.add(fType);
			}
		}
		if(company.isProspectCompany()) {
			commonData.setFundingTypes(prospectFundingTypes);
		}
		commonData.setEligibleGroups(groupRuleService.getApplicableGroups(company, false));
		commonData.getCompanyCommonData().setExchangeId(BenExchngEnums.getById(company.getRealm().getId()).getExchangeId());
		return commonData;
	}

	/**
	 * This method is for setting the roles for the logged in user.
	 * 
	 * @param company
	 * @param emplId
	 */
	private void setUser(Company company, String emplId) {
		String code = company.getCode();
		company.setEmplId(emplId);
		boolean isCSAUser = false;
		boolean isBDMUser = false;
		boolean isBMGUser = false;
		boolean isTMTUser = false;
		boolean isBenCorpAdUser = false;
		logger.info(" user emplid is {} code is {} ", emplId, code);

		if (emplId != null && code != null) {
			isBDMUser = psCompanyDao.isBDMUser(emplId, code);
			isBMGUser = psCompanyDao.isBMGUser(emplId);
			isCSAUser = psCompanyDao.isCSAUser(emplId, company.getRealm().getId());
			isTMTUser = psCompanyDao.isTMTUser(emplId);
			isBenCorpAdUser = psCompanyDao.isBenCorpAdUser(emplId);
		}
		if (isBDMUser) {
			company.setCSAUser(false);
			company.setBMGUser(false);
			company.setTMTUser(false);
			company.setBenAdvisorUser(false);
		} else if (isCSAUser) {
			company.setCSAUser(true);
			company.setBMGUser(false);
			company.setTMTUser(false);
		} else if (isBMGUser) {
			company.setCSAUser(false);
			company.setBMGUser(true);
			company.setTMTUser(false);
		}

		if (isTMTUser) {
			company.setCSAUser(false);
			company.setBMGUser(false);
			company.setTMTUser(true);
		}
		if (isBenCorpAdUser) {
			company.setBenCorpAdUser(isBenCorpAdUser);
		}
		if (!isBDMUser) {
			company.setBenAdvisorUser(psCompanyDao.isBenAdvisorUser(emplId, code));
		}

		logger.info("isBMGUser : {}, isCSAUser : {}, isTMTUser : {}, isBenAdvisorUser : {} ", isBMGUser, isCSAUser,
				isTMTUser, company.isBenAdvisorUser());
	}

	/**
	 * When handling a history strategy, it may be desirable to find the ALE flag
	 * from the beginning of the plan year because this is the value that was
	 * actually used for the last renewal. This method will find that historical
	 * value.
	 * 
	 * @param company a company whose history data is required
	 * @param rpy     the RealmPlanYear for which the data is desired
	 * @return the same company object with the ALE flag updated according to the
	 *         history RealmPlanYear
	 */
	private Company populateAleFlagForHistory(Company company, RealmPlanYear rpy) {
		try {
			Date lookupDate = CommonUtils.chooseGreaterDate(rpy.getPlanYearStart(), CommonUtils.formatStringToDate(
					company.getBenefitStartDate(), BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
			Company aleCo = new Company();
			aleCo.setCode(company.getCode());
			aleCo = psCompanyDao.getCompanyDetailsByEffdt(aleCo, lookupDate);
			company.setEligAle(aleCo.isEligAle());
			if (company.isEligAle()) {
				company.setAleAmount(rpy.getAleAmount());
			}
		} catch (BSSApplicationException bssEx) {
			// This can happen for a newer client. The company object is still valid and
			// should be returned
		}
		return company;
	}

	/**
	 * this method is for applying OMS prospect filters
	 * @param company
	 * @param selectedBenefits
	 */

	private void applyOMSFilters(Company company, Map<String, Boolean> selectedBenefits){
		boolean OMSExchange = CompanyServiceHelper.isOMSExchange(company);
		boolean tibProspect = CompanyServiceHelper.isTibProspect(company);
		if (OMSExchange && !tibProspect){
			selectedBenefits.remove(PlanTypesEnum.MEDICAL.getName());
			selectedBenefits.remove(PlanTypesEnum.DENTAL.getName());
			selectedBenefits.remove(PlanTypesEnum.VISION.getName());
		}
	}

	/**
	 * This method is for populating company Common Data.
	 * 
	 * @param company
	 * @return CompanyData
	 */
	private CompanyData populateCompanyCommonData(Company company) {
		String vertical = company.getIndustry().getIndustryType().getType();
		String region = null;
		String hqState = null;
		if (company.getHeadQuatersState() != null) {
			hqState = company.getHeadQuatersState().trim().toUpperCase();
			region = StrategyUtils.getRegion(hqState, vertical);
		}
		setStrategyHistoryAvailable(company);

		Set<String> regions = StrategyServiceHelper.getHqStateCity(company);
		Map<String, Boolean> selectedBenefits = realmDataDao.getSelectedBenefits(company.getRealmPlanYearId(), regions);

		benOfferExceptionService.applyException(company, selectedBenefits);

		this.applyOMSFilters(company, selectedBenefits);

		// setting the open dates based on schedule table
		SelectionDate selectionDate = CompanyServiceHelper.constructSelectionDate(company.getSchedTbl());

		NewCompany newCompany = setUpNewCompany(company);

		boolean optionalPlanEligible = employerEmployeePlansMappingService
				.isEmployerEmployeePlansMappingByRealmYearIdOffered(company.getRealmPlanYearId());

		Set<String> companyPlusMandatoryRegions = new HashSet<>();
		companyPlusMandatoryRegions.addAll(company.getCompanyRegions());
		companyPlusMandatoryRegions.addAll(company.getFundingRegions());
		
		CompanyData companyData = new CompanyData(company);
		companyData.setPickAndChooseFlag(RulesAndConfigsUtils.findPickChooseWithExceptions(company));
		companyData.setRegion(region);
		companyData.setHqState(hqState);
		companyData.setLocations(companyPlusMandatoryRegions);
		if (company.isProspectCompany()) {
			companyData.setCompanyStates(company.getCompanyRegions());
		} else {
			companyData.setCompanyStates(realmDataDao.getCompanyLocationStates(company.getCode()));
		}
		companyData.setPlanYearChangeSyncExcuted(
				company.getPlYrChangeSyncExcuted() != null && company.getPlYrChangeSyncExcuted() == 1);
		companyData.setContingentPricing(company.isContingentPricing());
		companyData.setSelectionDate(selectionDate);
		companyData.setNewCompany(newCompany);
		companyData.setSelectedBenefits(selectedBenefits);
		companyData.setOptionalPlanEligible(optionalPlanEligible);
		companyData.setPreferencesLinkStatus(getPreferencesLinkStatus(company));
		return companyData;
	}

	private String getPreferencesLinkStatus(Company company) {
		if (!RulesAndConfigsUtils.isDisplayPreferenceProspectEnabled(company.getRealmPlanYearId())) {
			return PREFERENCES_LINK_STATUS_HIDE;
		}

		int bundleSeq = company.getBundleSeq();
		// If bundleSeq is 0 or negative (unset/unconfigured), hide preferences
		if (bundleSeq <= 0) {
			return PREFERENCES_LINK_STATUS_HIDE;
		}

		int maxBundleSeq = AppRulesAndConfigsUtils.getMaxBundleSeq();
		if (bundleSeq < maxBundleSeq) {
			return PREFERENCES_LINK_STATUS_SHOW;
		} else if (bundleSeq > maxBundleSeq) {
			return PREFERENCES_LINK_STATUS_DISABLED;
		} else {
			// bundleSeq == maxBundleSeq
			return PREFERENCES_LINK_STATUS_READ_ONLY;
		}
	}

	/**
	 * Please test all the below scenarios after changing this logic.
	 * 
	 * New client : 1.Normal flow before no new hire and no payroll is run
	 * 
	 * Renewal Client: 2. Normal flow: next realm year is not inserted: should see
	 * current strategy. 3. Normal flow: next realm year is inserted: should see
	 * current strategy. 4. Internal Open: next realm year is already inserted:
	 * Internal dates are open. Login as non-CSA, should see current strategy, Login
	 * as CSA should see future strategies, Login and logout as non-CSA should see
	 * current strategy 5. Renewal Open: next realm year is already inserted:
	 * internal dates open/closed. Login as CSA or non-CSA: Should create future
	 * strategies. 6. Transition period: internal open dates and renewal open dates
	 * are in the past: and current date is before plan year start: should still see
	 * the future strategies 7. Normal flow: next realm year is not inserted. should
	 * see current strategy.
	 * 
	 * New Client: Mid year Renewal 8. Mid year Renewal or extension: Open site
	 * after hiring the employee or running payroll. UI logic: if actualHeadCount >
	 * 0, they check for extensionEndate, if its null check for closedate if its
	 * open, then the site is open for New client extension.
	 * 
	 * TODO :Renewal Client: Mid year Renewal
	 */
	private RealmPlanYear populateRealmPlanYear(Company basicCompany, boolean history, boolean isRenewalCompany) {
		long realmId = basicCompany.getRealm().getId();
		String planStartDate = basicCompany.getPlanStartDate();
		String quarter = basicCompany.getQuater();
		RealmPlanYear realmPlanYear = null;
		basicCompany.setRenewalOpen(false);
		if (isRenewalCompany) {
			realmPlanYear = getRealmPlanYearForRenewalClient(basicCompany, history, realmId, quarter);
		} else {
			realmPlanYear = getRealmPlanYearForNonRenewalClient(realmId, planStartDate, quarter);
			basicCompany.setMbg(realmPlanYear.isMbgNew());
			if (basicCompany.isK1Company()) {
				basicCompany.setK1Company(realmPlanYear.isK1Flag());
			}
		}
		basicCompany.setRealmPlanYear(realmPlanYear);
		basicCompany.setRealmPlanYearId(realmPlanYear.getId());
		return realmPlanYear;
	}
	
	private RealmPlanYear getRealmPlanYearForRenewalClient(Company basicCompany, boolean history, long realmId, String quarter) {
		RealmPlanYear realmPlanYear = realmPlanYearService.getCurrentRealmPlanYear(realmId, quarter);
		RealmPlanYear nextRealmPlanYear = realmPlanYearService.getNextRealmPlanYear(realmPlanYear);
		boolean renewalPeriod = false;
		if (nextRealmPlanYear != null) {
			renewalPeriod = isRenewalOpen(basicCompany, nextRealmPlanYear);
			if (renewalPeriod || basicCompany.isTransitionPeriod()) {
				realmPlanYear = nextRealmPlanYear;
			}
		} else {
			renewalPeriod = isRenewalOpen(basicCompany, realmPlanYear);
		}
		if (history) {
			RealmPlanYear prevRealmPlanYear = realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear);
			if (null != prevRealmPlanYear) {
				realmPlanYear = prevRealmPlanYear;
			}
		}
		basicCompany.setRenewalOpen(renewalPeriod);
		basicCompany.setMbg(realmPlanYear.isMbgRenewal());
		return realmPlanYear;
	}
	
	private RealmPlanYear getRealmPlanYearForNonRenewalClient(long realmId, String planStartDate, String quarter) {
		RealmPlanYear realmPlanYear = realmPlanYearService.getRealmPlanYear(realmId, quarter,
				Utils.convertStringToDate(planStartDate, Constants.DATE_FORMAT));
		// If current planStartDate does not fall between one of the
		// realm_plan_year dates
		// for that quarter get the next realm_plan_year
		if (realmPlanYear == null) {
			realmPlanYear = realmPlanYearService.getLatestRealmPlanYear(realmId, quarter,
					Utils.convertStringToDate(planStartDate, Constants.DATE_FORMAT));
		}
		return realmPlanYear;
	}
	
	private NewCompany setUpNewCompany(Company bisCompany) {
		NewCompany newCompany = null;
		List<Strategy> strategies = strategyService.getAllStrategies(bisCompany.getId());
		if (!bisCompany.isRenewalCompany() && !bisCompany.isProspectCompany() && strategies.isEmpty()) {
			newCompany = NewCompany.builder().headCounts(setUpHeadCounts(bisCompany))
					.coverageLevels(setUpCoverageLevels(bisCompany)).annualBudget(0).build();
		}

		return newCompany;
	}

	@Override
	public Company createUpdateCompany(Company p) {
		logger.info("CREATE OR UPDATING STRATEGY :{} HEAD COUNT : {} ", p.getCode(), p.getHeadcount());
		Company company = null;
		try {
			company = companyDao.saveAndFlush(p);
		} catch (Exception exc) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_COMPANY_SAVE_FAIL,
					BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, CLASS_NAME,
					"Error saving company in XBSS_COMPANY table", null, null));
		}
		return company;
	}

	@Override
	public Company getCompanyDetailsEffdt(String companyCode, Date effdt, long currentRealmPlanYearId) {
		Company bssCompany = companyDao.findByCodeAndRealmPlanYearId(companyCode, currentRealmPlanYearId);
		Company company = null;
		if (null != bssCompany) {
			company = psCompanyDao.getCompanyDetailsByEffdt(bssCompany, effdt);
			RealmPlanYear realmPlanYear = realmPlanYearService.getRealmPlanYear(company.getRealm().getId(),
					company.getQuater(), effdt);
			RealmCloneProgram cloneProgram = realmDataDao.getRealmCloneProgram(realmPlanYear.getId());
			realmPlanYear.setCloneProgram(cloneProgram.getCloneProgram());
			company.setRealmPlanYear(realmPlanYear);
			company.setRealmPlanYearId(realmPlanYear.getId());
			company.setRenewalCompany(isRenewalCompany(company));

			// setting regions
			setRegions(bssCompany, bssCompany, null, false, null);

			// override the hqState and postal code for Exceptions to HQ State Vendor Rules
			overrideHQStateAndPostalCode(company);
		}
		return company;
	}

	@Override
	public Company getCompanyDetailsEffdtPlanRates(String companyCode, Date effdt, long currentRealmPlanYearId) {
		Company bssCompany = companyDao.findByCodeAndRealmPlanYearId(companyCode, currentRealmPlanYearId);
		if (null == bssCompany) {
			bssCompany = new Company();
			bssCompany.setCode(companyCode);
		}
		Company company = null;
		company = psCompanyDao.getCompanyDetailsByEffdt(bssCompany, effdt);

		RealmPlanYear realmPlanYear = realmPlanYearService.getRealmPlanYear(company.getRealm().getId(),
				company.getQuater(), effdt);
		RealmCloneProgram cloneProgram = realmDataDao.getRealmCloneProgram(realmPlanYear.getId());
		realmPlanYear.setCloneProgram(cloneProgram.getCloneProgram());
		company.setRealmPlanYear(realmPlanYear);
		company.setRealmPlanYearId(realmPlanYear.getId());

		// setting regions
		setRegions(bssCompany, bssCompany, null, false, null);
		// override the hqState and postal code for Exceptions to HQ State Vendor Rules
		overrideHQStateAndPostalCode(company);
		return company;
	}

	private Map<String, LinkedHashMap<String, Integer>> setUpCoverageLevels(Company bisCompany) {
		long realmPlanYearId = bisCompany.getRealmPlanYearId();
		Map<String, LinkedHashMap<String, Integer>> benOfferCvgCodes = realmDataDao
				.getCoverageCodesByPlanTypes(Constants.primaryPlanTypesCodes, realmPlanYearId);
		benOfferExceptionService.applyException(bisCompany, benOfferCvgCodes);
		return benOfferCvgCodes;
	}

	// :TODO : NewCompany object will have either HeadCounts or CoverageLevels,
	// based on UI(Acceptance) one needs to be eliminated.
	private LinkedHashMap<String, Integer> setUpHeadCounts(Company bisCompany) {
		LinkedHashMap<String, Integer> headCounts = new LinkedHashMap<>();
		long realmPlanYearId = bisCompany.getRealmPlanYearId();
		List<CoverageLevel> coverageLevels = realmDataDao.getCoverageCodes(Constants.medicalPlanTypeList.get(0),
				realmPlanYearId);
		if (CollectionUtils.isEmpty(coverageLevels)) {
			coverageLevels = realmDataDao.getCoverageCodes(Constants.DENTAL_CODE, realmPlanYearId);
		}
		for (CoverageLevel coverageLevelNew : coverageLevels) {
			headCounts.put(coverageLevelNew.getId(), 0);
		}
		return headCounts;
	}

	/**
	 * 
	 * @param company
	 * @param realmPlanYear
	 * @return
	 */
	private boolean isRenewalOpen(Company company, RealmPlanYear realmPlanYear) {
		SchedTbl schedTbl = schedTblService.getCalcuatedScheduleDates(company.getCode(), company.getQuater(),
				realmPlanYear.getId());
		return CompanyServiceHelper.isRenewalOpen(company, schedTbl, realmPlanYear);
	}

	@Override
	public List<Company> getCompaniesByReamYear(Long realmYearId) {
		return companyDao.findByRealmPlanYearId(realmYearId);
	}

	@Override
	public Map<String, String> findCompaniesNames(Set<String> companyCodes) {
		return psCompanyDao.findCompaniesNames(companyCodes);
	}

	@Override
	public boolean isRenewalCompany(String companyCode) {
		if (!CompanyServiceHelper.isClientCompanyPattern(companyCode)) {
			return false;
		}
		Company basicPsCompany = psCompanyDao.getBasicCompanyDetails(companyCode);
		return isRenewalCompany(basicPsCompany);
	}

	/**
	 * Checks if given company is termed company or not
	 *
	 * @return true if termed company<br>
	 *         false if not termed company
	 */
	@Override
	public boolean isTermedCompany(String companyCode) {
		return psCompanyDao.isTermedCompany(companyCode);
	}

	public String getCompanyName(String companyCode) {
		Map<String, String> companyNames = psCompanyDao.findCompaniesNames(Set.of(companyCode));
		return companyNames.containsKey(companyCode) ? companyNames.get(companyCode) : StringUtils.EMPTY;
	}
	
	private boolean isRenewalCompany(Company basicPsCompany) {
		boolean isRenewalCompany = false;
		// Existing company
		if (basicPsCompany.isPayrollProcessed()) {
			isRenewalCompany = true;
		} else { // New company who wants to come in as renewal company.
			OLPProcessStatus olpProcessStatus = hrpDao.getOlpHiringCompletedStatus(basicPsCompany);

			RealmPlanYear nextPlanYear = realmPlanYearService.getMaxRealmPlanYear(basicPsCompany.getRealm().getId(),
					basicPsCompany.getQuater());

			boolean newbandsAvailable = psCompanyDao.isNewBandsAvailable(basicPsCompany,
					nextPlanYear.getPlanYearStart());

			if (olpProcessStatus != null && newbandsAvailable) {
				List<RealmConfiguration> realmConfigurations = realmConfigurationService
						.findByRealmId(basicPsCompany.getRealm().getId());
				String newToRenewalNumDaysStr = CommonServiceHelper.findRealmConfigurationValueByKey(
						realmConfigurations, RealmConfigurationKeysEnum.NEW_TO_RENEWAL_NUM_DAYS.getValue());
				if (hasNewToRenewalNumDaysPassed(olpProcessStatus, newToRenewalNumDaysStr)) {
					List<Strategy> strategies = strategyService
							.getAllSubmittedStrategiesByCompanyCode(basicPsCompany.getCode());
					if (strategies != null && !strategies.isEmpty()) {
						isRenewalCompany = true;
					}
				}
			}
		}
		return isRenewalCompany;
	}

	/**
	 * This method checks whether the NEW_TO_RENEWAL_NUM_DAYS has been passed or not
	 * from the date all the employees has been submitted by client. If weekend days
	 * falls in between then those weekend days will get added to
	 * NEW_TO_RENEWAL_NUM_DAYS .
	 * 
	 * For example client has submitted all the employees on "1/1/2018" and
	 * NEW_TO_RENEWAL_NUM_DAYS = 5 then this method will return true on or after
	 * "1/8/2018"
	 * 
	 */
	private boolean hasNewToRenewalNumDaysPassed(OLPProcessStatus olpProcessStatus, String newToRenewalNumDaysStr) {
		Date todaysDate = new Date();
		int newToRenewalNumDays = newToRenewalNumDaysStr == null ? 0 : Integer.parseInt(newToRenewalNumDaysStr);
		Date renewalEligibilityDate = Utils.addWeekDaysToDate(olpProcessStatus.getUpdateDate(), newToRenewalNumDays);
		int comparisonResult = DateTimeComparator.getDateOnlyInstance().compare(todaysDate, renewalEligibilityDate);
		return (comparisonResult == 0 || comparisonResult > 0);
	}

	/**
	 * This method checks if there is a strategy submitted for previous realm year,
	 * id yes then it sets the StrategyHistoryAvailable to true otherwise false.
	 * 
	 * @param company
	 */
	private void setStrategyHistoryAvailable(Company company) {
		RealmPlanYear prevRealmPlanYear = realmPlanYearService.getPreviousRealmPlanYear(company.getRealmPlanYear());
		if (prevRealmPlanYear != null) {
			int strategiesHistoryCount = strategyService.getStrategiesHistoryCount(company.getCode(),
					prevRealmPlanYear.getId());
			if (strategiesHistoryCount > 0) {
				company.setStrategiesHistoryAvailable(true);
			}
		}
	}

	private void overrideHQStateAndPostalCode(Company company) {
		Optional<HQException> hqException = hqExceptionDao.findByIdRealmYrIdAndIdCompany(company.getRealmPlanYearId(),
				company.getCode());
		if (hqException.isPresent()) {
			company.setZipCode(hqException.get().getPostalCode());
			company.setHeadQuatersState(hqException.get().getHqState());
		}
	}

	private void setBundleName(Company company) {
		Bundle bundle = null;
		if (null != company.getBundleId()) {
			bundle = benefitsBundleService.getBundleById(company.getBundleId().longValue());
			company.setBundleName(bundle.getName());
		}
	}
	
	private void setProspectConvertedClient(Company company) {
		Date cutOffDate = null;
		String prospectConversionCutOffDate = AppRulesAndConfigsUtils.getProspectConversionCutOffDate();
		String companySetupDate = company.getCompanySetupDate();
		if (company.getProspectId() != null && prospectConversionCutOffDate != null) {
			cutOffDate = Utils.convertStringToDate(prospectConversionCutOffDate, Constants.DATE_FORMAT);
			Date setupDate = Utils.convertStringToDate(companySetupDate, Constants.DATE_FORMAT);
			boolean isConvertedClient = setupDate.compareTo(cutOffDate) >= 0;
			company.setProspectConvertedClient(isConvertedClient);
		}
	}
	
	private Date findEffectiveDate(boolean history, String basicCompPlanStDt, boolean isRenewalComp, RealmPlanYear rpy,
			SchedMidYearFunding smf, Company company) {
		Date effDt = null;
		Date companyBenStart = Utils.convertStringToDate(basicCompPlanStDt, Constants.DATE_FORMAT);

		if (history) {
			effDt = rpy.getPlanYearEnd();
		} else if (isRenewalComp) {
			if( isRenewalOpen(company, rpy) ) {
				effDt = rpy.getPlanYearStart();
			} else {
				effDt = CommonUtils.chooseGreaterDate(rpy.getPlanYearStart(), CommonUtils.getCurrentDate());
			}
		} else {
			effDt = companyBenStart;
		}
		if (null != smf) {
			effDt = smf.getMidYearFundingEffDate();
		}
		// whatever returned must not be less than company benefit start date
		return CommonUtils.chooseGreaterDate(effDt, companyBenStart);
	}
	
	private void populateSchedTblInfo(Company bssCompany) {
		SchedTbl schedTbl = schedTblService.getCalcuatedScheduleDates(bssCompany.getCode(), bssCompany.getQuater(),
				bssCompany.getRealmPlanYearId());
		if (schedTbl == null) {
			// Setting dummy dates as UI strictly needs them to be not null.
			// These dates are irrelevant as UI would not be checking for
			// these in new client flow.
			CompanyServiceHelper.populateScheduleTableData(bssCompany, bssCompany.getRealmPlanYear());
		} else {
			bssCompany.setSchedTbl(schedTbl);
			bssCompany.getSchedTbl().setPayrollProcessed(bssCompany.isPayrollProcessed());
		}
	}

	private void populateCloneProgram(RealmPlanYear realmPlanYear) {
		RealmCloneProgram cloneProgram = realmDataDao.getRealmCloneProgram(realmPlanYear.getId());
		if (cloneProgram != null) {
			logger.info("REALM PLAN YEAR ID : {} CLONE PROGRAM :  {}", realmPlanYear.getId(),
					cloneProgram.getCloneProgram());
			realmPlanYear.setCloneProgram(cloneProgram.getCloneProgram());
		}
	}

	@Override
	public List<CompanyRealmData> getCompanyPlanYearData(String companyCode, String emplId) {
		boolean isRenewalCompany = isRenewalCompany(companyCode);
		return companyDataDao.getAvailableCompanyRealms(companyCode, isRenewalCompany);
	}
	
	@Override
	public long createUpdateCompany(Company foundCompany, String companyCode, long realmPlanYearId, String omsOffering,
			Long bundleId, RiskTypeEnum riskType, Integer bssNaicsCode) {
		Company savedCompany;
		boolean isOmsOfferingValueChanged = false;
		boolean isBundleIdChanged = false;
		boolean isRiskTypeChanged = false;
		boolean isNaicsCodeChanged = false;
		boolean isLargeDealProspect = Objects.equals(BSSApplicationConstants.CUSTOM_BUNDLE_ID, bundleId);
		// if company not available in BSS creating the BSS company
		if (foundCompany == null) {
			Company company = new Company();
			company.setCode(companyCode);
			company.setRealmPlanYearId(realmPlanYearId);
			company.setCurrentYearTotalCost(BigDecimal.ZERO);
			company.setDescription(companyCode);
			company.setName(companyCode);
			company.setPercentChange(BigDecimal.ZERO);
			company.setUpdateTime(new Date());
			company.setProspectId(companyCode);
			company.setOmsOffering(omsOffering);
            if (isLargeDealProspect) {
				company.setLargeDealProspect(1);
            } else {
				company.setBundleId(bundleId);
			}
            company.setRiskType(riskType);
			company.setBssNaicsCode(bssNaicsCode);
			savedCompany = companyDao.saveAndFlush(company);
		} else {
			if (!Objects.equals(foundCompany.getOmsOffering(), omsOffering)) {
				isOmsOfferingValueChanged = true;
				foundCompany.setOmsOffering(omsOffering);
			}
			if (foundCompany.isLargeDealProspect() != isLargeDealProspect || !Objects.equals(foundCompany.getBundleId(), bundleId)) {
				isBundleIdChanged = true;
				if (isLargeDealProspect) {
					foundCompany.setLargeDealProspect(1);
					foundCompany.setBundleId(null);
				} else {
					foundCompany.setLargeDealProspect(0);
					foundCompany.setBundleId(bundleId);
				}
			}
			if (!Objects.equals(foundCompany.getRiskType(), riskType)) {
				isRiskTypeChanged = true;
				foundCompany.setRiskType(riskType);
			}
			if (!Objects.equals(foundCompany.getBssNaicsCode(), bssNaicsCode)) {
				isNaicsCodeChanged = true;
				foundCompany.setBssNaicsCode(bssNaicsCode);
			}

			savedCompany = (isOmsOfferingValueChanged || isBundleIdChanged || isRiskTypeChanged || isNaicsCodeChanged)
					? companyDao.saveAndFlush(foundCompany)
					: foundCompany;
		}
		return savedCompany.getId();
	}
	
	@Override
	public List<Company> getXbssCompaniesByCode(String code) {
		return companyDao.findByCode(code);
	}
	
	@Override
	public Map<Long, CompanyStrategyDetailsDto> getCompanyStrategyDetails(String companyCode) {
		return companyDataDao.getCompanyStrategyDetails(companyCode);
	}
	
	@Override
	public Company findCompanyBy(String companyCode, long realmPlanYearId) {
		return companyDao.findByCodeAndRealmPlanYearId(companyCode, realmPlanYearId);
	}

	@Override
	@Transactional
	public void updatePlYrChangeSyncExecutedFlag(String companyCode, BenExchngEnums benExchange) {
		Company prospectCompanyDtls = getCompanyDetails(companyCode, false, null, benExchange);
		if (prospectCompanyDtls != null) {
			prospectCompanyDtls.setPlYrChangeSyncExcuted(null);
		}
	}

    @Override
	public Company getPsCompanyDetails(String code) {
        Company company = psCompanyDao.getBasicCompanyDetails(code);
        String errorMessage = null;
        if (company.getLiveDate() == null) {
            errorMessage = "Live date is null for company: " + code;
        } else if (company.getPlanStartDate() == null) {
            errorMessage = "Plan start date is null for company: " + code;
        }
        if (StringUtils.isNotEmpty(errorMessage)) {
            cacheService.invalidateCache(CacheObjectTypeEnum.BASIC_COMPANY_DETAILS.getObjectType(),
                    CacheObjectLevelEnum.COMPANY.getObjectLevel(), code);
            throw new BSSBadDataException(errorMessage);
        }
        return company;
    }

	@Override
	public void updatePsCompanyCodeForProspect(long companyId, String companyCode) {
		companyDao.updatePsCompanyCodeForProspect(companyId, companyCode);
	}
	
	@Override
	public void updateAleUpdatedFlag(Company company, Integer status) {
		company.setAleUpdated(status);
		companyDao.save(company);
	}

	@Override
	public Company findByCompanyId(Long bssCompanyId) {
		return companyDao.findById(bssCompanyId).orElse(null);
	}

	@Override
	public Company getLatestCompany(String companyCode) {
		return companyDao.findLatestCompanyBy(companyCode);
	}

	/**
	 * This method returns Set of company & employee locations for the given company
	 * + mandatory locations for given realmYearId.
	 * 
	 * @param company
	 * @param realmPlanYear
	 */
	private void getLocations(Company company, RealmPlanYear realmPlanYear) {
		Set<String> companyRegions = realmDataDao.getCompanyLocationStates(company.getCode());
		Set<String> fundingRegions = Collections.emptySet();
		if (realmPlanYear != null) {
			fundingRegions = realmDataDao.getFundingPlanStates(company, companyRegions, realmPlanYear);
		}
		List<MandatoryRegion> mandatoryRegions = mandatoryRegionDao.findAllByRealmYrId(company.getRealmPlanYearId());
		for (MandatoryRegion mandatoryRegion : mandatoryRegions) {
			companyRegions.add(mandatoryRegion.getId().getRegion());
		}
		if (CollectionUtils.isEmpty(companyRegions)) {
			companyRegions.add("All");
		}
		List<String> employeeRegions = Collections.emptyList();

		if (CompanyServiceHelper.isProspectConvertedOnboardingClient(company)) {
			// Setting employee home state for prospect converted to client
			employeeRegions = getEmployeeHomeRegionsProspectConvertedClient(company.getCode());
		} else
			employeeRegions = realmDataDao.getEmployeeHomeStates(company.getCode());

		if (CollectionUtils.isEmpty(employeeRegions)) {
			companyRegions.add("All");
		}
		company.setEmployeeRegions(employeeRegions);
		company.setCompanyRegions(companyRegions);
		company.setFundingRegions(fundingRegions);
	}
	
	private List<String> getEmployeeHomeRegionsProspectConvertedClient(String companyCode) {
		Optional<List<ProspectCensusResponse>> coreCensus = Optional
				.ofNullable(bssCoreServiceClient.getCensusByCompanyCode(companyCode));
		return coreCensus
				.map(census -> census.stream().map(ProspectCensusResponse::getState).distinct().collect(Collectors.toList()))
				.orElse(Collections.emptyList());
	}

	@Override
	public boolean initiateQuarterChange(String companyCode, String quarter, String messageSeq) {
		if (!BenExchngEnums.TRINET_III.getQuarters().contains(quarter)) {
			logger.info("Skipping initiateQuarterChange: Quarter value: {} is not associated with TriNet III exchange.", quarter);
			return false;
		}
		// Get the latest company based on realm plan year.
		Company company = findLatestCompanyBy(companyCode);

		if (!BenExchngEnums.TRINET_III.equals(BenExchngEnums.getByBenExchange(company.getRealm().getBenExchange()))) {
			logger.info("Skipping initiateQuarterChange: Company with code: {} is not associated with TriNet III exchange.", companyCode);
			return false;
		}
		if (company.getQuater().equals(quarter)) {
			logger.info("Skipping initiateQuarterChange: Company with code: {} is already in quarter: {}.", companyCode, quarter);
			return false;
		}
		String processStatus = processStatusService.findPendingOrInProgressPSQuarterChangeProcessStatus(companyCode, quarter);
		if (!StringUtils.isEmpty(processStatus)) {
			logger.info("Skipping initiateQuarterChange: there is already a pending or in-progress process status: {} for companyCode: {} and quarter: {}.",
					processStatus, companyCode, quarter);
			return false;
		}

		ProcessInfoDto processInfoDto = new ProcessInfoDto();
		processInfoDto.setProcessName(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName());
		processInfoDto.setOldRealmPlanYear(company.getRealmPlanYearId());
		processInfoDto.setOldCompanyId(company.getId());
		processInfoDto.setExchangeId(company.getRealm().getId());

		RiskTypeEnum newQuarterRiskTypeEnum = realmPlanYearService.getRenewalRiskTypeForLatestPlanYearInQuarter(quarter);

		if (RiskTypeEnum.DIFFERENTIALS.equals(newQuarterRiskTypeEnum)) {
			processStatusService.createStrategySyncProcess(companyCode,
					JsonConverterUtils.convertObjectToJson(processInfoDto),
					ProcessStatusEnum.QUARTER_CHANGE.getProcessName(),
					ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName());
		} else {
			processStatusService.createStrategySyncProcess(companyCode,
					JsonConverterUtils.convertObjectToJson(processInfoDto),
					ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
					ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getIdentifierName());
		}
		return true;
    }

	@Override
	public Company findCompanyByQuarterAndEffDate(Date benefitStartDate, String quarter, String companyIdentifier){
		RealmPlanYear realmPlanYear = realmPlanYearDao.findByOeQuarter(benefitStartDate, quarter);
		if (realmPlanYear == null) {
			logger.error("RealmPlanYear not found for effectiveDate: {} and quarter: {}", benefitStartDate, quarter);
			return null;
		}
		Company company = companyService.findCompanyBy(companyIdentifier, realmPlanYear.getId());
		if (company != null) {
			company.setRealmPlanYear(realmPlanYear);
		}
		return company;
	}

	private Company findLatestCompanyBy(String companyCode) {
		Company company = companyDao.findLatestCompanyBy(companyCode);
		if (company == null) {
			throw new BSSApplicationException(
					new BSSApplicationError(
							BSSErrorResponseCodes.BSS_COMPANY_NOT_FOUND,
							BSSHttpStatusConstants.BAD_REQUEST,
							"",
							"No Company found for company " + companyCode,
							null,
							null
					)
			);
		}
		RealmPlanYear rpy = realmPlanYearService.getRealmPlanYearById(company.getRealmPlanYearId());
		company.setRealmPlanYear(rpy);
		company.setQuater(rpy.getOeQuarter());
		Realm realm = realmTypeService.findById(rpy.getRealmId());
		company.setRealm(realm);

		return company;
	}

	@Override
	public CompanyDetailsDto getCompanyDetailsById(Long companyId) {
		return companyDataDao.getCompanyDetailsById(companyId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void resetCompany(long companyId) {
		resetCompany(companyId, false);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void resetCompany(long companyId, boolean deleteCompanyRecord) {
		logger.info("resetCompany() - starting full reset for companyId: {} [deleteCompanyRecord={}]", companyId, deleteCompanyRecord);
		strategyService.deleteStrategies(companyId);
		hrpDao.deleteCompanyDataByCompanyId(companyId);
		
		// Conditionally delete company record based on flag.
		// Default (false) preserves prospect company safety.
		// When true, performs hard-delete for quarter-change flows.
		if (deleteCompanyRecord) {
			logger.debug("resetCompany() - deleting company record for companyId: {}", companyId);
			hrpDao.deleteCompanyByCompanyId(companyId);
		}
		
		logger.info("resetCompany() - completed for companyId: {} [deleteCompanyRecord={}]", companyId, deleteCompanyRecord);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteCompanyData(long companyId) {
		logger.info("deleteCompanyData() - start for companyId: {}", companyId);
		hrpDao.deleteCompanyDataByCompanyId(companyId);
		logger.info("deleteCompanyData() - completed for companyId: {}", companyId);
	}
}
