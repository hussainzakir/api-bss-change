
<#assign months = ["", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]>

<#assign q1 = 0>
<#assign q2 = 0>
<#assign q3 = 0>
<#assign q4 = 0>
<#assign annualMonth = 0>

<#if hsaFunding.q1Month?has_content && (hsaFunding.q1Month > 0) && (hsaFunding.q1Month < 13)>
    <#assign q1 = hsaFunding.q1Month>
</#if>
<#if hsaFunding.q2Month?has_content && (hsaFunding.q2Month > 0) && (hsaFunding.q2Month < 13)>
    <#assign q2 = hsaFunding.q2Month>
</#if>
<#if hsaFunding.q3Month?has_content && (hsaFunding.q4Month > 0) && (hsaFunding.q3Month < 13)>
    <#assign q3 = hsaFunding.q3Month>
</#if>
<#if hsaFunding.q4Month?has_content && (hsaFunding.q4Month > 0) && (hsaFunding.q4Month < 13)>
    <#assign q4 = hsaFunding.q4Month>
</#if>
<#if hsaFunding.annualMonth?has_content && (hsaFunding.annualMonth > 0) && (hsaFunding.annualMonth < 13)>
    <#assign annualMonth = hsaFunding.annualMonth>
</#if>

<#if hsaFunding.optionId == 5>
    <table border='1' style="border-width: 1px; border-collapse: collapse;">
        <tr>
            <td colspan='2' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 15px; background-color: #0077BC; color:white;">
                <p><strong>Company Contribution to Worksite Employee HSAs - Monthly</strong></p>
            </td>
        </tr>
        <tr>
            <td width='160' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p><strong>Coverage Level</strong></p>
            </td>
            <td width='440' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px;">
                <p><strong>Company Contribution/Month</strong></p>
            </td>
        </tr>
        <tr>
            <td width='160' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Employee Only</p>
            </td>
            <td align="center" width=440 style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.monthlyEeAmount?string.currency)!"$0.00"}</p>
            </td>
        </tr>
        <tr style="background-color: ${colorZebra};">
            <td width='160'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Family</p>
            </td>
            <td align="center" width=440 style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.monthlyFamilyAmount?string.currency)!"$0.00"}</p>
            </td>
        </tr>
    </table>
<#elseif (hsaFunding.optionId > 0) && ((hsaFunding.lumpSumFrequency)! == 'Q')>
    <table border='1' style="border-width: 1px; border-collapse: collapse;">
        <tr>
            <td colspan='3' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 15px; background-color: #0077BC; color:white;">
                <p><strong>Company Frontload Contribution to Worksite Employee HSAs - Quarterly</strong></p>
            </td>
        </tr>
        <tr>
            <td width='200' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px;">
                <p><strong>Quarter/Frontload Month</strong></p>
            </td>
            <td width='190' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px;">
                <p><strong>Coverage Level</strong></p>
            </td>
            <td width='200' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px;">
                <p><strong>Co Contribution/Quarter</strong></p>
            </td>
        </tr>
        <tr>
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Q1 ${months[q1]}</p>
            </td>
            <td width='190' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Employee Only</p>
            </td>
            <td align="center" width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.quarterlyEeAmount?string.currency)!"$0.00"}</p>
            </td>
        </tr>
        <tr style="background-color: ${colorZebra};">
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p></p>
            </td>
            <td width='190' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Family</p>
            </td>
            <td align="center" width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.quarterlyFamilyAmount?string.currency)!"$0.00"}</p>
            </td>
        </tr>
        <tr>
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Q2 ${months[q2]}</p>
            </td>
            <td width='190' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Employee Only</p>
            </td>
            <td align="center" width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.quarterlyEeAmount?string.currency)!"$0.00"}</p>
            </td>
        </tr>
        <tr style="background-color: ${colorZebra};">
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p></p>
            </td>
            <td width='190' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Family</p>
            </td>
            <td align="center" width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.quarterlyFamilyAmount?string.currency)!"$0.00"}</p>
            </td>
        </tr>
        <tr>
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Q3 ${months[q3]}</p>
            </td>
            <td width='190' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Employee Only</p>
            </td>
            <td align="center" width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.quarterlyEeAmount?string.currency)!"$0.00"}</p>
            </td>
        </tr>
        <tr style="background-color: ${colorZebra};">
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p></p>
            </td>
            <td width='190' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Family</p>
            </td>
            <td align="center" width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.quarterlyFamilyAmount?string.currency)!"$0.00"}</p>
            </td>
        </tr>
        <tr>
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Q4 ${months[q4]}</p>
            </td>
            <td width='190' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Employee Only</p>
            </td>
            <td align="center" width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.quarterlyEeAmount?string.currency)!"$0.00"}</p>
            </td>
        </tr>
        <tr style="background-color: ${colorZebra};">
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p></p>
            </td>
            <td width='190' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Family</p>
            </td>
            <td align="center" width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.quarterlyFamilyAmount?string.currency)!"$0.00"}</p>
            </td>
        </tr>
    </table>
<#elseif (hsaFunding.optionId > 0) && ((hsaFunding.lumpSumFrequency)! = 'A') >
    <table border=1 style="border-width: 1px; border-collapse: collapse;">
        <tr>
            <td colspan='4' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 15px; background-color: #0077BC; color:white;">
                <p><strong>Company Frontload and Monthly Contribution to Worksite Employee HSAs</strong></p>
            </td>
        </tr>
        <tr>
            <td width='145' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p><strong></strong></p>
            </td>
            <td width='145' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px;">
                <p><strong>Employee Only</strong></p>
            </td>
            <td width='145' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px;">
                <p><strong>Family</strong></p>
            </td>
            <td width='145' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px;">
                <p><strong>Frontload Month</strong></p>
            </td>
        </tr>
        <tr>
            <td width='145' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 5px;">
                <p>Up-Front Lump Sum</p>
            </td>
            <td align="center" width='145' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.annualEeAmount?string.currency)!"$0.00"}</p>
            </td>
            <td align="center" width='145' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.annualFamilyAmount?string.currency)!"$0.00"}</p>
            </td>
            <td align="center" width='145' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${months[annualMonth]}</p>
            </td>
        </tr>
        <#if hsaFunding.optionId == 7 >
        <tr>
            <td width='145' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 5px;">
                <p>Monthly Contribution</p>
            </td>
            <td align="center" width='145' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.monthlyEeAmount?string.currency)!"$0.00"}/mo</p>
            </td>
            <td align="center" width='145' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${(hsaFunding.monthlyFamilyAmount?string.currency)!"$0.00"}/mo</p>
            </td>
            <td align="center" width='145' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p></p>
            </td>
        </tr>
        </#if>
    </table>
</#if>
