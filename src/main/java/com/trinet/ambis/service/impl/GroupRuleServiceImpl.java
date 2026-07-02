package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.MultiKeyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.GroupRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.GroupRule;
import com.trinet.ambis.service.GroupRuleService;
import com.trinet.ambis.service.model.GroupRuleDto;

@Service
public class GroupRuleServiceImpl implements GroupRuleService {

	@Autowired
	GroupRuleDao groupRuleDao;
	@Autowired
	RealmDataDao realmDataDao;

	@SuppressWarnings("unchecked")
	@Override
	public List<GroupRuleDto> findByDate(Date date) {
		
		List<GroupRule> groupRuleList = groupRuleDao.findByDate(date);
		MultiKeyMap groupRuleMap = new MultiKeyMap();
		
		for (GroupRule groupRule : groupRuleList) {
			GroupRuleDto groupRuleDto = new GroupRuleDto();
			groupRuleDto.setId(groupRule.getId());
			groupRuleDto.setGroupType(groupRule.getGroupType());
			groupRuleDto.setState(groupRule.getState());
			groupRuleDto.setMandatory(groupRule.isMandatory());
			groupRuleDto.setFlowType(groupRule.getFlowType());
			groupRuleDto.setAllowMultiplies(groupRule.isAllowMultiples());
			groupRuleDto.setAppliesToNoMed(groupRule.isAppliesToNoMed());
			groupRuleDto.setGroupName(groupRule.getDefaultGroupName());
			groupRuleDto.setSortOrder(groupRule.getSortOrder());
			groupRuleDto.setEffDate(groupRule.getEffDate());
			groupRuleDto.setExpDate(groupRule.getExpDate());
			
			GroupRuleDto existingGroupRuleDto = null;
			if (groupRuleMap.containsKey(groupRuleDto.getGroupType(), groupRuleDto.getState())) {
				existingGroupRuleDto = (GroupRuleDto) groupRuleMap.get(groupRuleDto.getGroupType(),
						groupRuleDto.getState());
			} else {
				existingGroupRuleDto = groupRuleDto;
				groupRuleMap.put(existingGroupRuleDto.getGroupType(), existingGroupRuleDto.getState(),
						existingGroupRuleDto);
			}
			if (groupRule.getPlanType() != null) {
				GroupRuleDto.PlanTypeRule planTypeRule =  existingGroupRuleDto.new PlanTypeRule();
				planTypeRule.setPlanType(groupRule.getPlanType());
				planTypeRule.setRuleName(groupRule.getRuleName());
				existingGroupRuleDto.getRules().add(planTypeRule);
			}

		}
		return (List<GroupRuleDto>) groupRuleMap.values().stream().collect(Collectors.toList());	
	}

	@Override
	public List<GroupRuleDto> getApplicableGroups(Company company, boolean onlyMandatory) {
		List<GroupRuleDto> resultList = new ArrayList<>();
		List<GroupRuleDto> groupRuleDtoList = findByDate(company.getRealmPlanYear().getPlanYearStart());
		Map<String, Boolean> selectedBenefits = realmDataDao.getSelectedBenefits(company.getRealmPlanYearId(),
				StrategyServiceHelper.getHqStateCity(company));
		boolean clientIsNoMed = true;
		if (selectedBenefits.containsKey(BSSApplicationConstants.MEDICAL) && selectedBenefits.get(BSSApplicationConstants.MEDICAL)) {
			clientIsNoMed = false;
		}
		
		Set<String> states;
		if (company.isRenewalCompany()) {
			states = realmDataDao.getEmployeeHomeStates(company.getCode()).stream().collect(Collectors.toSet());
		}
		else {
			states = realmDataDao.getCompanyLocationStates(company.getCode());
		}

		for (GroupRuleDto groupRuleDto : groupRuleDtoList) {
			if ((!onlyMandatory || groupRuleDto.isMandatory())
					&& (company.isK1Company()
							&& BSSApplicationConstants.K1_GROUP_TYPE.equals(groupRuleDto.getGroupType())
							|| (BSSApplicationConstants.STD_GROUP_TYPE.equals(groupRuleDto.getGroupType())))
					&& (groupRuleDto.getState() == null || states.contains(groupRuleDto.getState()))
					&& (!clientIsNoMed || groupRuleDto.isAppliesToNoMed())
					&& (groupRuleDto.getFlowType() == null
							|| (company.isRenewalCompany() && "R".equals(groupRuleDto.getFlowType()))
							|| (!company.isRenewalCompany() && "N".equals(groupRuleDto.getFlowType())))) {
				resultList.add(groupRuleDto);
			}
		}
		Collections.sort(resultList);
		return resultList;
	}

}