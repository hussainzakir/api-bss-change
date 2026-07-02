package com.trinet.ambis.persistence.model;

import com.trinet.ambis.persistence.model.embeddable.BundleQuarterPlanId;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "XBSS_BUNDLE_QUARTER_PLAN")
public class BundleQuarterPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private BundleQuarterPlanId id;

    /**
     * Carrier portfolio ID.
     * NOT part of the PK per DDL — regular NOT NULL column.
     */
    @Column(name = "PORTFOLIO_ID", nullable = false)
    private Long portfolioId;

    @Column(name = "ENDDT", nullable = false)
    private LocalDate enddt;

    @Column(name = "PLAN_TYPE", nullable = false)
    private String planType;

    // ── relationship ──────────────────────────────────────────────────────

    /**
     * Read-only navigation to the parent XBSS_BUNDLE_QUARTER row.
     * BUNDLE_QUARTER_ID is already in the embedded PK so we mark
     * insertable/updatable = false to avoid duplicate-mapping errors.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BUNDLE_QUARTER_ID", referencedColumnName = "ID",
                insertable = false, updatable = false)
    private BundleQuarter bundleQuarter;
}


