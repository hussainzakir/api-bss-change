package com.trinet.ambis.persistence.template.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The persistent class for the XBSS_TEMPLATE_PORTFOLIO_PLAN database table.
 * 
 */
@Entity
@Table(name = "XBSS_TEMPLATE_PORTFOLIO_PLAN")
@NamedQuery(name = "XbssTemplatePortfolioPlan.findAll", query = "SELECT x FROM XbssTemplatePortfolioPlan x")
@ToString
@NoArgsConstructor
public class XbssTemplatePortfolioPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tbl-bss-gen")
    @TableGenerator(name = "tbl-bss-gen", allocationSize = 1, table = "XBSS_SEQUENCES_GENERATORS")
    @Column(unique = true, nullable = false, precision = 19)
    private long id;

    @Column(name = "BENEFIT_PLAN", length = 6)
    private String benefitPlan;

    @Column(name = "PLAN_TYPE", length = 2)
    private String planType;
    
    @Column(name="SITUS", precision=2)
    private String situs;

    //bi-directional many-to-one association to XbssPortfolio
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PORTFOLIO_ID")
    private XbssPortfolio xbssPortfolio;

    //bi-directional many-to-one association to XbssTemplate
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "TEMPLATE_ID")
    private XbssTemplate xbssTemplate;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
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

    public XbssPortfolio getXbssPortfolio() {
        return this.xbssPortfolio;
    }

    public void setXbssPortfolio(XbssPortfolio xbssPortfolio) {
        this.xbssPortfolio = xbssPortfolio;
    }

    public XbssTemplate getXbssTemplate() {
        return this.xbssTemplate;
    }

    public void setXbssTemplate(XbssTemplate xbssTemplate) {
        this.xbssTemplate = xbssTemplate;
    }
    
    public String getSitus() {
        return situs;
    }

    public void setSitus(String situs) {
        this.situs = situs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((benefitPlan == null) ? 0 : benefitPlan.hashCode());
        result = prime * result + ((xbssTemplate == null) ? 0 : xbssTemplate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XbssTemplatePortfolioPlan other = (XbssTemplatePortfolioPlan) obj;
        if (benefitPlan == null) {
            if (other.benefitPlan != null)
                return false;
        } else if (!benefitPlan.equals(other.benefitPlan))
            return false;
        if (xbssTemplate == null) {
            if (other.xbssTemplate != null)
                return false;
        } else if (!xbssTemplate.equals(other.xbssTemplate))
            return false;
        return true;
    }

}
