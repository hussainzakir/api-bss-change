package com.trinet.ambis.service.impl;

import static com.trinet.ambis.exception.BSSErrorResponseCodes.ERR_BSS_PROSPECT_REQUEST;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.trinet.ambis.enums.RiskTypeEnum;

import com.trinet.ambis.service.*;
import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.CompanyBandCodesDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.RealmRegionMinFunding;
import com.trinet.ambis.service.dto.RealmPlanYearDetailsDto;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.CompanyBandCodes;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.service.model.RealmTypeService;
import com.trinet.ambis.service.model.prospect.ProspectInfoResponse;
import com.trinet.ambis.util.ProspectServiceRestClient;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;
import com.trinet.common.DateUtils;
import com.trinet.domain.common.ReturnResponse;
import com.trinet.security.exception.NotFoundException;

@Service
public class ProspectCompanyServiceImpl implements ProspectCompanyService {

	private static final Logger logger = LoggerFactory.getLogger(ProspectCompanyServiceImpl.class);

	@Autowired
	CompanyDao companyDao;

	@Autowired
	GroupRuleService groupRuleService;

	@Autowired
	MinFundExceptionService minFundExceptionService;

	@Autowired
	RealmDataDao realmDataDao;

	@Autowired
	RealmPlanYearService realmPlanYearService;

	@Autowired
	RealmRegionMinFundingService realmRegionMinFundingService;

	@Autowired
	RealmTypeService realmTypeService;

	@Autowired
	ProspectServiceRestClient prospectServiceRestClient;

	@Autowired
	CompanyBandCodesDao companyBandCodesDao;
	
	@Autowired
	PsCompanyDao psCompanyDao;
	
	@Autowired
	BenefitsBundleService benefitsBundleService;

	@Autowired
	private BandCodesService bandCodesService;

	@Autowired
	RateSystemService rateSystemService;

