package com.trinet.ambis.service.model;

import java.util.List;

import lombok.Data;

/**
 * @author jshuali
 * 
 * Use by Freemarker to generate the confirmation email.
 *
 */
@Data
public class ContributionHeader {

    private String planCarriers;
    private String benefitPlans;
    private String fundingBasePlan;
    private String companyPercent;
    private String coverageLevel;
    private String employeePercent;
    private String employeePlusSpousePercent;
    private String employeePlusChildPercent;
    private String employeePlusFamilyPercent;
    private String waiverAllowance;
    private String employeeFlatMax;
    private String spouseFlatMax;
    private String childFlatMax;
    private String familyFlatMax;
    private String fbpEmployeeLimit;
    private String fbpEmployeePlusSpouseLimit;
    private String fbpEmployeePlusChildLimit;
    private String fbpEmployeePlusFamilyLimit;
    private String isWaiverAllowance;
    private String isFundingFlatMax;
    private String fundingTypeDescription;
    private String fundingTypeCode;
    private String disability;
    private String life;
    private String commuter;
    private String surBenSupplementId;
    private String surBenSupplement;
    private List<String> surPlanAllocation;
     
}