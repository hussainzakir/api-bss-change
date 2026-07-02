package com.trinet.ambis.persistence.model.embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NaicsBandCodeUK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "NAICS_CODE")
    @NotNull
    private String naicsCode;

    @Column(name = "EFF_DT")
    @NotNull
    private Date effectiveDate;

}