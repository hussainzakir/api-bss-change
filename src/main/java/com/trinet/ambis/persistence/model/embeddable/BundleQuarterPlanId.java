package com.trinet.ambis.persistence.model.embeddable;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Composite PK for XBSS_BUNDLE_QUARTER_PLAN.
 * PK columns per DDL:  BUNDLE_QUARTER_ID, BASE_BENEFIT_PLAN, EFFDT
 * Non-PK NOT NULL col: PORTFOLIO_ID  (regular column on the entity)
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BundleQuarterPlanId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "BUNDLE_QUARTER_ID", nullable = false)
    private Long bundleQuarterId;

    @Column(name = "BASE_BENEFIT_PLAN", nullable = false)
    private String baseBenefitPlan;

    @Column(name = "EFFDT", nullable = false)
    private LocalDate effdt;
}

