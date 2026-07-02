package com.trinet.ambis.service.email.dto;

import com.trinet.ambis.configuration.BSSMessageConfig;

import lombok.Getter;
import lombok.Setter;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
@SuperBuilder
@Getter
@Setter
public class ClientConversionFailureEmailDto extends SupportEmailDto{

    private String companyCode;
    private String emailBody;

    @Override
    public String getEmailSubject() {
        return "Prospect to Client Conversion Failed - " + this.getCompanyCode();
    }

    @Override
    public String getEmailBody() {
        return emailBody;
    }

    @Override
    public String getToAddress() {
        return BSSMessageConfig.getProperty("service-now.email");
    }

}
