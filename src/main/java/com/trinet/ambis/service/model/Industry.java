package com.trinet.ambis.service.model;

import java.io.Serializable;

import com.trinet.ambis.enums.IndustryType;

import lombok.ToString;

/**
 * This is a sample class to launch a rule.
 */
@ToString
public class Industry implements Serializable {

    private static final long serialVersionUID = 1L;

    private int NAICSCode;

    private IndustryType industryType;

    public Industry(int code) {
        NAICSCode = code;
    }

    public int getNAICSCode() {
        return NAICSCode;
    }

    public void setNAICSCode(int code) {
        NAICSCode = code;
    }

    public IndustryType getIndustryType() {
        return industryType;
    }

    public void setIndustryType(IndustryType industryType) {
        this.industryType = industryType;
    }

}