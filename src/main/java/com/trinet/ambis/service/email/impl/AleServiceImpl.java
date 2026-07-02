package com.trinet.ambis.service.email.impl;

import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.helper.EmailServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.email.AleService;
import com.trinet.ambis.service.email.EmailNotificationService;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.model.notification.NotificationRequestParam;
import com.trinet.ambis.service.model.notification.Recipient;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AleServiceImpl implements AleService {

    @Autowired
    CompanyService companyService;

    @Autowired
    private PersonService personService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    private static final String TO = "to";
    private static final String CC = "cc";
    private static final String BCC = "bcc";

    @Override
    public void sendConfirmationEmail(Company company, SubmissionInfo submissionInfo) {
        List<Recipient> recipients = new ArrayList<>();
        recipients.add(createRecipient(personService.getCSAuthEmail(submissionInfo.getSubmitStatusInfo().getUserId()), TO));
        recipients.add(createRecipient(personService.getCompanyHrmEmail(company.getCode()), BCC));
        recipients.add(createRecipient(BSSMessageConfig.getProperty("acacoeEmailId"), CC));
        submissionInfo.getEmailInfo().setClientRecipients(recipients);

        if (CollectionUtils.isNotEmpty(submissionInfo.getEmailInfo().getClientRecipients())) {
            NotificationRequestParam aleRequest = EmailServiceHelper.createAleNotificationRequest(company,
                    submissionInfo);
            emailNotificationService.sendConfirmationEmail(aleRequest);
            submissionInfo.getEmailInfo().setAleEmailSent(true);
        }
    }

    private Recipient createRecipient(String emailId, String recipientType) {
        Recipient recipient = new Recipient();
        recipient.setId(emailId);
        recipient.setType(recipientType);
        return recipient;
    }

    @Override
    public void updateAleChangeStatus(long bssCompanyId, String companyCode) {
        Company company = Optional.ofNullable(companyService.findByCompanyId(bssCompanyId))
                .orElseThrow(() -> new IllegalArgumentException("Company not found for ID: " + bssCompanyId));

        if (!company.getCode().equalsIgnoreCase(companyCode)) {
            throw new IllegalArgumentException("Company code does not match the provided company ID.");
        }
        companyService.updateAleUpdatedFlag(company, 1);
    }
}