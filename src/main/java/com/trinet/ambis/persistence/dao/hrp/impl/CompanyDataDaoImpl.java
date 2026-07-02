/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.CompanyDataDao;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyDetailsDto;
import com.trinet.ambis.rest.controllers.dto.BundleSelectionDetailsRequest.ExchangeDates;
import com.trinet.ambis.service.model.BundleSelectionDetailsDto;
import com.trinet.ambis.service.model.CompanyRealmData;
import com.trinet.ambis.util.DaoUtils;

/**
 * @author rvutukuri
 *
 */
public class CompanyDataDaoImpl implements CompanyDataDao {

	private static final Logger logger = LoggerFactory.getLogger(CompanyDataDaoImpl.class);

	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	@Override
	public Set<String> getRegionsByCompanyId(Long companyId) {
		Query query = em.createNamedQuery("REGIONS_BY_COMPANY_ID");
		query.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
		Set<String> resultSet = new HashSet<>();
		resultSet.addAll(DaoUtils.getResultStringList(query, "REGIONS_BY_COMPANY_ID"));
		return resultSet;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int insertUpdateCompanyRegions(Long companyId, Set<String> regions) {
		// deleting the band codes before inserting.
		Query dquery = em.createNamedQuery("DELETE_COMPANY_REGIONS");
		dquery.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
		DaoUtils.executeUpdate(dquery, "DELETE_COMPANY_REGIONS");

		Query query = em.createNamedQuery("INSERT_COMPANY_REGIONS");
		int result = 0;
		for (String region : regions) {
			query.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
			query.setParameter("region", region);
			int num = DaoUtils.executeUpdate(query, "INSERT_COMPANY_REGIONS");
			result = result + num;
		}
		return result;
	}

	@Override
	public List<CompanyRealmData> getAvailableCompanyRealms(String companyCode, boolean isRenewalCompany) {
		List<CompanyRealmData> returnList = new ArrayList<>();
		Query query = em.createNamedQuery("GET_COMPANY_REALM_YEARS");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);

		List<Object[]> results = DaoUtils.getResultList(query, "GET_COMPANY_REALM_YEARS");

		for (Object[] result : results) {
			if (result[0] != null) {
				CompanyRealmData companyRealmData = new CompanyRealmData();
				companyRealmData.setCode(companyCode);
				companyRealmData.setRenewalCompany(isRenewalCompany);
				companyRealmData.setRecordType((String) result[0]);
				companyRealmData.setCompanyId(((BigDecimal) result[1]).longValue());
				companyRealmData.setRealmYearId(((BigDecimal) result[2]).longValue());
				companyRealmData.setProduct((String) result[3]);
				companyRealmData.setOeQuarter((String) result[4]);
				companyRealmData.setPlanYearStartDate((Date) result[5]);
				companyRealmData.setPlanYearEndDate((Date) result[6]);
				returnList.add(companyRealmData);
			}
		}
		return returnList;
	}

	// this setter is required for junit test.
	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int updateAcaLargeEmplr(Long companyId, boolean acaLargeEmplr) {
		Query dquery = em.createNamedQuery("UPDATE_COMPANY_ACA_LARGE_EMPLR");
		dquery.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
		dquery.setParameter(BSSQueryConstants.ACA_LARGE_EMPLR, acaLargeEmplr);
		return DaoUtils.executeUpdate(dquery, "UPDATE_COMPANY_ACA_LARGE_EMPLR");
	}
	
