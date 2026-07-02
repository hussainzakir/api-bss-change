/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.hrp.RealmTypeDao;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.service.impl.RealmTypeServiceImpl;

/**
 * @author rvutukuri
 *
 */
@RunWith(JUnit4.class)
public class RealmTypeServiceImplTest {

	@InjectMocks
	private RealmTypeServiceImpl rtService;

	@Mock
	RealmTypeDao realmTypeDao;

	@Before
	public void setup() throws ParseException {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findByQuarter() {
		Realm rl = new Realm();
		rl.setId(new Long(1));
		rl.setDescription("Ambrose Product");
		rl.setPeoid("AMB");
		rl.setRealmType("P");
		rl.setVerticalCode(null);
		when(realmTypeDao.findByQuarter("Q1")).thenReturn(rl);
		Realm rlReturn = rtService.findByQuarter("Q1");
		assertEquals(rlReturn.getId(), rl.getId());
	}
}
