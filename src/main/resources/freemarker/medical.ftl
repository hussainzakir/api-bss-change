
<#-- Base for Maximum Company Contribution -->
<table border='1'  style="border-width: 0px; border-collapse: collapse;">
    <tr>
        <td colspan='2' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 15px; background-color: #0077BC; color:white;">
            <p><strong>Base for Maximum Company Contribution</strong></p>
        </td>
    </tr>
    <tr style="background-color: ${colorZebra};">
        <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>Fund Type:</p>
        </td>
        <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>${medicalContributionHeaders[bgIndex].fundingTypeDescription}</p>
        </td>
    </tr>
    <#if medicalContributionHeaders[bgIndex].isWaiverAllowance! == 'true'>
    <tr>
        <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p>Medical Waiver Allowance:</p>
        </td>
        <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
            <p> ${medicalContributionHeaders[bgIndex].waiverAllowance} </p>
        </td>
    </tr>
    </#if>
  
    <#if medicalContributionHeaders[bgIndex].fundingTypeCode! == 'BSUPP'>
	    <tr>
	        <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	            <p>Surplus Benefit Supplement:</p>
	        </td>
	        <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	            <p> ${medicalContributionHeaders[bgIndex].surBenSupplement} </p>
	        </td>
	    </tr>
	    <#if medicalContributionHeaders[bgIndex].surBenSupplementId! == '2'>
		    <tr>
		        <td width='200' valign="top" style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
		            <p>Surplus Plan Allocation:</p>
		        </td>
		        <td width='400' style="${font} border-width: 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
		            <#list medicalContributionHeaders[bgIndex].surPlanAllocation as planDesc>
	            		${planDesc!}
		            	<#if (medicalContributionHeaders[bgIndex].surPlanAllocation?size > 0) > 
		            		</br>
		            	</#if>
	            	</#list>
		        </td>
		    </tr>
		</#if>
    </#if>
    
    <#if medicalContributionHeaders[bgIndex].fundingTypeCode! != 'BSUPP'>
	    <tr>
	        <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	        <#if medicalContributionHeaders[bgIndex].fundingTypeCode == 'CFPCT'>
	            <p>Limit Plan:</p>
	        <#else>
	            <p>Funding Base Plan:</p>
	        </#if>
	        </td>
	        <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	            <p> ${medicalContributionHeaders[bgIndex].fundingBasePlan!} </p>
	        </td>
	    </tr>
    </#if>
    </table>
    
    <#if medicalContributionHeaders[bgIndex].fundingTypeCode??>
    <#if medicalContributionHeaders[bgIndex].fundingTypeCode != 'FLT' && medicalContributionHeaders[bgIndex].fundingTypeCode != 'BSUPP'>
   	<#if medicalContributionHeaders[bgIndex].fundingTypeCode == 'CFPCT'>
   		<p>Funding Maximum Amount:</p>
   	<#else>
   		<p>Funding Base Plan Amount:</p>
	</#if>
	</#if>
	</#if>
	
    <#if medicalContributionHeaders[bgIndex].fundingTypeCode??>
	<#if medicalContributionHeaders[bgIndex].fundingTypeCode == 'BFPCT'>
	<table border='1'  style="border-width: 0px; margin-top:5px; border-collapse: collapse;">
	    <tr>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee Only:</p>
	         <#if medicalContributionHeaders[bgIndex].isFundingFlatMax  != 'true'>
   				<p>${medicalContributionHeaders[bgIndex].fbpEmployeeLimit!}</p>
   			<#else>
   				<p>${medicalContributionHeaders[bgIndex].employeeFlatMax!}</p>
			</#if>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee + Spouse:</p>
	         <#if medicalContributionHeaders[bgIndex].isFundingFlatMax  != 'true'>
   				<p>${medicalContributionHeaders[bgIndex].fbpEmployeePlusSpouseLimit!}</p>
   			<#else>
   				<p>${medicalContributionHeaders[bgIndex].spouseFlatMax!}</p>
			</#if>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee + Child(ren):</p>
	         <#if medicalContributionHeaders[bgIndex].isFundingFlatMax  != 'true'>
   				<p>${medicalContributionHeaders[bgIndex].fbpEmployeePlusChildLimit!}</p>
   			<#else>
   				<p>${medicalContributionHeaders[bgIndex].childFlatMax!}</p>
			</#if>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Family:</p> 
	         <#if medicalContributionHeaders[bgIndex].isFundingFlatMax  != 'true'>
   				<p>${medicalContributionHeaders[bgIndex].fbpEmployeePlusFamilyLimit!}</p>
   			<#else>
   				<p>${medicalContributionHeaders[bgIndex].familyFlatMax!}</p>
			</#if>
	        </td>
	    </tr>
	  </table>
    </#if>
    </#if>
    
    <#if medicalContributionHeaders[bgIndex].fundingTypeCode??>
	<#if medicalContributionHeaders[bgIndex].fundingTypeCode == 'CFPCT' && medicalContributionHeaders[bgIndex].isFundingFlatMax  != 'true'>
	<table border='1'  style="border-width: 0px; margin-top:5px; border-collapse: collapse;">
	    <tr>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee Only:</p>
   			 <p>${medicalContributionHeaders[bgIndex].fbpEmployeeLimit!}</p>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee + Spouse:</p>
   			 <p>${medicalContributionHeaders[bgIndex].fbpEmployeePlusSpouseLimit!}</p>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Employee + Child(ren):</p>
   			 <p>${medicalContributionHeaders[bgIndex].fbpEmployeePlusChildLimit!}</p>
	        </td>
	        <td width='150' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
	         <p>Family:</p> 
   			 <p>${medicalContributionHeaders[bgIndex].fbpEmployeePlusFamilyLimit!}</p>
	        </td>
	    </tr>
	  </table>
    </#if>
    </#if>
    
    <table border='1'  style="border-width: 0px;margin-top:5px; border-collapse: collapse;">
    <#if medicalContributionHeaders[bgIndex].fundingTypeCode != 'BFPCT'>
        <tr style="background-color: ${colorZebra};">
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Company Contribution -</p>
                <p>${employeeCoverageCode!}</p>
            </td>
            <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <#if medicalContributionHeaders[bgIndex].isFundingFlatMax == 'true'>
                    <p> ${medicalContributionHeaders[bgIndex].employeePercent!} up to ${medicalContributionHeaders[bgIndex].employeeFlatMax!} </p>
                <#else>
                    <p> ${medicalContributionHeaders[bgIndex].employeePercent!} </p>
                </#if>
            </td>
        </tr>
        <tr>
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Company Contribution -</p>
                <p>${spouseCoverageCode}</p>
            </td>
            <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <#if medicalContributionHeaders[bgIndex].isFundingFlatMax == 'true'>
                    <p> ${medicalContributionHeaders[bgIndex].employeePlusSpousePercent!} up to ${medicalContributionHeaders[bgIndex].spouseFlatMax!}</p>
                <#else>
                    <p> ${medicalContributionHeaders[bgIndex].employeePlusSpousePercent!} </p>
                </#if>
            </td>
        </tr>
        <tr style="background-color: ${colorZebra};">
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Company Contribution -</p>
                <p>${childCoverageCode}</p>
            </td>
            <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <#if medicalContributionHeaders[bgIndex].isFundingFlatMax == 'true'>
                    <p> ${medicalContributionHeaders[bgIndex].employeePlusChildPercent!} up to ${medicalContributionHeaders[bgIndex].childFlatMax!}</p>
                <#else>
                    <p> ${medicalContributionHeaders[bgIndex].employeePlusChildPercent!} </p>
                </#if>
            </td>
        </tr>
        <tr>
           <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Company Contribution -</p>
                <p>${familyCovarageCode}</p>
            </td>
            <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <#if medicalContributionHeaders[bgIndex].isFundingFlatMax == 'true'>
                    <p> ${medicalContributionHeaders[bgIndex].employeePlusFamilyPercent!} up to ${medicalContributionHeaders[bgIndex].familyFlatMax!}</p>
                <#else>
                    <p> ${medicalContributionHeaders[bgIndex].employeePlusFamilyPercent!} </p>
                </#if>
            </td>
        </tr>
    <#else>
        <tr>
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Company Contribution</p>
            </td>
            <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${medicalContributionHeaders[bgIndex].companyPercent!}</p>
            </td>
        </tr>
        <tr>
            <td width='200' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>Coverage Level</p>
            </td>
            <td width='400' style="border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 1px 5px 1px 5px;">
                <p>${medicalContributionHeaders[bgIndex].coverageLevel!}</p>
            </td>
        </tr>
    </#if>
</table>

<br/>
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
    <#if (groupsMedicalPlanContribution[bgIndex]?size == 0) >
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
    <#list groupsMedicalPlanContribution[bgIndex] as planContribution>
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
