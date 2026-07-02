package com.trinet.ambis.service.model;

import java.util.List;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class HrisPlanAttributeRequest {

    private List<String> planIds;
    
    private String benefitType;
}
