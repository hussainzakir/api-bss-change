package com.trinet.ambis.service;

import com.trinet.ambis.service.dto.LifeDisabilityBandOverrideDto;

public interface LifeDisabilityBandOverrideService {

    /**
     * Creates a new life/disability band override for the provided request data.
     *
     * @param lifeDisabilityBandOverridedto override details to persist
     */
    void createOverride(LifeDisabilityBandOverrideDto lifeDisabilityBandOverridedto);
}
