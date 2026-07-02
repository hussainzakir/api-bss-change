package com.trinet.ambis.persistence.model;

import com.trinet.ambis.persistence.model.embeddable.NaicsBandCodeUK;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "XBSS_NAICS_BAND_CODE")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NaicsBandCode {

    @EmbeddedId
    private NaicsBandCodeUK naicsBandCodeUK;

    @Column(name = "LIFE_BAND_CODE")
    @NotNull
    private String lifeBandCode;

    @Column(name = "DIS_BAND_CODE")
    @NotNull
    private String disabilityBandCode;

    @Column(name = "END_DT")
    @NotNull
    private Date endDate;

}