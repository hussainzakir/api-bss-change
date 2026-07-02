package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;

/**
 * @author schaudhari
 *
 */
@Entity
@Table(name = "XBSS_EE_DEFAULT_PLAN_ASSIGNMENT")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmplDefaultPlanAssignment implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	@Delegate
	private EmplDefaultPlanAssignmentId emplDefaultPlanAssignmentId;

}
