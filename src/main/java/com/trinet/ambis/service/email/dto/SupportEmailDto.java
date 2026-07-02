package com.trinet.ambis.service.email.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public abstract class SupportEmailDto {
    @Builder.Default
    private String userId = "00000000000";
    private String companyCode;
    private String companyName;
    private boolean sendToBSS;
    public abstract String getEmailSubject();
    public abstract String getEmailBody();
    public abstract String getToAddress();
}
