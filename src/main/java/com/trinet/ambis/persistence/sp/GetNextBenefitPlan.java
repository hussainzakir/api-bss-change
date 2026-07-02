package com.trinet.ambis.persistence.sp;

import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.object.StoredProcedure;

public class GetNextBenefitPlan extends StoredProcedure {
	private static final Logger LOGGER = LoggerFactory.getLogger( GetNextBenefitPlan.class );
	private static final String SP_NAME = "t2_bss.getNextBenefitPlan";
	private static final String NEW_BENEFIT_PLAN = "A";

	public GetNextBenefitPlan( DataSource dataSource ) {
		super( dataSource, SP_NAME );
		declareParameter( new SqlOutParameter( NEW_BENEFIT_PLAN, Types.VARCHAR ) );
		super.setFunction( true );
		compile();
	}

	public String execute() {
		LOGGER.info( "calling sp: {} ", SP_NAME );
		Map<String,Object> output = super.execute();
		String benefitPlan = (String) output.get( NEW_BENEFIT_PLAN );
		return benefitPlan;
	}
}
