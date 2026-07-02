package com.trinet.ambis.persistence.model;

import javax.persistence.*;

import com.trinet.ambis.persistence.model.embeddable.BundlePlansId;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "XBSS_BUNDLE_PLANS")
public class BundlePlans implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private BundlePlansId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BUNDLE_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private Bundle bundle;

    @Column(name = "ENDDT")
    private LocalDate endDate;

    @Column(name = "PLAN_TYPE")
    private String planType;
}