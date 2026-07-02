package com.trinet.ambis.persistence.sp;


import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.object.StoredProcedure;

public class NextBenProgram extends StoredProcedure {
	private static final Logger BSS_LOGGER = LoggerFactory.getLogger(NextBenProgram.class);
	
	private static final String SP_NAME = "t2_bss.getNextBenProg";
    private static final String OUT_BENEFIT_PROG = " ";


   public NextBenProgram(DataSource dataSource) {
      super(dataSource, SP_NAME);
      declareParameter(new SqlOutParameter(OUT_BENEFIT_PROG, Types.VARCHAR));
      super.setFunction(true);
      compile();
   }
	
	public String execute() {
		BSS_LOGGER.info("calling sp: {} ",SP_NAME);      
       Map<String, Object>  output = super.execute();
       
       for (Map.Entry<String, Object> entry : output.entrySet())
       {
    	   BSS_LOGGER.info("KEY : {}\t VALUE : {}", entry.getKey(), entry.getValue());    	   
       }     
       BSS_LOGGER.info("End of Executing stored procedure : {}", SP_NAME);
       return (String) output.get(OUT_BENEFIT_PROG);       
	}
	
}
