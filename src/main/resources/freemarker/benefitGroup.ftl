
<#if hasMedical == true>
    <header>
        <h3>Medical</h3>
    </header>

    <#include '/medical.ftl'>
<#else>
    <#if exchange != TriNetXI>
        <br/>
        <table border='1'  style="border-width: 0px; margin-left:10px; border-collapse: collapse;">
            <tr>
                <td style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 15px; background-color: #0077BC; color:white;">
                    <p><strong>Not offering Trinet Medical Coverage</strong></p>
                </td>
            </tr>
        </table>
    </#if>
</#if>

<header>
    <h3>Dental</h3>
</header>
<#if hasDental == true>
    <#include '/dental.ftl'>
<#else>
    <table border='1'  style="border-width: 0px; margin-left:10px; border-collapse: collapse;">
        <tr>
            <td style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 15px; background-color: #0077BC; color:white;">
                <p><strong>Not offering Trinet Dental Coverage</strong></p>
            </td>
        </tr>
    </table>
</#if>

<header>
    <h3>Vision</h3>
</header>
<#if hasVision == true>
    <#include '/vision.ftl'>
<#else>
    <table border='1'  style="border-width: 0px; margin-left:10px; border-collapse: collapse;">
        <tr>
            <td style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 15px; background-color: #0077BC; color:white;">
                <p><strong>Not offering Trinet Vision Coverage</strong></p>
            </td>
        </tr>
    </table>
</#if>

<header>
    <h3>Additional Benefits</h3>
</header>

<#if additionalBenefitContributionHeaders[bgIndex]?has_content>
    <#include '/additionalBenefits.ftl'>
<#else>
    <table border='1'  style="border-width: 0px; margin-left:10px; border-collapse: collapse;">
        <tr>
            <td style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 15px; background-color: #0077BC; color:white;">
                <p><strong>Not offering Trinet Additional Benefits Coverage</strong></p>
            </td>
        </tr>
    </table>
</#if>
<br/>
