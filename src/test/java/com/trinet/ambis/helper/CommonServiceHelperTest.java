package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmConfiguration;
import com.trinet.ambis.persistence.model.RealmConfigurationId;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.MinimumFunding;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author schaudhari
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class CommonServiceHelperTest extends ServiceUnitTest {

	@Mock
	RealmDataDao realmDataDao;

	private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

	@Before
	public void setUp() {
		rulesAndConfigsUtilsMockedStatic = org.mockito.Mockito.mockStatic(RulesAndConfigsUtils.class);

	}

	@After
	public void tearDown() {
		rulesAndConfigsUtilsMockedStatic.close();
	}

	@Test
	public void formatStringToDate() {
		String date = "01-01-2019";
		String format = "dd-MM-yyyy";
		String outFormat = "MM/dd/yyyy";
		String outDate = "01/01/2019";

		Date actualResult = CommonServiceHelper.formatStringToDate(date, format);
		assertEquals(CommonServiceHelper.formatDateToString(actualResult, outFormat), outDate);

		date = "2019/12/28";
		format = "yyyy/MM/dd";
		outFormat = "MMM dd yyyy";
		outDate = "Dec 28 2019";

		actualResult = CommonServiceHelper.formatStringToDate(date, format);
		assertEquals(CommonServiceHelper.formatDateToString(actualResult, outFormat), outDate);

		date = "2019/JAN/28";
		format = "yyyy/MM/dd";

		actualResult = CommonServiceHelper.formatStringToDate(date, format);
		assertNull(actualResult);
	}

	@Test
	public void randomAlphanumeric() {
		String actualResult = CommonServiceHelper.randomAlphanumeric();

		assertEquals(12, actualResult.length());
		assertTrue(StringUtils.isAlphanumeric(actualResult));
	}

	@Test
	public void ObjectToJsonString() {
		Person person = new Person();
		person.setFirstName("Dave");
		person.setLastName("Bush");

		String actualResult = CommonServiceHelper.objectToJsonString(person);

		assertEquals("{\"firstName\":\"Dave\",\"lastName\":\"Bush\"}", actualResult);
	}

	@Test
	public void findRealmConfigurationValueByKey() {
		List<RealmConfiguration> configurations = new ArrayList<RealmConfiguration>();
		RealmConfiguration rc = new RealmConfiguration();
		RealmConfigurationId id = new RealmConfigurationId();
		id.setConfigKey("NEW_TO_RENEWAL_NUM_DAYS");
		rc.setId(id);
		rc.setConfigValue("5");
		configurations.add(rc);
		rc = new RealmConfiguration();
		id = new RealmConfigurationId();
		id.setConfigKey("SOME_OTHER_KEY");
		rc.setId(id);
		rc.setConfigValue("someValue");
		configurations.add(rc);

		String key = "SOME_OTHER_KEY";

		String actualResult = CommonServiceHelper.findRealmConfigurationValueByKey(configurations, key);

		assertEquals("someValue", actualResult);
	}

	// when primaryPlanCarriers contains BCBSCA and exchange is TNIII then BCBS out
	// of region plans
	// should be returned.
	@Test
	public void getOutOfRegionPlansToExclude_test1() {
		Company company = new Company();
		company.setCode("6PR");
		Realm realm = new Realm();
		realm.setBenExchange("TriNet III");
		company.setRealm(realm);
		RealmPlanYear rp = new RealmPlanYear();
		rp.setId(50);
		company.setRealmPlanYear(rp);
		Set<String> primaryPlanCarriers = new HashSet<>();
		primaryPlanCarriers.add("11");

		Set<String> bsOutOfRegionPlans = new HashSet<>();
		bsOutOfRegionPlans.add("BSPLAN1");

		when(realmDataDao.getBSOutOfRegionPlans(company)).thenReturn(bsOutOfRegionPlans);
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(company)).thenReturn(false);

		Set<String> actualResult = CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao);

		assertEquals(1, actualResult.size());
		assertTrue(actualResult.contains("BSPLAN1"));
	}

	// when primaryPlanCarriers contains BCBSCA and exchange is not TNIII then only
	// carrier out of region plans should be returned.
	@Test
	public void getOutOfRegionPlansToExclude_test2() {
		Company company = new Company();
		company.setCode("3MP");
		Realm realm = new Realm();
		realm.setBenExchange("TriNet II");
		company.setRealm(realm);
		RealmPlanYear rp = new RealmPlanYear();
		rp.setId(50);
		company.setRealmPlanYear(rp);
		Set<String> primaryPlanCarriers = new HashSet<>();
		primaryPlanCarriers.add("11");
		primaryPlanCarriers.add("29");
		ArgumentCaptor<Set> medplanGrpArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);

		Set<String> bsOutOfRegionPlans = new HashSet<>();
		bsOutOfRegionPlans.add("BSPLAN1");

		Set<String> carrierOutOfRegionPlans = new HashSet<>();
		carrierOutOfRegionPlans.add("BSPLAN2");
		
		when(realmDataDao.getCarrierOutOfRegionPlans(companyArgCaptor.capture(), medplanGrpArgCaptor.capture()))
				.thenReturn(bsOutOfRegionPlans);
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(company)).thenReturn(false);

		Set<String> actualResult = CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao);

		assertEquals(1, actualResult.size());
		assertTrue(actualResult.contains("BSPLAN1"));
		assertTrue(medplanGrpArgCaptor.getAllValues().get(0).containsAll(Arrays.asList("BSCA", "EM01")));
	}

	@Test
	public void extractMinFundingDetails() {
		Company company = new Company();
		company.setMinFundings(prepareMinFundings());

		MinimumFunding minFunding = CommonServiceHelper.extractMinFundingDetails("medical", company);

		assertEquals(BigDecimal.valueOf(1120), minFunding.getMinFundValue());
		assertEquals("FLT", minFunding.getMinFundType());
		assertEquals("medical", minFunding.getPlanType());

		minFunding = CommonServiceHelper.extractMinFundingDetails("dental", company);

		assertEquals(BigDecimal.valueOf(80), minFunding.getMinFundValue());
		assertEquals("PCT", minFunding.getMinFundType());
		assertEquals("dental", minFunding.getPlanType());

		minFunding = CommonServiceHelper.extractMinFundingDetails("DentalVoluntary", company);

		assertEquals(BigDecimal.valueOf(80), minFunding.getMinFundValue());
		assertEquals("PCT", minFunding.getMinFundType());
		assertEquals("dental", minFunding.getPlanType());

		minFunding = CommonServiceHelper.extractMinFundingDetails("vision", company);

		assertEquals(BigDecimal.valueOf(60), minFunding.getMinFundValue());
		assertEquals("PCT", minFunding.getMinFundType());
		assertEquals("vision", minFunding.getPlanType());

		minFunding = CommonServiceHelper.extractMinFundingDetails("VisionVoluntary", company);

		assertEquals(BigDecimal.valueOf(60), minFunding.getMinFundValue());
		assertEquals("PCT", minFunding.getMinFundType());
		assertEquals("vision", minFunding.getPlanType());
	}

	// @Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = CommonServiceHelper.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	private Set<MinimumFunding> prepareMinFundings() {
		Set<MinimumFunding> minFundings = new HashSet<>();
		MinimumFunding minFund = new MinimumFunding("medical", "FLT", BigDecimal.valueOf(1120), true);
		minFundings.add(minFund);
		minFund = new MinimumFunding("dental", "PCT", BigDecimal.valueOf(80), false);
		minFundings.add(minFund);
		minFund = new MinimumFunding("vision", "PCT", BigDecimal.valueOf(60), false);
		minFundings.add(minFund);
		return minFundings;
	}

	class Person {
		String firstName;
		String lastName;

		public Person() {

		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}
}
