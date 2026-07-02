package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.hrp.RealmRegionMinFundingDao;
import com.trinet.ambis.persistence.model.RealmRegionMinFunding;
import com.trinet.ambis.service.impl.RealmRegionMinFundingServiceImpl;

@RunWith(JUnit4.class)
public class RealmRegionMinFundingServiceImplTest {

	@InjectMocks
	RealmRegionMinFundingServiceImpl minFundingService;

	@Mock
	RealmRegionMinFundingDao realmRegionMinFundingDao;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Before
	public void findAll() {
		List<RealmRegionMinFunding> minFundings = new ArrayList<>();

		when(realmRegionMinFundingDao.findAll()).thenReturn(minFundings);

		List<RealmRegionMinFunding> actual = minFundingService.findAll();

		assertEquals(minFundings, actual);
	}

	@Test
	public void findByid_realmYearId() {
		List<RealmRegionMinFunding> minFundings = new ArrayList<>();

		when(realmRegionMinFundingDao.findByid_realmYearId(31)).thenReturn(minFundings);

		List<RealmRegionMinFunding> actual = minFundingService.findByid_realmYearId(31);

		assertEquals(minFundings, actual);
	}

}