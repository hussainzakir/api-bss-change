package com.trinet.ambis.service.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenefitsBundleDto {
    private String quarter;
    private List<BundleDetails> bundles;

    @Data
    @Builder
    public static class BundleDetails {
        private long id;
        private String name;
    }
}