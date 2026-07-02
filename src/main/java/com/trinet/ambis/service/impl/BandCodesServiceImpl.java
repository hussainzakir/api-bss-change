package com.trinet.ambis.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.NaicsBandCodeRepository;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.trinet.ambis.persistence.dao.hrp.BandCodesRepository;
import com.trinet.ambis.persistence.model.BandCodes;
import com.trinet.ambis.persistence.model.embeddable.BandCodesUK;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.CarrierBand;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.CarrierBand.CarrierBandDetails;
import com.trinet.ambis.service.BandCodesService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BandCodesServiceImpl implements BandCodesService {

	private final BandCodesRepository bandCodesRepository;

    private final NaicsBandCodeRepository naicsBandCodeRepository;

	@Override
	public void save(CarrierBand carrierBand, long companyId) {
		List<CarrierBandDetails> carrierBands = carrierBand.getCarrierBands();
		List<BandCodes> bandCodes = carrierBands.stream().map(band -> {
			BandCodesUK bandCodesUK = BandCodesUK.builder().companyId(companyId).bandCodeType(band.getCarrier())
					.effectiveDt(carrierBand.getEffectiveDate()).build();
			return BandCodes.builder().bandCodesUK(bandCodesUK).bandCodeVal(band.getBandCode()).build();
		}).collect(Collectors.toList());
		bandCodesRepository.saveAll(bandCodes);
	}

	@Override
	public void deleteCompanyBands(List<Long> companyIds) {
		if(CollectionUtils.isEmpty(companyIds)) {
			//nothing to delete
		} else {
			bandCodesRepository.deleteWhereCompanyIdIn(companyIds);
		}
	}

    @Override
    public String getBandCodeByType(String naicsCode, Date effDate, String bandCodeType, BenExchngEnums exchange) {
        if (exchange.getExchangeId().equalsIgnoreCase(BenExchngEnums.TRINET_IV.getExchangeId())) {
            Map<String, String> appRulesConfigsList = AppRulesAndConfigsUtils.getTNIVLifeAndDisabilityBandCode();
            return getBandCode(bandCodeType, appRulesConfigsList.get(BSSApplicationConstants.LIFE),
                    appRulesConfigsList.get(BSSApplicationConstants.DISABILITY));
        }
        return naicsBandCodeRepository.findActiveByNaicsCodeAndDate(naicsCode, effDate)
                .map(bandCode -> getBandCode(bandCodeType, bandCode.getLifeBandCode(), bandCode.getDisabilityBandCode()))
                .orElse(null);
    }

    private String getBandCode(String bandCodeType, String lifeBandCode, String disabilityBandCode) {
        if (bandCodeType == null) {
            return null;
        }
        if (BSSApplicationConstants.LIFE.equalsIgnoreCase(bandCodeType)) {
            return lifeBandCode;
        }
        if (BSSApplicationConstants.DISABILITY.equalsIgnoreCase(bandCodeType)) {
            return disabilityBandCode;
        }
        return null;
    }
}
