package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.exception.InvalidOmsOfferingException;
import com.trinet.ambis.util.JsonConverterUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.ApiBssPropertiesConstants;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.exception.PlanYearNotFound;
import com.trinet.ambis.persistence.dao.hrp.CompanyBandCodesDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.ExchangeDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.dto.ExchangeCarrierBandDto;
import com.trinet.ambis.persistence.dao.hrp.dto.ExchangeCarrierDetailsDto;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.exchange.CarrierDto;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.BandTypeEnum;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.CarrierBand;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.CarrierBand.CarrierBandDetails;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeCarrierDto;
import com.trinet.ambis.service.BandCodesService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ExchangeService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.ProspectCompanyService;
import com.trinet.ambis.service.ProspectStrategySyncService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.dto.ProcessInfoDto;
import com.trinet.ambis.service.dto.RealmPlanYearDetailsDto;
import com.trinet.ambis.service.model.prospect.ProspectInfoResponse;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.service.prospect.exception.NotFoundException;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;
import com.trinet.ambis.validator.NaicsCodeValidator;
import com.trinet.common.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.trinet.ambis.enums.BenExchngEnums.TRINET_OMS;

@Service
@RequiredArgsConstructor
@Log4j2
public class ExchangeServiceImpl implements ExchangeService {
	
	private static final String SFDC_BCBSNC = "bcbsnc";
	private static final String SFDC_BC_ODAHO = "bc idaho";
	private static final String SFDC_UHC = "uhc";
	private static final String SFDC_AETNA = "aetna";
	private static final String SFDC_BSCA = "bsca";
	private static final String SFDC_KAISER_CA = "kaiser ca";
	private static final String SFDC_KAISER_CO_GA = "kaiser co/ga";
	private static final String SFDC_KAISER_NW_WA = "kaiser nw/wa";
	private static final String SFDC_TUFTS = "tufts";
	private static final String SFDC_BCBSFL = "bcbsfl";
	private static final String SFDC_KAISER_HI = "kaiser hi";
	private static final String SFDC_BCBSMN = "bcbsmn";
	private static final String SFDC_KAISER_DC_MD_VA = "kaiser dc/md/va";
	private static final String SFDC_EMPIRE = "empire";
	private static final String SFDC_ANTHEM = "anthem";
	private static final String SFDC_HARVARD_PILGRIM = "harvard pilgrim";
	private static final String SFDC_LIFE = "life";
	private static final String SFDC_DISABILITY = "disability";
	private static final String SFDC_HIGHMARK = "highmark";

	private final ExchangeDao exchangeDao;

	private final RealmPlanYearService realmPlanYearService;

	private final StrategySyncService strategySyncService;

	private final CompanyService companyService;
	
	private final ProspectCompanyService prospectCompanyService;

	private final ProcessStatusService processStatusService;

	private final BandCodesService bandCodesService;
	
	private final StrategyDataDao strategyDataDao;

	private final CompanyBandCodesDao companyBandCodesDao;
	
	private final ProspectStrategySyncService prospectStrategySyncService;
	
	private final CompanyDao companyDao;

	private final NaicsCodeValidator naicsCodeValidator;

	private final BenefitsBundleService benefitsBundleService;

	private static final List<String> LIFE_DISABILITY_CARRIER_DESC = List.of("Disability", "Life");

