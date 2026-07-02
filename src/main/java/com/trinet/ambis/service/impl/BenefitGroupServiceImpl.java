package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSRateType;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.CacheObjectLevelEnum;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.BenefitGroupServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.BenefitStrategyGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.GroupCovrgHeadCountDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.GroupHeadCount;
import com.trinet.ambis.persistence.model.GroupRate;
import com.trinet.ambis.persistence.model.GroupRatePK;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyFundBsuppPlanTypeId;
import com.trinet.ambis.persistence.model.StrategyFundBsuppPlanTypes;
import com.trinet.ambis.persistence.model.StrategyFundingDetail;
import com.trinet.ambis.persistence.model.StrategyFundingDetailId;
import com.trinet.ambis.persistence.model.StrategyFundingFlatMax;
import com.trinet.ambis.persistence.model.StrategyFundingModel;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCountId;
import com.trinet.ambis.persistence.sp.GetNextEligRulesId;
import com.trinet.ambis.persistence.sp.NextBenProgram;
import com.trinet.ambis.persistence.sp.NextRateTblID;
import com.trinet.ambis.rest.controllers.StrategyController;
import com.trinet.ambis.service.BenefitClassService;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.GroupRuleService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.RealmWaitPeriodService;
import com.trinet.ambis.service.StrategyFundingModelService;
import com.trinet.ambis.service.StrategyGroupHeadCountService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.model.BenefitGroupRateMapper;
import com.trinet.ambis.service.model.EmployeeCensusStrategyGroupDetails;
import com.trinet.ambis.service.model.GroupData;
import com.trinet.ambis.service.model.GroupRuleDto;
import com.trinet.ambis.service.prospect.ProspectGroupService;

/**
 * @author rvutukuri
 *
 */
@Service
public class BenefitGroupServiceImpl implements BenefitGroupService {

	@Autowired
	BenefitGroupDao benefitGroupDao;

	@Autowired
	private NextBenProgram nextBenProgram;

	@Autowired
	private NextRateTblID nextRateTblID;

	@Autowired
	GroupCovrgHeadCountDao groupCovrgHeadCountDao;
	
	@Autowired
	GroupRuleService groupRuleService;

	@Autowired
	private GetNextEligRulesId spGetNextEligRulesId;

	@Autowired
	BenefitClassService benefitClassService;
	
	@Autowired
	StrategyGroupService strategyGroupService;
	
	@Autowired
	StrategyGroupHeadCountService strategyGroupHeadCountService;

	@Autowired
	PlanSelectionService planSelectionService;

	@Autowired
	ContributionService contributionService;

	@Autowired
	StrategyFundingModelService strategyFundingModelService;

	@Autowired
	BenefitStrategyGroupDao benefitStrategyGroupDao;

	@Autowired
	ProspectGroupService prospectGroupService;
	
	@Autowired
	StrategyDataDao strategyDataDao;
	
	@Autowired
	StrategyService strategyService;
	
	@Autowired
	RealmWaitPeriodService realmWaitPeriodService;
	
	@Autowired
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Autowired
	CacheService cacheService;
	
	private static final Logger logger = LoggerFactory.getLogger(BenefitGroupServiceImpl.class);
	
	/**
	 * 
	 * @param company
	 */
	public BenefitGroup constructW2Group(Company company, boolean isDefaultGroup) {
		BenefitGroup bg = new BenefitGroup();
		bg.setName("W2");
		bg.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
		bg.setCompanyId(company.getId());
		bg.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		bg.setDefaultGroup(isDefaultGroup);
		bg.setWaitingPeriod(BSSApplicationConstants.WAIT_PERIOD_NONE);
		if (company.isProspectCompany()) {
			bg.setBenefitProgram(BenefitGroupServiceHelper.generateProspectBenProgram());
		} else {
			updateGroupWithPSDetails(bg, company, false);
		}
		return bg;
	}
	
	@Override
	public BenefitGroup updateGroupWithPSDetails(BenefitGroup bg, Company company, boolean prospectConversion) {
		// Set benefit program based on prospect conversion status and group type
		if (prospectConversion && bg.isDefaultGroup() && null != company.getBenefitProgram()) {
			bg.setBenefitProgram(company.getBenefitProgram());
		} else {
			bg.setBenefitProgram(nextBenProgram.execute());
		}
		// Set eligibility details if they are missing
		if (StringUtils.isBlank(bg.getEligRuleId()) || StringUtils.isBlank(bg.getEligConfig1())
				|| (bg.getGroupRate() == null || bg.getGroupRate().isEmpty())) {
			bg.setEligRuleId(spGetNextEligRulesId.execute());
			// Make sure the complete set of rate IDs is present
			this.completeRateTblSet(bg);
			// Set the eligibility configuration value based on the company and group
			bg.setEligConfig1(benefitClassService.generateClassCode(company, bg));
		}

		return bg;
	}
	
