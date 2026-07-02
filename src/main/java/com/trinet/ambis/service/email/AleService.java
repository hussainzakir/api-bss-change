package com.trinet.ambis.service.email;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.SubmissionInfo;

public interface AleService {

    /**
     * Sends a confirmation email to the specified company based on the provided submission information.
     *
     * @param company
     * @param submissionInfo
     */
    void sendConfirmationEmail(Company company, SubmissionInfo submissionInfo);

    /**
     * Checks if the company code matches the given bssCompanyId, and updates the aleUpdated flag.
     *
     * @param bssCompanyId
     * @param companyCode
     * @throws IllegalArgumentException
     */
    void updateAleChangeStatus(long bssCompanyId, String companyCode);

    }