	@Override
	public List<ExchangeCarrierDto> getExchangeCarriers(String companyCode, BenExchngEnums benExchange) {
		ProspectInfoResponse prospectInfoResponse = prospectCompanyService.getProspectBasicDetails(companyCode, benExchange);
		Date benStartDate = Utils.convertStringToDate(prospectInfoResponse.getBenStartDate(),
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
		Map<Long, List<ExchangeCarrierDetailsDto>> exchangeCarriers = exchangeDao
		.getExchangeCarriers(companyCode, prospectInfoResponse.getHqState(), prospectInfoResponse.getZipCode(),
				benStartDate)
		.stream().collect(Collectors.groupingBy(ExchangeCarrierDetailsDto::getRealmId));
		return buildExchangeCarriers(companyCode, exchangeCarriers);
	}

	@Override
	public List<ExchangeBandsDto> getExchangeBands(String companyCode, BenExchngEnums benExchange) {
		ProspectInfoResponse prospectInfoResponse = prospectCompanyService.getProspectBasicDetails(companyCode, benExchange);
		Date benStartDate = Utils.convertStringToDate(prospectInfoResponse.getBenStartDate(),
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
		Map<Long, List<ExchangeCarrierBandDto>> exchangeBandsDtos = exchangeDao
				.getExchangeCarriersBands(companyCode, benStartDate)
				.stream().collect(Collectors.groupingBy(ExchangeCarrierBandDto::getRealmId));
		return buildExchangeBandsDtos(exchangeBandsDtos);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<ExchangeBandsDto> saveExchangeBands(List<ExchangeBandsDto> exchangeBands, String companyCode) {
		boolean isPlYrChangeEnabled = AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled();
		boolean usingQueuedStrategySync = AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled();
		exchangeBands.forEach(exchangeBand -> {
			BenExchngEnums exchange = BenExchngEnums.getByBenExchange(exchangeBand.getExchangeId());
			boolean isTrinetOMSExchange = TRINET_OMS.equals(exchange);
			String omsOffering = exchangeBand.getOmsOffering() != null ? exchangeBand.getOmsOffering().name() : null;
			if (isTrinetOMSExchange && omsOffering == null || (!isTrinetOMSExchange && omsOffering != null)) {
				String exceptionMessage = isTrinetOMSExchange ? "OMS Offering should not be null for OMS exchange." : "OMS Offering should be null for non-OMS exchange.";
				throw new InvalidOmsOfferingException(exceptionMessage);
			}
			List<CarrierBand> carrierBands = exchangeBand.getBands();
			
			if (exchangeBand.isBenefitsQuarterException() && carrierBands.size() > 1) {
				throw new BSSBadDataException(String.format("Prospect %s should not have more than one carrier band when it is a quarter exception.",companyCode));
			}

			if (BSSApplicationConstants.CUSTOM_BUNDLE_NAME.equalsIgnoreCase(exchangeBand.getBundleId())) {
				exchangeBand.setBundleId(String.valueOf(BSSApplicationConstants.CUSTOM_BUNDLE_ID));
			}

			Long bundleId = StringUtils.isNotBlank(exchangeBand.getBundleId())
					? Long.valueOf(exchangeBand.getBundleId())
					: null;

			Integer bssNaicsCode = StringUtils.isNotBlank(exchangeBand.getNaicsCode())
					? Integer.valueOf(exchangeBand.getNaicsCode())
					: null;

			Date oldBandEffectiveDate = getOldBandEffectiveDate(companyCode, exchangeBand, isPlYrChangeEnabled,
					exchange);
			deleteBandsForQuarterException(exchangeBand, companyCode);
			List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = realmPlanYearService
					.findByRealmId(exchange.getId());

			saveExchangeBands(companyCode, omsOffering, bundleId, realmPlanYearDetailsDtos, carrierBands, exchangeBand.getRiskType(), bssNaicsCode);

			createStrategySyncEvent(companyCode, isPlYrChangeEnabled, usingQueuedStrategySync, exchangeBand, exchange,
					oldBandEffectiveDate, realmPlanYearDetailsDtos);

			if (!usingQueuedStrategySync) {
				strategySyncService.syncStrategiesForCompany(companyCode, exchange, null);
			}
		});
		return exchangeBands;
	}

	private Date getOldBandEffectiveDate(String companyCode, ExchangeBandsDto exchangeBand, boolean isPlYrChangeEnabled,
	                                     BenExchngEnums exchange) {
		if ((exchangeBand.isBenefitsQuarterException() || exchangeBand.isOldBenefitsQuarterException())
				&& isPlYrChangeEnabled) {
			if (exchangeBand.getOldQuarterEffectiveDate() != null) {
				return exchangeBand.getOldQuarterEffectiveDate();
			} else {
				return companyBandCodesDao.getProspectBandEffDate(companyCode, exchange.getId());
			}
		}
		return null;
	}

	private void saveExchangeBands(String companyCode, String omsOffering, Long bundleId,
			List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos, List<CarrierBand> carrierBands,
			RiskTypeEnum riskType, Integer bssNaicsCode) {
		carrierBands.forEach(carrierband -> {
			// find realm plan year id based on effective date
			Date bandEffectiveDate = carrierband.getEffectiveDate();

			List<CarrierBandDetails> transformedCarrierBandDetails = new ArrayList<>();
			for (CarrierBandDetails carrierBandDetails : carrierband.getCarrierBands()) {
				transformedCarrierBandDetails.addAll(transformCarrier(carrierBandDetails));
			}
			carrierband.setCarrierBands(transformedCarrierBandDetails);
			Optional<RealmPlanYearDetailsDto> realmPlanYearDetailsDtoOpt = realmPlanYearDetailsDtos.stream()
					.filter(realmPlanYearDetailsDto -> DateUtils.isDateWithinRange(bandEffectiveDate,
							realmPlanYearDetailsDto.getStartDate(), realmPlanYearDetailsDto.getEndDate()))
					.findFirst();
			if (realmPlanYearDetailsDtoOpt.isPresent()) {
				RealmPlanYearDetailsDto realmPlanYearDetailsDto = realmPlanYearDetailsDtoOpt.get();
				Company foundCompany = companyService.findCompanyBy(companyCode, realmPlanYearDetailsDto.getId());
				if (foundCompany != null && !Objects.equals(foundCompany.getBundleId(), bundleId)) {
					prospectStrategySyncService.resetStrategiesBy(foundCompany, realmPlanYearDetailsDto.getId());
				}
				// create or update company
				long companyId = companyService.createUpdateCompany(foundCompany, companyCode,
						realmPlanYearDetailsDto.getId(), omsOffering, bundleId, riskType, bssNaicsCode);

				carrierband.setCompanyId(companyId);
				carrierband.setOeQuarter(realmPlanYearDetailsDto.getQuarter());

				// Only save band codes if riskType is not DIFFERENTIALS
				if (!Objects.equals(riskType, RiskTypeEnum.DIFFERENTIALS)) {
					bandCodesService.save(carrierband, companyId);
				}
			}
		});
	}

	private void createStrategySyncEvent(String companyCode, boolean isPlYrChangeEnabled,
			boolean usingQueuedStrategySync, ExchangeBandsDto exchangeBand, BenExchngEnums exchange,
			Date oldBandEffectiveDate, List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos) {
		CarrierBand primaryCarrierBand ;
		if (!exchangeBand.isBenefitsQuarterException()) {
			primaryCarrierBand = findPrimaryCarrierBand(realmPlanYearDetailsDtos, companyCode, exchange,
					exchangeBand.getBands(), exchangeBand.getBenefitsStartDate());
		} else {
			primaryCarrierBand = exchangeBand.getBands().get(0);
		}

		if (primaryCarrierBand == null) {
			throw new NotFoundException(
					String.format("Primary carrier band is not found for the prospect %s.", companyCode));
		}

		ProcessInfoDto processInfoDto = new ProcessInfoDto();
		boolean isPlanYearChanged = false;

		if (isPlYrChangeEnabled) {
			Date newEffectiveDate = !exchangeBand.isBenefitsQuarterException() ? exchangeBand.getBenefitsStartDate()
					: primaryCarrierBand.getEffectiveDate();
			Date oldEffectiveDate = getOldEffectiveDate(exchangeBand, oldBandEffectiveDate,
					primaryCarrierBand.getEffectiveDate());
			isPlanYearChanged = isPlanYearChanged(companyCode, realmPlanYearDetailsDtos, newEffectiveDate,
					oldEffectiveDate, processInfoDto);
		}
		if (isPlanYearChanged) {
			if (RiskTypeEnum.DIFFERENTIALS.equals(exchangeBand.getRiskType())) {
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
		} else if (usingQueuedStrategySync && RiskTypeEnum.BANDS.equals(exchangeBand.getRiskType())) {
			processStatusService.createBandUpdateProcess(exchange.getId(), companyCode, primaryCarrierBand.getCompanyId());
		}

	}

	private static Date getOldEffectiveDate(ExchangeBandsDto exchangeBand, Date oldBandEffectiveDate,
			Date bandEffectiveDate) {
		if (!exchangeBand.isBenefitsQuarterException()) {
			return exchangeBand.isOldBenefitsQuarterException() && Objects.nonNull(oldBandEffectiveDate)
					? oldBandEffectiveDate
					: Objects.nonNull(exchangeBand.getOldBenefitsStartDate()) ? exchangeBand.getOldBenefitsStartDate()
							: exchangeBand.getBenefitsStartDate();
		}
		if (!exchangeBand.isOldBenefitsQuarterException() && Objects.nonNull(exchangeBand.getOldBenefitsStartDate())) {
			return exchangeBand.getOldBenefitsStartDate();
		}
		return Objects.nonNull(oldBandEffectiveDate) ? oldBandEffectiveDate : bandEffectiveDate;
	}

	private CarrierBand findPrimaryCarrierBand(List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos,
			String companyCode, BenExchngEnums exchange, List<CarrierBand> carrierBands, Date benefitsStartDate) {
		Optional<RealmPlanYearDetailsDto> realmPlanYearDetailsDtoOpt = realmPlanYearDetailsDtos.stream()
				.filter(realmPlanYearDetailsDto -> DateUtils.isDateWithinRange(benefitsStartDate,
						realmPlanYearDetailsDto.getStartDate(), realmPlanYearDetailsDto.getEndDate()))
				.findFirst();
		if (realmPlanYearDetailsDtoOpt.isPresent()) {
			String oeQuarter = realmPlanYearDetailsDtoOpt.get().getQuarter();
			Company foundCompany = companyDao.findCompanyBy(companyCode, exchange.getBenExchng(), oeQuarter,
					benefitsStartDate);
			if (foundCompany != null) {
				return carrierBands.stream().filter(carrierBand -> carrierBand.getCompanyId() == foundCompany.getId())
						.findFirst().orElse(null);
			}
		}

		return null;
	}

	private boolean isPlanYearChanged(String companyCode, List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos, Date newDate,
			Date oldDate, ProcessInfoDto processDataPlanYrDto) {
		Optional<RealmPlanYearDetailsDto> futureRealmPlanYearDetailsDtoOpt = realmPlanYearDetailsDtos.stream()
				.filter(realmPlanYearDetailsDto -> DateUtils.isDateWithinRange(newDate,
						realmPlanYearDetailsDto.getStartDate(), realmPlanYearDetailsDto.getEndDate()))
				.findFirst();
		Optional<RealmPlanYearDetailsDto> oldRealmPlanYearDetailsDtoOpt = realmPlanYearDetailsDtos.stream()
				.filter(realmPlanYearDetailsDto -> DateUtils.isDateWithinRange(oldDate,
						realmPlanYearDetailsDto.getStartDate(), realmPlanYearDetailsDto.getEndDate()))
				.findFirst();
		if(!futureRealmPlanYearDetailsDtoOpt.isPresent()) {
			throw new PlanYearNotFound();
		}

		if (oldRealmPlanYearDetailsDtoOpt.isPresent()
				&& futureRealmPlanYearDetailsDtoOpt.get().getId() != oldRealmPlanYearDetailsDtoOpt.get().getId()) {
			Company oldCompany = companyService.findCompanyBy(companyCode, oldRealmPlanYearDetailsDtoOpt.get().getId());
			if (null == oldCompany) {
				return false;
			}
			processDataPlanYrDto.setProcessName(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName());
			processDataPlanYrDto.setOldRealmPlanYear(oldRealmPlanYearDetailsDtoOpt.get().getId());
			processDataPlanYrDto.setOldCompanyId(oldCompany.getId());
			processDataPlanYrDto.setExchangeId(futureRealmPlanYearDetailsDtoOpt.get().getRealmId());
			return true;
		}
		return false;
	}

	/**
	 * When a prospect has a quarter exception (such as when the prospect is related to an existing client)
	 * and bands are updated for the prospect, we will delete all the existing bands for the prospect so
	 * that the new bands are the only bands for the prospect.  This will ensure a clean and accurate strategy.
	 * 
	 * @param exchangeBand
	 * @param companyCode
	 */
	private void deleteBandsForQuarterException(ExchangeBandsDto exchangeBand, String companyCode) {
		if( exchangeBand.isBenefitsQuarterException() ) {
			BenExchngEnums exchange = BenExchngEnums.getByBenExchange(exchangeBand.getExchangeId());
			bandCodesService.deleteCompanyBands(companyService.getIdsByCodeAndExchange( companyCode, exchange ));
		}
	}

	/**
	 * Builds list of exchange carriers
	 *
	 * @param companyCode
	 * @param exchangeCarriers Map of Realm, ExchangeCarrierDetailsDto
	 * @return list of ExchangeCarrierDto
	 */
	private List<ExchangeCarrierDto> buildExchangeCarriers(
			String companyCode,
			Map<Long, List<ExchangeCarrierDetailsDto>> exchangeCarriers) {

		return exchangeCarriers.entrySet().stream().map(entry -> {
			BenExchngEnums exchange = BenExchngEnums.getById(entry.getKey());
			List<ExchangeCarrierDetailsDto> carrierDetails = entry.getValue();
			boolean benefitsStartDateValid = true;
			long realmYearId = 0;
			Company company = null;
			try {
				company = prospectCompanyService.getProspectCompanyDetails(companyCode, exchange);
				realmYearId = company.getRealmPlanYearId();
			}
			catch (BSSApplicationException e) {
				benefitsStartDateValid = false;
				log.error("Company details not found for company code {} and exchange {}.", companyCode, exchange.getExchangeName());
			}
			String customBundleCreated = determineCustomBundleCreated(company);
			boolean offerMedical = isMedicalOffered(realmYearId);
			ExchangeCarrierDto dto = ExchangeCarrierDto.builder()
					.exchangeId(exchange.getExchangeId())
					.exchangeName(exchange.getExchangeName())
					.carriers(buildCarriers(carrierDetails,offerMedical))
					.strategyCreated(
							CollectionUtils.isNotEmpty(carrierDetails) ? carrierDetails.get(0).isStrategyCreated()
									: Boolean.FALSE)
					.benefitsStartDateValid(benefitsStartDateValid)
					.customBundleCreated(customBundleCreated)
					.build();
			if(!offerMedical || BenExchngEnums.TRINET_OMS.equals(exchange )) {
				dto.setCarrierSelectionRequired( false );
			} else {
				dto.setCarrierSelectionRequired( ! dto.isStrategyCreated() );
			}
			return dto;
		}).collect(Collectors.toList());
	}

	private String determineCustomBundleCreated(Company company) {
		// If the prospect is a large deal prospect, then check if there is a custom bundle created for the company. If there is, return YES, if not return NO. For non-large deal prospects, return N/A.
		if (company != null && company.getLargeDealProspect() == 1) {
			return benefitsBundleService.getCustomBundleCreatedStatus(company.getCode(), BSSApplicationConstants.CUSTOM);
		}
		return BSSApplicationConstants.NA_STRING;
	}

	/**
	 * Builds list of carrier dtos.  Since this is for medical coverage, the list does not apply for TriNetEleven
	 * and an empty list is returned for that exchange.
	 * 
	 * @param carrierDetails List of ExchangeCarrierDetailsDto
	 * @param offerMedical boolean
	 * @return List of CarrierDto
	 */
	private List<CarrierDto> buildCarriers(List<ExchangeCarrierDetailsDto> carrierDetails, boolean offerMedical) {
		if(!offerMedical) {
				return new ArrayList<>();
		}
		return carrierDetails.stream().map(carrier -> CarrierDto.builder().portfolioId(carrier.getPortfolioId())
				.portfolioName(carrier.getPortfolioName()).build()).collect(Collectors.toList());
	}

	/**
	 * @param realmIdToDtos
	 * @return
	 */
	private List<ExchangeBandsDto> buildExchangeBandsDtos(Map<Long, List<ExchangeCarrierBandDto>> realmIdToDtos) {
		return realmIdToDtos.entrySet().stream().map(entry -> {
			return buildExchangeBandsDto(entry.getKey(), entry.getValue());
		}).sorted(Comparator.comparing(ExchangeBandsDto::getExchangeName)).collect(Collectors.toList());
	}

	/**
	 * @param realmId
	 * @param exchangeCarrierBandDtos
	 * @return
	 */
	private ExchangeBandsDto buildExchangeBandsDto(long realmId, List<ExchangeCarrierBandDto> exchangeCarrierBandDtos) {
		BenExchngEnums benExchngEnums = BenExchngEnums.getById(realmId);
		return ExchangeBandsDto.builder().exchangeId(benExchngEnums.getExchangeId())
				.exchangeName(benExchngEnums.getExchangeName()).bands(buildBands(exchangeCarrierBandDtos))
				.buildExchangeBands();
	}

	/**
	 * @param exchangeCarrierBandDtos
	 * @return
	 */
	private List<CarrierBand> buildBands(List<ExchangeCarrierBandDto> exchangeCarrierBandDtos) {
		List<CarrierBand> result = exchangeCarrierBandDtos.stream().map(this::buildCarrierBand).sorted().collect(Collectors.toList());
		Optional<CarrierBand> firstItem = result.stream().findFirst();				
		if (firstItem.isPresent()) {
			firstItem.get().setBandType(BandTypeEnum.PRIMARY.name().toLowerCase());
		}
		return result;
	}

	/**
	 * @param exchangeCarrierBandDto
	 * @return
	 */
	private CarrierBand buildCarrierBand(ExchangeCarrierBandDto exchangeCarrierBandDto) {
		return CarrierBand.builder().effectiveDate(exchangeCarrierBandDto.getEffectiveDt())
				.companyId(exchangeCarrierBandDto.getCompanyId())
				.carrierBands(buildCarrierBandDetails(exchangeCarrierBandDto)).buildCarrierBand();
	}

	/**
	 * @param exchangeCarrierBandDto
	 * @return
	 */
	private List<CarrierBandDetails> buildCarrierBandDetails(ExchangeCarrierBandDto exchangeCarrierBandDto) {
		Map<String, String> carrierCodeToDescMapping = buildCarrierCodeToDescMapping();
		List<CarrierBandDetails> carrierBandDetails = StreamUtils.zip(exchangeCarrierBandDto.getCarrierCode().stream(),
				exchangeCarrierBandDto.getBandCodeValue().stream(),
				(carrier, bandCodeVal) -> CarrierBandDetails.builder()
						.carrier(getCarrier(carrier, carrierCodeToDescMapping, exchangeCarrierBandDto.getCompanyId()))
						.bandCode(StringUtils.trim(bandCodeVal)).buildCarrierBandDetails())
				.filter(carrierBandDetails1 -> carrierBandDetails1.getCarrier() != null)
				.collect(Collectors.toList());
		List<CarrierBandDetails> lifeAndDisabilitycarrierBandDetails = carrierBandDetails.stream()
				.filter(carrierBandDetail -> LIFE_DISABILITY_CARRIER_DESC.contains(carrierBandDetail.getCarrier()))
				.collect(Collectors.toList());
		carrierBandDetails.removeAll(lifeAndDisabilitycarrierBandDetails);
		Collections.sort(carrierBandDetails, Comparator.comparing(CarrierBandDetails::getCarrier));
		carrierBandDetails.addAll(lifeAndDisabilitycarrierBandDetails);
		return carrierBandDetails;
	}

	private static String getCarrier(String carrier, Map<String, String> carrierCodeToDescMapping, long companyId) {
		String carrierDesc = carrierCodeToDescMapping.get(carrier);
		if (carrierDesc == null) {
			log.warn(carrier + " is not present in carrierCodeToDescMapping for company id: " + companyId);
		}
		return carrierDesc;
	}

	/**
	 * Builds carrier code to desc mapping
	 * 
	 * @return
	 */
	private Map<String, String> buildCarrierCodeToDescMapping() {
		String carrierCodeToDescMappingProperty = BSSMessageConfig
				.getProperty(ApiBssPropertiesConstants.CARRIER_CODE_TO_DESC_MAPPING);
		List<String> carrierCodeToDescMappings = Arrays
				.asList(StringUtils.split(carrierCodeToDescMappingProperty, ";"));
		return carrierCodeToDescMappings.stream().collect(Collectors
				.toMap(carrier -> StringUtils.split(carrier, ",")[0], carrier -> StringUtils.split(carrier, ",")[1]));
	}
	
	public static List<CarrierBandDetails> transformCarrier(CarrierBandDetails sfdcCarrierBand) {
		String sfdcCarrier = Objects.isNull(sfdcCarrierBand.getCarrier()) ? ""
				: sfdcCarrierBand.getCarrier().toLowerCase();
		List<CarrierBandDetails> transformedValue;
		if(StringUtils.isEmpty(StringUtils.trim(sfdcCarrierBand.getBandCode()))) {
			sfdcCarrierBand.setBandCode(StringUtils.SPACE);		
		}
		switch (sfdcCarrier) {
		case SFDC_BCBSNC : {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.BCBSNC_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_BC_ODAHO: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.BCBSID_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_UHC: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.UHC_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_AETNA: {
			CarrierBandDetails aetna = CarrierBandDetails.builder().carrier(BSSApplicationConstants.AETNA_BAND_CARRIER)
					.bandCode(sfdcCarrierBand.getBandCode()).buildCarrierBandDetails();
			CarrierBandDetails aetnaPpo = CarrierBandDetails.builder().carrier(BSSApplicationConstants.AETNA_PPO_BAND_CARRIER)
					.bandCode(sfdcCarrierBand.getBandCode()).buildCarrierBandDetails();
			CarrierBandDetails aetnaHmo = CarrierBandDetails.builder().carrier(BSSApplicationConstants.AETNA_HMO_BAND_CARRIER)
					.bandCode(sfdcCarrierBand.getBandCode()).buildCarrierBandDetails();
			transformedValue = Arrays.asList(aetna, aetnaPpo, aetnaHmo);
			break;
		}
		case SFDC_BSCA: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.BCBSCA_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_KAISER_CA: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.KAISER_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_KAISER_CO_GA: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.KAISERCO_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_KAISER_NW_WA: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.KAISERNW_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_TUFTS: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.TUFFS_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_BCBSFL: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.BCBS_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_KAISER_HI: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.KAISERHI_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_BCBSMN: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.BCBSMN_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_KAISER_DC_MD_VA: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.KAISERMD_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_EMPIRE: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.EMPIRENY_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_ANTHEM: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.EMPIRENY_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_HARVARD_PILGRIM: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.HARVARD_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_LIFE: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.LIFE_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_DISABILITY: {
			sfdcCarrierBand.setCarrier(BSSApplicationConstants.DISABILITY_BAND_CARRIER);
			transformedValue = Arrays.asList(sfdcCarrierBand);
			break;
		}
		case SFDC_HIGHMARK: {			
		     sfdcCarrierBand.setCarrier(BSSApplicationConstants.HIGHMARK_BAND_CARRIER);			
		     transformedValue = Arrays.asList(sfdcCarrierBand);			
		     break;		
		} 
		default:
			throw new RuntimeException("Invalid carrier value : " + sfdcCarrier);
		}
		return transformedValue;
	}

	@Override
	public boolean isMedicalOffered(long realmYearId) {
		Set<String> planTypes = strategyDataDao.getRealmPlanTypes(realmYearId);
		return planTypes.stream().anyMatch(planType -> BSSApplicationConstants.MEDICAL_PLAN_TYPES.contains(planType));
	}
	
}
