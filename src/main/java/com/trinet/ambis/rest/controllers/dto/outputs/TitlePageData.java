package com.trinet.ambis.rest.controllers.dto.outputs;


import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TitlePageData {

    private String loggedInUser;
    private String exchange;
    private String zipcode;
    private String expirationDate;
    private List<String> regions;
    private List<String> employeeRegions;
    private String proposalId;
    private String effectiveDate;
    private List<String> additionalZipCodes;
    private String groupName;
    private String planYearStartDate;
    private String planYearEndDate;
}
