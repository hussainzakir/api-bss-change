package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.ps.impl.BnRateDataForInsert;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class BnRateDataForInsertTest {

	@Before
	public void setup() {
		;
	}

	@Test
	public void createRateDataObjTest() {
		final String     RATETBLID = "RATEID";
		final String     EFFDT = "11/12/1234";
		final String     BNRATEKEY01 = "KEY01";
		final String     BNRATEKEY02 = "KEY02";
		final String     BNRATEKEY03 = "KEY03";
		final BigDecimal EMPLRATE = new BigDecimal("1");
		final BigDecimal EMPLRRATE = new BigDecimal("2");
		final BigDecimal BTAXRATE = new BigDecimal("3");
		final BigDecimal ATAXRATE = new BigDecimal("4");
		final BigDecimal NTAXRATE = new BigDecimal("5");
		final BigDecimal TTAXRATE = new BigDecimal("6");
		final BigDecimal PTAXRATE = new BigDecimal("7");
		final String     OTHERRATESEXIST = "EXIST";
		final BigDecimal PROVCOVRGRATE = new BigDecimal("8");
		final String     PFCLIENT = "1234";

		final String PLANTYPE = "10";
		final String QUARTER = "Q1";
		final String BANDCODE = "A1";
		
		BnRateDataForInsert data = new BnRateDataForInsert();

		// superclass fields
		data.setRateTblId( RATETBLID );
		data.setEffdt( EFFDT );
		data.setBnRateKey01( BNRATEKEY01 );
		data.setBnRateKey02( BNRATEKEY02 );
		data.setBnRateKey03( BNRATEKEY03 );
		data.setBnEmplRate( EMPLRATE );
		data.setBnEmplrRate( EMPLRRATE );
		data.setBnBTaxRate( BTAXRATE );
		data.setBnATaxRate( ATAXRATE );
		data.setBnNTaxRate( NTAXRATE );
		data.setBnTTaxRate( TTAXRATE );
		data.setBnPTaxRate( PTAXRATE );
		data.setOtherRatesExist( OTHERRATESEXIST );
		data.setT2ProvCovrgRate( PROVCOVRGRATE );
		data.setPfClient( PFCLIENT );

		// subclass fields
		data.setPlanType( PLANTYPE );
		data.setQuarter( QUARTER );
		data.setBandCode( BANDCODE );
		String string = data.toString();

		assertTrue( string.length() > 1 );
		assertEquals( RATETBLID, data.getRateTblId() );
		assertEquals( EFFDT, data.getEffdt() );
		assertEquals( BNRATEKEY01, data.getBnRateKey01() );
		assertEquals( BNRATEKEY02, data.getBnRateKey02() );
		assertEquals( BNRATEKEY03, data.getBnRateKey03() );
		assertEquals( EMPLRATE, data.getBnEmplRate() );
		assertEquals( EMPLRRATE, data.getBnEmplrRate() );
		assertEquals( BTAXRATE, data.getBnBTaxRate() );
		assertEquals( ATAXRATE, data.getBnATaxRate() );
		assertEquals( NTAXRATE, data.getBnNTaxRate() );
		assertEquals( TTAXRATE, data.getBnTTaxRate() );
		assertEquals( PTAXRATE, data.getBnPTaxRate() );
		assertEquals( OTHERRATESEXIST, data.getOtherRatesExist() );
		assertEquals( PROVCOVRGRATE, data.getT2ProvCovrgRate() );
		assertEquals( PFCLIENT, data.getPfClient() );

		assertEquals( PLANTYPE, data.getPlanType() );
		assertEquals( QUARTER, data.getQuarter() );
		assertEquals( BANDCODE, data.getBandCode() );
	}
}