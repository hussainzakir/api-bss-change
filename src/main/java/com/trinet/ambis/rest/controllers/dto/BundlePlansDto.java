package com.trinet.ambis.rest.controllers.dto;

import com.trinet.ambis.service.model.bundle.BundleDetail;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BundlePlansDto {
    private String benefitType;
    private List<BundleDetail> bundleDetails;
}
