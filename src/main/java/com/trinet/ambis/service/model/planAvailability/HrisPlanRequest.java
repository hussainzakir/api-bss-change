package com.trinet.ambis.service.model.planAvailability;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class HrisPlanRequest {

    private String benefitsType;
    private String hqState;
    private String hqZipCode;
    private String effDate;
    private Long numOfEligibleWse;
    private String naicsCode;
    private List<LocationDetails> emplLocDetails;

    public static class HrisPlanRequestBuilder {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        public HrisPlanRequestBuilder effDate(String effDate) {
            if (isValidDateFormat(effDate)) {
                this.effDate = effDate;
            } else {
                throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
            }
            return this;
        }

        private boolean isValidDateFormat(String date) {
            dateFormat.setLenient(false);
            try {
                dateFormat.parse(date);
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
    }

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationDetails {

        private String homeState;
        private List<String> homeZipCodes;

    }

}
