package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.GroupRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.GroupRule;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.impl.GroupRuleServiceImpl;
import com.trinet.ambis.service.model.GroupRuleDto;

/**
 * @author rvutukuri
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class GroupRuleServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	GroupRuleServiceImpl groupRuleService;

	@Mock
	GroupRuleDao groupRuleDao;

	@Mock
	RealmDataDao realmDataDao;

    private MockedStatic<StrategyServiceHelper> mockStaticBSSSecurityUtils;

    @Before
    public void setUp() {
        mockStaticBSSSecurityUtils = Mockito.mockStatic(StrategyServiceHelper.class);
    }

    @After
    public void tearDown() {
        if (mockStaticBSSSecurityUtils != null) {
            mockStaticBSSSecurityUtils.close();
        }
    }

	@Test
	public void findByDate() {

		when(groupRuleDao.findByDate(ArgumentMatchers.any(Date.class))).thenReturn(prepareGroupRuleList());

		List<GroupRuleDto> result = groupRuleService.findByDate(new Date());

		verify(groupRuleDao, times(1)).findByDate(any(Date.class));
		assertEquals(4, result.size());
		assertEquals(3, result.get(0).getId());
		assertEquals(2, result.get(0).getRules().size());
		assertEquals(2, result.get(1).getId());
		assertEquals(1, result.get(2).getId());
	}

	@Test
	public void getApplicableGroups() {

		Company company = new Company();
		company.setCode("TEST");
		company.setK1Company(true);
		company.setRealmPlanYearId(1);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearStart(new Date());
		company.setRealmPlanYear(realmPlanYear);
		Set<String> locations = new HashSet<>(Arrays.asList("MA", "NC"));

		when(groupRuleDao.findByDate(Mockito.any(Date.class))).thenReturn(prepareGroupRuleList());
		when(StrategyServiceHelper.getHqStateCity(Mockito.any(Company.class))).thenReturn(locations);
		when(realmDataDao.getSelectedBenefits(company.getRealmPlanYearId(), locations))
				.thenReturn(prepareSelectedBenefits(true, true));
		when(realmDataDao.getEmployeeHomeStates(Mockito.anyString())).thenReturn(Arrays.asList("MA", "NC"));
		when(realmDataDao.getCompanyLocationStates(Mockito.anyString()))
				.thenReturn(new HashSet<String>());

		// When renewal company - only mandatory
		company.setRenewalCompany(true);
		List<GroupRuleDto> result = groupRuleService.getApplicableGroups(company, true);
		assertEquals(3, result.size());

		// When renewal company - not only mandatory
		company.setRenewalCompany(true);
		result = groupRuleService.getApplicableGroups(company, false);
		assertEquals(4, result.size());

		// When not renewal company - only mandatory
		company.setRenewalCompany(false);
		result = groupRuleService.getApplicableGroups(company, true);
		assertEquals(1, result.size());

		// When not renewal company - not only mandatory
		company.setRenewalCompany(false);
		result = groupRuleService.getApplicableGroups(company, false);
		assertEquals(2, result.size());
		
		// When no medical returned in selected benefits  
		company.setRenewalCompany(true);
		when(realmDataDao.getSelectedBenefits(company.getRealmPlanYearId(), locations))
		.thenReturn(prepareSelectedBenefits(false, false));
		result = groupRuleService.getApplicableGroups(company, false);
		assertEquals(2, result.size());
		
		// When medical is false in selected benefits
		company.setRenewalCompany(true);
		company.setK1Company(false);
		when(realmDataDao.getSelectedBenefits(company.getRealmPlanYearId(), locations))
		.thenReturn(prepareSelectedBenefits(true, false));
		result = groupRuleService.getApplicableGroups(company, false);
		assertEquals(1, result.size());

		verify(groupRuleDao, times(6)).findByDate(any(Date.class));
		verify(realmDataDao, times(6)).getSelectedBenefits(company.getRealmPlanYearId(), locations);
		verify(realmDataDao, times(4)).getEmployeeHomeStates(ArgumentMatchers.anyString());
		verify(realmDataDao, times(2)).getCompanyLocationStates(ArgumentMatchers.anyString());
	}

	private List<GroupRule> prepareGroupRuleList() {
		List<GroupRule> groupRuleList = new ArrayList<>();

		GroupRule groupRule = new GroupRule();
		groupRule.setId(1);
		groupRule.setGroupType(BSSApplicationConstants.STD_GROUP_TYPE);
		groupRule.setState(null);
		groupRule.setPlanType(null);
		groupRule.setMandatory(false);
		groupRule.setAllowMultiples(true);
		groupRule.setAppliesToNoMed(true);
		groupRule.setDefaultGroupName("W2");
		groupRule.setRuleName(null);
		groupRule.setEffDate(new Date());
		groupRule.setExpDate(new Date());
		groupRule.setFlowType(null);
		groupRuleList.add(groupRule);

		groupRule = new GroupRule();
		groupRule.setId(2);
		groupRule.setGroupType(BSSApplicationConstants.K1_GROUP_TYPE);
		groupRule.setState(null);
		groupRule.setPlanType(null);
		groupRule.setMandatory(true);
		groupRule.setAllowMultiples(false);
		groupRule.setAppliesToNoMed(true);
		groupRule.setDefaultGroupName("K1");
		groupRule.setRuleName(null);
		groupRule.setEffDate(new Date());
		groupRule.setExpDate(new Date());
		groupRule.setFlowType(null);
		groupRuleList.add(groupRule);

		groupRule = new GroupRule();
		groupRule.setId(3);
		groupRule.setGroupType(BSSApplicationConstants.K1_GROUP_TYPE);
		groupRule.setState("MA");
		groupRule.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		groupRule.setMandatory(true);
		groupRule.setAllowMultiples(false);
		groupRule.setAppliesToNoMed(false);
		groupRule.setDefaultGroupName("K1 MA");
		groupRule.setRuleName("MA MEDICAL RULE");
		groupRule.setEffDate(new Date());
		groupRule.setExpDate(new Date());
		groupRule.setFlowType(null);
		groupRuleList.add(groupRule);

		groupRule = new GroupRule();
		groupRule.setId(4);
		groupRule.setGroupType(BSSApplicationConstants.K1_GROUP_TYPE);
		groupRule.setState("MA");
		groupRule.setPlanType(BSSApplicationConstants.DENTAL_PLAN_TYPE);
		groupRule.setMandatory(true);
		groupRule.setAllowMultiples(false);
		groupRule.setAppliesToNoMed(false);
		groupRule.setDefaultGroupName("K1 MA");
		groupRule.setRuleName("MA DENTAL RULE");
		groupRule.setEffDate(new Date());
		groupRule.setExpDate(new Date());
		groupRule.setFlowType(null);
		groupRuleList.add(groupRule);
		
		groupRule = new GroupRule();
		groupRule.setId(5);
		groupRule.setGroupType(BSSApplicationConstants.K1_GROUP_TYPE);
		groupRule.setState("NC");
		groupRule.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		groupRule.setMandatory(true);
		groupRule.setAllowMultiples(false);
		groupRule.setAppliesToNoMed(false);
		groupRule.setDefaultGroupName("K1 NC");
		groupRule.setRuleName("NC MEDICAL RULE");
		groupRule.setEffDate(new Date());
		groupRule.setExpDate(new Date());
		groupRule.setFlowType("R");
		groupRuleList.add(groupRule);

		groupRule = new GroupRule();
		groupRule.setId(6);
		groupRule.setGroupType(BSSApplicationConstants.K1_GROUP_TYPE);
		groupRule.setState("NC");
		groupRule.setPlanType(BSSApplicationConstants.DENTAL_PLAN_TYPE);
		groupRule.setMandatory(true);
		groupRule.setAllowMultiples(false);
		groupRule.setAppliesToNoMed(false);
		groupRule.setDefaultGroupName("K1 NC");
		groupRule.setRuleName("NC DENTAL RULE");
		groupRule.setEffDate(new Date());
		groupRule.setExpDate(new Date());
		groupRule.setFlowType("N");
		groupRuleList.add(groupRule);	

		return groupRuleList;

	}

	private Map<String, Boolean> prepareSelectedBenefits(boolean includeMedical, boolean selectMedical) {
		Map<String, Boolean> selectedBenefitsMap = new HashMap<>();
		if (includeMedical) {
			selectedBenefitsMap.put(BSSApplicationConstants.MEDICAL, selectMedical);
		}
		selectedBenefitsMap.put(BSSApplicationConstants.DENTAL, true);
		selectedBenefitsMap.put(BSSApplicationConstants.VISION, true);
		return selectedBenefitsMap;
	}

}