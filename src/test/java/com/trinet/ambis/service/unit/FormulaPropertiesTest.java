package com.trinet.ambis.service.unit;


import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.service.model.FormulaDefinition;
import com.trinet.ambis.service.model.FormulaProperties;
import com.trinet.ambis.util.Constants;

/**
 * @author mikebro
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class FormulaPropertiesTest {

	@Test
	public void calculateLifeADDCoverageTest() {

		BigDecimal salBase = new BigDecimal( "175000.23" );
		FormulaProperties fp = this.prepareFormulaPropertiesMockData1();
		BigDecimal actualResult = fp.calculateLifeADDCoverage( salBase );
		assertEquals( new BigDecimal( "526000" ), actualResult);

		// try a lower max coverage
		fp.setMaxCovrg( BigDecimal.valueOf( 500000 ) );
		actualResult = fp.calculateLifeADDCoverage( salBase );
		assertEquals( new BigDecimal( "500000" ), actualResult);

		// test the min coverage
		salBase = new BigDecimal( "1000.23" );
		actualResult = fp.calculateLifeADDCoverage( salBase );
		assertEquals( new BigDecimal( "10000" ), actualResult);

	}

	@Test
	public void calculateDisabilityCoverageTest() {

		BigDecimal salBase = new BigDecimal( "375000" );
		FormulaProperties fp = this.prepareFormulaPropertiesMockData2();
		BigDecimal actualResult = fp.calculateDisabilityCoverage( salBase );
		assertEquals( new BigDecimal( "20000" ), actualResult);

		// test a smaller max base (below MaxBenefitBase)
		fp.setMaxBenefitBase( BigDecimal.valueOf( 102000 ) );
		actualResult = fp.calculateDisabilityCoverage( salBase );
		assertEquals( new BigDecimal( "8500" ), actualResult);

	}

	@Test
	public void calculateSillyTest() {

		BigDecimal salBase = new BigDecimal( "375000" );
		FormulaProperties fp = this.prepareFormulaPropertiesMockData3();
		BigDecimal actualResult = fp.calculateLifeADDCoverage( salBase );
		assertEquals( new BigDecimal( "9" ), actualResult);

		// test a different formula definition with plus
		fp.setFormulaDefs( this.prepareFormulaDefinitionMockData4() );
		actualResult = fp.calculateLifeADDCoverage( salBase );
		assertEquals( new BigDecimal( "9" ), actualResult);

		// test a different formula definition with minus
		fp.setFormulaDefs( this.prepareFormulaDefinitionMockData5() );
		actualResult = fp.calculateLifeADDCoverage( salBase );
		assertEquals( new BigDecimal( "1" ), actualResult);

	}



	private FormulaProperties prepareFormulaPropertiesMockData1() {
		FormulaProperties fp = new FormulaProperties();
		fp.setPlanType( Constants.LIFE );
		fp.setBenefitPlan( "3XLIFE" );
		fp.setFormulaID( "03AS0000TB" );
		fp.setFormulaEffDt( java.sql.Date.valueOf( "2019-01-01" ) );
		fp.setBaseSource( "ABBR" );
		fp.setMaxBenefitBase( BigDecimal.ZERO );
		fp.setMinCovrg( BigDecimal.valueOf( 10000 ) );
		fp.setMaxCovrg( BigDecimal.valueOf( 1000000 ) );
		fp.setCoverageAsOfDate( java.sql.Date.valueOf( "2018-06-01" ) );
		fp.setPremiumAsOfDate( java.sql.Date.valueOf( "2018-06-01" ) );
		List<FormulaDefinition> formulaDefs = this.prepareFormulaDefinitionMockData1();
		fp.setFormulaDefs( formulaDefs );
		return fp;
	}

	private List<FormulaDefinition> prepareFormulaDefinitionMockData1() {
		List<FormulaDefinition> data = new ArrayList<FormulaDefinition>();
		data.add( objectFromDefData( 10, "(", " ",   0,0,0 ));
		data.add( objectFromDefData( 20, "(", " ",   0,0,0 ));
		data.add( objectFromDefData( 30, "(", " ",   0,0,0 ));
		data.add( objectFromDefData( 40, " ", "BASE",0,0,0 ));
		data.add( objectFromDefData( 50, ")", " ",   0,0,0 ));
		data.add( objectFromDefData( 70, "*", "CNST",3,0,0 ));
		data.add( objectFromDefData( 80, ")", " ",   0,0,0 ));
		data.add( objectFromDefData( 90, "R", " ",   0,0.01,1000 ));
		return data;
	}


	private FormulaProperties prepareFormulaPropertiesMockData2() {
		FormulaProperties fp = new FormulaProperties();
		fp.setPlanType( Constants.STD );
		fp.setBenefitPlan( "000JNH" );
		fp.setFormulaID( "DSAS240KCA" );
		fp.setFormulaEffDt( java.sql.Date.valueOf( "2019-01-01" ) );
		fp.setBaseSource( "ABBR" );
		fp.setMaxBenefitBase( BigDecimal.valueOf( 240000 ) );
		fp.setMinCovrg( BigDecimal.ZERO );
		fp.setMaxCovrg( BigDecimal.ZERO );
		fp.setCoverageAsOfDate( java.sql.Date.valueOf( "2018-06-01" ) );
		fp.setPremiumAsOfDate( java.sql.Date.valueOf( "2018-06-01" ) );
		List<FormulaDefinition> formulaDefs = this.prepareFormulaDefinitionMockData2();
		fp.setFormulaDefs( formulaDefs );
		return fp;
	}

	private List<FormulaDefinition> prepareFormulaDefinitionMockData2() {
		List<FormulaDefinition> data = new ArrayList<FormulaDefinition>();
		data.add( objectFromDefData( 10, "(", " "   , 0, 0,0 ) );
		data.add( objectFromDefData( 20, " ", "BASE", 0, 0,0 ) );
		data.add( objectFromDefData( 30, "/", "CNST", 12,0,0 ) );
		data.add( objectFromDefData( 40, ")", " "   , 0, 0,0 ) );
		return data;
	}


	private FormulaProperties prepareFormulaPropertiesMockData3() {
		FormulaProperties fp = new FormulaProperties();
		fp.setPlanType( Constants.STD );
		fp.setBenefitPlan( "BOGUS" );
		fp.setFormulaID( "EXTRABOGUS" );
		fp.setFormulaEffDt( java.sql.Date.valueOf( "2019-01-01" ) );
		fp.setBaseSource( "ABBR" );
		fp.setMaxBenefitBase( BigDecimal.valueOf( 110000 ) );
		fp.setMinCovrg( BigDecimal.ZERO );
		fp.setMaxCovrg( BigDecimal.ZERO );
		fp.setCoverageAsOfDate( java.sql.Date.valueOf( "2018-06-01" ) );
		fp.setPremiumAsOfDate( java.sql.Date.valueOf( "2018-06-01" ) );
		List<FormulaDefinition> formulaDefs = this.prepareFormulaDefinitionMockData3();
		fp.setFormulaDefs( formulaDefs );
		return fp;
	}

	private List<FormulaDefinition> prepareFormulaDefinitionMockData3() {
		List<FormulaDefinition> data = new ArrayList<FormulaDefinition>();
		data.add( objectFromDefData( 10, "(", " "   , 0, 0,0 ) );
		data.add( objectFromDefData( 20, " ", "CNST", 1, 0,0 ) );
		data.add( objectFromDefData( 30, "+", "CNST", 2, 0,0 ) );
		data.add( objectFromDefData( 40, ")", " "   , 0, 0,0 ) );
		data.add( objectFromDefData( 50, "*", "CNST", 3, 0,0 ) );
		return data;
	}

	private List<FormulaDefinition> prepareFormulaDefinitionMockData4() {
		List<FormulaDefinition> data = new ArrayList<FormulaDefinition>();
		data.add( objectFromDefData( 20, " ", "CNST", 1, 0,0 ) );
		data.add( objectFromDefData( 30, "+", "CNST", 2, 0,0 ) );
		data.add( objectFromDefData( 50, "*", "CNST", 3, 0,0 ) );
		return data;
	}

	private List<FormulaDefinition> prepareFormulaDefinitionMockData5() {
		List<FormulaDefinition> data = new ArrayList<FormulaDefinition>();
		data.add( objectFromDefData( 20, " ", "CNST", 3, 0,0 ) );
		data.add( objectFromDefData( 30, "-", "CNST", 2, 0,0 ) );
		data.add( objectFromDefData( 50, "*", "CNST", 1, 0,0 ) );
		return data;
	}



	private FormulaDefinition objectFromDefData( long bnSeqNum, String benOperand
			, String bnEntryTyp, long bnValue, double roundUpAmt, long roundTo ) {
		FormulaDefinition r = new FormulaDefinition();
		r.setBenOperand( benOperand );
		r.setBnEntryTyp( bnEntryTyp );
		r.setBnValue( BigDecimal.valueOf( bnValue ) );
		r.setRoundUpAmt( BigDecimal.valueOf( roundUpAmt ) );
		r.setRoundTo( BigDecimal.valueOf( roundTo ) );
		return r;
	}



}