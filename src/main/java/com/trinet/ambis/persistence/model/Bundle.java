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
@Table(name = "XBSS_BUNDLE")
public class Bundle implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "EFFDT")
    private LocalDate effectiveDate;

    @Column(name = "ENDDT")
    private LocalDate endDate;

    @Column(name = "NAME")
    private String name;

    @Column(name = "COMPANY_CODE")
    private String companyCode;

    @Column(name = "TYPE")
    private String type;

    @OneToMany(mappedBy = "bundle", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<BundlePlans> bundlePlans;

    @OneToMany(mappedBy = "bundle", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BundleQuarter> bundleQuarters;
}