package com.trinet.ambis.rest.controllers.dto.outputs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentByPlanType {
    private String benType;
    private String eeTier;
    private String eeSpouseTier;
    private String eeChildTier;
    private String eeFamilyTier;
    private String waivedTier;
    private String total;
    private boolean displayOnReport;
}
