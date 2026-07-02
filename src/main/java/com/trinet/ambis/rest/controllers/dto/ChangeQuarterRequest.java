package com.trinet.ambis.rest.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Request DTO for processing PeopleSoft quarter change
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeQuarterRequest {

    /**
     * new quarter
     */
    private String quarter;

    /**
     * message sequence for the quarter change process.
     */
    private String messageSeq;
}