package com.trinet.ambis.persistence.template.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The persistent class for the XBSS_PORTFOLIO database table.
 */
@Entity
@Table(name = "XBSS_PORTFOLIO")
@NamedQuery(name = "XbssPortfolio.findAll", query = "SELECT x FROM XbssPortfolio x")
@ToString
@NoArgsConstructor
public class XbssPortfolio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(unique = true, nullable = false, precision = 19)
    private long id;

    //bi-directional many-to-one association to XbssTemplateFunding
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xbssPortfolio")
    private Set<XbssTemplateFundingRel> xbssTemplateFundingsRel;

    //bi-directional many-to-one association to XbssTemplatePortfolioPlan
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xbssPortfolio")
    private Set<XbssTemplatePortfolioPlan> xbssTemplatePortfolioPlans;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<XbssTemplateFundingRel> getXbssTemplateFundingsRel() {
        return xbssTemplateFundingsRel;
    }

    public void setXbssTemplateFundingsRel(Set<XbssTemplateFundingRel> xbssTemplateFundingsRel) {
        this.xbssTemplateFundingsRel = xbssTemplateFundingsRel;
    }

    public Set<XbssTemplatePortfolioPlan> getXbssTemplatePortfolioPlans() {
        return this.xbssTemplatePortfolioPlans;
    }

    public void setXbssTemplatePortfolioPlans(Set<XbssTemplatePortfolioPlan> xbssTemplatePortfolioPlans) {
        this.xbssTemplatePortfolioPlans = xbssTemplatePortfolioPlans;
    }

}
