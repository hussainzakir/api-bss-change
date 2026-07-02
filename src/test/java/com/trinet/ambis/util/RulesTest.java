package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.service.model.MinimumFunding;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class RulesTest {

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getAnnualCap() {
		BigDecimal actualResult = Rules.getAnnualCap("000TM9");

		assertEquals(BigDecimal.valueOf(50000), actualResult);

		actualResult = Rules.getAnnualCap("000SRO");

		assertEquals(BigDecimal.valueOf(200000), actualResult);

		actualResult = Rules.getAnnualCap("000TMA");

		assertEquals(BigDecimal.valueOf(400000), actualResult);

		actualResult = Rules.getAnnualCap("000TMB");

		assertEquals(BigDecimal.valueOf(750000), actualResult);

		actualResult = Rules.getAnnualCap("000SRT");

		assertEquals(BigDecimal.valueOf(250000), actualResult);

		actualResult = Rules.getAnnualCap("000TMF");

		assertEquals(BigDecimal.valueOf(250000), actualResult);

		actualResult = Rules.getAnnualCap(null);

		assertEquals(BigDecimal.ZERO, actualResult);
	}

	// HQ is TX and exchange is TNIII then min funding should be 50 PCT
	@Test
	public void overrideMiniumFunding_test1() {
		Company comp = createCompany();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		comp.setRealm(realm);
		comp.setHeadQuatersState("TX");

		Rules.overrideMiniumFunding(comp);

		verifyAsserts(comp);
	}

	// HQ is TX and exchange is TNII then min funding should be 50 PCT
	@Test
	public void overrideMiniumFunding_test2() {
		Company comp = createCompany();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_II.getBenExchng());
		comp.setRealm(realm);
		comp.setHeadQuatersState("FL");

		Rules.overrideMiniumFunding(comp);

		assertEquals(3, comp.getMinFundings().size());
		assertEquals(BigDecimal.valueOf(800), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("medical")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("FLT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("medical"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(75), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("dental")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("dental"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(250), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("vision")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("FLT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("vision"))
				.collect(Collectors.toList()).get(0).getMinFundType());
	}

	// HQ is FL and exchange is TNIII then min funding should be 50 PCT
	@Test
	public void overrideMiniumFunding_test3() {
		Company comp = createCompany();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		comp.setRealm(realm);
		comp.setHeadQuatersState("FL");

		Rules.overrideMiniumFunding(comp);

		verifyAsserts(comp);
	}

	// HQ is FL and exchange is TNII then min funding should be as is
	@Test
	public void overrideMiniumFunding_test4() {
		Company comp = createCompany();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_II.getBenExchng());
		comp.setRealm(realm);
		comp.setHeadQuatersState("FL");

		Rules.overrideMiniumFunding(comp);

		assertEquals(3, comp.getMinFundings().size());
		assertEquals(BigDecimal.valueOf(800), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("medical")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("FLT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("medical"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(75), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("dental")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("dental"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(250), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("vision")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("FLT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("vision"))
				.collect(Collectors.toList()).get(0).getMinFundType());
	}

	@Test
	public void overrideExceptionMiniumFunding() throws Exception {
		Company comp = createCompany();
		Set<MinimumFunding> minFundings = new HashSet<>();
		minFundings.add(new MinimumFunding("medical", "PCT", BigDecimal.valueOf(70), false));
		minFundings.add(new MinimumFunding("dental", "PCT", BigDecimal.valueOf(70), false));
		minFundings.add(new MinimumFunding("vision", "PCT", BigDecimal.valueOf(70), false));
		comp.setMinFundings(minFundings);

		Set<MinFundExceptionDto> minFundingsExceptions = new HashSet<>();
		MinFundExceptionDto mfeDto = new MinFundExceptionDto();
		mfeDto.setPlanType("10");
		mfeDto.setMinFundType("FLT");
		mfeDto.setMinFundValue(BigDecimal.valueOf(1200));
		minFundingsExceptions.add(mfeDto);

		Rules.overrideExceptionMiniumFunding(comp, minFundingsExceptions);

		assertEquals(BigDecimal.valueOf(1200), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("medical")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("FLT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("medical"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(70), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("dental")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("dental"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(70), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("vision")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("vision"))
				.collect(Collectors.toList()).get(0).getMinFundType());
	}

	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = Rules.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}
	
	private void verifyAsserts(Company comp) {
		assertEquals(3, comp.getMinFundings().size());
		assertEquals(BigDecimal.valueOf(50), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("medical")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("medical"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(50), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("dental")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("dental"))
				.collect(Collectors.toList()).get(0).getMinFundType());
		assertEquals(BigDecimal.valueOf(50), comp.getMinFundings().stream()
				.filter(e -> e.getPlanType().equals("vision")).collect(Collectors.toList()).get(0).getMinFundValue());
		assertEquals("PCT", comp.getMinFundings().stream().filter(e -> e.getPlanType().equals("vision"))
				.collect(Collectors.toList()).get(0).getMinFundType());
	}

	private Company createCompany() {
		Company comp = new Company();
		Set<MinimumFunding> minFundings = new HashSet<>();
		MinimumFunding medMinFund = new MinimumFunding("medical", "FLT", BigDecimal.valueOf(800), true);
		MinimumFunding denMinFund = new MinimumFunding("dental", "PCT", BigDecimal.valueOf(75), false);
		MinimumFunding visMinFund = new MinimumFunding("vision", "FLT", BigDecimal.valueOf(250), true);
		minFundings.add(medMinFund);
		minFundings.add(denMinFund);
		minFundings.add(visMinFund);
		comp.setMinFundings(minFundings);
		return comp;
	}

}
