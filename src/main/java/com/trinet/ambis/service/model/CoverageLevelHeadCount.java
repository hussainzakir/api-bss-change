/**
 * 
 */
package com.trinet.ambis.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * @author kpamulapati
 *
 */

@Data
public class CoverageLevelHeadCount {
	private String coverageLevel;
	private int headCount;
	private int hsaHeadCount;
	@JsonIgnore
	private String benefitProgram;

}
