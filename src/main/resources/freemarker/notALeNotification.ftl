<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd/">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta http-equiv="Content-Style-Type" content="text/css">
    <style>
      p { font-family: Arial, sans-serif, 'Open Sans';}
      td { font-family: Arial, sans-serif, 'Open Sans';}
    </style>
</head>

<body>

<#global font = "font-family: Arial, sans-serif, 'Open Sans';">
<img src="${emailContentAssetsUrl}trinet-logo.svg" alt="Trinet-Logo"/>
<br />
<#assign currentYear = .now?string("yyyy")?number>

<#assign benStartDt = benefitStartDt?date("dd-MMM-yyyy")>
<#assign benStartDtYr = benStartDt?string("yyyy")?number>
<#assign previousYr = benStartDtYr - 1>
<p><strong><u>${benStartDtYr?string["####"]} CLIENT CERTIFICATION AS TO APPLICABLE LARGE EMPLOYER (ALE) STATUS</u></strong>
	<br /><br />I hereby certify that I have calculated the average Full-Time Equivalent (FTE) count (which for the purposes of this attestation includes full-time and part-time employees) on business days during the ${previousYr?string["####"]} calendar year, including FTE count of any controlled group members if applicable, in accordance with the requirements set forth under the Affordable Care Act (ACA).
	<br /><br /> I have determined that my company is <u>not an ALE</u> subject to the ACA's Employer Shared Responsibility provisions for the benefits plan year starting in ${benStartDtYr?string["####"]} because we had an average of fewer than 50 FTEs* on business days during ${previousYr?string["####"]}.
	<br /><br />* To calculate your company FTE count and determine ALE status you need to use monthly ${previousYr?string["####"]} employment data for all 12 months. You will need to make estimates for any months that have not yet concluded in ${previousYr?string["####"]}.
	<br /><br />By submitting this certification, I attest that I have been advised to consult with a tax or legal advisor and have carefully reviewed the ACA rules and regulations regarding the Employer Shared Responsibility provisions under Section 4980H of the Internal Revenue Code and the applicable Treasury Regulations thereunder. I understand that I will be responsible for any applicable reporting, including any potential liability related to reporting such as a Section 4980H tax penalty assessment under ACA Employer Shared Responsibility. My company shall be <strong><u>solely</u></strong> responsible for responding to such notice and for paying any Code Section 4980H penalties that may be assessed. 
	<br /><br /><strong>What is Next?</strong><br /><br />
	Please retain a copy of this confirmation for your records. You will have an opportunity to submit a new ALE designation annually. However, your company's designation as a non-ALE will remain in effect unless and until a new certification is submitted by an authorized representative for a subsequent year. 
	<br /><br />
	&#169; ${currentYear?string["####"]} TriNet Group, Inc. All rights reserved. This communication is for informational purposes only, is not legal, tax or accounting advice, and is not an offer to sell, buy or procure insurance. TriNet is the single-employer sponsor of all its benefit plans, which does not include voluntary benefits that are not ERISA-covered group health insurance plans and enrollment is voluntary. Official plan documents always control and TriNet reserves the right to amend the benefit plans or change the offerings and deadlines.
	<p style="font-size: 7px; font-style: italic; color:lightgray">Email ID: 991</p>

</body>

</html>