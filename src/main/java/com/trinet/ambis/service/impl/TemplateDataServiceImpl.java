package com.trinet.ambis.service.impl;

import static com.trinet.ambis.util.Constants.LIFE_CMTR_PLANS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.PackageTypeEnums;
import com.trinet.ambis.persistence.dao.hrp.CompanyOptionsDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.DisabilityOptionService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.TemplateDataService;
import com.trinet.ambis.service.model.AdditionalBenefitOfferPlan;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitOfferPackage;
import com.trinet.ambis.service.model.NewCompanyOptions;
import com.trinet.ambis.service.model.OptionsNew;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanTypeDescription;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@Service
public class TemplateDataServiceImpl implements TemplateDataService {
	private static final Logger logger = LoggerFactory.getLogger(TemplateDataServiceImpl.class);

	@Autowired
	CompanyOptionsDao companyOptionsDao;

	@Autowired
	StrategyDataDao strategyDataDao;

	@Autowired
	CompanyService companyService;

	@Autowired
	RealmDataDao realmDataDao;
	
	@Autowired
	DisabilityOptionService disabilityOptionService;
	
	@Autowired
	BenefitOfferExceptionService benOfferExceptionService;
	
	@Autowired
	private PortfolioService portfolioService;
	
	@Override
	@Transactional
	public NewCompanyOptions getNewCompanyOptions(Company company) {
		long startTime = System.currentTimeMillis();

		String industryType = company.getIndustry().getIndustryType().name();
		String vertical = company.getIndustry().getIndustryType().getType();
		String state = company.getHeadQuatersState();
		NewCompanyOptions newCompanyOptions = new NewCompanyOptions();
		Set<OptionsNew> optionsDto = new TreeSet<>();
		long realmYearId = company.getRealmPlanYearId();

		Map<String, Boolean> packageTypes = companyOptionsDao.getPackageTypes(realmYearId, industryType, state);

		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );

		Map<String, Map<String, List<Long>>> portfolioMap = companyOptionsDao.getDefaultPortfolios(realmYearId,
				industryType, state, isPickChoose );

		Map<String, Set<PlanCarrier>> planCarrierMap = portfolioService.findPrimaryPlanCarriers(company);

		for (String pkgType : packageTypes.keySet()) {
			Map<String, List<Long>> portfoliosByPlanType = portfolioMap.get(pkgType);
			if (BenExchngEnums.TRINET_III.getBenExchng().equals(company.getRealm().getBenExchange())
					&& ! isPickChoose ) {
				addMissingChildCarriers(planCarrierMap, portfoliosByPlanType);
			}
		}

		Map<String, Map<String, List<String>>> additionalPlans = companyOptionsDao
				.getTemplateAdditionalPlans(realmYearId, industryType, state);
		Map<String, PlanTypeDescription> planTypeDescMap = strategyDataDao
				.getPlanTypeDescriptions(company.getRealmPlanYearId());
		
		Map<String, Boolean> benOfferExceptions = benOfferExceptionService.findApplicableBy(company);

		// Looping through package types CON, INT, PRM
		for (String pkgType : packageTypes.keySet()) {
			List<BenefitOfferPackage> benefitOfferPkgs = new ArrayList<>();
			List<AdditionalBenefitOfferPlan> additionalBenefitPlans = new ArrayList<>();
			OptionsNew option = new OptionsNew();
			option.setId(pkgType);
			option.setName(PackageTypeEnums.getName(pkgType));

			// For Medical, Dental, Vision
			Map<String, List<Long>> portfoliosByPlanType = portfolioMap.get(pkgType);
			for (String planType : portfoliosByPlanType.keySet()) {
				if (Constants.primaryPlanTypeList.contains(planType) && planTypeDescMap.get(planType) != null) {
					BenefitOfferPackage benefitOfferPkg = new BenefitOfferPackage();
					benefitOfferPkg.setType(planTypeDescMap.get(planType).getType());
					benefitOfferPkg.setPlanPackageId(PackageTypeEnums.valueOf(option.getId()).getType());
					benefitOfferPkg.setPlanCarrierIds(new HashSet<>(portfoliosByPlanType.get(planType)));
					benefitOfferPkgs.add(benefitOfferPkg);
				}
			}
			List<String> disabilityPlans = new ArrayList<>();
			// For Additional Types
			Map<String, List<String>> benefitPlansByPlanType = additionalPlans.get(pkgType);
			for (String planType : benefitPlansByPlanType.keySet()) {
				if (LIFE_CMTR_PLANS.contains(planType)) {
					AdditionalBenefitOfferPlan plan = new AdditionalBenefitOfferPlan();
					plan.setType(planTypeDescMap.get(planType).getType());
					plan.setBenefitPlan(benefitPlansByPlanType.get(planType).get(0));
					additionalBenefitPlans.add(plan);
				} else {
					disabilityPlans.add(benefitPlansByPlanType.get(planType).get(0));
				}
			}
			// converting STD & LTD plans to options
			AdditionalBenefitPlan dPlan = disabilityOptionService.getDisabilityOptionByPlans(disabilityPlans, company,
					false);
			if (null != dPlan) {
				AdditionalBenefitOfferPlan plan = new AdditionalBenefitOfferPlan();
				plan.setType("DISABILITY");
				plan.setBenefitPlan(dPlan.getId());
				additionalBenefitPlans.add(plan);
			}
			applyBenOfferException(benefitOfferPkgs, additionalBenefitPlans, benOfferExceptions);
			option.setBenefitOfferPackages(benefitOfferPkgs);
			option.setAdditionalBenefitOfferPlans(additionalBenefitPlans);
			optionsDto.add(option);
		}
		newCompanyOptions.setOptions(optionsDto);
		newCompanyOptions.setVertical(vertical);

