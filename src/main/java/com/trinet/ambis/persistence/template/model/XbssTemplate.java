package com.trinet.ambis.persistence.template.model;

import java.io.Serializable;
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
import javax.persistence.Transient;

import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The persistent class for the XBSS_TEMPLATE database table.
 */
@Entity
@Table(name = "XBSS_TEMPLATE")
@NamedQuery(name = "XbssTemplate.findAll", query = "SELECT x FROM XbssTemplate x")
@ToString
@NoArgsConstructor
public class XbssTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tbl-bss-gen")
    @TableGenerator(name = "tbl-bss-gen", allocationSize = 1, table = "XBSS_SEQUENCES_GENERATORS")
    @Column(unique = true, nullable = false, precision = 19)
    private long id;

    @Column(length = 255)
    private String descr;

    @Transient
    private String indType;

    @Transient
    private String pkgType;

    @Transient
    private String state;

    @Transient
    private String defaultTemplate;

    //bi-directional many-to-one association to XbssRealmTemplate
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xbssTemplate")
    private Set<XbssRealmTemplate> xbssRealmTemplates;

    //bi-directional many-to-one association to XbssTemplateFunding
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xbssTemplate")
    private Set<XbssTemplateFundingRel> xbssTemplateFundingsRel;

    //bi-directional many-to-one association to XbssTemplatePortfolioPlan
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xbssTemplate")
    private Set<XbssTemplatePortfolioPlan> xbssTemplatePortfolioPlans;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescr() {
        return this.descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getIndType() {
        return this.indType;
    }

    public void setIndType(String indType) {
        this.indType = indType;
    }

    public String getPkgType() {
        return this.pkgType;
    }

    public void setPkgType(String pkgType) {
        this.pkgType = pkgType;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDefaultTemplate() {
        return this.defaultTemplate;
    }

    public void setDefaultTemplate(String defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    public Set<XbssRealmTemplate> getXbssRealmTemplates() {
        return this.xbssRealmTemplates;
    }

    public void setXbssRealmTemplates(Set<XbssRealmTemplate> xbssRealmTemplates) {
        this.xbssRealmTemplates = xbssRealmTemplates;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
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
        XbssTemplate other = (XbssTemplate) obj;
        if (id != other.id)
            return false;
        return true;
    }

}
