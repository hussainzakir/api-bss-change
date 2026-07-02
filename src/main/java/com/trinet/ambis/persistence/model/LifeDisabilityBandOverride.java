package com.trinet.ambis.persistence.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "XBSS_LIFE_DIS_BAND_OVERRIDE")
public class LifeDisabilityBandOverride implements IExceptionDto {

    @Id
    @SequenceGenerator(name = "lifedisabilityseq", sequenceName = "XBSS_LIFE_DIS_BAND_OVERRIDE_SEQ", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lifedisabilityseq")
    private long id;

    @Column(name = "COMPANY")
    private String companyCode;

    @Column(name = "LIFE_BAND")
    private String lifeBand;

    @Column(name = "DIS_BAND")
    private String disBand;

    @Column(name = "START_DATE")
    private Date startDate;

    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "APPROVER")
    private String approverId;

    @Column(name = "CREATE_TIME")
    private Date createTime;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @Column(name = "ACTIVE")
    private boolean active;

}