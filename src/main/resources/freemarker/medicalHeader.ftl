
<#-- bgIndex is Zero for medical header
 -->
<#-- ALE -->
<#if exchange == TriNetI || exchange == TriNetII>
    <#if isEligAle == 'true' && AcaFplOpted != '0'>
    <div style="width:600px; ${font}">
    <p><b>You designated your company as an Applicable Large Employer under the Affordable Care Act (ACA).  We have applied ACA compliant funding to one plan in all regions. This ensures at least one offered plan meets the ACA affordability requirements.</b></p>
    </div>
    </#if>
</#if>

<#-- MEDICAL HEADER -->
<#if medicalContributionHeaders[0]?has_content>
<br/>
    <table border='1'  style="border-width: 0px; border-collapse: collapse;">
        <tr>
            <td  colspan='2' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 15px; background-color: #0077BC; color:white;">
                <p><strong>Medical</strong></p>
            </td>
        </tr>
        <tr>
            <td width='115' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Plan Carrier:</p>
            </td>
            <td width='485' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${medicalContributionHeaders[0].planCarriers!}</p>
            </td>
        </tr>
        <tr style="background-color: ${colorZebra};">
            <td colspan='2' width='600' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Offered Plans:</p>
                <p>${medicalContributionHeaders[0].benefitPlans!}</p>
            </td>
        </tr>
    </table>
    <br/>
</#if>