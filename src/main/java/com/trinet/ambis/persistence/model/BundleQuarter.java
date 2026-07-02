package com.trinet.ambis.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "XBSS_BUNDLE_QUARTER")
public class BundleQuarter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "BUNDLE_ID", nullable = false)
    private Long bundleId;

    @Column(name = "OE_QUARTER", nullable = false)
    private String oeQuarter;

    @Column(name = "EFFDT", nullable = false)
    private LocalDate effdt;

    @Column(name = "ENDDT", nullable = false)
    private LocalDate enddt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BUNDLE_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private Bundle bundle;

    @OneToMany(mappedBy = "bundleQuarter", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BundleQuarterPlan> bundleQuarterPlans;
}

