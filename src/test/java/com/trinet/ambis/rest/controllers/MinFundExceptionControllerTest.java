package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;


@RunWith(MockitoJUnitRunner.class)
public class MinFundExceptionControllerTest extends ServiceUnitTest {

	@InjectMocks
	MinFundExceptionController minFundExceptionController;

	@Mock
	private MinFundExceptionService minFundExceptionService;

	private static final String EMPLID = "0000000123456";
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
	}

	@Test
	public void getAllMinFundExceptions() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();

		when(minFundExceptionService.findAllActive()).thenReturn(prepareMinFundExceptionDtos());
		List<MinFundExceptionDto> actulResult = minFundExceptionController.getAllMinFundExceptions(request);

		assertEquals(2, actulResult.size());
	}
	
	@Test
	public void getMinFundException() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();
		long id = 1111L;
		
		when(minFundExceptionService.findBy(id)).thenReturn(prepareMinFundExceptionDtos().get(0));
		MinFundExceptionDto actulResult = minFundExceptionController.getMinFundException(request, id);

		assertEquals(1111L, actulResult.getId());
	}

	@Test(expected = Exception.class)
	public void getAllMinFundExceptions_exception() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();

		when(minFundExceptionService.findAllActive()).thenThrow(new RuntimeException());

		minFundExceptionController.getAllMinFundExceptions(request);
	}

	@Test(expected = BSSApplicationException.class)
	public void getAllMinFundExceptions_bssexception() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();

		when(minFundExceptionService.findAllActive()).thenThrow(new BSSApplicationException());

		minFundExceptionController.getAllMinFundExceptions(request);
	}

	@Test
	public void updateMinFundException() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();
		MinFundExceptionDto dto = new MinFundExceptionDto();
		dto.setId(1111L);

		when(minFundExceptionService.update(dto)).thenReturn(dto);
		MinFundExceptionDto actulResult = minFundExceptionController.updateMinFundException(request, dto);

		assertEquals(1111L, actulResult.getId());
	}

	@Test(expected = Exception.class)
	public void updateMinFundException_exception() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();
		MinFundExceptionDto dto = new MinFundExceptionDto();
		dto.setId(1111L);

		when(minFundExceptionService.update(dto)).thenThrow(new RuntimeException());
		minFundExceptionController.updateMinFundException(request, dto);
	}

	@Test(expected = BSSApplicationException.class)
	public void updateMinFundException_bssexception() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();
		MinFundExceptionDto dto = new MinFundExceptionDto();
		dto.setId(1111L);

		when(minFundExceptionService.update(dto)).thenThrow(new BSSApplicationException());
		minFundExceptionController.updateMinFundException(request, dto);
	}

	@Test
	public void saveMinFundException() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();
		MinFundExceptionDto dto = new MinFundExceptionDto();
		dto.setId(1111L);

		when(minFundExceptionService.save(dto)).thenReturn(dto);
		MinFundExceptionDto actulResult = minFundExceptionController.saveMinFundException(request, dto);

		assertEquals(1111L, actulResult.getId());
	}

	@Test(expected = Exception.class)
	public void saveMinFundException_exception() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();
		MinFundExceptionDto dto = new MinFundExceptionDto();
		dto.setId(1111L);

		when(minFundExceptionService.save(dto)).thenThrow(new RuntimeException());
		minFundExceptionController.saveMinFundException(request, dto);
	}

	@Test(expected = BSSApplicationException.class)
	public void saveMinFundException_bssexception() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();
		MinFundExceptionDto dto = new MinFundExceptionDto();
		dto.setId(1111L);

		when(minFundExceptionService.save(dto)).thenThrow(new BSSApplicationException());
		minFundExceptionController.saveMinFundException(request, dto);
	}

	private List<MinFundExceptionDto> prepareMinFundExceptionDtos() {
		MinFundExceptionDto dto = new MinFundExceptionDto();
		dto.setId(1111L);
		MinFundExceptionDto dto1 = new MinFundExceptionDto();
		dto1.setId(2222L);
		return Arrays.asList(dto, dto1);
	}

}