	@Override
	public Company getProspectCompanyDetails(String code, BenExchngEnums benExchange) {

		if (benExchange == null) {
			throw new BSSApplicationException(new BSSApplicationError(ERR_BSS_PROSPECT_REQUEST,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Invalid exchange code passed for Prospect.", null, null));
		}

		Company prospectData = getProspectData(code, benExchange);
		prospectData.setCode(code);
		setOeQuarterAndSitus(prospectData, benExchange);

		Date effectiveDate = getEffectiveDate(prospectData);

		Company prospectCompany = companyDao.findCompanyBy(prospectData.getCode(), benExchange.getBenExchng(),
		prospectData.getQuater(), effectiveDate);

		return setCompanyDetails(prospectData, prospectCompany);
	}
	
	@Override
	public Company getProspectCompanyDetails(String code, long planYrId) {
		RealmPlanYear rpy = realmPlanYearService.getRealmPlanYearById(planYrId);
		BenExchngEnums benExchange = BenExchngEnums.getById(rpy.getRealmId());
		Company prospectData = getProspectData(code, benExchange);
		prospectData.setCode(code);
		String commonOwnerCompanyCode = prospectData.getCommonOwnerCompanyCode();
		boolean isTexasSitus = true;
		if (StringUtils.isNotEmpty(commonOwnerCompanyCode)) {
			isTexasSitus = psCompanyDao.isTexasSitus(commonOwnerCompanyCode, getEffectiveDate(prospectData));
		}
		prospectData.setTexasSitus(isTexasSitus);
		prospectData.setQuater(rpy.getOeQuarter());
		Company prospectCompany = companyDao.findByCodeAndRealmPlanYearId(code, planYrId);
		return setCompanyDetails(prospectData, prospectCompany);
	}
	
	@Override
	public ProspectInfoResponse getProspectBasicDetails(String companyCode, BenExchngEnums benExchange) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.put("prospectId", List.of(companyCode));
		if(null != benExchange) {
			map.put("exchangeId", List.of(benExchange.getExchangeId()));
		}
		ParameterizedTypeReference<ReturnResponse<ProspectInfoResponse>> prospectDetailsBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest<ProspectInfoResponse> prospectApiGetRequest = ProspectApiRequest.<ProspectInfoResponse>builder()
				.method(HttpMethod.GET)
				.uri(ProspectURIConstants.PROSPECT_INFO_URI)
				.queryParams(map)
				.parameterizedTypeReference(prospectDetailsBean)
				.build();
		return prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
	}
	
	@Override
	public void updateProspectExpiryDate(String companyCode, String exchangeId, LocalDate expiryDate) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.put("prospectId", List.of(companyCode));

		ParameterizedTypeReference<ReturnResponse<Object>> updateExpiryDateBean = new ParameterizedTypeReference<>() {
		};

		String prospectProposalReqJson = "{\"exchangeId\": \"" + exchangeId + "\", \"expiryDate\":\"" + expiryDate
				+ "\"}";
		ProspectApiRequest prospectApiPutRequest = ProspectApiRequest.builder()
				.method(HttpMethod.PUT).uri(ProspectURIConstants.PROSPECT_UPDATE_EXPIRY_DATE).queryParams(map)
				.requestBody(prospectProposalReqJson).parameterizedTypeReference(updateExpiryDateBean).build();
		prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiPutRequest);
	}

	private Company setCompanyDetails(Company prospectData, Company prospectCompany) {
		Date effectiveDate = getEffectiveDate(prospectData);

		if (prospectCompany == null) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_PS_COMPANY_NOT_FOUND,
					BSSHttpStatusConstants.NOT_FOUND, ProspectCompanyServiceImpl.class.getName(), "No results found.",
					"", null));
		}
		prospectCompany.setName(prospectData.getName());
		prospectCompany.setDescription(prospectData.getName());
		prospectCompany.setHeadQuatersState(prospectData.getHeadQuatersState());
		prospectCompany.setZipCode(prospectData.getZipCode());
		prospectCompany.setK1Company(prospectData.isK1Company());
		prospectCompany.setTexasSitus(prospectData.isTexasSitus());
		prospectCompany.setQuater(prospectData.getQuater());
		String benefitsStartDate = Utils.convertDateToString(effectiveDate);
		prospectCompany.setProspectCompany(true);
		prospectCompany.setContingentPricing(prospectData.isContingentPricing());
		prospectCompany.setNaicsCode(prospectData.getNaicsCode());

		prospectCompany.setBenefitStartDate(benefitsStartDate);
		prospectCompany.setLiveDate(benefitsStartDate);
		prospectCompany.setPlanStartDate(benefitsStartDate);
		prospectCompany.setBenefitsQuarterException(prospectData.isBenefitsQuarterException());

		if (null != prospectData.getExpiryDate()) {
			Date expiryDate = Utils.convertStringToDate(prospectData.getExpiryDate(),
					BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
			prospectCompany.setExpiryDate(
					Utils.convertDateToString(expiryDate, BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD));
		}

		RealmPlanYear rpy = realmPlanYearService.getRealmPlanYearById(prospectCompany.getRealmPlanYearId());
		RealmCloneProgram cloneProgram = realmDataDao.getRealmCloneProgram(rpy.getId());
		rpy.setCloneProgram(cloneProgram.getCloneProgram());
		prospectCompany.setRealmPlanYear(rpy);
		Realm realm = realmTypeService.findById(rpy.getRealmId());
		prospectCompany.setRealm(realm);

		// setting industry
		prospectCompany.setIndustry(CompanyServiceHelper.getIndustry(prospectCompany));

		CompanyServiceHelper.populateScheduleTableData(prospectCompany, prospectCompany.getRealmPlanYear());

		Set<String> regions = new HashSet<>();
		regions.add(prospectCompany.getHeadQuatersState());
		prospectCompany.setCompanyRegions(regions);
		prospectCompany.setFundingRegions(regions);
		prospectCompany.setEmployeeRegions(prospectData.getEmployeeRegions());
		prospectCompany.setPlanEndDate(Utils.convertDateToString(rpy.getPlanYearEnd()));
		prospectCompany.setProposalId(prospectData.getProposalId());

		List<RealmRegionMinFunding> realmRegionMinFundings = realmRegionMinFundingService
				.findByid_realmYearId(prospectCompany.getRealmPlanYearId());
		Set<MinFundExceptionDto> minFundExceptions = minFundExceptionService
				.findActiveByCompanyCodeAndQuarter(prospectCompany);
		CompanyServiceHelper.updateMinimumFunding(prospectCompany, realmRegionMinFundings, minFundExceptions);

		Set<String> sdiStates = RulesAndConfigsUtils.getSDIStates(rpy.getId());
		prospectCompany.setSdiStates(sdiStates);
		
		if (null != prospectCompany.getBundleId()) {
			Bundle bundle = benefitsBundleService.getBundleById(prospectCompany.getBundleId().longValue());
			prospectCompany.setBundleName(bundle.getName());
		}

		List<CompanyBandCodes> companyBandCodes = companyBandCodesDao
				.getBandCodesByCompanyIdAndEffDate(prospectCompany.getId(), prospectCompany.getBenefitStartDate());
		BandCodes bandCodes = new BandCodes();
		if (!CollectionUtils.isEmpty(companyBandCodes)) {
			setCompanyBandCodes(bandCodes, companyBandCodes);
		}
		prospectCompany.setBandCodes(bandCodes);
		setLifeAndDisabilityBandCodes(prospectCompany, effectiveDate);

		String rateType="REGIONAL";
		boolean phase2OutputsEnabled = AppRulesAndConfigsUtils.isBssOutputPhase2Enabled();
		if(phase2OutputsEnabled){
			rateType = rateSystemService.getRateSystemRateType(prospectCompany);
		}
		prospectCompany.setRateType(rateType.toUpperCase());

		return prospectCompany;
	}

	private void setLifeAndDisabilityBandCodes(Company prospectCompany, Date effectiveDate) {
		// Set Life and Disability band codes for Differential risk type
		if (Objects.equals(RiskTypeEnum.DIFFERENTIALS, prospectCompany.getRiskType())) {
			String naicsCode = prospectCompany.getBssNaicsCode() == null ? null : String.valueOf(prospectCompany.getBssNaicsCode());
			BenExchngEnums exchangeEnum = BenExchngEnums.getByBenExchange(prospectCompany.getRealm().getBenExchange());
			String lifeBandCode = bandCodesService.getBandCodeByType(
					naicsCode, effectiveDate, BSSApplicationConstants.LIFE, exchangeEnum
			);
			String disBandCode = bandCodesService.getBandCodeByType(
					naicsCode, effectiveDate, BSSApplicationConstants.DISABILITY, exchangeEnum
			);
			BandCodes bandCodes = prospectCompany.getBandCodes();
			bandCodes.setLifeBandCode(lifeBandCode);
			bandCodes.setDisBandCode(disBandCode);
		}
	}

	public Company getProspectData(String companyCode, BenExchngEnums benExchange) {
		ProspectInfoResponse data = getProspectBasicDetails(companyCode, benExchange);
		if (data != null) {
			logger.info("Setting prospect api details in Company Details");
			Company prospectData = new Company();
			prospectData.setZipCode(data.getZipCode());
			prospectData.setHeadQuatersState(data.getHqState());
			prospectData.setEmployeeRegions(data.getEmployeeHomeStates());
			prospectData.setBenefitStartDate(data.getBenStartDate());
			prospectData.setK1Company(data.isK1Company());
			prospectData.setNaicsCode(
					StringUtils.isNotEmpty(data.getPrimaryNaicsCode()) ? Integer.parseInt(data.getPrimaryNaicsCode()) : null
			);

			prospectData.setName(data.getCompanyName());
			prospectData.setProposalId(data.getProposalId());
			prospectData.setContingentPricing(data.isContingentPricing());
			prospectData.setExpiryDate(data.getExpiryDate());
			prospectData.setCommonOwnerCompanyCode(data.getCommonOwnerCompanyCode());
			prospectData.setBenefitsQuarterException(data.isBenefitsQuarterException());
			prospectData.setQuarterEffectiveDate(data.getQuarterEffectiveDate());

			return prospectData;
		} else {
			throw new NotFoundException("Prospect details not found. Id =  " + companyCode);
		}
	}
	
	private void setOeQuarterAndSitus(Company prospectData, BenExchngEnums benExchange) {
		String oeQuarter = null;
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = realmPlanYearService
				.findByRealmId(benExchange.getId());
		Date effDt = getEffectiveDate(prospectData);
		if (prospectData.isBenefitsQuarterException()) {
			if (prospectData.getQuarterEffectiveDate() != null) {
				effDt = Utils.convertStringToDate(prospectData.getQuarterEffectiveDate(),
						BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
			} else {
				effDt = companyBandCodesDao.getProspectBandEffectiveDate(prospectData.getCode(), benExchange.getId());
			}
		}
		final Date effDtFinal = effDt;
		Optional<RealmPlanYearDetailsDto> realmPlanYearDetailsDtoOpt = realmPlanYearDetailsDtos.stream()
				.filter(realmPlanYearDetailsDto -> DateUtils.isDateWithinRange(effDtFinal,
						realmPlanYearDetailsDto.getStartDate(), realmPlanYearDetailsDto.getEndDate())
						)
				.findFirst();
		if (realmPlanYearDetailsDtoOpt.isPresent())
			oeQuarter = realmPlanYearDetailsDtoOpt.get().getQuarter();
		prospectData.setTexasSitus(true);
		prospectData.setQuater(oeQuarter);
	}

	private void setCompanyBandCodes(BandCodes bandCode, List<CompanyBandCodes> companyBandCodes) {
		for (CompanyBandCodes companyBandCode : companyBandCodes) {
			String bandCodeType = companyBandCode.getBandCodeType();
			String bandCodeValue = companyBandCode.getBandCodeValue();
			switch (bandCodeType) {
			case "AETNA":
				bandCode.setAetnaBandCode(bandCodeValue);
				break;
			case "AETNAHMO":
				bandCode.setAetnaHmoBandCode(companyBandCode.getBandCodeValue());
				break;
			case "AETNAPPO":
				bandCode.setAetnaPpoBandCode(bandCodeValue);
				break;
			case "BCBS":
				bandCode.setBcbsBandCode(bandCodeValue);
				break;
			case "BCBSCA":
				bandCode.setBsOfCaBandCode(bandCodeValue);
				break;
			case "BCBSID":
				bandCode.setBcOfIdBandCode(bandCodeValue);
				break;
			case "BCBSMN":
				bandCode.setBcbsMNBandCode(bandCodeValue);
				break;
			case "BCBSNC":
				bandCode.setBcbsNcBandCode(bandCodeValue);
				break;
			case "DIABILITY":
				bandCode.setDisBandCode(bandCodeValue);
				break;
			case "EMPIRENY":
				bandCode.setEmpireNYBand(bandCodeValue);
				break;
			case "HARVARD":
				bandCode.setHarvardBandCode(bandCodeValue);
				break;
			case "KAISER":
				bandCode.setKaiserBandCode(bandCodeValue);
				break;
			case "KAISERCO":
				bandCode.setKaisCoBandCode(bandCodeValue);
				break;
			case "KAISERHI":
				bandCode.setKaiHawaiiBandCode(bandCodeValue);
				break;
			case "KAISERMD":
				bandCode.setKaiMidAtlBandCode(bandCodeValue);
				break;
			case "KAISERNW":
				bandCode.setKaisNwBandCode(bandCodeValue);
				break;
			case "LIFE":
				bandCode.setLifeBandCode(bandCodeValue);
				break;
			case "TUFFS":
				bandCode.setTuftsBandCode(bandCodeValue);
				break;
			case "UHC":
				bandCode.setUhcBandCode(bandCodeValue);
				break;
			case "HIGHMARK":
				bandCode.setHighmarkBandCode(bandCodeValue);
				break;
			default:

			}
		}
	}
	
	private Date getEffectiveDate(Company prospectData) {
		return Utils.convertStringToDate(prospectData.getBenefitStartDate(),
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
	}

}
