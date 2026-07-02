
package com.trinet.ambis.service;
import com.trinet.ambis.service.model.SchedTblDto;
import javax.servlet.http.HttpServletRequest;

/**
 * Service for sending broker notifications to api-profile service
 */
public interface BrokerNotificationService {

    /**
     * Validates schedule internal open date changes and invokes broker notification API.
     *
     * @param request the HTTP servlet request containing request context and user information
     * @param schedTblDto the schedule table DTO containing new schedule information
     */
    void validateAndSendBrokerNotification(HttpServletRequest request, SchedTblDto schedTblDto);
}