
<table border='1'  style='border-width: 0px; border-collapse: collapse;'>
    <tr>
        <td  colspan='2' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 15px; background-color: #0077BC; color:white;">
            <p><strong>Additional Benefits</strong></p>
        </td>
    </tr>
    <tr>
        <td width='156'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>Disability:</p>
        </td>
        <td width='444'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p> ${((additionalBenefitContributionHeaders[bgIndex].disability)!"Not offered")?html} </p>
        </td>
    </tr>
    <tr style="background-color: ${colorZebra};">
        <td width='156'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>Basic Life Insurance:</p>
        </td>
        <td width='444'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p> ${((additionalBenefitContributionHeaders[bgIndex].life)!"Not offered")?html} </p>
        </td>
    </tr>
    <tr>
        <#if exchange == TriNetIV>
            <td width='156'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Commuter Benefits:</p>
            </td>
            <td width='444'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p> ${((additionalBenefitContributionHeaders[bgIndex].commuter)!"Not offered")?html} </p>
            </td>
        </#if>
    </tr>
</table>
