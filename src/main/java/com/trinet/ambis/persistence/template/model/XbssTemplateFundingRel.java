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
 * The persistent class for the XBSS_TEMPLATE_FUNDING_REL database table.
 * 
 */
@Entity
@Table(name = "XBSS_TEMPLATE_FUNDING_REL")
@NamedQuery(name = "XbssTemplateFundingRel.findAll", query = "SELECT x FROM XbssTemplateFundingRel x")
@ToString
@NoArgsConstructor
public class XbssTemplateFundingRel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tbl-bss-gen")
    @TableGenerator(name = "tbl-bss-gen", allocationSize = 1, table = "XBSS_SEQUENCES_GENERATORS")
    @Column(unique = true, nullable = false, precision = 19)
    private long id;

    //bi-directional many-to-one association to XbssFunding
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FUNDING_ID")
    private XbssTemplateFunding xbssTemplateFunding;

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

    public XbssTemplateFunding getXbssTemplateFunding() {
        return xbssTemplateFunding;
    }

    public void setXbssTemplateFunding(XbssTemplateFunding xbssTemplateFunding) {
        this.xbssTemplateFunding = xbssTemplateFunding;
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

}
