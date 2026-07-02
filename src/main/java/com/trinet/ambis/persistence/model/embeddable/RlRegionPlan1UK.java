/**
 * 
 */
package com.trinet.ambis.persistence.model.embeddable;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RlRegionPlan1UK implements Serializable {

	private static final long serialVersionUID = -5892697037102502685L;

	@Column(name = "REALM_PLYR_PLAN_ID")
	private long realmPlyrPlanId;

	@Column(name = "REGION")
	private String region;

}
