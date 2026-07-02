package com.trinet.ambis.service.dto;

import com.trinet.ambis.service.model.ExceptionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LifeDisabilityBandOverrideDto extends ExceptionDto {

    @NotNull
    private String lifeBand;

    @NotNull
    private String disBand;
}
