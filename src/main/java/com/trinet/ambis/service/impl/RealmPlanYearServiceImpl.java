package com.trinet.ambis.service.impl;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.QuarterAndPlanYearDto;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.plancompare.dao.hrp.PlanCompareDao;
import com.trinet.ambis.persistence.plancompare.model.PlanYearDetailDto;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.dto.RealmPlanYearDetailsDto;
import com.trinet.ambis.util.CommonUtils;

@Service
public class RealmPlanYearServiceImpl implements RealmPlanYearService {
	
	private static final Logger logger = LoggerFactory.getLogger(PlanCompareServiceImpl.class);
	
	private static final String QUARTER_SY = "SY";
	
	private static final String QUARTER_Q5 = "Q5";
	
	@Autowired
	RealmPlanYearDao realmPlanYearDao;
	
	@Autowired
	private PlanCompareDao planCompareDao;
	

	@Override
	public RealmPlanYear getRealmPlanYear(long id, String quarter, Date companyPlanStartDate) {
		return realmPlanYearDao.findByRealmIdAndOeQuarterAndPlanYearStart(id, quarter,  companyPlanStartDate) ;
	}
	
	@Override
	public RealmPlanYear getMaxRealmPlanYear(long realmId, String quarter) {
		return realmPlanYearDao.getMaxRealmPlanYearByRealmIdAndQuarter(realmId, quarter);		
	}
	
	@Override
	public RealmPlanYear getRealmPlanYearById(long id) {
		return realmPlanYearDao.findById(id) ;
	}

	/**
	 * @param realmPlanYearDao the realmPlanYearDao to set
	 */
	public void setRealmPlanYearDao(RealmPlanYearDao realmPlanYearDao) {
		this.realmPlanYearDao = realmPlanYearDao;
	}

	@Override
	public RealmPlanYear getPreviousRealmPlanYear(String code, long realmPlanYearId) {
		return realmPlanYearDao.findPreviousRealmPlanYearByRealmPlanYearId(code, realmPlanYearId);	
	}
	
	@Override
	public RealmPlanYear getPreviousRealmPlanYear(RealmPlanYear realmPlanYear) {
		return realmPlanYearDao.findPreviousRealmPlanYearByRealmIdAndOeQuarter(realmPlanYear.getId(),realmPlanYear.getRealmId(),realmPlanYear.getOeQuarter());	
	}
	
	@Override
	public RealmPlanYear getCurrentRealmPlanYear(long realmId, String quarter) {
		return realmPlanYearDao.getCurrentRealmPlanYear(realmId, quarter);	
	}

	@Override
	public RealmPlanYear getNextRealmPlanYear(RealmPlanYear realmPlanYear) {
		return realmPlanYearDao.getNextRealmPlanYear(realmPlanYear.getRealmId(),realmPlanYear.getOeQuarter(),realmPlanYear.getPlanYearEnd());	
	}

	@Override
	public RealmPlanYear getLatestRealmPlanYear(long realmId, String quarter, Date companyPlanStartDate) {
		return realmPlanYearDao.findByLatestRealmIdAndOeQuarterAndPlanYearStart(realmId, quarter,companyPlanStartDate) ;
	}

	@Override
	public RealmPlanYear getRealmForCompanyId(long companyId) {
		return realmPlanYearDao.findByCompanyId(companyId);
	}

	@Override
	public List<RealmPlanYear> getRealmPlanYearByIds(Set<Long> realmIds) {
		return realmPlanYearDao.findBy(realmIds);
	}
	public List<QuarterAndPlanYearDto> getOeQuartersAndPlanYearsInfo() {
		List<QuarterAndPlanYearDto> oeQuartersAndPlanYearDtos = new ArrayList<>();
		QuarterAndPlanYearDto quartersAndPlanYearsDto = null;
		List<Object[]> results = realmPlanYearDao.getQuartersAndPlanYearsInfo();
		for (Object[] r : results) {
			Date planYear = (Date) r[3];
			quartersAndPlanYearsDto = QuarterAndPlanYearDto.builder().benExchng((String) r[0]).peoId((String) r[1])
					.oeQuarter((String) r[2])
					.planYearStart(
							CommonUtils.formatDateToString(planYear, BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY))
					.build();
			oeQuartersAndPlanYearDtos.add(quartersAndPlanYearsDto);
		}

		return oeQuartersAndPlanYearDtos;
	}
	
