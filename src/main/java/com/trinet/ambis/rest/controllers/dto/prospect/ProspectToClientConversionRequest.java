package com.trinet.ambis.rest.controllers.dto.prospect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProspectToClientConversionRequest {

    private String streamEventId;
    private String prospectId;
    private String companyCode;
    private String riskType;
    private String bundleId;
    private String bundleName;
}
