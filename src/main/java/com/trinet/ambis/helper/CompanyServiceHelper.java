package com.trinet.ambis.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trinet.ambis.enums.OmsOfferingEnum;
import org.joda.time.DateTime;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.RealmRegionMinFunding;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.persistence.model.SchedTblId;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.CompanyBandCodes;
import com.trinet.ambis.service.model.Industry;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.service.model.MinimumFunding;
import com.trinet.ambis.service.model.PlanYearCommonData;
import com.trinet.ambis.service.model.RegionalMinimumFunding;
import com.trinet.ambis.service.model.SelectionDate;
import com.trinet.ambis.service.model.UserData;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Rules;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;

/**
 * @author rvutukuri
 *
 */
public class CompanyServiceHelper {

	private static final String COMPANY_REGEX = "^[A-Z0-9]{3,4}$";
	public static final Pattern COMPANY_PATTERN = Pattern.compile( COMPANY_REGEX, Pattern.CASE_INSENSITIVE );


	private CompanyServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + CompanyServiceHelper.class.getName() + " can not be instantiated.");
	}
	
	/**
	 * This method is for getting the industry for a company.
	 * 
	 * @param company
	 * @return
	 */
	public static Industry getIndustry(Company company) {
		int code = (company.getNaicsCode() != null) ? company.getNaicsCode() : 0;

        // make sure we only get the first two digits of the NAICS code
        String codeStr = String.valueOf(code);
        if (codeStr.length() > 2) {
            code = Integer.parseInt(codeStr.substring(0, 2));
        }

        Industry industry = new Industry(code);
        IndustryType industryType = null;
        String state = company.getHeadQuatersState();
        String exchange = company.getRealm().getBenExchange();

        boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );

        if (exchange.equals(BenExchngEnums.TRINET_I.getBenExchng())) {
            industryType = IndustryType.HS; // Test
        } else if (exchange.equals(BenExchngEnums.TRINET_IV.getBenExchng())) {
            industryType = getIndustryTypeForTrinetIV(code);
        } else if (exchange.equals(BenExchngEnums.TRINET_XI.getBenExchng())) {
            industryType = IndustryType.EX;
        } else if (exchange.equals(BenExchngEnums.TRINET_II.getBenExchng())) {
            industryType = getIndustryTypeForTrinetII(code);
        } else if (exchange.equals(BenExchngEnums.TRINET_OMS.getBenExchng())) {
            industryType = IndustryType.EX;
        } else if (exchange.equals(BenExchngEnums.TRINET_III.getBenExchng()) && ! isPickChoose ) {
            industryType = getIndustryTypeForTrinetIII(code, state);
		} else if (exchange.equals(BenExchngEnums.TRINET_III.getBenExchng()) && isPickChoose ) {
			industryType = IndustryType.EH;
		}
        industry.setIndustryType(industryType);
        return industry;
    }

	private static IndustryType getIndustryTypeForTrinetIII(int code, String state) {
		IndustryType industryType;
		if (state.equals("ID") || state.equals("MN")) {
		/**
		 * AMBIS-4537 21.22.23, 31 - 33, 42, 44-45, 48-49 -
		 * ManufWhlSlRetWhtr (MF) 51 - InfoServ (IS) 52, 53 - FinInsRlEst
		 * (FR) 54 - ProfSciTechServ (PT) 55, 56, 61, 62, 71, 72, 81, 92 -
		 * AdminOthBusSrv (AO)
		 */
		switch (code) {
		case 21:
		case 22:
		case 23:
		case 31:
		case 33:
		case 42:
		case 44:
		case 45:
		case 48:
		case 49:
		    industryType = IndustryType.MF;
		    break;
		case 51:
		    industryType = IndustryType.IS;
		    break;
		case 52:
		case 53:
		    industryType = IndustryType.FR;
		    break;
		case 54:
		    industryType = IndustryType.PT;
		    break;
		case 55:
		case 56:
		case 61:
		case 62:
		case 71:
		case 72:
		case 81:
		case 92:
		    industryType = IndustryType.AO;
		    break;
		default:
		    industryType = IndustryType.IS;
		    break;
		}
      }
		else {
		    /**
		     * For states other than ID/MN
		        AM("AgricultureMiningDurableGoods",11, 21, 22, 31, 32, 33),
		        EH("EducationHealth", 61, 62),
		        FP("FinanceProfessionalBusiness", 52, 53, 54, 55, 56, 92),
		        LH("LeisureHospitality", 51, 71, 72),
		        RW("RetailWholesale",42, 44, 45),
		        TC("TransportationConstructionRepairOther", 23, 48, 49, 81),
		     */
		    switch (code) {
		    case 11:
		    case 21:
		    case 22:
		    case 31:
		    case 32:
		    case 33:
		        industryType = IndustryType.AM;
		        break;
		    case 61:
		    case 62:
		        industryType = IndustryType.EH;
		        break;
		    case 52:
		    case 53:
		    case 54:
		    case 55:
		    case 56:
		    case 92:
		        industryType = IndustryType.FP;
		        break;
		    case 51:
		    case 71:
		    case 72:
		        industryType = IndustryType.LH;
		        break;
		    case 42:
		    case 44:
		    case 45:
		        industryType = IndustryType.RW;
		        break;
		    case 23:
		    case 48:
		    case 49:
		    case 81:
		        industryType = IndustryType.TC;
		        break;
		    default:
		        industryType = IndustryType.TC;
		        break;
		    }
		}
		return industryType;
	}

	private static IndustryType getIndustryTypeForTrinetII(int code) {
		IndustryType industryType;
		/**
		 * AMBIS-3422 1) NAICS Codes = 11%, 21%, 48%, 49%, 69%, 81% Blank
		 * NAICS codes (Agriculture/Transportation/Other) - Default Industry
		 * 2) NAICS Codes = 23% (Construction) 3) NAICS Codes = 31%, 32%,
		 * 33% (durable/nondurable goods) 4) NAICS Codes = 42%, 44%, 45%
		 * (Retail/Wholesale) 5) NAICS Codes = 51%, 52%, 53%, 54%, 56%
		 * (Finance/Professional/Business) 6) NAICS Codes = 61%, 62%, 92%
		 * (education/Health) 7) NAICS Codes = 71%, 72% (leisure and
		 * hospitality)
		 */
		switch (code) {
		case 11:
		case 21:
		case 48:
		case 49:
		case 69:
		case 81:
		    industryType = IndustryType.AT;
		    break;
		case 23:
		    industryType = IndustryType.CN;
		    break;
		case 31:
		case 32:
		case 33:
		    industryType = IndustryType.DG;
		    break;
		case 42:
		case 44:
		case 45:
		    industryType = IndustryType.RW;
		    break;
		case 51:
		case 52:
		case 53:
		case 54:
		case 56:
		    industryType = IndustryType.FB;
		    break;
		case 61:
		case 62:
		case 92:
		    industryType = IndustryType.EH;
		    break;
		case 71:
		case 72:
		    industryType = IndustryType.LH;
		    break;
		default:
		    industryType = IndustryType.AT;
		    break;
		}
		return industryType;
	}

	private static IndustryType getIndustryTypeForTrinetIV(int code) {
		IndustryType industryType;
		switch (code) {
		case 51:
		    industryType = IndustryType.TM;
		    break;

		case 52:
		    industryType = IndustryType.FS;
		    break;
		default:
		    industryType = IndustryType.BS;
		    break;
		}
		return industryType;
	}

	/**
	 * This method is for creating a BSS company.
	 * 
	 * @param company
	 * @return
	 */
	public static Company createBssCompany(Company company) {
		Company bssCompany = new Company();
		bssCompany.setCode(company.getCode());
		bssCompany.setUpdateTime(new Date());
		bssCompany.setCurrentYearTotalCost(BigDecimal.ZERO);
		bssCompany.setPercentChange(BigDecimal.ZERO);
		bssCompany.setDescription(company.getDescription());
		bssCompany.setName(company.getName());
		bssCompany.setRealmPlanYearId(company.getRealmPlanYearId());
		if (company.isRenewalCompany()) {
			bssCompany.setActualHeadCount(company.getActualHeadCount());
			bssCompany.setHeadcount(company.getActualHeadCount());
		} else {
			bssCompany.setHeadcount(company.getHeadcount());
		}
		bssCompany.setAcaLargeEmplr(company.isAcaLargeEmplr());
		return bssCompany;
	}

	/**
	 * This method is for populating the PS company details to BSS company.
	 * 
	 * @param company
	 * @param bssCompany
	 */
	public static void mapPSCompanyDataToBSSCompany(Company company, Company bssCompany) {
		bssCompany.setBenefitProgram(company.getBenefitProgram());
		bssCompany.setName(company.getName());
		bssCompany.setDescription(company.getDescription());
		bssCompany.setRealmPlanYear(company.getRealmPlanYear());
		bssCompany.setRealm(company.getRealm());
		bssCompany.setBandCodes(company.getBandCodes());
		if (company.isEligAle()) {
			bssCompany.setAleAmount(company.getRealmPlanYear().getAleAmount());
		}
		bssCompany.setAleAmountHistory(company.getAleAmountHistory());
		bssCompany.setEligAle(company.isEligAle());
		bssCompany.setBMGUser(company.isBMGUser());
		bssCompany.setCSAUser(company.isCSAUser());
		bssCompany.setTMTUser(company.isTMTUser());
		bssCompany.setBenCorpAdUser(company.isBenCorpAdUser());
		bssCompany.setBenAdvisorUser(company.isBenAdvisorUser());
		bssCompany.setHeadQuatersCity(company.getHeadQuatersCity());
		bssCompany.setHeadQuatersState(company.getHeadQuatersState());
		bssCompany.setPayrollProcessed(company.isPayrollProcessed());
		bssCompany.setQuater(company.getQuater());
		bssCompany.setActualHeadCount(company.getActualHeadCount());
		bssCompany.setIndustry(company.getIndustry());
		bssCompany.setRenewalOpen(company.isRenewalOpen());
		bssCompany.setRenewalCompany(company.isRenewalCompany());
		bssCompany.setMbg(company.isMbg());
		bssCompany.setPfClient(company.getPfClient());
		bssCompany.setTransitionPeriod(company.isTransitionPeriod());
		bssCompany.setPlanEndDate(Utils.convertDateToString(company.getRealmPlanYear().getPlanYearEnd()));
		bssCompany.setCompanySetupDate(company.getCompanySetupDate());
		bssCompany.setSchedTbl(company.getSchedTbl());
		bssCompany.setTexasSitus(company.isTexasSitus());
		bssCompany.setK1Company(company.isK1Company());
		bssCompany.setRegionalMinimumFundings(company.getRegionalMinimumFundings());
		bssCompany.setExclusiveMedPlan(company.getExclusiveMedPlan());
		bssCompany.setDefaultMinFundingPct(company.getDefaultMinFundingPct());
		bssCompany.setMinFundings(company.getMinFundings());
		bssCompany.setZipCode(company.getZipCode());
		bssCompany.setEmplId(company.getEmplId());
		bssCompany.setCompanyRegions(company.getCompanyRegions());
		bssCompany.setFundingRegions(company.getFundingRegions());
		bssCompany.setEmployeeRegions(company.getEmployeeRegions());
		bssCompany.setRegionsUpdated(company.isRegionsUpdated());
		bssCompany.setLiveDate(company.getLiveDate());
		bssCompany.setPlanStartDate(company.getPlanStartDate());
		bssCompany.setBenefitStartDate(company.getBenefitStartDate());
		bssCompany.setAcaLargeEmplrStatusUpdated(bssCompany.isAcaLargeEmplr() ^ company.isEligAle());
		bssCompany.setProspectConvertedClient(company.isProspectConvertedClient());
		bssCompany.setAleUpdatedNewClient(company.isAleUpdatedNewClient());
		if (company.isProspectConvertedClient() && !company.isRenewalCompany()) {
		    bssCompany.setProspectConvertedOnboardingClient(true);
		}
		
	}

	/**
	 * This method is for populating the PlanYearCommonData.
	 * 
	 * @param company
	 * @return
	 */
	public static PlanYearCommonData populatePlanYearCommonData(Company company) {
		return new PlanYearCommonData(company.getRealmPlanYear().getPlanYearStart(),
				company.getRealmPlanYear().getPlanYearEnd());
	}

	/**
	 * 
	 * @param company
	 * @return
	 */
	public static PlanYearCommonData populateCurrentPlanYearData(Company company, RealmPlanYearService realmPlanYearService) {
		PlanYearCommonData planYearCommonData = null;
		if (company.isRenewalCompany()) {
			RealmPlanYear currentRealmPlanYear = realmPlanYearService
					.getPreviousRealmPlanYear(company.getRealmPlanYear());
			if(null != currentRealmPlanYear){
				planYearCommonData = new PlanYearCommonData(currentRealmPlanYear.getPlanYearStart(),
						currentRealmPlanYear.getPlanYearEnd());
			}else{
				planYearCommonData = new PlanYearCommonData(company.getRealmPlanYear().getPlanYearStart(),
						company.getRealmPlanYear().getPlanYearEnd());
			}
		} else {
			planYearCommonData = new PlanYearCommonData(company.getRealmPlanYear().getPlanYearStart(),
					company.getRealmPlanYear().getPlanYearEnd());
		}

		return planYearCommonData;
	}

	/**
	 * This method is for populating the populateUserCommonData.
	 * 
	 * @param company
	 * @return
	 */
	public static UserData populateUserCommonData(Company company) {
		Boolean bmgUser = false;
		if (!company.isCSAUser()) {
			bmgUser = company.isBMGUser();
		}
		return new UserData(company.isCSAUser(), bmgUser, company.isTMTUser(), company.isBenCorpAdUser(), company.isBenAdvisorUser());
	}

	/**
	 * This method is for populating the scheduleDates.
	 * 
	 * @param bssCompany
	 * @param realmPlanYear
	 */
	public static void populateScheduleTableData(Company bssCompany, RealmPlanYear realmPlanYear) {
		SchedTbl scheduleDates = new SchedTbl();
		scheduleDates.setOpenDate(new DateTime().minusDays(2).toDate());
		scheduleDates.setCloseDate(new DateTime().minusDays(2).toDate());
		scheduleDates.setExtensionEndDate(new DateTime().minusDays(2).toDate());
		SchedTblId sid = new SchedTblId();
		sid.setRealmYearId(realmPlanYear.getId());
		sid.setCompany(bssCompany.getCode());
		scheduleDates.setSched(sid);
		bssCompany.setSchedTbl(scheduleDates);
	}
	/**
	 * 
	 * @param schedDates
	 * @param realmYearId
	 * @param company
	 */
	public static void constructBlankScheduleDates(SchedTbl schedDates,
			Long realmYearId, String company) {
		schedDates.setOpenDate(new DateTime().minusDays(2).toDate());
		schedDates.setCloseDate(new DateTime().minusDays(2).toDate());
		schedDates.setExtensionEndDate(new DateTime().minusDays(2).toDate());
		schedDates.setSched(new SchedTblId());
		schedDates.getSched().setCompany(company);
		schedDates.getSched().setRealmYearId(realmYearId);
	}


	/**
	 * <p>This constructs a SelectionDate object from the schedule table.</p>
	 * <p>This method was moved here from CompanyServiceImpl so that it can be used by client company common data and prospect company common data.</p>
	 * 
	 * @param schedTbl
	 * @return
	 */
	public static SelectionDate constructSelectionDate( SchedTbl schedTbl ) {
		if( schedTbl.getCloseDate().compareTo( schedTbl.getExtensionEndDate() ) < 0 ) {
			return new SelectionDate( schedTbl.getInternalOpenDate(),
					schedTbl.getInternalCloseDate(),
					schedTbl.getOpenDate(),
					schedTbl.getExtensionEndDate() );
		} else {
			return new SelectionDate(schedTbl.getInternalOpenDate(),
					schedTbl.getInternalCloseDate(),
					schedTbl.getOpenDate(),
					schedTbl.getCloseDate() );
		}
	}


	/**
	 * This method is for validating if the companies renewal period is open.
	 * 
	 * @param company
	 * @param schedTbl
	 * @param realmPlanYear
	 * @return
	 */
	public static boolean isRenewalOpen(Company company, SchedTbl schedTbl, RealmPlanYear realmPlanYear) {
		boolean renewalPeriod = false;
		boolean transitionPeriod = false;
		company.setSchedTbl(schedTbl);
		if (schedTbl != null) {
			company.getSchedTbl().setPayrollProcessed(company.isPayrollProcessed());
			Date extensionDate = schedTbl.getExtensionEndDate();
			Date internalOpenDate = schedTbl.getInternalOpenDate();
			Date internalCloseDate = schedTbl.getInternalCloseDate();
			
			boolean isExternalRenewalOpen = isExternalRenewalOpen(company);
			Date currentDate = CommonUtils.getCurrentDate();
			Date planYearStartDate = realmPlanYear.getPlanYearStart();
			
			if (isExternalRenewalOpen) {
				renewalPeriod = true;
			} else if ((company.isCSAUser() || company.isBMGUser() || company.isTMTUser() || company.isBenAdvisorUser()) && internalOpenDate != null
					&& internalCloseDate != null
					&& (internalOpenDate.compareTo(currentDate) * currentDate.compareTo(internalCloseDate) >= 0)) {
				renewalPeriod = true;
			} else if (extensionDate != null && planYearStartDate != null
					&& ((extensionDate.compareTo(currentDate)) <= 0)
					&& ((currentDate.compareTo(planYearStartDate)) < 0)) {
				transitionPeriod = true;
			}
		}
		company.setTransitionPeriod(transitionPeriod);
		return renewalPeriod;
	}

	/**
	 * <p>Assuming client company codes are 3 or 4 alpha-numeric characters,
	 * this method returns true when the passed company code matches this pattern.</p>
	 * <p>When this method returns false, we can assume the code is a prospect company code.</p>
	 * @param code
	 * @return true: client company code<br>
	 * false: prospect company code
	 */
	public static boolean isClientCompanyPattern( String code ) {
		Matcher matcher = COMPANY_PATTERN.matcher( code );
		return matcher.find();
	}


	/**
	 * This method is identify if renewal period is open for external clients.
	 * @param company
	 * @return
	 */
	public static boolean isExternalRenewalOpen(Company company) {
		boolean renewalPeriod = false;

		SchedTbl schedTbl = company.getSchedTbl();
		if (schedTbl != null) {
			Date extensionDate = schedTbl.getExtensionEndDate();
			Date openDate = schedTbl.getOpenDate();
			Date closeDt = schedTbl.getCloseDate();
			if (extensionDate != null && extensionDate.after(closeDt)) {
				closeDt = extensionDate;
			}
			Date currentDate = CommonUtils.getCurrentDate();
			if (extensionDate != null && openDate != null
					&& (openDate.compareTo(currentDate) * currentDate.compareTo(closeDt) >= 0)) {
				renewalPeriod = true;
			}
		}
		return renewalPeriod;
	}

	/**
	 * 
	 * @param companyId
	 * @param bandCodes
	 * @return
	 */
	public static List<CompanyBandCodes> getBssBandCodeList(Long companyId, BandCodes bandCodes) {
		List<CompanyBandCodes> bssBandcodes = new ArrayList<>();
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.AETNA_BAND_CARRIER, bandCodes.getAetnaBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.AETNA_HMO_BAND_CARRIER, bandCodes.getAetnaHmoBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.AETNA_PPO_BAND_CARRIER, bandCodes.getAetnaPpoBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.BCBS_BAND_CARRIER, bandCodes.getBcbsBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.BCBSNC_BAND_CARRIER, bandCodes.getBcbsNcBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.BCBSID_BAND_CARRIER, bandCodes.getBcOfIdBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.BCBSCA_BAND_CARRIER, bandCodes.getBsOfCaBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.DISABILITY_BAND_CARRIER, bandCodes.getDisBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.KAISERCO_BAND_CARRIER, bandCodes.getKaisCoBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.KAISER_BAND_CARRIER, bandCodes.getKaiserBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.LIFE_BAND_CARRIER, bandCodes.getLifeBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.TUFFS_BAND_CARRIER, bandCodes.getTuftsBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.UHC_BAND_CARRIER, bandCodes.getUhcBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.BCBSMN_BAND_CARRIER, bandCodes.getBcbsMNBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.KAISERHI_BAND_CARRIER, bandCodes.getKaiHawaiiBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.KAISERMD_BAND_CARRIER, bandCodes.getKaiMidAtlBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.KAISERNW_BAND_CARRIER, bandCodes.getKaisNwBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.EMPIRENY_BAND_CARRIER, bandCodes.getEmpireNYBand()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.HARVARD_BAND_CARRIER, bandCodes.getHarvardBandCode()));
		bssBandcodes.add(new CompanyBandCodes(companyId, BSSApplicationConstants.HIGHMARK_BAND_CARRIER, bandCodes.getAetnaHmoBandCode()));
		return bssBandcodes;
	}

	/**
	 * 
	 * @param bssBancodes
	 * @param psBancodes
	 * @return
	 */
	public static boolean compareBandCodes(List<CompanyBandCodes> bssBancodes, List<CompanyBandCodes> psBancodes) {
		boolean bandCodesMatch = true;
		Map<String, String> bssBandCodeValueMap = new HashMap<>();
		for (CompanyBandCodes cbc : bssBancodes) {
			bssBandCodeValueMap.put(cbc.getBandCodeType(), cbc.getBandCodeValue());
		}
		Map<String, String> psBandCodeValueMap = new HashMap<>();
		for (CompanyBandCodes cbc : psBancodes) {
			psBandCodeValueMap.put(cbc.getBandCodeType(), cbc.getBandCodeValue());
		}
		for (Map.Entry<String, String> entry : bssBandCodeValueMap.entrySet()) {
			String psBandCodeValue = psBandCodeValueMap.get(entry.getKey());
			String bssBandCodeValue = entry.getValue();
			if (null != psBandCodeValue) {
				if (!psBandCodeValue.equals(bssBandCodeValue)) {
					bandCodesMatch = false;
				}
			} else {
				bandCodesMatch = false;
			}
		}
		if (bssBandCodeValueMap.size() != psBandCodeValueMap.size()) {
			bandCodesMatch = false;
		}
		return bandCodesMatch;
	}

	/**
	 * 
	 * @param headCount
	 * @param company
	 */
	public static void saveStrategyUpdateCompany(int headCount, Company company) {
		company.setHeadcount(headCount);
		company.setUpdateTime(new Date());
	}
	
	/**
	 * 
	 * @param company
	 */
	public static void updateMinimumFunding(Company company, List<RealmRegionMinFunding> realmRegionMinFundings,
			Set<MinFundExceptionDto> minFundExceptions) {
		setPlanYearMinimumFunding(company);
		Rules.overrideMiniumFunding(company);
		setRegionalMiniumFunding(company, realmRegionMinFundings);
		Rules.overrideExceptionMiniumFunding(company, minFundExceptions);
	}

	private static void setPlanYearMinimumFunding(Company company) {
		company.setDefaultMinFundingPct(company.getRealmPlanYear().getMinFunding());
		boolean tibProspect = CompanyServiceHelper.isTibProspect(company);
		BigDecimal minFundingValue;
		Set<MinimumFunding> minFundings = new HashSet<>();
		if (tibProspect) {
			minFundingValue = BigDecimal.valueOf(0);
		} else {
		    	minFundingValue = BigDecimal.valueOf(company.getRealmPlanYear().getMinFunding());
		}
		MinimumFunding medMinFund = new MinimumFunding(PlanTypesEnum.MEDICAL.getName(), MinFundExceptionService.PERCENT,
			minFundingValue, false);
		MinimumFunding denMinFund = new MinimumFunding(PlanTypesEnum.DENTAL.getName(), MinFundExceptionService.PERCENT,
			minFundingValue, false);
		MinimumFunding visMinFund = new MinimumFunding(PlanTypesEnum.VISION.getName(), MinFundExceptionService.PERCENT,
			minFundingValue, false);
		minFundings.add(medMinFund);
		minFundings.add(denMinFund);
		minFundings.add(visMinFund);
		company.setMinFundings(minFundings);
	}

	private static void setRegionalMiniumFunding(Company company, List<RealmRegionMinFunding> realmRegionMinFundings) {
		List<RegionalMinimumFunding> regionalMinimumFundings = new ArrayList<>();
		if (null != realmRegionMinFundings && !realmRegionMinFundings.isEmpty()) {
			for (RealmRegionMinFunding realmRegionMinFunding : realmRegionMinFundings) {
				RegionalMinimumFunding regionalMinimumFunding = new RegionalMinimumFunding();
				regionalMinimumFunding.setRegion(realmRegionMinFunding.getId().getRegion());
				regionalMinimumFunding.setFundingPct(realmRegionMinFunding.getMinFundingPct());
				regionalMinimumFundings.add(regionalMinimumFunding);
			}
		}
		company.setRegionalMinimumFundings(regionalMinimumFundings);
	}

	/**
	 * This method is to verify that the company
	 * @param company
	 * @return
	 */
	public static boolean isBundledCompany(Company company) {
		if (company != null && null != company.getBundleId()) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * This method returns a boolean to indicate if the prospect
	 * is a TIB prospect
	 *
	 * @param company
	 */
	public static boolean isTibProspect(Company company) {
		return CompanyServiceHelper.isOMSExchange(company)
				&& company.getOmsOffering() != null
				&& !OmsOfferingEnum.OMB_TLD.name().equals(company.getOmsOffering());
	}

	public static boolean isOMSExchange(Company company) {
		return company.getRealm().getBenExchange().equals(BenExchngEnums.TRINET_OMS.getBenExchng());
	}
	
	/**
	 * This method returns a boolean to indicate if the company
	 * is a TNXI company
	 *
	 * @param company
	 */
	public static boolean isTNXIExchange(Company company) {
		return company.getRealm().getBenExchange().equals(BenExchngEnums.TRINET_XI.getBenExchng());
	}

    /**
     * This method checks if the company is prospectConvertedClient
     * @param company
     */
    public static boolean isProspectConvertedOnboardingClient(Company company) {
        return company.isProspectConvertedClient() && !company.isRenewalCompany();
    }
}
