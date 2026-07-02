package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.dao.hrp.impl.TemplateFundingDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.Industry;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;


@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class TemplateFundingDaoImplTest extends ServiceUnitTest {

	TemplateFundingDaoImpl templateFundingDao = new TemplateFundingDaoImpl();
	EntityManager entityManager = null;
	Query mockedQuery = null;
	Query mockedQueryPickAndChoose = null;
	Query mockedQueryNonPickAndChoose = null;

    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setup() {
        entityManager = mock(EntityManager.class);
        mockedQuery = mock(Query.class);
        mockedQueryPickAndChoose = mock(Query.class);
        mockedQueryNonPickAndChoose = mock(Query.class);
        templateFundingDao.setEntityManager(entityManager);

        rulesAndConfigsUtilsMockedStatic = org.mockito.Mockito.mockStatic(RulesAndConfigsUtils.class);

        when(mockedQuery.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.anyIterable())).thenReturn(mockedQuery);
        when(templateFundingDao.getEntityManager().createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
    }

    @After
    public void tearDown() {
        rulesAndConfigsUtilsMockedStatic.close();
    }

	@Test
	public void getTemplateHeadCountPlansTestNonPickAndChoose() {
		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);

		RealmPlanYear realmPlanYear = new RealmPlanYear();
		company.setRealmPlanYear(realmPlanYear);
		Industry industry = new Industry(1);
		industry.setIndustryType(IndustryType.BS);
		company.setIndustry(industry);

		when(templateFundingDao.getEntityManager().createNamedQuery("TEMPLT_PORTFOLIO_PLANS_BY_IND_REGION_EXCIII"))
				.thenReturn(mockedQueryNonPickAndChoose);
		when(mockedQueryNonPickAndChoose.getResultList()).thenReturn(getBenefitPlanResults());
		when( RulesAndConfigsUtils.findPickChooseWithExceptions( any( Company.class )) ).thenReturn( false );

		Map<String, List<String>> result = templateFundingDao.getTemplateHeadCountPlans(company,
				BSSApplicationConstants.CONSERVATIVE_ID);

		assertEquals(3, result.size());
		assertEquals(Arrays.asList("001EKY", "001EKS"), result.get(PlanTypesEnum.MEDICAL.getCode()));
		assertEquals(Arrays.asList("001EKX"), result.get(PlanTypesEnum.DENTAL.getCode()));
		assertEquals(Arrays.asList("001EKA"), result.get(PlanTypesEnum.VISION.getCode()));
	}

	@Test
	public void getTemplateHeadCountPlansTestPickAndChoose() {
		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_II.getBenExchng());
		company.setRealm(realm);

		RealmPlanYear realmPlanYear = new RealmPlanYear();
		company.setRealmPlanYear(realmPlanYear);
		Industry industry = new Industry(1);
		industry.setIndustryType(IndustryType.BS);
		company.setIndustry(industry);

		when(templateFundingDao.getEntityManager().createNamedQuery("TEMPLT_PORTFOLIO_PLANS_BY_IND_REGION"))
				.thenReturn(mockedQueryPickAndChoose);
		when(mockedQueryPickAndChoose.getResultList()).thenReturn(getBenefitPlanResults());
		when( RulesAndConfigsUtils.findPickChooseWithExceptions( any( Company.class )) ).thenReturn( true );

		Map<String, List<String>> result = templateFundingDao.getTemplateHeadCountPlans(company,
				BSSApplicationConstants.CONSERVATIVE_ID);

		assertEquals(3, result.size());
		assertEquals(Arrays.asList("001EKY", "001EKS"), result.get(PlanTypesEnum.MEDICAL.getCode()));
		assertEquals(Arrays.asList("001EKX"), result.get(PlanTypesEnum.DENTAL.getCode()));
		assertEquals(Arrays.asList("001EKA"), result.get(PlanTypesEnum.VISION.getCode()));
	}

	@Test
	public void getAllTemplateFundingTest() {
		when(mockedQuery.getResultList()).thenReturn(getFundingResults());
		List<String> list = Constants.PKG_TYPES;
		Map<String, List<PlanPackage>> planPackages = templateFundingDao.getAllTemplateFundingDetails("FS", "OT", list, 2, true);
		List<PlanPackage> planPackageList = planPackages.get("medical");
		PlanPackage planPackage = planPackageList.get(0);
		
		assertEquals(3, planPackages.size());
		assertEquals(1, planPackage.getTemplateId());
		assertEquals("10", planPackage.getPlanType());
		assertEquals("001EKY", planPackage.getFundingBasePlanList().get(0));
	}

	/**
	 * constructing benefit plans
	 * 
	 * @return List<Object[]>
	 */
	private List<Object[]> getBenefitPlanResults() {
		List<Object[]> list = new ArrayList<Object[]>();
		Object[] temp = new Object[4];
		temp[0] = BigDecimal.valueOf(11111);
		temp[1] = new String("001EKY");
		temp[2] = new String("10");
		temp[3] = BSSApplicationConstants.CONSERVATIVE_ID;;
		list.add(temp);
		
		temp = new Object[4];
		temp[0] = BigDecimal.valueOf(11112);
		temp[1] = new String("001EKS");
		temp[2] = new String("10");
		temp[3] = BSSApplicationConstants.CONSERVATIVE_ID;;
		list.add(temp);
		
		temp = new Object[4];
		temp[0] = BigDecimal.valueOf(11113);
		temp[1] = new String("001EKX");
		temp[2] = new String("11");
		temp[3] = BSSApplicationConstants.CONSERVATIVE_ID;;
		list.add(temp);
		
		temp = new Object[4];
		temp[0] = BigDecimal.valueOf(11114);
		temp[1] = new String("001EKA");
		temp[2] = new String("14");
		temp[3] = BSSApplicationConstants.CONSERVATIVE_ID;;
		list.add(temp);
		return list;
	}

	/**
	 * Constructing funding object.
	 * 
	 * @return List<Object[]>
	 */
	private List<Object[]> getFundingResults() {
		List<Object[]> list = new ArrayList<Object[]>();
		Object[] temp = new Object[8];
		temp[0] = BigDecimal.valueOf(53009);
		temp[1] = new String("001EKY");
		temp[2] = new String("10");
		temp[3] = BSSApplicationConstants.CONSERVATIVE_ID;
		temp[4] = "NY";
		temp[5] = "PCT";
		temp[6] = BigDecimal.valueOf(75);
		temp[7] = "employee";
		list.add(temp);
		
		temp = new Object[8];
		temp[0] = BigDecimal.valueOf(53010);
		temp[1] = new String("0013HJ");
		temp[2] = new String("10");
		temp[3] = BSSApplicationConstants.CONSERVATIVE_ID;
		temp[4] = "CA";
		temp[5] = "BFPCT";
		temp[6] = BigDecimal.valueOf(85);
		temp[7] = "all";
		list.add(temp);
		
		temp = new Object[8];
		temp[0] = BigDecimal.valueOf(53016);
		temp[1] = new String("000SR7");
		temp[2] = new String("11");
		temp[3] = BSSApplicationConstants.BALANCED_ID;
		temp[4] = "CA";
		temp[5] = "BFPCT";
		temp[6] = BigDecimal.valueOf(90);
		temp[7] = "all";
		list.add(temp);

		temp = new Object[8];
		temp[0] = BigDecimal.valueOf(53012);
		temp[1] = new String("000SR6");
		temp[2] = new String("11");
		temp[3] = BSSApplicationConstants.CONSERVATIVE_ID;
		temp[4] = "CA";
		temp[5] = "BFPCT";
		temp[6] = BigDecimal.valueOf(75);
		temp[7] = "all";
		list.add(temp);
		
		temp = new Object[8];
		temp[0] = BigDecimal.valueOf(53021);
		temp[1] = new String("002J23");
		temp[2] = new String("14");
		temp[3] = BSSApplicationConstants.TOP_QUALITY_ID;
		temp[4] = "CA";
		temp[5] = "BFPCT";
		temp[6] = BigDecimal.valueOf(100);
		temp[7] = "all";
		list.add(temp);
		
		temp = new Object[8];
		temp[0] = BigDecimal.valueOf(53017);
		temp[1] = new String("002J24");
		temp[2] = new String("14");
		temp[3] = BSSApplicationConstants.TOP_QUALITY_ID;
		temp[4] = "CA";
		temp[5] = "BFPCT";
		temp[6] = BigDecimal.valueOf(100);
		temp[7] = "all";
		list.add(temp);
		
		return list;
	}
}