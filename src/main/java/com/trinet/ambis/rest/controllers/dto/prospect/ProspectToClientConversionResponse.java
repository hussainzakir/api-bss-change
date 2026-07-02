package com.trinet.ambis.rest.controllers.dto.prospect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProspectToClientConversionResponse {

	private long bssCompanyId;
    private boolean k1;

}
