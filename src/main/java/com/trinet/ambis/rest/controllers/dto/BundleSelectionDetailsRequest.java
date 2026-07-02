package com.trinet.ambis.rest.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for fetching bundle selection details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleSelectionDetailsRequest {

    @NotBlank
    private String companyCode;

    @NotEmpty
    @Valid
    private List<ExchangeDates> exchangeDatePairs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExchangeDates {

        @NotBlank
        private String exchange;

        @NotBlank
        private String effectiveDate;
    }
}


