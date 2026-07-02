/**
 * 
 */
package com.trinet.ambis.service.model;


import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 
 * @author svemulapalli
 *
 */
@Data
public class DMRequest {
	private Integer docTypeId;
    private Integer exchangeId;
    
   // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_PATTERN)
	private String planYearStartDate;
    
   // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_PATTERN)
	private String planYearEndDate;
    
    @JsonProperty(value="plandatesQuarters")
	private List<QuarterPlanYearDate> quarterPlanYearDates;
    
    // Optional additional attributes
    private Map<String, String> attributes;
}
