package com.trinet.ambis.rest.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating prospect data on location or NAICS code changes
 *
 * @author echavarria
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProspectDataUpdateRequest {

    /**
     * Indicates whether location has been updated
     */
    private boolean locationUpdate;

    /**
     * Indicates whether NAICS code has been updated
     */
    private boolean naicsCodeUpdate;

    /**
     * The new NAICS code value (2-6 digits, numeric)
     */
    private String naicsCode;
}
