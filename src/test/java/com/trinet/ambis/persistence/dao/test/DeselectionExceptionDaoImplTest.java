
package com.trinet.ambis.persistence.dao.test;

import static com.trinet.ambis.persistence.dao.hrp.impl.DeselectionExceptionDaoImpl.PICK_CHOOSE_FLAG_WITH_EXCEPTION;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.persistence.dao.hrp.impl.DeselectionExceptionDaoImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.DaoUtils;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class DeselectionExceptionDaoImplTest extends ServiceUnitTest {

	DeselectionExceptionDaoImpl deselectionExceptionDao;

	Query mockedQuery;

	EntityManager entityManager;

	@Before
	public void setup() {
		entityManager = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		deselectionExceptionDao = new DeselectionExceptionDaoImpl();
		deselectionExceptionDao.setEntityManager( entityManager );
	}

	@Test
	public void getPickChooseWithExceptionTest() {
		long realmYearId = 50;
		String companyCode = "AAA";
		Date effdt = new Date();
		
		when( entityManager.createNamedQuery( PICK_CHOOSE_FLAG_WITH_EXCEPTION )).thenReturn( mockedQuery );
		when( DaoUtils.getResultList( mockedQuery, PICK_CHOOSE_FLAG_WITH_EXCEPTION ) ).thenReturn( getPickChooseWithExceptionData() );


		List<Object[]> result = deselectionExceptionDao.getPickChooseWithException( realmYearId, companyCode, effdt );

		assertEquals( 1, result.size() );
		Object[] row = result.get(0);
		assertEquals( "PICK_CHOOSE_FLAG", row[0] );
		assertEquals( 0, row[1] );
		assertEquals( "0", row[2] );
	}


	private List<Object[]> getPickChooseWithExceptionData() {
		List<Object[]> list = new ArrayList<>();
		list.add( new Object[] { "PICK_CHOOSE_FLAG", 0, "0" } );
		return list;
	}
}
