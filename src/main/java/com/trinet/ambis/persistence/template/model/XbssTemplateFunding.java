package com.trinet.ambis.persistence.template.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The persistent class for the XBSS_TEMPLATE_FUNDING database table.
 */
@Entity
@Table(name = "XBSS_TEMPLATE_FUNDING")
@NamedQuery(name = "XbssTemplateFunding.findAll", query = "SELECT x FROM XbssTemplateFunding x")
@ToString
@NoArgsConstructor
public class XbssTemplateFunding implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tbl-bss-gen")
    @TableGenerator(name = "tbl-bss-gen", allocationSize = 1, table = "XBSS_SEQUENCES_GENERATORS")
    @Column(unique = true, nullable = false, precision = 19)
    private long id;

    @Column(name = "BASE_COVERAGE_LEVEL", length = 10)
    private String baseCoverageLevel;

    @Column(name = "BASE_PCT", precision = 19, scale = 2)
    private BigDecimal basePct;

    @Column(name = "BENEFIT_PLAN", length = 6)
    private String benefitPlan;

    @Column(name = "PLAN_TYPE", length = 2)
    private String planType;

    @Column(name = "FUNDING_TYPE", length = 3)
    private String fundingType;

    //bi-directional many-to-one association to XbssTemplateFunding
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xbssTemplateFunding")
    private Set<XbssTemplateFundingRel> xbssTemplateFundings;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBaseCoverageLevel() {
        return this.baseCoverageLevel;
    }

    public void setBaseCoverageLevel(String baseCoverageLevel) {
        this.baseCoverageLevel = baseCoverageLevel;
    }

    public BigDecimal getBasePct() {
        return this.basePct;
    }

    public void setBasePct(BigDecimal basePct) {
        this.basePct = basePct;
    }

    public String getBenefitPlan() {
        return this.benefitPlan;
    }

    public void setBenefitPlan(String benefitPlan) {
        this.benefitPlan = benefitPlan;
    }

    public String getPlanType() {
        return this.planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getFundingType() {
        return fundingType;
    }

    public void setFundingType(String fundingType) {
        this.fundingType = fundingType;
    }

    public Set<XbssTemplateFundingRel> getXbssTemplateFundings() {
        return this.xbssTemplateFundings;
    }

    public void setXbssTemplateFundings(Set<XbssTemplateFundingRel> xbssTemplateFundings) {
        this.xbssTemplateFundings = xbssTemplateFundings;
    }

}
