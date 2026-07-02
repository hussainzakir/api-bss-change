/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.hrp.TemplatePortfolioPlanDao;
import com.trinet.ambis.persistence.template.model.XbssPortfolio;
import com.trinet.ambis.persistence.template.model.XbssRealmTemplate;
import com.trinet.ambis.persistence.template.model.XbssTemplate;
import com.trinet.ambis.persistence.template.model.XbssTemplateFundingRel;
import com.trinet.ambis.persistence.template.model.XbssTemplatePortfolioPlan;
import com.trinet.ambis.service.impl.TemplatePortfolioPlanServiceImpl;

/**
 * @author rvutukuri
 *
 */
@RunWith(JUnit4.class)
public class TemplatePortfolioPlanServiceImplTest {

	@InjectMocks
	private TemplatePortfolioPlanServiceImpl tppService;

	@Mock
	TemplatePortfolioPlanDao templatePortfolioPlanDao;

	@Before
	public void setup() throws ParseException {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findByXbssTemplateTest() {
		List<XbssTemplatePortfolioPlan> ls = new ArrayList<XbssTemplatePortfolioPlan>();
		ls.add(prepareXbssTemplatePortfolioPlan());

		when(templatePortfolioPlanDao.findByXbssTemplate(prepareXbssTemplate())).thenReturn(ls);
		List<XbssTemplatePortfolioPlan> lsReturn = tppService.findByXbssTemplate(prepareXbssTemplate());
		assertEquals(ls.get(0).getXbssTemplate().getId(), lsReturn.get(0).getXbssTemplate().getId());
	}

	private XbssTemplatePortfolioPlan prepareXbssTemplatePortfolioPlan() {
		XbssTemplatePortfolioPlan xtpp = new XbssTemplatePortfolioPlan();
		xtpp.setBenefitPlan("0023MU");
		xtpp.setId(1111L);
		xtpp.setPlanType("10");
		xtpp.setSitus("FL");
		xtpp.setXbssPortfolio(prepareXbssPortfolio());
		xtpp.setXbssTemplate(prepareXbssTemplate());
		return xtpp;
	}

	private XbssPortfolio prepareXbssPortfolio() {
		XbssPortfolio xbssPortfolio = new XbssPortfolio();
		xbssPortfolio.setId(1111L);
		xbssPortfolio.setXbssTemplateFundingsRel(prepareXbssTemplateFundingsRel());
		return null;
	}

	private Set<XbssTemplateFundingRel> prepareXbssTemplateFundingsRel() {
		Set<XbssTemplateFundingRel> set = new HashSet<>();
		XbssTemplateFundingRel fundingRel = new XbssTemplateFundingRel();
		fundingRel.setId(1111L);
		fundingRel.setXbssTemplate(prepareXbssTemplate());
		set.add(fundingRel);
		return set;
	}

	private XbssTemplate prepareXbssTemplate() {
		XbssTemplate xbssTemplate = new XbssTemplate();
		xbssTemplate.setId(1088);
		xbssTemplate.setDescr("Hospitality CA PREMIER");
		xbssTemplate.setIndType("HS");
		xbssTemplate.setState("CA");
		xbssTemplate.setPkgType("PRM");
		xbssTemplate.setDefaultTemplate("0");
		xbssTemplate.setXbssRealmTemplates(prepareXbssRealmTemplates());
		return xbssTemplate;
	}

	private Set<XbssRealmTemplate> prepareXbssRealmTemplates() {
		Set<XbssRealmTemplate> xbssRealmTemplates = new HashSet<>();
		XbssRealmTemplate xbssRealmTemplate = new XbssRealmTemplate();
		xbssRealmTemplate.setId(1111L);
		xbssRealmTemplate.setRealmYearId(BigDecimal.valueOf(31));
		xbssRealmTemplates.add(xbssRealmTemplate);
		return xbssRealmTemplates;
	}
}
