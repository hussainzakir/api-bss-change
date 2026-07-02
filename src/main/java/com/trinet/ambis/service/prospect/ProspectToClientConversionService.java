package com.trinet.ambis.service.prospect;

import com.trinet.ambis.rest.controllers.dto.prospect.ProspectToClientConversionRequest;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectToClientConversionResponse;

public interface ProspectToClientConversionService {

	ProspectToClientConversionResponse processProspectToClientConversion(ProspectToClientConversionRequest request);
}
