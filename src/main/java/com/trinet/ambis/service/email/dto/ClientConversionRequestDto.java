package com.trinet.ambis.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * This is the request DTO used to send prospect and stream event information
 * when a prospect-to-client conversion fails.
 * */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientConversionRequestDto {
    private String prospectId;
    private String psCompanyCode;
    private String streamEventId;
}
