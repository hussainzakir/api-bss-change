package com.trinet.ambis.service.model;

import java.io.Serializable;

import lombok.ToString;
import lombok.Data;

@ToString
@Data
public class BandCodes implements Serializable {

    private static final long serialVersionUID = 1L;

    private String kaiserBandCode;
    private String uhcBandCode;
    private String aetnaBandCode;
    private String aetnaHmoBandCode;
    private String aetnaPpoBandCode;
    private String bcbsBandCode;
    private String bcbsNcBandCode;
    private String kaisCoBandCode;
    private String bsOfCaBandCode;
    private String bcOfIdBandCode;
    private String kaisNwBandCode;
    private String tuftsBandCode;
    private String lifeBandCode;
    private String disBandCode;
    private String kaiMidAtlBandCode;
    private String kaiHawaiiBandCode;
    private String bcbsMNBandCode;
    private String empireNYBand;
    private String harvardBandCode;
    private String highmarkBandCode;

    public BandCodes() {
        super();
    }

}
