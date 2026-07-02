/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.mockito.Mockito.when;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.hrp.TemplateDao;
import com.trinet.ambis.persistence.template.model.XbssTemplate;
import com.trinet.ambis.service.impl.TemplateServiceImpl;

/**
 * @author rvutukuri
 *
 */
@RunWith(JUnit4.class)
public class TemplateServiceImplTest {
	
	@InjectMocks
	TemplateServiceImpl tService;

	@Mock
	TemplateDao templateDao;

	@Before
	public void setup() throws ParseException {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void saveTemplate() {
		XbssTemplate xbssTemplate = new XbssTemplate();
		xbssTemplate.setId(1088);
		xbssTemplate.setDescr("Hospitality CA PREMIER");
		xbssTemplate.setIndType("HS");
		xbssTemplate.setState("CA");
		xbssTemplate.setPkgType("PRM");
		xbssTemplate.setDefaultTemplate("0");
		when(templateDao.saveAndFlush(xbssTemplate)).thenReturn(xbssTemplate);
		tService.saveTemplate(xbssTemplate);
	}

}
