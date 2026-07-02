package com.trinet.ambis.service.model.planAvailability;

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
public class HrisPlanResponse implements Serializable {

    private static final long serialVersionUID = 1L;
	private int planId;
    private String planName;
    private int carrierId;
    private String carrierName;
    private int dependentAgeLimit;
    private RateDetails rateDetails;

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties
    public static class RateDetails implements Serializable {

        private static final long serialVersionUID = 1L;
		private String rateType;
        private List<RatesByZip> ratesByZip;

        @Data
        @Builder(toBuilder = true)
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties
        public static class RatesByZip implements Serializable {

            private static final long serialVersionUID = 1L;
			private List<String> zips;
            private List<Rate> rates;

            @Data
            @Builder(toBuilder = true)
            @AllArgsConstructor
            @NoArgsConstructor
            @JsonIgnoreProperties
            public static class Rate implements Serializable {

                private static final long serialVersionUID = 1L;
				private String tierCode;
                private double rate;
            }
        }
    }
}
