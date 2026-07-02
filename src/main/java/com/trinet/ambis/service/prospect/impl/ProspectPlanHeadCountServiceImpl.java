package com.trinet.ambis.service.prospect.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.PortfolioHeadCountDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.BenefitProgramHeadCountPlans;
import com.trinet.ambis.service.model.EmployeeData;
import com.trinet.ambis.service.model.HeadCountBenefitPlan;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.prospect.ProspectPlanHeadCountService;
import com.trinet.ambis.util.BssCoreServiceClient;
import com.trinet.ambis.util.RulesAndConfigsUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author schaudhari
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProspectPlanHeadCountServiceImpl implements ProspectPlanHeadCountService {

	@Autowired
	ProspectCensusService prospectCensusService;
	@Autowired
	EmployeeDataService employeeDataService;
	@Autowired
	BenefitGroupService benefitGroupService;
	@Autowired
	PortfolioHeadCountDataDao portfolioHeadCountDataDao;
	@Autowired
	BssCoreServiceClient bssCoreServiceClient;

	@Override
	public List<BenefitProgramHeadCountPlans> getBenefitProgramHeadCountPlans(Company company, Long strategyId) {
		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts = portfolioHeadCountDataDao
				.getProspectHeadCountPlans(strategyId);
		addMissingHeadCountPlansToPrograms(strategyId, benefitProgramHeadCounts);
		Map<String, String> emplToBenProgMapping = getEmplToBenProgMapping(company, strategyId);

		Set<String> sdiStates = RulesAndConfigsUtils.getSDIStates(company.getRealmPlanYearId());
		List<ProspectCensusResponse> censusRecords = null;
		if (company.isProspectConvertedClient()) {
			censusRecords = bssCoreServiceClient.getCensusByCompanyCode(company.getCode());
		} else {
			censusRecords = prospectCensusService.getProspectCensus(company.getCode());
		}
		List<BenefitProgramHeadCountPlans> benPrgHcPlans = new ArrayList<>();

		for (Entry<String, List<HeadCountBenefitPlan>> bpEntry : benefitProgramHeadCounts.entrySet()) {
			String benProg = bpEntry.getKey();
			BenefitProgramHeadCountPlans benPrgHcPlan = new BenefitProgramHeadCountPlans();
			benPrgHcPlan.setBenefitProgram(benProg);
			benPrgHcPlan.setBenefitPlans(bpEntry.getValue());

			Map<String, Integer> adBenefitPlans = getAdditionalBenefitHC(company, sdiStates, censusRecords,
					emplToBenProgMapping, benProg);
			benPrgHcPlan.setAdBenefitPlans(adBenefitPlans);
			benPrgHcPlans.add(benPrgHcPlan);
		}
		return benPrgHcPlans;
	}

	@Override
	public Map<String, ActiveEligibleEECount> getProspectEligibleEmployeeCount(Company company, long strategyId) {
		Map<String, ActiveEligibleEECount> eligibleEmployeeCount = new HashMap<>();
		boolean isDisabledBundledOn = RulesAndConfigsUtils.isDisabledBundledOn(company.getRealmPlanYear().getId());
		List<BenefitProgramHeadCountPlans> benProgHeadCountPlans = getBenefitProgramHeadCountPlans(company, strategyId);
		for (BenefitProgramHeadCountPlans benefitProgramHeadCountPlan : benProgHeadCountPlans) {
			ActiveEligibleEECount activeEligEECount = new ActiveEligibleEECount();
			activeEligEECount.setBenProg(benefitProgramHeadCountPlan.getBenefitProgram());
			if (isDisabledBundledOn) {
				int primaryHC = benefitProgramHeadCountPlan.getAdBenefitPlans()
						.get(BSSApplicationConstants.PRIMARY_HEADCOUNT_KEY);
				int secondaryHC = benefitProgramHeadCountPlan.getAdBenefitPlans()
						.get(BSSApplicationConstants.SECONDARY_HEADCOUNT_KEY);
				primaryHC = activeEligEECount.getPrimaryHeadCount() + primaryHC;
				secondaryHC = activeEligEECount.getSecondaryHeadCount() + secondaryHC;
				activeEligEECount.setPrimaryHeadCount(primaryHC);
				activeEligEECount.setSecondaryHeadCount(secondaryHC);
			} else {
				int totalHC = benefitProgramHeadCountPlan.getAdBenefitPlans()
						.get(BSSApplicationConstants.TOTAL_HEADCOUNT_KEY);
				totalHC = activeEligEECount.getTotalHeadCount() + totalHC;
				activeEligEECount.setTotalHeadCount(totalHC);
			}
			eligibleEmployeeCount.put(benefitProgramHeadCountPlan.getBenefitProgram(), activeEligEECount);
		}
		return eligibleEmployeeCount;
	}

	protected void addMissingHeadCountPlansToPrograms(Long strategyId,
			Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts) {

		List<BenefitGroup> benefitGroups = benefitGroupService.getBenefitGroupByStrategy(strategyId,
				BSSApplicationConstants.STATUS_ACTIVE);

		Map<String, HeadCountBenefitPlan> distinctHeadCountMap = getDistinctHeadCountPlans(benefitProgramHeadCounts);

		for (BenefitGroup benefitGroup : benefitGroups) {
			String benefitProgram = benefitGroup.getBenefitProgram();

			if (!benefitProgramHeadCounts.containsKey(benefitProgram)) {
				benefitProgramHeadCounts.put(benefitProgram, new ArrayList<>());
			}
			List<HeadCountBenefitPlan> headCountBenefitPlans = benefitProgramHeadCounts.get(benefitProgram);

			for (Entry<String, HeadCountBenefitPlan> headCountEntry : distinctHeadCountMap.entrySet()) {
				String benefitPlan = headCountEntry.getKey();
				boolean bpExists = false;
				for (HeadCountBenefitPlan hb : headCountBenefitPlans) {
					if (hb.getBenefitPlanId().equals(benefitPlan)) {
						bpExists = true;
					}
				}
				if (!bpExists) {
					headCountBenefitPlans.add(headCountEntry.getValue());
				}
			}
		}
	}

	private Map<String, HeadCountBenefitPlan> getDistinctHeadCountPlans(
			Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts) {
		Map<String, HeadCountBenefitPlan> distinctHeadCountMap = new HashMap<>();
		for (List<HeadCountBenefitPlan> headCountBenefitPlanList : benefitProgramHeadCounts.values()) {
			for (HeadCountBenefitPlan headCountBenefitPlan : headCountBenefitPlanList) {

				HeadCountBenefitPlan emptyHeadCountBenefitPlan = new HeadCountBenefitPlan.HeadCountBenefitPlanBuilder()
						.benefitPlanId(headCountBenefitPlan.getBenefitPlanId())
						.planType(headCountBenefitPlan.getPlanType())
						.planCarrierId(headCountBenefitPlan.getPlanCarrierId()).populateZeroCvgLvlHeadCounts(true)
						.build();

				distinctHeadCountMap.put(emptyHeadCountBenefitPlan.getBenefitPlanId(), emptyHeadCountBenefitPlan);
			}
		}
		return distinctHeadCountMap;
	}

	private Map<String, String> getEmplToBenProgMapping(Company company, Long strategyId) {
		Set<EmployeeData> employeeDataRecords = employeeDataService.getEmployeesData(company, strategyId);
		return employeeDataRecords.stream()
				.collect(Collectors.toMap(EmployeeData::getEmplId, EmployeeData::getBenefitProgram));
	}

	private Map<String, Integer> getAdditionalBenefitHC(Company company, Set<String> sdiStates,
			List<ProspectCensusResponse> censusRecords, Map<String, String> emplToBenProgMapping, String benProg) {
		AtomicInteger sdiStateCount = new AtomicInteger();
		AtomicInteger nonSdiStateCount = new AtomicInteger();
		Map<String, Integer> adBenefitPlans = new HashMap<>();
		for (ProspectCensusResponse censusRecord : censusRecords) {
			if (emplToBenProgMapping.get(censusRecord.getEmployeeId()) == null) {
				log.error("No benefit program assigned to employee id :: ", censusRecord.getEmployeeId());
				continue;
			}
			if (emplToBenProgMapping.get(censusRecord.getEmployeeId()).equals(benProg)) {
				if (sdiStates.contains(censusRecord.getState())) {
					sdiStateCount.incrementAndGet();
				} else {
					nonSdiStateCount.incrementAndGet();
				}
			}
		}
		if (sdiStates.contains(company.getHeadQuatersState())) {
			adBenefitPlans.put(BSSApplicationConstants.PRIMARY_HEADCOUNT_KEY, sdiStateCount.get());
			adBenefitPlans.put(BSSApplicationConstants.SECONDARY_HEADCOUNT_KEY, nonSdiStateCount.get());
		} else {
			adBenefitPlans.put(BSSApplicationConstants.PRIMARY_HEADCOUNT_KEY, nonSdiStateCount.get());
			adBenefitPlans.put(BSSApplicationConstants.SECONDARY_HEADCOUNT_KEY, sdiStateCount.get());
		}
		adBenefitPlans.put(BSSApplicationConstants.TOTAL_HEADCOUNT_KEY,
				sdiStateCount.addAndGet(nonSdiStateCount.get()));
		return adBenefitPlans;
	}

}