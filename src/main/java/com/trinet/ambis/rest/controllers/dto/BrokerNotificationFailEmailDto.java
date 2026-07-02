package com.trinet.ambis.rest.controllers.dto;

import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.email.dto.SupportEmailDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class BrokerNotificationFailEmailDto extends SupportEmailDto {

    private String exchange;
    private String quarter;
    private String internalOpenDate;

    @Override
    public String getEmailSubject() {
        return "BSS Broker Notification API Failure - Exchange: " + exchange + ", Quarter: " + quarter + ", Internal Open Date: " + internalOpenDate;
    }

    @Override
    public String getEmailBody() {
        return  "Dear BSS-Dev Team,<br><br>" +
                "The BSS Broker Notification API call has failed for the following details:<br>" +
                "Exchange: " + exchange + "<br>" +
                "Quarter: " + quarter + "<br>" +
                "Internal Open Date: " + internalOpenDate + "<br><br>" +
                "Please investigate the issue and take necessary actions to resolve it.<br><br>" +
                "Best Regards,<br>" +
                "BSS System";
    }

    @Override
    public String getToAddress() {
        return BSSMessageConfig.getProperty("bssTeamToAddress");
    }

}