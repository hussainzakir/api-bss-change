package com.trinet.ambis.service.outputs;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.TitlePageData;

public interface TitlePageService {

   /**
    * This method returns the data required to generate the output report title page
    * 
    * @param outputRequest
    * @param company
    * 
    * @return OutputData -- set title page to output data
    */
    TitlePageData getTitlePageData(OutputRequest outputRequest, Company company);
}
