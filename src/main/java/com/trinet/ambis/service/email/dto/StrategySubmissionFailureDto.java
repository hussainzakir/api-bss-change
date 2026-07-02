package com.trinet.ambis.service.email.dto;


import com.trinet.ambis.configuration.BSSMessageConfig;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@Getter
@Setter
public class StrategySubmissionFailureDto extends SupportEmailDto{
     String confirmationNumber;

    @Override
    public String getEmailSubject() {
        return "BSS Submit to PeopleSoft Failed";
    }

    @Override
    public String getEmailBody() {
        if (this.getConfirmationNumber() == null || this.getConfirmationNumber().isEmpty()) {
            throw new IllegalStateException("Confirmation Number must be set before calling getEmailBody()");
        }
        if (this.getCompanyCode() == null || this.getCompanyCode().isEmpty()) {
            throw new IllegalStateException("Company Code must be set before calling getEmailBody()");
        }

        return "Tier II Support Team, <BR><BR>"
                + "An error was encountered submitting to PeopleSoft the selected Benefit Strategy for: <BR><BR>"
                + "Company : " + this.getCompanyCode() + "<BR><BR>" + "Confirmation Number: " + this.getConfirmationNumber() + "<BR><BR>"
                + "Sincerely,<BR><BR>Benefit Strategy Solutions";
    }
    
    @Override
    public String getToAddress() {
        return BSSMessageConfig.getProperty("Tier2SupportEmail");
    }

}
