package com.trinet.ambis.rest.controllers.dto;

import com.trinet.ambis.common.ApiBssPropertiesConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.email.dto.SupportEmailDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@SuperBuilder
@Getter
@Setter
public class ProcessFailureEmailDto extends SupportEmailDto {

    private String processName;
    private String errorMessage;
    private Set<Long> processStatusIds;

    @Override
    public String getEmailSubject() {
        return "BSS Process Failure - " + getProcessName() + " - " + getCompanyCode();
    }

    @Override
    public String getEmailBody() {
        return "Dear BSS-Dev Team,<BR><BR>"
                + "A process failure has occurred in BSS.<BR><BR>"
                + "Process Name: " + getProcessName() + "<BR>"
                + "Company Code: " + getCompanyCode() + "<BR>"
                + "Error Message: "
                + (getErrorMessage() != null ? getErrorMessage() : "N/A") + "<BR>"
                + "Process Status Id's: " + processStatusIds + "<BR><BR>"
                + "Please investigate the issue.<BR><BR>"
                + "Best Regards,<BR>"
                + "Benefit Strategy Solutions";
    }

    @Override
    public String getToAddress() {
        return BSSMessageConfig.getProperty(ApiBssPropertiesConstants.BSS_TEAM_TO_ADDRESS);
    }
}
