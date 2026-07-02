package com.trinet.ambis.service.model;

import static org.junit.Assert.assertEquals;

import java.sql.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.model.RealmPlanYearConfiguration;
import com.trinet.ambis.persistence.model.RealmPlanYearConfigurationId;

@RunWith(JUnit4.class)
public class RealmPlanYearConfigurationTest {

	private static final String OE_QUARTER = "Q1";
	private static final String CONFIG_KEY = "KEY";
	private static final Date EFFDT = Date.valueOf( "1999-01-01" );
	private static final String CONFIG_VALUE = "VALUE";
	private static final String CONFIG_DESCR = "CFG_TEST";
	private static final Date ENDDT = java.sql.Date.valueOf( "2099-12-31" );

	private RealmPlanYearConfigurationId id;
	RealmPlanYearConfiguration config;

	@Before
	public void setup() {
		this.prepareRPYConfigInstance();
	}

	@Test
	public void setGetRealmPlanYearConfigurationTest() {
		String serializedConfig = CommonServiceHelper.objectToJsonString( this.config );
		RealmPlanYearConfiguration testConfig = this.config;
		RealmPlanYearConfigurationId testId = testConfig.getId();
		assertEquals( this.getJsonString(), serializedConfig );
		assertEquals( OE_QUARTER, testId.getOeQuarter() );
		assertEquals( CONFIG_KEY, testId.getConfigKey() );
		assertEquals( EFFDT, testId.getEffdt() );
		assertEquals( CONFIG_VALUE, testConfig.getConfigValue() );
		assertEquals( CONFIG_DESCR, testConfig.getConfigDesc() );
		assertEquals( ENDDT, testConfig.getEnddt() );
		
	}

	private void prepareRPYConfigInstance() {
		this.id = new RealmPlanYearConfigurationId();
		id.setOeQuarter( OE_QUARTER );
		id.setConfigKey( CONFIG_KEY );
		id.setEffdt( EFFDT );
		this.config = new RealmPlanYearConfiguration(); 
		config.setId( id );
		config.setConfigValue( CONFIG_VALUE );
		config.setConfigDesc( CONFIG_DESCR );
		config.setEnddt( ENDDT );
	}

	/**
	 * A constant JSON string that should match the serialized version of the object created by prepareRPYConfigInstance
	 * @return
	 */
	private String getJsonString() {
		return "{\"id\":{\"oeQuarter\":\"Q1\",\"configKey\":\"KEY\",\"effdt\":\"1999-01-01\"},\"configValue\":\"VALUE\",\"configDesc\":\"CFG_TEST\",\"enddt\":\"2099-12-31\"}";
	}
}