
<table border='1'  style="border-width: 0px; border-collapse: collapse;">
    <tr>
        <td  colspan='2'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 15px; background-color: #0077BC; color:white;">
            <#if benefitGroup.hasVolDental == true>
            	<p><strong>Employee Paid Dental</strong></p>
        	<#else>
            	<p><strong>Dental</strong></p>
        	</#if>
        </td>
    </tr>
    <tr>
        <td width='115'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>Plan Carrier:</p>
        </td>
        <td width='485'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>${dentalContributionHeaders[bgIndex].planCarriers!}</p>
        </td>
    </tr>
    <tr style="background-color: ${colorZebra};">
        <td colspan='2' width='600'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>Offered Plans:</p>
            <p>${dentalContributionHeaders[bgIndex].benefitPlans!}</p>
        </td>
    </tr>
</table>
<br/>

<#-- Base for Maximum Company Contribution -->
<table border='1'  style="border-width: 0px; border-collapse: collapse;">
    <tr>
        <td  colspan='2'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 15px; background-color: #0077BC; color:white;">
            <p><strong>Base for Maximum Company Contribution</strong></p>
        </td>
    </tr>
    <#if benefitGroup.hasVolDental != true>
	    <tr style="background-color: ${colorZebra};">
	        <td width='200'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	            <p>Fund Type:</p>
	        </td>
	        <td width='400'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	            <p>${dentalContributionHeaders[bgIndex].fundingTypeDescription}</p>
	        </td>
	    </tr>
	    <tr>
	        <td width='200'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <#if dentalContributionHeaders[bgIndex].fundingTypeCode == 'CFPCT'>
	            <p>Limit Plan:</p>
	        <#else>
	            <p>Funding Base Plan:</p>
	        </#if>
	        </td>
	        <td width='400'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	            <p> ${dentalContributionHeaders[bgIndex].fundingBasePlan!} </p>
	        </td>
	    </tr>
	</#if>
	</table>
	
	<#if dentalContributionHeaders[bgIndex].fundingTypeCode??>
	<#if dentalContributionHeaders[bgIndex].fundingTypeCode != 'FLT' && dentalContributionHeaders[bgIndex].fundingTypeCode != 'BSUPP'>
   	<#if dentalContributionHeaders[bgIndex].fundingTypeCode == 'CFPCT'>
   		<p>Funding Maximum Amount:</p>
   	<#else>
   		<p>Funding Base Plan Amount:</p>
	</#if>
	</#if>
	</#if>
	
	<#if dentalContributionHeaders[bgIndex].fundingTypeCode??>
	<#if dentalContributionHeaders[bgIndex].fundingTypeCode == 'BFPCT'>
	<table border='1'  style="border-width: 0px; margin-top:5px; border-collapse: collapse;">
	    <tr>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee Only:</p>
	         <#if dentalContributionHeaders[bgIndex].isFundingFlatMax  != 'true'>
   				<p>${dentalContributionHeaders[bgIndex].fbpEmployeeLimit!}</p>
   			<#else>
   				<p>${dentalContributionHeaders[bgIndex].employeeFlatMax!}</p>
			</#if>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee + Spouse:</p>
	         <#if dentalContributionHeaders[bgIndex].isFundingFlatMax  != 'true'>
   				<p>${dentalContributionHeaders[bgIndex].fbpEmployeePlusSpouseLimit!}</p>
   			<#else>
   				<p>${dentalContributionHeaders[bgIndex].spouseFlatMax!}</p>
			</#if>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee + Child(ren):</p>
	         <#if dentalContributionHeaders[bgIndex].isFundingFlatMax  != 'true'>
   				<p>${dentalContributionHeaders[bgIndex].fbpEmployeePlusChildLimit!}</p>
   			<#else>
   				<p>${dentalContributionHeaders[bgIndex].childFlatMax!}</p>
			</#if>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Family:</p> 
	         <#if dentalContributionHeaders[bgIndex].isFundingFlatMax  != 'true'>
   				<p>${dentalContributionHeaders[bgIndex].fbpEmployeePlusFamilyLimit!}</p>
   			<#else>
   				<p>${dentalContributionHeaders[bgIndex].familyFlatMax!}</p>
			</#if>
	        </td>
	    </tr>
	  </table>
    </#if>
    </#if>
    
    <#if dentalContributionHeaders[bgIndex].fundingTypeCode??>
	<#if dentalContributionHeaders[bgIndex].fundingTypeCode == 'CFPCT' && dentalContributionHeaders[bgIndex].isFundingFlatMax  != 'true'>
	<table border='1'  style="border-width: 0px; margin-top:5px; border-collapse: collapse;">
	    <tr>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee Only:</p>
   			 <p>${dentalContributionHeaders[bgIndex].fbpEmployeeLimit!}</p>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee + Spouse:</p>
   			 <p>${dentalContributionHeaders[bgIndex].fbpEmployeePlusSpouseLimit!}</p>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee + Child(ren):</p>
   			 <p>${dentalContributionHeaders[bgIndex].fbpEmployeePlusChildLimit!}</p>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Family:</p> 
   			 <p>${dentalContributionHeaders[bgIndex].fbpEmployeePlusFamilyLimit!}</p>
	        </td>
	    </tr>
	  </table>
    </#if>
    </#if>
    
    <table border='1'  style="border-width: 0px;margin-top:5px; border-collapse: collapse;">
    <#if dentalContributionHeaders[bgIndex].fundingTypeCode != 'BFPCT'>
        <tr style="background-color: ${colorZebra};">
            <td width='200'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Company Contribution -</p>
                <p>${employeeCoverageCode!}</p>
            </td>
            <td width='400'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <#if dentalContributionHeaders[bgIndex].isFundingFlatMax == 'true'>
                    <p> ${dentalContributionHeaders[bgIndex].employeePercent!} up to ${dentalContributionHeaders[bgIndex].employeeFlatMax!} </p>
                <#else>
                    <p> ${dentalContributionHeaders[bgIndex].employeePercent!} </p>
                </#if>
            </td>
        </tr>
        <tr>
            <td width='200'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Company Contribution -</p>
                <p>${spouseCoverageCode}</p>
            </td>
            <td width='400'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <#if dentalContributionHeaders[bgIndex].isFundingFlatMax == 'true'>
                    <p> ${dentalContributionHeaders[bgIndex].employeePlusSpousePercent!} up to ${dentalContributionHeaders[bgIndex].spouseFlatMax!}</p>
                <#else>
                    <p> ${dentalContributionHeaders[bgIndex].employeePlusSpousePercent!} </p>
                </#if>
            </td>
        </tr>
        <tr style="background-color: ${colorZebra};">
            <td width='200'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Company Contribution -</p>
                <p>${childCoverageCode}</p>
            </td>
            <td width='400'  style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <#if dentalContributionHeaders[bgIndex].isFundingFlatMax == 'true'>
                    <p> ${dentalContributionHeaders[bgIndex].employeePlusChildPercent!} up to ${dentalContributionHeaders[bgIndex].childFlatMax!}</p>
                <#else>
                    <p> ${dentalContributionHeaders[bgIndex].employeePlusChildPercent!} </p>
                </#if>
            </td>
        </tr>
        <tr>
           <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Company Contribution -</p>
                <p>${familyCovarageCode}</p>
            </td>
            <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <#if dentalContributionHeaders[bgIndex].isFundingFlatMax == 'true'>
                    <p> ${dentalContributionHeaders[bgIndex].employeePlusFamilyPercent!} up to ${dentalContributionHeaders[bgIndex].familyFlatMax!}</p>
                <#else>
                    <p> ${dentalContributionHeaders[bgIndex].employeePlusFamilyPercent!} </p>
                </#if>
            </td>
        </tr>
     <#else>
        <tr style="background-color: ${colorZebra};">
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Company Contribution</p>
            </td>
            <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${dentalContributionHeaders[bgIndex].companyPercent!}</p>
            </td>
        </tr>
        <tr>
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Coverage Level</p>
            </td>
            <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${dentalContributionHeaders[bgIndex].coverageLevel!}</p>
            </td>
        </tr>
    </#if>
