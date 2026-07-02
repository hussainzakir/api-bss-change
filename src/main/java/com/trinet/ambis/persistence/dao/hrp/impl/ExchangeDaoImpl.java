package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Repository;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.ExchangeDao;
import com.trinet.ambis.persistence.dao.hrp.dto.ExchangeCarrierBandDto;
import com.trinet.ambis.persistence.dao.hrp.dto.ExchangeCarrierDetailsDto;
import com.trinet.ambis.util.DaoUtils;

@Repository
public class ExchangeDaoImpl implements ExchangeDao {

	private static final String GET_EXCHANGE_CARRIERS = "GET_EXCHANGE_CARRIERS";
	private static final String GET_EXCHANGE_CARRIERS_BANDS = "GET_EXCHANGE_CARRIERS_BANDS";
	private static final String COMPANY_CODE = "companyCode";
	private static final String BEN_START_DT = "benStartDt";


	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	/**
	 *
	 */
	@Override
	public List<ExchangeCarrierDetailsDto> getExchangeCarriers(String companyCode, String hqState, String zipCode,
			Date benStartDt) {
		Query query = em.createNamedQuery(GET_EXCHANGE_CARRIERS);
		query.setParameter(COMPANY_CODE, companyCode);
		query.setParameter(BEN_START_DT, benStartDt);
		query.setParameter(BSSQueryConstants.STATE, hqState);
		query.setParameter(BSSQueryConstants.ZIP_CODE, zipCode);
		return mapper(DaoUtils.getResultList(query, GET_EXCHANGE_CARRIERS));
	}

	/**
	 * Maps list of carriers to list of ExchangeCarrierDetailsDto
	 *
	 * @param companyCode
	 * @param benStartDt
	 * @return
	 */
	@Override
	public List<ExchangeCarrierBandDto> getExchangeCarriersBands(String companyCode, Date benStartDt) {
		Query query = em.createNamedQuery(GET_EXCHANGE_CARRIERS_BANDS);
		query.setParameter(COMPANY_CODE, companyCode);
		query.setParameter(BEN_START_DT, benStartDt);
		return carrierBandMapper(DaoUtils.getResultList(query, GET_EXCHANGE_CARRIERS_BANDS));
	}

	private List<ExchangeCarrierDetailsDto> mapper(List<Object[]> carriers) {
		return carriers.stream()
				.map(carrier -> ExchangeCarrierDetailsDto.builder()
						.realmId( ((BigDecimal) carrier[2]).longValue() )
						.portfolioId( carrier[4] == null ? 0 : ((BigDecimal) carrier[4]).longValue() )
						.portfolioName( carrier[5] == null ? "" : carrier[5].toString() )
						.strategyCreated( BooleanUtils.toBoolean( carrier[9].toString() ))
						.build())
				.collect(Collectors.toList());
	}

	private List<ExchangeCarrierBandDto> carrierBandMapper(List<Object[]> carriersBands) {
		return carriersBands.stream()
				.map(carrierBand -> ExchangeCarrierBandDto
						.builder()
						.realmId(((BigDecimal) carrierBand[0]).longValue())
						.effectiveDt((Date) carrierBand[1])
						.companyId(((BigDecimal) carrierBand[2]).longValue())
						.carrierCode(Arrays.asList(((String) carrierBand[3]).split(";")))
						.bandCodeValue(Arrays.asList(((String) carrierBand[4]).split(";")))
						.build())
				.collect(Collectors.toList());
	}

}
