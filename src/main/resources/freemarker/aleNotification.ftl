<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd/">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta http-equiv="Content-Style-Type" content="text/css" />
        <style>
            p {
                font-family: Arial, sans-serif, "Open Sans";
            }
            td {
                font-family: Arial, sans-serif, "Open Sans";
            }
        </style>
    </head>

<body>
<#global font = "font-family: Arial, sans-serif, 'Open Sans';">
<img src="${emailContentAssetsUrl}trinet-logo.svg" alt="Trinet-Logo"/>
</br>
<#assign currentYear = .now?string("yyyy")?number>

<#assign benStartDt = benefitStartDt?date("dd-MMM-yyyy")>
<#assign benStartDtYr = benStartDt?string("yyyy")?number>
<#assign reportingYr = benStartDtYr + 1>
<p>
    Thank you for submitting your company's Applicable Large Employer (ALE) designation, as defined under the Affordable Care Act (ACA). Below is a summary of your selections.
    <br />
    <br />
    <strong>Note:</strong> If you represent a company that is a subsidiary of or commonly owned by other entities, you will need to certify each ALE status separately for each TriNet client company you represent by logging in-to TriNet- <a href="https://login.trinet.com/" target="_blank" rel="noopener">(login.TriNet.com)</a> for each client ID.
    <br />
    <br />
    <strong>ALE Status</strong> <br />
    <br />
    Yes, my company is an ALE subject to ACA Employer Shared Responsibility for the ${benStartDtYr?string["####"]} calendar year.
    <br />
    <br />
    <strong>What's Next?</strong><br />
    <br />
    Please retain a copy of this confirmation for your records. You will have an opportunity to submit a new ALE designation annually. However, your company's designation as an ALE will remain in effect unless and until a new certification is submitted by an authorized representative for a subsequent year.
    <br />
    <br />
    <u><strong>Affordability Requirement</strong></u><br />
    To maintain compliance with the ACA mandate, it is recommended that you offer at least one ACA affordable plan based on the Federal Poverty Level (FPL) safe harbor (i.e., the cost to the worksite employee will not exceed $${fplAmount} for ${benStartDtYr?string["####"]} (as indexed) of the FPL), or you may face potential penalties.
    <br />
    <br />
    For further information, reach out to TriNet. Reporting Requirements as set forth under Internal Revenue Code Section 6056 of the ACA requires that your company be accountable for the data that is reported to the IRS whether it offers full-time employees and dependents an opportunity to enroll in an employer sponsored medical plan and, if so, provide the IRS with the coverage and rate information.
    <br />
    <br />
    If you designate your company as an ALE, it will be included in the annual required Section 6056 ACA reporting for the next calendar year. The ${benStartDtYr?string["####"]} Section 6056 reporting is due in early ${reportingYr?string["####"]}.
    <br />
    <br />
    Please retain a copy of this confirmation for your records. You will have an opportunity to submit a new ALE designation prior to the start of each calendar year. If you fail to submit a new designation your company's designation as an ALE will remain in effect until a new certification is submitted by an authorized company representative for a subsequent calendar year. If you have any questions about the ACA’s ALE requirements, please reach out to TriNet.<br />
    <br />
    <br />
    &#169; ${currentYear?string["####"]} TriNet Group, Inc. All rights reserved. This communication is for informational purposes only, is not legal, tax or accounting advice, and is not an offer to sell, buy or procure insurance. TriNet is the single-employer sponsor of all its benefit plans, which does not include voluntary benefits that are not ERISA-covered group health insurance plans and enrollment is voluntary. Official plan documents always control and TriNet reserves the right to amend the benefit plans or change the offerings and deadlines.
</p>
<p style="font-size: 7px; font-style: italic; color: lightgray;">Email ID: 990</p>
</body>
</html>