	@Override
	public List<BundleSelectionDetailsDto> getBundleSelectionDetails(String companyCode, List<ExchangeDates> exchangeDatePairs) {
		if (CollectionUtils.isEmpty(exchangeDatePairs)) {
			return Collections.emptyList();
		}
		// Build dynamic SQL with OR conditions for each exchange/date pair
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT CO.ID AS COMPANY_ID, CO.REALM_YEAR_ID, CO.CODE AS COMPANY_CODE, ")
		   .append("XR.BEN_EXCHNG AS EXCHANGE ")
		   .append("FROM XBSS_COMPANY CO ")
		   .append("JOIN XBSS_REALM_PLAN_YEAR RPY ON CO.REALM_YEAR_ID = RPY.ID ")
		   .append("JOIN XBSS_REALM XR ON RPY.REALM_ID = XR.ID ")
		   .append("WHERE CO.CODE = :companyCode AND (");

		for (int i = 0; i < exchangeDatePairs.size(); i++) {
			if (i > 0) {
				sql.append(" OR ");
			}
			sql.append("(XR.BEN_EXCHNG = :exchange").append(i)
			   .append(" AND (")
			   .append("(RPY.REALM_ID != 3 AND :benStartDt").append(i).append(" BETWEEN RPY.PLAN_YEAR_START AND RPY.PLAN_YEAR_END)")
			   .append(" OR ")
			   .append("(RPY.REALM_ID = 3 AND RPY.PLAN_YEAR_START = :benStartDt").append(i).append(")")
			   .append("))");
		}
		sql.append(")");

		Query q = em.createNativeQuery(sql.toString());
		q.setParameter("companyCode", companyCode);
		for (int i = 0; i < exchangeDatePairs.size(); i++) {
			ExchangeDates pair = exchangeDatePairs.get(i);
			q.setParameter("exchange" + i, pair.getExchange());
			q.setParameter("benStartDt" + i, java.sql.Date.valueOf(pair.getEffectiveDate()));
		}
		@SuppressWarnings("unchecked")
		List<Object[]> queryResults = q.getResultList();

		if (CollectionUtils.isEmpty(queryResults)) {
			return Collections.emptyList();
		}
		List<BundleSelectionDetailsDto> resultList = new ArrayList<>();
		for (Object[] row : queryResults) {
			Long companyId = ((Number) row[0]).longValue();
			String exchange = (String) row[3];

			BenExchngEnums exchangeEnum = BenExchngEnums.getByBenExchange(exchange);
			String exchangeId = exchangeEnum != null ? exchangeEnum.getExchangeId() : null;

			resultList.add(BundleSelectionDetailsDto.builder()
					.companyId(companyId)
					.exchangeId(exchangeId)
					.build());
		}
		return resultList;
	}

	@Override
	public Map<Long, CompanyStrategyDetailsDto> getCompanyStrategyDetails(String companyCode) {
		Query q = em.createNamedQuery("getCompanyStrategyDetails");
		q.setParameter("companyCode", companyCode);
		List<Object[]> results = DaoUtils.getResultList(q, "getCompanyStrategyDetails");
		return results.stream().map(result -> {
			String allStategyIdsResult = (String) result[1];
			Set<Long> allStategyIds = StringUtils.isNotEmpty(allStategyIdsResult)
					? Stream.of((allStategyIdsResult).split(",")).map(Long::valueOf).collect(Collectors.toSet())
					: Collections.emptySet();
			return CompanyStrategyDetailsDto.builder().companyId(((BigDecimal) result[0]).longValue())
					.allStrategyIds(allStategyIds).realmPlanYearId(Long.parseLong((String) result[2])).build();
		}).collect(Collectors.toMap(CompanyStrategyDetailsDto::getCompanyId, Function.identity()));
	}

	@Override
	public CompanyDetailsDto getCompanyDetailsById(Long companyId) {
		Query q = em.createNamedQuery("GET_COMPANY_DETAILS_BY_ID");
		q.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
		List<Object[]> results = DaoUtils.getResultList(q, "GET_COMPANY_DETAILS_BY_ID");

		if (CollectionUtils.isEmpty(results)) {
			return null;
		}

		Object[] row = results.get(0);
		return CompanyDetailsDto.builder()
				.code((String) row[0])
				.planYearStart((Date) row[1])
				.cloneBenpgm((String) row[2])
				.bundleSeq(row[3] != null ? ((Number) row[3]).longValue() : null)
				.oeQuarter((String) row[4])
				.naicsCode(row[5] != null ? ((Number) row[5]).intValue() : null)
				.largeDealProspect(row[6] != null ? ((Number) row[6]).intValue() : null)
				.naicsBundleId(row[7] != null ? ((Number) row[7]).longValue() : null)
				.exchangeId(BenExchngEnums.getByBenExchange((String) row[8]).getExchangeId())
				.build();
	}

}