		long endTime = System.currentTimeMillis();
		logger.info("getNewCompanyOptions() took " + (endTime - startTime) + " ms");

		return newCompanyOptions;
	}
	
	/**
	 * 
	 * @param planCarrierMap
	 * @param selectedPlancarriers
	 */
	public  void addMissingChildCarriers(Map<String, Set<PlanCarrier>> planCarrierMap,
			Map<String, List<Long>> selectedPlancarriers) {
		Set<PlanCarrier> medicalCarriers = planCarrierMap.get(BSSApplicationConstants.MEDICAL);
		Map<Long, Set<Long>> planCarriersByParentPortfolio = new HashMap<>();
		for (PlanCarrier pc : medicalCarriers) {
			if (null != pc.getParentId()) {
				pc.getParentId().stream().filter(Objects::nonNull).map(Long::valueOf)
						.forEach(parentId -> planCarriersByParentPortfolio
								.computeIfAbsent(parentId, k -> new HashSet<>()).add((long) pc.getId()));
			}
		}
		List<Long> selectedMedicalCarriers = selectedPlancarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		Set<Long> missingChildCarriers = new HashSet<>();
		for (Long sc : selectedMedicalCarriers) {
			for (Long parentPortfolio : planCarriersByParentPortfolio.keySet()) {
				if (planCarriersByParentPortfolio.get(parentPortfolio).contains(sc) || parentPortfolio.equals(sc)) {
					missingChildCarriers.addAll(planCarriersByParentPortfolio.get(parentPortfolio));
					missingChildCarriers.add(parentPortfolio);
				}
			}
		}
		selectedMedicalCarriers.addAll(missingChildCarriers);
	}
	

	private void applyBenOfferException(List<BenefitOfferPackage> benefitOfferPkgs,
			List<AdditionalBenefitOfferPlan> additionalBenefitPlans, Map<String, Boolean> benOfferExceptions) {
		benefitOfferPkgs.removeIf(e -> CommonUtils.isBenOfferExceptionAvailable(benOfferExceptions, e.getType()));
		additionalBenefitPlans.removeIf(e -> CommonUtils.isBenOfferExceptionAvailable(benOfferExceptions, e.getType()));
	}

	/**
	 * @param companyOptionsDao
	 *            the companyOptionsDao to set
	 */
	public void setCompanyOptionsDao(CompanyOptionsDao companyOptionsDao) {
		this.companyOptionsDao = companyOptionsDao;
	}

	/**
	 * @param strategyDataDao
	 *            the strategyDataDao to set
	 */
	public void setStrategyDataDao(StrategyDataDao strategyDataDao) {
		this.strategyDataDao = strategyDataDao;
	}

	/**
	 * @param companyService
	 *            the companyService to set
	 */
	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	/**
	 * @return the realmDataDao
	 */
	public RealmDataDao getRealmDataDao() {
		return realmDataDao;
	}

	/**
	 * @param realmDataDao
	 *            the realmDataDao to set
	 */
	public void setRealmDataDao(RealmDataDao realmDataDao) {
		this.realmDataDao = realmDataDao;
	}

}