	/**
	 * 
	 * @param company
	 */
	public BenefitGroup constructK1Group(Company company) {
		BenefitGroup bg = new BenefitGroup();
		bg.setName(BSSApplicationConstants.CLIENT_K1_GROUP_NAME);
		bg.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
		bg.setCompanyId(company.getId());
		bg.setType(BSSApplicationConstants.K1_GROUP_TYPE);
		bg.setDefaultGroup(false);
		bg.setWaitingPeriod(BSSApplicationConstants.WAIT_PERIOD_NONE);
		if (company.isProspectCompany()) {
			bg.setBenefitProgram(BenefitGroupServiceHelper.generateProspectBenProgram());
		} else {
			updateGroupWithPSDetails(bg, company, false);
		}
		return bg;
	}

    /**
     *
     * @param company
     */
    public BenefitGroup constructMAGroup(Company company) {
        BenefitGroup bg = new BenefitGroup();
        bg.setName(BSSApplicationConstants.CLIENT_MA_GROUP_NAME);
        bg.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
        bg.setCompanyId(company.getId());
        bg.setType(BSSApplicationConstants.STD_GROUP_TYPE);
        bg.setDefaultGroup(false);
        bg.setWaitingPeriod(BSSApplicationConstants.WAIT_PERIOD_NONE);
        if (company.isProspectCompany()) {
            bg.setBenefitProgram(BenefitGroupServiceHelper.generateProspectBenProgram());
        } else {
            updateGroupWithPSDetails(bg, company, false);
        }
        return bg;
    }

	@Override
	public BenefitGroup saveBenefitGroup(BenefitGroup benefitGroup) {
		return benefitGroupDao.saveAndFlush(benefitGroup);
	}

	@Override
	public void deleteBenefitGroup(long id) {
		benefitGroupDao.deleteById(id);
	}

	@Override
	public BenefitGroup getBenefitGroupByCompanyIdAndId(long companyId, long groupId) {
		return benefitGroupDao.findByCompanyIdAndId(companyId, groupId);
	}

	@Override
	public List<BenefitGroup> getAllBenefitGroups(long companyId, String status) {
		return benefitGroupDao.findByCompanyIdAndStatus(companyId, status);
	}

	@Override
	public List<BenefitGroup> getBenefitGroupByStrategy(long strategyId, String status) {
		return benefitGroupDao.getBenefitGroupsByStrategyId(strategyId, status);
	}
	
	@Override
	public List<BenefitGroup> getBenefitGroupByStrategy(long strategyId, List<String> status) {
		return benefitGroupDao.getBenefitGroupsByStrategyId(strategyId, status);
	}

	@Override
	public BenefitGroup getBenefitGroupsByStrategyIdAndGroupId(long strategyId, long groupId, String status) {
		return benefitGroupDao.getBenefitGroupsByStrategyIdAndGroupId(strategyId, groupId, status);
	}

	@Override
	public List<BenefitGroup> saveAll(List<BenefitGroup> benefitGroups) {
		return benefitGroupDao.saveAll(benefitGroups);
	}

	@Override
	public GroupHeadCount getGroupHeadCount(long id) {
		return groupCovrgHeadCountDao.findByGroupId(id);
	}

