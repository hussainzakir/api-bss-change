package com.trinet.ambis.helper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.enums.VendorEnum;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmConfiguration;
import com.trinet.ambis.service.model.MinimumFunding;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author schaudhari
 *
 */
public class CommonServiceHelper {

	private static final Logger logger = LoggerFactory.getLogger(CommonServiceHelper.class);
	
	private CommonServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + CommonServiceHelper.class.getName() + " can not be instantiated.");
	}

	/**
	 * This method is for formatting date into a specific format.
	 * 
	 * @deprecated Code consolidation.  Use {@link CommonUtils#formatStringToDate(String, String)} instead
	 * @param date
	 * @param format
	 * @return
	 */
	@Deprecated
	public static Date formatStringToDate(String date, String format) {
		Date formattedDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			formattedDate = formatter.parse(date);
		} catch (ParseException e) {
			CommonUtils.logExceptions(e, logger, "", "");
		}
		return formattedDate;
	}

	/**
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDateToString(Date date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	/**
	 * Creates a random string whose length is 12 characters. Characters will be
	 * chosen from the set of alpha-numeric characters.
	 * 
	 * @return String
	 */
	public static String randomAlphanumeric() {
		return RandomStringUtils.randomAlphanumeric(12).toUpperCase();
	}

	/**
	 * Method that can be used to serialize any Java value as a String.
	 * 
	 * @param object
	 * @return String
	 */
	public static String objectToJsonString(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		String body = null;

		try {
			body = mapper.writeValueAsString(object);
		} catch (IOException e) {
			logger.debug(e.getMessage());
		}
		logger.info("******* PAYLOAD : {} " , body);

		return body;
	}

	/**
	 * Method that can be used to convert JSON string to object of give type.
	 * 
	 * @param strJson
	 * @param type
	 * @return T
	 */
	public static <T> T jsonToObject(String strJson, Class<T> type) {
		ObjectMapper mapper = new ObjectMapper();
		T dto = null;
		try {
			dto = mapper.readValue(strJson, type);
		} catch (IOException e) {
			logger.debug(e.getMessage());
		}
		return dto;
	}

	/**
	 * Find the value for given key in the given List of RealmConfiguration objects.
	 * Returns null if no key is found.
	 * 
	 * @param configurations
	 * @param key
	 * @return String
	 */
	public static String findRealmConfigurationValueByKey(List<RealmConfiguration> configurations, String key) {
		String value = null;
		for (RealmConfiguration realmConfiguration : configurations) {
			if (key.equals(realmConfiguration.getId().getConfigKey())) {
				value = realmConfiguration.getConfigValue();
			}
		}
		return value;
	}
	
	/**
	 * This method returns all out of region plans (not available in the company's zipcode)
	 * for given plan carriers. 
	 * 
	 * @param company
	 * @param primaryPlanCarriers
	 * @param realmDataDao
	 * @return
	 */
	public static Set<String> getOutOfRegionPlansToExclude(Company company, Set<String> primaryPlanCarriers,
			RealmDataDao realmDataDao) {
		Set<String> outOfRegionBSPlans = null;
		Set<String> medPlanGrps = new HashSet<>(1);
		// Get all BC national plans NOT available for the HQ zip code
		if (primaryPlanCarriers.contains(VendorEnum.BCBSCA.getPortfolioId())) {
			
			boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
			if (BenExchngEnums.TRINET_III.getBenExchng().equals(company.getRealm().getBenExchange())
					&& ! isPickChoose ) {
				outOfRegionBSPlans = realmDataDao.getBSOutOfRegionPlans(company);
			} else {
				medPlanGrps.add(BSSApplicationConstants.BCBSCA_MED_PLAN_GRP);
			}
		}
		if (primaryPlanCarriers.contains(VendorEnum.EMPIRENY.getPortfolioId())) {
			medPlanGrps.add(BSSApplicationConstants.EMPIRE_MED_PLAN_GRP);
		}
		// Get all plans NOT available for the HQ zipcode for given carriers.
		Set<String> carrierOutOfRegionPlans = null;
		if (CollectionUtils.isNotEmpty(medPlanGrps)) {
			carrierOutOfRegionPlans = realmDataDao.getCarrierOutOfRegionPlans(company, medPlanGrps);
		}

		Set<String> outOfRegionPlans = null;
		if (CollectionUtils.isNotEmpty(outOfRegionBSPlans)) {
			outOfRegionPlans = createIfNull(outOfRegionPlans);
			outOfRegionPlans.addAll(outOfRegionBSPlans);
		}
		if (CollectionUtils.isNotEmpty(carrierOutOfRegionPlans)) {
			outOfRegionPlans = createIfNull(outOfRegionPlans);
			outOfRegionPlans.addAll(carrierOutOfRegionPlans);
		}
		return outOfRegionPlans;
	}

	public static <T> Set<T> createIfNull(Set<T> collection) {
		if (CollectionUtils.isEmpty(collection)) {
			collection = new HashSet<>();
		}
		return collection;
	}


	public static MinimumFunding extractMinFundingDetails(String planType, Company company) {
		String[] primaryPlanType = { planType };
		if(PlanTypesEnum.VISION_VOLUNTARY.getName().equals(planType)) {
			primaryPlanType[0] = PlanTypesEnum.VISION.getName();
		} else if (PlanTypesEnum.DENTAL_VOLUNTARY.getName().equals(planType)) {
			primaryPlanType[0] = PlanTypesEnum.DENTAL.getName();
		}
		return company.getMinFundings().stream().filter(e -> e.getPlanType().equals(primaryPlanType[0]))
				.collect(Collectors.toList()).get(0);
	}
	
	public static String getSitusValue(boolean isTexasSitus) {
		return isTexasSitus ? BSSApplicationConstants.SITUS_TX : BSSApplicationConstants.SITUS_FL;
	}

	public static boolean isTriNetCompany(String companyCode) {
		return BSSApplicationConstants.TRINET_COMPANIES.contains(companyCode);
	}
	
	public static boolean isResubmit(final String processName) {
		return ProcessStatusEnum.RESUBMIT_PROCESS.getProcessName().equals(processName)
				|| ProcessStatusEnum.BAND_CODE_RESUBMIT_PROCESS.getProcessName().equals(processName);
	}
}
