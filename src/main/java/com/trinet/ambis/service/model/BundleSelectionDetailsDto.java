package com.trinet.ambis.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleSelectionDetailsDto {
    private Long companyId;
    private String exchangeId;
}