	@Override
	public void updateBenefitGroupName( Long groupId, String newName ) {
		BenefitGroup group = benefitGroupDao.findById(groupId.longValue());
		resetStrategyCache(group);
        if (group.isK1Group() && !newName.startsWith("K1")) {
            throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_GROUP_NAME_CHANGE_FAIL,
                    BSSHttpStatusConstants.BAD_REQUEST, "", "Name must start with K1", null, null));
        }
		group.setName( newName );
		benefitGroupDao.save( group );
	}

	@Override
	public void updateBenefitGroupName(long strategyId, long groupId, String newName) {
		if(Long.compare(ProspectConstants.PROSPECT_STRATEGY_ID, strategyId) == 0) {
			prospectGroupService.updateGroupName(groupId, newName);
		} else {
			updateBenefitGroupName(groupId, newName);
		}
	}

	@Override
	public void updateBenefitGroupMetaData(String companyCode, Long groupId, Long strategyId, String waitingPeriod,
			boolean isDefaultGroup, long realmPlanYearId) {
		
		List<String> waitPeriods = realmWaitPeriodService.getWaitPeriodCodesForRelamPlanYear(realmPlanYearId);
		if (waitPeriods.contains(waitingPeriod)) {
			List<BenefitGroupStrategy> benefitGroupStrategies = strategyGroupService
					.findByStrategyIdAndStatus(strategyId, BSSApplicationConstants.STATUS_ACTIVE);
			if (CollectionUtils.isNotEmpty(benefitGroupStrategies)) {
				if (isDefaultGroup) {
					refreshDefaultGroupProperties(benefitGroupStrategies, groupId, waitingPeriod);
					benefitStrategyGroupDao.saveAll(benefitGroupStrategies);
				} else {
					refreshNotTheDefaultGroupProperties(benefitGroupStrategies, groupId, waitingPeriod);
				}
			} else {
				throw new BSSApplicationException( new BSSApplicationError( BSSErrorResponseCodes.BSS_STRATEGY_GROUP_INVALID,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, "", "Invalid Strategy Group", null, null ));
			}
		}
		else {
			throw new BSSApplicationException( new BSSApplicationError( BSSErrorResponseCodes.BSS_GROUP_WAIT_PERIOD_INVALID,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Invalid Wait Period", null, null ));
		}
	}

	private void refreshDefaultGroupProperties( List<BenefitGroupStrategy> benefitGroupStrategies,
			Long groupId, String waitingPeriod ) {
		for (BenefitGroupStrategy bgs : benefitGroupStrategies) {
			if (groupId.equals(bgs.getGroupId())) {
				bgs.setWaitingPeriod(waitingPeriod);
				bgs.setDefaultGroup(true);
			} else {
				bgs.setDefaultGroup(false);
			}
		}
	}

	private void refreshNotTheDefaultGroupProperties( List<BenefitGroupStrategy> benefitGroupStrategies,
			Long groupId, String waitingPeriod ) {
		for (BenefitGroupStrategy bgs : benefitGroupStrategies) {
			if (groupId.equals(bgs.getGroupId())) {
				bgs.setWaitingPeriod(waitingPeriod);
				benefitStrategyGroupDao.saveAndFlush(bgs);
			}
		}
	}

	@Override
	public void getBenefitGroupMetaData(Company company, BenefitGroup benefitGroup) {
		benefitGroup.setCompanyId(company.getId());
		// Get benefit program and plan rate id for non-default benefit groups
		if (benefitGroup.isDefaultGroup()) {
			benefitGroup.setBenefitProgram(company.getBenefitProgram());
		} else {
			String benProg = nextBenProgram.execute();
			benefitGroup.setBenefitProgram(benProg);
		}

		String eligRulesId = spGetNextEligRulesId.execute();
		benefitGroup.setEligRuleId(eligRulesId);

		Map<String, String> rateMap = this.generateRateTableId();
		benefitGroup.getGroupRate().addAll(BenefitGroupRateMapper.convertMapToGroupRate(rateMap, benefitGroup));


		logger.info("BEN PROG : {}\t RATE_TBL_ID_10 : {}\t RATE_TBL_ID_15 : {}\t RATE_TBL_ID_OTHER : {}\t ELIG RULES ID : {}", benefitGroup.getBenefitProgram(),
				rateMap.get( BSSRateType.MEDICAL.rateIdType() ), rateMap.get( BSSRateType.DP_MEDICAL.rateIdType() ),
				rateMap.get( BSSRateType.OTHER.rateIdType() ), eligRulesId);
	}

	@Override
	public Map<String, String> generateRateTableId() {
		Map<String, String> rateTableIds = new HashMap<>();
		String rateTblIdMedical = nextRateTblID.execute();
		String rateTblIdDPMedical = nextRateTblID.execute();
		String rateTblIdOther = nextRateTblID.execute();
		rateTableIds.put( BSSRateType.MEDICAL.rateIdType(), rateTblIdMedical);
		rateTableIds.put( BSSRateType.DP_MEDICAL.rateIdType(), rateTblIdDPMedical);
		rateTableIds.put( BSSRateType.OTHER.rateIdType(), rateTblIdOther);
		return rateTableIds;
	}

	@Override
	public void generateRateTableIdsNonMedical(Map<String, String> rateTableIds) {
		String rateTblIdDPMedical = nextRateTblID.execute();
		String rateTblIdOther = nextRateTblID.execute();
		rateTableIds.put( BSSRateType.DP_MEDICAL.rateIdType(), rateTblIdDPMedical);
		rateTableIds.put( BSSRateType.OTHER.rateIdType(), rateTblIdOther);
	}

	@Override
	public Map<String, Integer> getBenefitProgramHeadCount(long companyId, String status) {
		List<BenefitGroup> bgs = benefitGroupDao.findByCompanyIdAndStatus(companyId, status);
		Map<String, Integer> bgHeadCountMap = new HashMap<>();
		for (BenefitGroup bg : bgs) {
			bgHeadCountMap.put(bg.getBenefitProgram(), (int) bg.getHeadcount());
		}
		return bgHeadCountMap;
	}

	/**
	 * Add New Group to the company and copy plans, contributions, and funding
	 * details from source benefit group
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public long addGroup(Company company, GroupData groupData, long strategyId) {
		if (StrategyServiceHelper.isProspectStrategy(strategyId)) {
			return prospectGroupService.addGroup(company.getCode(), groupData.getSourceStrategyGroupId(),
					groupData.getDestGroupName());
		} else {
			BenefitGroupStrategy bgs = strategyGroupService
					.getBenefitGroupStrategyBy(groupData.getSourceStrategyGroupId(), strategyId);
			if (null == bgs) {
				throw new BSSApplicationException(new BSSApplicationError(
						BSSErrorResponseCodes.BSS_STRATEGY_SAVE_FAILED, BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
						StrategyController.class.getName(), "Benefit group not found", null, null));
			}
			// Create Benefit group
			BenefitGroup benefitGroup = createGroupAndCovgHeadCounts(company, groupData, strategyId);

			// adding the group in only to one strategy
			saveGroupPlans(bgs.getBenefitGroup().getId(), benefitGroup.getId(), strategyId);
			saveGroupFundings(bgs.getBenefitGroup().getId(), benefitGroup.getId(), strategyId);

			return benefitGroup.getId();
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteGroup(long strategyGroupId, long strategyId) {
		if (strategyId == ProspectConstants.PROSPECT_STRATEGY_ID) {
			prospectGroupService.deleteBenefitGroup(strategyGroupId);
		} else {
			BenefitGroupStrategy bgs = strategyGroupService.getBenefitGroupStrategyBy(strategyGroupId, strategyId);
			if (bgs == null) {
				throw new BSSApplicationException(new BSSApplicationError(
						BSSErrorResponseCodes.BSS_STRATEGY_SAVE_FAILED, BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
						StrategyController.class.getName(), "Benefit group not found", null, null));
			}
			bgs.setStatus(BSSApplicationConstants.PENDING_STATUS);
			strategyGroupService.saveBenefitGroupStrategy(bgs);

			strategyDataDao.deleteAllPlanContributionsByBenefitgroupAndStrategy(bgs.getGroupId(), bgs.getStrategyId());
			strategyDataDao.deleteAllPlanSelectionsByBenefitgroupAndStrategy(bgs.getGroupId(), bgs.getStrategyId());
			strategyDataDao.deleteStrategyFundingsByBenefitgroupAndStrategy(bgs.getGroupId(), bgs.getStrategyId());
		}
	}

	private BenefitGroup createGroupAndCovgHeadCounts(Company company, GroupData groupData, long strategyId) {

        BenefitGroup benefitGroup = null;
        if (BSSApplicationConstants.CLIENT_MA_GROUP_NAME.equals(groupData.getDestGroupName())) {
            benefitGroup = benefitGroupDao
                    .findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE).stream()
                    .filter(grp -> BSSApplicationConstants.CLIENT_MA_GROUP_NAME.equals(grp.getName())).findAny()
                    .orElse(null);
        }
        if (benefitGroup == null) {
            benefitGroup = constructW2Group(company, false);
            benefitGroup.setName(groupData.getDestGroupName());
            benefitGroup = saveBenefitGroup(benefitGroup);
        }

		BenefitGroupStrategy bgs = new BenefitGroupStrategy();
		bgs.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
		bgs.setWaitingPeriod(groupData.getWaitPeriod());
		bgs.setDefaultGroup(false);
		bgs.setGroupId(benefitGroup.getId());
		bgs.setBenefitGroup(benefitGroup);
		bgs.setStrategyId(strategyId);
		// creating benefit group strategy
		bgs = strategyGroupService.saveBenefitGroupStrategy(bgs);
		if (company.isRenewalCompany() || company.isProspectCompany() || company.isProspectConvertedClient()) {
			// No action needed
		} else {
			List<String> covgCodes = Arrays.asList(CoverageCodesEnums.COV_EMPLOYEE.getCode(),
					CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode(),
					CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode(),
					CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode());
			for (String covgCode : covgCodes) {
				StrategyGroupHeadCount newSghc = new StrategyGroupHeadCount();
				StrategyGroupHeadCountId newId = new StrategyGroupHeadCountId();
				newId.setStrategyGroupId(bgs.getId());
				newId.setCovrgCd(covgCode);
				newSghc.setId(newId);
				newSghc.setHeadcount(0);
				strategyGroupHeadCountService.saveStrategyGroupHeadCount(newSghc);
			}
		}
		return benefitGroup;
	}

	private void saveGroupPlans(long sourceGroupId, long newGroupId, long strategyId) {
		// Get All Plan Types for the given strategyId and groupId
		List<PlanSelection> planList = planSelectionService.getPlansByStrategyIdGroupId(strategyId, sourceGroupId);
		List<PlanSelection> newPlanList = new ArrayList<>();
		for (PlanSelection ps : planList) {
			PlanSelection planSelection = new PlanSelection(ps, newGroupId);
			newPlanList.add(planSelection);
		}
		// save Plans in database
		newPlanList = planSelectionService.saveAll(newPlanList);

		// save contributions for each plan
		List<Contribution> newList = new ArrayList<>();
		for (PlanSelection ps : planList) {
			long planSelectionId = getNewPlanSelectionId(newPlanList, ps.getBenefitPlan());
			List<Contribution> list = contributionService.getContributions(ps.getId());
			for (Contribution contrib : list) {
				Contribution newContrib = new Contribution(contrib);
				newContrib.setPlanSelectionId(planSelectionId);
				newList.add(newContrib);
			}
		}
		if (!newList.isEmpty()) {
			contributionService.saveAll(newList);
		}
	}

	private void saveGroupFundings(long sourceGroupId, long newGroupId, long strategyId) {
		// Get Source group Funding Models
		List<StrategyFundingModel> sfmList = strategyFundingModelService
				.getStrategyFundingModelByStrategyIdAndGroupId(strategyId, sourceGroupId);
		List<StrategyFundingModel> newSfmList = new ArrayList<>();

		for (StrategyFundingModel sfm : sfmList) {
			StrategyFundingModel model = new StrategyFundingModel(sfm, newGroupId);
			Set<StrategyFundingDetail> sfdSet = new HashSet<>();
			Set<StrategyFundingFlatMax> sffmSet = new HashSet<>();
			Set<StrategyFundBsuppPlanTypes> fbptSet = new HashSet<>();

			if (CollectionUtils.isNotEmpty(sfm.getFundingDetails())) {
				for (StrategyFundingDetail detail : sfm.getFundingDetails()) {
					StrategyFundingDetail sfd = new StrategyFundingDetail();
					sfd.setContribution(detail.getContribution());
					StrategyFundingDetailId detailId = new StrategyFundingDetailId();
					detailId.setCoverageId(detail.getSfDetailId().getCoverageId());
					sfd.setSfDetailId(detailId);
					sfd.setStrategyFundingModel(model);
					sfdSet.add(sfd);
				}
			}
			if (CollectionUtils.isNotEmpty(sfm.getFundingFlatMax())) {
				for (StrategyFundingFlatMax sffm : sfm.getFundingFlatMax()) {
					StrategyFundingFlatMax sffmax = new StrategyFundingFlatMax();
					sffmax.setContribution(sffm.getContribution());
					StrategyFundingDetailId detailId = new StrategyFundingDetailId();
					detailId.setCoverageId(sffm.getSfDetailId().getCoverageId());
					sffmax.setSfDetailId(detailId);
					sffmax.setStrategyFundingModel(model);
					sffmSet.add(sffmax);
				}
				model.setFundingFlatMax(sffmSet);
			}

			if (CollectionUtils.isNotEmpty(sfm.getFundingBsuppPlanTypes())) {
				for (StrategyFundBsuppPlanTypes sfbpts : sfm.getFundingBsuppPlanTypes()) {
					StrategyFundBsuppPlanTypes sfbp = new StrategyFundBsuppPlanTypes();
					StrategyFundBsuppPlanTypeId sfpti = new StrategyFundBsuppPlanTypeId();
					sfpti.setPlanType(sfbpts.getStrategyFundBsuppPlanTypeId().getPlanType());
					sfbp.setStrategyFundBsuppPlanTypeId(sfpti);
					sfbp.setStrategyFundingModel(model);
					fbptSet.add(sfbp);
				}
				model.setFundingBsuppPlanTypes(fbptSet);
			}
			model.setFundingDetails(sfdSet);
			newSfmList.add(model);
		}
		// Save funding model and funding details in the Database
		strategyFundingModelService.saveAll(newSfmList);
	}

	private long getNewPlanSelectionId(List<PlanSelection> list, String benefitPlan) {
		for (PlanSelection ps : list) {
			if (ps.getBenefitPlan().equals(benefitPlan)) {
				return ps.getId();
			}
		}
		return 0;
	}

	private void resetStrategyCache(BenefitGroup group) {
		List<Strategy> strategies = strategyService.getAllStrategies(group.getCompanyId());
		strategies.stream()
				.forEach(strategy -> cacheService.invalidateCache(
						CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE.getObjectType(),
						CacheObjectLevelEnum.STRATEGY.getObjectLevel(), String.valueOf(strategy.getId())));
	}

	@Override
	public void addMandatoryBenefitGroups(Company company, List<BenefitGroup> benefitGroups, Map<String, String> waitPeriodMap) {
		List<GroupRuleDto> mandatoryGroupRuleList = groupRuleService.getApplicableGroups(company, true);
		
		BenefitGroup defaultBenefitGroup = null;
		BenefitGroup k1BenefitGroup = null;

		for (BenefitGroup benefitGroup : benefitGroups) {
			if (BSSApplicationConstants.K1_GROUP_TYPE.equals(benefitGroup.getType())) {
				k1BenefitGroup = benefitGroup;
			}
			if (benefitGroup.getBenefitProgram().equals(company.getBenefitProgram())) {
				defaultBenefitGroup = benefitGroup;
			}
		}

		for (GroupRuleDto groupRuleDto : mandatoryGroupRuleList) {
			if (!mandatoryGroupExists(groupRuleDto, benefitGroups)) {
				
				BenefitGroup sourceGroup = null;
				if (BSSApplicationConstants.K1_GROUP_TYPE.equals(groupRuleDto.getGroupType()) && k1BenefitGroup != null) {
					sourceGroup = k1BenefitGroup;
				} else if (defaultBenefitGroup != null) {
					sourceGroup = defaultBenefitGroup;
				}
				
				// create the new benefit group here.
				BenefitGroup newBenefitGroup = new BenefitGroup();
				newBenefitGroup.setBenefitProgram(nextBenProgram.execute());
				newBenefitGroup.setName(groupRuleDto.getGroupName());
				newBenefitGroup.setType(groupRuleDto.getGroupType());
				newBenefitGroup.setState(groupRuleDto.getState());
				newBenefitGroup.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
				newBenefitGroup.setSystemCreated(true);
				benefitGroups.add(newBenefitGroup);
				waitPeriodMap.put(newBenefitGroup.getBenefitProgram(), waitPeriodMap.get(sourceGroup.getBenefitProgram()));
				
				if (k1BenefitGroup == null && BSSApplicationConstants.K1_GROUP_TYPE.equals(newBenefitGroup.getType())) {
					k1BenefitGroup = newBenefitGroup;
				}
			}
		}
	}

	@Override
	public boolean mandatoryGroupExists(GroupRuleDto groupRuleDto, List<BenefitGroup> benefitGroups) {
		boolean exists = false;
		for (BenefitGroup benefitGroup : benefitGroups) {
			if (benefitGroup.getType().equals(groupRuleDto.getGroupType())
					&& ((groupRuleDto.getState() == null && benefitGroup.getState() == null)
							|| (groupRuleDto.getState() != null && benefitGroup.getState() != null
									&& groupRuleDto.getState().equals(benefitGroup.getState())))) {
				exists = true;
			}
		}
		return exists;
	}	
	
	
	/**
	 * This methods ensures that all the expected values for RATE_TBL_ID are present
	 * in the BenefitGroup and if any are missing, new values are allocated and
	 * added to the BenefitGroup
	 * 
	 * @param group
	 * @return the same group, after modifying the GroupRate collection if needed
	 */
	public BenefitGroup completeRateTblSet(BenefitGroup group) {
		if (group.getGroupRate().isEmpty()) {
			GroupRate medicalRate = new GroupRate();
			medicalRate.setRateIdType(BSSRateType.MEDICAL.rateIdType());
			GroupRatePK gpk = new GroupRatePK();
			gpk.setRateTblId(nextRateTblID.execute());
			medicalRate.setId(gpk);
			medicalRate.setBenefitGroup(group);
			group.getGroupRate().add(medicalRate);

			GroupRate medicalDpRate = new GroupRate();
			medicalDpRate.setRateIdType(BSSRateType.DP_MEDICAL.rateIdType());
			GroupRatePK gpkDp = new GroupRatePK();
			gpkDp.setRateTblId(nextRateTblID.execute());
			medicalDpRate.setId(gpkDp);
			medicalDpRate.setBenefitGroup(group);
			group.getGroupRate().add(medicalDpRate);

			GroupRate otherRate = new GroupRate();
			otherRate.setRateIdType(BSSRateType.OTHER.rateIdType());
			GroupRatePK gpkOther = new GroupRatePK();
			gpkOther.setRateTblId(nextRateTblID.execute());
			otherRate.setId(gpkOther);
			otherRate.setBenefitGroup(group);
			group.getGroupRate().add(otherRate);
		}
		return group;
	}

	/**
	 * @param benefitGroupsDao the benefitGroupsDao to set
	 */
	public void setBenefitGroupsDao(BenefitGroupDao benefitGroupsDao) {
		this.benefitGroupDao = benefitGroupsDao;
	}

	/**
	 * @param nextBenProgram the nextBenProgram to set
	 */
	public void setNextBenProgram(NextBenProgram nextBenProgram) {
		this.nextBenProgram = nextBenProgram;
	}

	/**
	 * @param nextRateTblID the nextRateTblID to set
	 */
	public void setNextRateTblID(NextRateTblID nextRateTblID) {
		this.nextRateTblID = nextRateTblID;
	}

	/**
	 * @param groupCovrgHeadCountDao the groupCovrgHeadCountDao to set
	 */
	public void setGroupCovrgHeadCountDao(GroupCovrgHeadCountDao groupCovrgHeadCountDao) {
		this.groupCovrgHeadCountDao = groupCovrgHeadCountDao;
	}

	/**
	 * @param spGetNextEligRulesId the spGetNextEligRulesId to set
	 */
	public void setSpGetNextEligRulesId(GetNextEligRulesId spGetNextEligRulesId) {
		this.spGetNextEligRulesId = spGetNextEligRulesId;
	}

	/**
	 * @param benefitStrategyGroupDao the benefitStrategyGroupDao to set
	 */
	public void setBenefitStrategyGroupDao(BenefitStrategyGroupDao benefitStrategyGroupDao) {
		this.benefitStrategyGroupDao = benefitStrategyGroupDao;
	}
	
	@Override
	public List<String> getBenefitProgramsForStrategy(String companyCode, Long strategyId) {
		Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeStrategyGroupDetails = employeeBenefitGroupDao
				.getEmployeeStrategyGroupDetails(companyCode);

		return employeeStrategyGroupDetails.values().stream().flatMap(List::stream).collect(Collectors.toList())
				.stream().filter(strategy -> Long.valueOf(strategy.getStrategyId()).equals(strategyId))
				.map(EmployeeCensusStrategyGroupDetails::getBenefitProgram).distinct()
				.collect(Collectors.toList());
	}
	
	@Override
	public List<BenefitGroup> findByCompanyId(long companyId) {
		return benefitGroupDao.findByCompanyId(companyId);
	}

	@Override
	public void updateBenefitGroupStatus(BenefitGroup benefitGroup, String status) {
		benefitGroup.setStatus(BSSApplicationConstants.STATUS_DELETED);
		benefitGroupDao.saveAndFlush(benefitGroup);
		
	}

    @Override
    public void updateBenefitGroupType(long strategyId, long groupId, String companyCode) {
        BenefitGroup group = benefitGroupDao.findById(groupId);
        resetStrategyCache(group);
        group.setType(BSSApplicationConstants.K1_GROUP_TYPE);
        benefitGroupDao.save( group );
    }
}
