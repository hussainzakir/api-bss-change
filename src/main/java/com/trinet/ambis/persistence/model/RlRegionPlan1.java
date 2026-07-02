/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trinet.ambis.persistence.model.embeddable.RlRegionPlan1UK;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "xbss_rl_region_plan1")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RlRegionPlan1 implements Serializable {

	private static final long serialVersionUID = 646685882458654460L;

	@EmbeddedId
	RlRegionPlan1UK rlRegionPlan1UK;

	@Column(name = "MANDATORY_FLAG")
	private boolean mandatoryFlag;

	@Column(name = "SUB_REGION")
	private String subRegion;

}
