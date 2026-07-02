package com.trinet.ambis.persistence.model.embeddable;

import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BundlePlansId implements Serializable {
    private static final long serialVersionUID = -3478234982349823498L;
    @Column(name = "BUNDLE_ID")
    private Long bundleId;

    @Column(name = "OE_QUARTER")
    private String oeQuarter;

    @Column(name = "PORTFOLIO_ID")
    private Long portfolioId;

    @Column(name = "BASE_BENEFIT_PLAN")
    private String baseBenefitPlan;

    @Column(name = "EFFDT")
    private LocalDate effectiveDate;
}