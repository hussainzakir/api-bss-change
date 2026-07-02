
<table border='1' style=' border-width: 0px; border-collapse: collapse;'>

    <tr>
        <td  colspan='2' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 15px; background-color: #0077BC; color:white;">
            <p><strong>Strategy</strong></p>
        </td>
    </tr>
    <tr style="background-color: ${colorZebra};">
        <td width='160'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>Selected Strategy:</p>
        </td>
        <td width='440' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>${strategy}</p>
        </td>
    </tr>
    <#-- sales Order Number -->
    <#if serviceOrderNum == 'true'>
    <tr>
        <td width='160' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>Sales Order Number:</p>
        </td>
        <td width='440'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>${salesOrder}</p>
        </td>
    </tr>
    </#if>
</table>
