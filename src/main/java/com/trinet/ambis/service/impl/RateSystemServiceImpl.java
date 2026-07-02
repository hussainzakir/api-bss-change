package com.trinet.ambis.service.impl;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.RateSystemService;
import com.trinet.ambis.service.model.FlexRateResponse;
import com.trinet.ambis.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.trinet.ambis.common.BSSApplicationConstants.REGIONAL;
import static com.trinet.ambis.util.Constants.DATE_FORMAT;

@Service
@Slf4j
public class RateSystemServiceImpl implements RateSystemService {

	private static final Logger logger = LoggerFactory.getLogger(RateSystemServiceImpl.class);

	@Autowired
	private FlexRateService flexRateService;

	/**
	 * Retrieves the rate_type from the cached FlexRateResponse.
	 * Delegates to {@link FlexRateService#getPlanRatesFromCache(Company, String)}.
	 *
	 * @param company       the company whose plan rates are being queried
	 * @return rate_type from the FlexRateResponse, or null if not present
	 */
	@Override
	public String getRateSystemRateType(Company company) {
		if (RiskTypeEnum.BANDS.equals(company.getRiskType())) {
			return REGIONAL;
		}
		String effectiveDate = Utils.convertStringToLocalDate(company.getPlanStartDate(),DATE_FORMAT).toString();
		logger.info("Fetching rate type for company id: {}, effectiveDate: {}", company.getCode(), effectiveDate);
		FlexRateResponse response = flexRateService.getPlanRatesFromCache(company, effectiveDate);
		return Objects.requireNonNullElse(response.getRateType(), REGIONAL);
	}
}