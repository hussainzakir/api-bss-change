package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.impl.BSSStatusDetailsDaoImpl;
import com.trinet.ambis.persistence.model.BSSStatusDetailsDto;
import com.trinet.ambis.service.impl.BSSStatusDetailsServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class BSSStatusDetailsServiceImplTest extends ServiceUnitTest {

	@Mock
	BSSStatusDetailsDaoImpl bssStatusDetailsDao;

	@InjectMocks
	BSSStatusDetailsServiceImpl bssStatusDetailsService;

	@Mock
	ApplicationContext context;

	@Mock
	HrpDao hrpDao;

	private final String CODE = "001";

	@Test
	public void bssStatus() {
		BSSStatusDetailsDto bssStatusDto = new BSSStatusDetailsDto();
		bssStatusDto.setBssStarted(true);
		when(bssStatusDetailsDao.getSubmitedStatus(Mockito.anyString())).thenReturn(bssStatusDto);
		BSSStatusDetailsDto actualResults = bssStatusDetailsService.getBssStatusDetail(CODE);
		assertEquals(true, actualResults.isBssStarted());
		assertEquals(false, actualResults.isBssSubmitted());

	}

}
