package com.trinet.ambis.rest.controllers.dto;

import com.trinet.ambis.client.DefaultPlanMappingServiceClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for creating strategies for a prospect company.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStrategiesRequest {

    @NotNull(message = "bundleId is required")
    private Long bundleId;

    @NotNull(message = "exchangeId is required")
    @NotEmpty(message = "exchangeId is required")
    private String exchangeId;

    @NotNull(message = "selectedCarrierId is required")
    private Long selectedCarrierId;

    @NotEmpty(message = "planMappingResponse is required")
    private List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse;
}