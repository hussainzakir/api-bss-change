package com.trinet.ambis.service.unit;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.service.impl.RefreshServiceImpl;

@RunWith(JUnit4.class)
public class RefreshServiceImplTest {

	@InjectMocks
	RefreshServiceImpl refreshService;

	@Mock
	HrpDao hrpDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void refreshPlanView() {
		doNothing().when(hrpDao).refreshPlanView();

		refreshService.refreshPlanView();

		verify(hrpDao, times(1)).refreshPlanView();
	}

}