	@Override
	public List<PlanYearDetailDto> findCurrentAndFuturePlanYearsBy(String code, String quarterName) {
		logger.info("COMPANY ID for current year plan: {} ", code);
		String planYearDate = LocalDate.now().format(DateTimeFormatter.ofPattern(BSSApplicationConstants.DATE_FORMAT_DD_MMM_YY));
		return planCompareDao.findPlanYearDetailsBy(quarterName, planYearDate, 0, 1);
	}

	@Override
	public List<RealmPlanYearDetailsDto> findByRealmId(long realmId) {
		List<RealmPlanYear> foundRealmPlanYears = realmPlanYearDao.findByRealmId(realmId);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = foundRealmPlanYears.stream()
				.map(realmPlanYear -> RealmPlanYearDetailsDto.builder().id(realmPlanYear.getId())
						.realmId(realmPlanYear.getRealmId()).quarter(realmPlanYear.getOeQuarter())
						.startDate(realmPlanYear.getPlanYearStart()).endDate(realmPlanYear.getPlanYearEnd()).build())
				.collect(Collectors.toList());
		applyCriterias(realmId, realmPlanYearDetailsDtos);
		return realmPlanYearDetailsDtos;
	}
	@Override
	public RealmPlanYear findRealmPlanYearBy(Date benifitStartDate , String quarter) {
		return realmPlanYearDao.findByOeQuarter(benifitStartDate, quarter);
	}

	@Override
	public RiskTypeEnum getRenewalRiskTypeForLatestPlanYearInQuarter(String quarter) {
		RealmPlanYear rpy = realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter);
		return RulesAndConfigsUtils.getRenewalRiskType(rpy.getId());
	}

	private void applyCriterias(long realmId, List<RealmPlanYearDetailsDto> realmPlanYearsDetailsDtos) {
		applyTrinetIICriteria(realmId, realmPlanYearsDetailsDtos);
		applyTrinetIIICriteria(realmId, realmPlanYearsDetailsDtos);
	}

	private void applyTrinetIIICriteria(long realmId, List<RealmPlanYearDetailsDto> realmPlanYearsDetailsDtos) {
		if (realmId == BenExchngEnums.TRINET_III.getId()) {
			List<RealmPlanYearDetailsDto> filteredRealmPlanYearsDetailsDtos = realmPlanYearsDetailsDtos.stream().filter(
					realmPlanYearsDetailsDto -> realmPlanYearsDetailsDto.getQuarter().equalsIgnoreCase(QUARTER_Q5))
					.collect(Collectors.toList());
			realmPlanYearsDetailsDtos.removeAll(filteredRealmPlanYearsDetailsDtos);
			Map<Long, RealmPlanYearDetailsDto> realmPlanYearsDetailsDtosByQuarter = realmPlanYearsDetailsDtos.stream()
					.collect(Collectors.toMap(plan -> plan.getId(), Function.identity()));
			Map<Long, Long> currentNextQuarterInfo = buildCurrentNextQuarterInfo(
					realmPlanYearsDetailsDtosByQuarter.keySet().stream().collect(Collectors.toList()));
			realmPlanYearsDetailsDtos.forEach(realmPlanYearsDetailsDto -> {
				if (currentNextQuarterInfo.containsKey(realmPlanYearsDetailsDto.getId())) {
					realmPlanYearsDetailsDto.setEndDate(DateUtils.addDays(
							realmPlanYearsDetailsDtosByQuarter
									.get(currentNextQuarterInfo.get(realmPlanYearsDetailsDto.getId())).getStartDate(),
							-1));
				}
			});
		}
	}

	private void applyTrinetIICriteria(long realmId, List<RealmPlanYearDetailsDto> realmPlanYearsDetailsDtos) {
		if (realmId == BenExchngEnums.TRINET_II.getId()) {
			List<RealmPlanYearDetailsDto> filteredRealmPlanYearsDetailsDtos = realmPlanYearsDetailsDtos.stream().filter(
					realmPlanYearsDetailsDto -> !realmPlanYearsDetailsDto.getQuarter().equalsIgnoreCase(QUARTER_SY))
					.collect(Collectors.toList());
			realmPlanYearsDetailsDtos.removeAll(filteredRealmPlanYearsDetailsDtos);
		}
	}
	
	private Map<Long, Long> buildCurrentNextQuarterInfo(List<Long> realmPlanYearIds) {
		Collections.sort(realmPlanYearIds);
		return IntStream.range(0, realmPlanYearIds.size() - 1).boxed().collect(Collectors.toMap(realmPlanYearIds::get,
				i -> i == realmPlanYearIds.size() - 1 ? realmPlanYearIds.get(i) : realmPlanYearIds.get(i + 1)));
	}
}