</table>
<br/>

<#if benefitGroup.hasVolDental != true>
    <#if TriNetIVrule != 1>
        <p>TriNet and/or your company have updated plan contributions for the plans listed below.</p>
	</#if>
	<table border='1'  style="border-width: 1px; border-collapse: collapse;">
	    <tr>
	        <td colspan='4' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 15px; background-color: #0077BC; color:white;">
	            <p><strong>Company Specified Contribution Amounts/Coverage Levels</strong></p>
	        </td>
	    </tr>
        <tr>
            <td width='180' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p><strong>Plan and Coverage Level</strong></p>
            </td>
            <td width='140' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px;">
                <p><strong>Company Pays</strong></p>
            </td>
            <td width='140' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px;">
                <p><strong>Co % Total Cost</strong></p>
            </td>
             <td width='140' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px;">
                <p><strong>Employee Pays</strong></p>
            </td>
        </tr>
        <#if (groupsDentalPlanContribution[bgIndex]?size == 0) >
            <tr>
                <td width='180' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px; ">
                    <p><span>${tab}None${tab}</span></p>
                </td>
                <td width='140' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px; ">
                    <p><span>${tab}-${tab}</span></p>
                </td>
                <td width='140' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px; ">
                    <p><span>${tab}-${tab}</span></p>
                </td>
                <td width='140' align="center" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 1px; ">
                    <p><span>${tab}-${tab}</span></p>
                </td>
            </tr>
        </#if>
        
        <#assign counter = 1>
        <#assign name = ''>
        <#list groupsDentalPlanContribution[bgIndex] as planContribution>
            <#if name != planContribution.planName>
                <#assign name = planContribution.planName>
                <tr>
                    <td colspan='4' style="${font} border-color: #bfbfbf; border-style: solid; padding: 1px 1px 1px 5px;">
                        <p>${planContribution.planName!}</p>
                    </td>
                </tr>
            </#if>
            <#if (counter % 2) == 0><tr style="background-color: white;"><#else><tr style="background-color: ${colorZebra};"></#if>
                <td style="border-top: 1px; border-bottom: 1px; border-color: #bfbfbf; padding: 1px 1px 1px 15px;">
                    <p>${planContribution.coverageCode!}</p>
                </td>
                <td style="border-top: 1px; border-bottom: 1px; border-color: #bfbfbf; padding: 1px 1px 1px 15px;">
                    <p>${planContribution.companyCost!}</p>
                </td>
                <td style="border-top: 1px; border-bottom: 1px; border-color: #bfbfbf; padding: 1px 1px 1px 15px;">
                    <p>${planContribution.companyPercent!}</p>
                </td>
                <td style="border-top: 1px; border-bottom: 1px; border-color: #bfbfbf; padding: 1px 1px 1px 15px;">
                    <p>${planContribution.employeeCost!}</p>
                </td>
            </tr>
            <#assign counter++>
        </#list>
     </table>
</#if>
