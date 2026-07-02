package com.trinet.ambis.persistence.sp;

import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.object.StoredProcedure;


public class NextRateTblID extends StoredProcedure  {

	private static final Logger BSS_LOGGER = LoggerFactory.getLogger(NextRateTblID.class);
	
   private static final String SP_NAME = "t2_bss.getNextRateTblId";
   private static final String OUT_RATE_TBL_ID = " ";

   public NextRateTblID(DataSource dataSource) {
      super(dataSource, SP_NAME);
      declareParameter(new SqlOutParameter(OUT_RATE_TBL_ID, Types.VARCHAR));
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
       String rateTblID = (String) output.get(OUT_RATE_TBL_ID);
       
       BSS_LOGGER.info("End of Executing stored procedure : {}", SP_NAME);
       return rateTblID;
	}

}
