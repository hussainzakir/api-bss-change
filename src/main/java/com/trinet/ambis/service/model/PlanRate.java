package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class PlanRate implements Serializable {

    private static final long serialVersionUID = 2025102912345678901L;
    private String planType;
    private String dpPlanType;
    private String planId;
    private String regionalPlanId;
    private String dpRegionalPlanId;
    private RateDetails rateDetails;

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties
    public static class RateDetails implements Serializable {

        private static final long serialVersionUID = 2025102912345678901L;
        private String rateType;
        private List<Rate> rates;

        @Data
        @Builder(toBuilder = true)
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties
        public static class Rate implements Serializable {

            private static final long serialVersionUID = 2025102912345678901L;
            private String tierCode;
            private Double retailRate;
            private Double payInRate;
        }
    }
}
