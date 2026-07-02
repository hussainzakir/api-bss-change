/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * @author khinton
 *
 */
@Data
public class BenefitsCategories {
	@JsonInclude(Include.NON_NULL)
	private BenefitsCategory medical;
	@JsonInclude(Include.NON_NULL)
	private BenefitsCategory dental;
	@JsonInclude(Include.NON_NULL)
	private BenefitsCategory vision;
	@JsonInclude(Include.NON_EMPTY)
	private List<String> validationErrors;

}
