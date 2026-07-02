package com.trinet.ambis.service;

import com.trinet.ambis.rest.controllers.dto.ProspectDataUpdateRequest;

/**
 * Service for handling prospect data updates
 *
 * @author echavarria
 */
public interface ProspectDataService {

    /**
     * Updates prospect strategies based on location or NAICS code changes
     *
     * @param companyCode the company code
     * @param request the update request containing location/NAICS update flags and data
     */
    void updateProspectData(String companyCode, ProspectDataUpdateRequest request);
}

