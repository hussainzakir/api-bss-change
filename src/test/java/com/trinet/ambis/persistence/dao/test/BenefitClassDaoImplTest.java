package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.hrp.BenefitClassDao;
import com.trinet.ambis.persistence.dao.hrp.impl.BenefitClassDaoImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class BenefitClassDaoImplTest {

	BenefitClassDao benefitClassDao = new BenefitClassDaoImpl();
	EntityManager entityManager = null;
	Query mockedQuery = null;
	Company company = null;
	BenefitGroup group = null;

	@Before
	public void setup() {
		entityManager = mock( EntityManager.class );
		mockedQuery = mock( Query.class );
		((BenefitClassDaoImpl) benefitClassDao).setEntityManager( entityManager );
		when( mockedQuery.getParameterValue( ArgumentMatchers.anyString() )).thenReturn( "mockParam" );
		when( ((BenefitClassDaoImpl) benefitClassDao).getEntityManager().createNamedQuery( ArgumentMatchers.anyString() )).thenReturn( mockedQuery );
		company = setupCompany();
		group = this.setupGroup();
	}

	@Test
	public void getEligClassTest() {
		String result = "ACMP";
		when( mockedQuery.getSingleResult() ).thenReturn(result);
		String eligClass = benefitClassDao.getEligClass(company, group);
		assertEquals( "ACMP".equals( eligClass ), true );
	}

	@Test
	public void getEligClassExceptionTest() {
		String result = "";
		when( mockedQuery.getSingleResult() ).thenThrow( new javax.persistence.NoResultException() );
		String eligClass = benefitClassDao.getEligClass(company, group);
		assertEquals( result, eligClass );
	}

	@Test
	public void getBenProgramBenClassMappingsTest() {
		when( mockedQuery.getResultList() ).thenReturn( getCMPResultList() );
		Map<String,String> map = benefitClassDao.getBenProgramBenClassMappings( company );
		assertTrue( map.size() == 26 );
		assertEquals( "JCMP", map.get( "002D3Z" ) );
	}


	public static Company setupCompany() {
		Company cmp = new Company();
		cmp.setId( 123 );
		cmp.setCode( "CMP" );
		cmp.setRealmPlanYear( new RealmPlanYear() );
		cmp.getRealmPlanYear().setPlanYearEnd( new Date() );
		return cmp;
	}
	
	private BenefitGroup setupGroup() {
		BenefitGroup grp = new BenefitGroup();
		grp.setBenefitProgram( "001ABC" );
		return grp;
	}

	public static List<Object[]> getCMPResultList() {
		List<Object[]> result = new ArrayList<>();
		result.add( new String[] { "0022U6", "ACMP" } );
		result.add( new String[] { "001FBK", "BCMP" } );
		result.add( new String[] { "001FBL", "CCMP" } );
		result.add( new String[] { "002D3T", "DCMP" } );
		result.add( new String[] { "002D3U", "ECMP" } );
		result.add( new String[] { "002D3V", "FCMP" } );
		result.add( new String[] { "002D3W", "GCMP" } );
		result.add( new String[] { "002D3X", "HCMP" } );
		result.add( new String[] { "002D3Y", "ICMP" } );
		result.add( new String[] { "002D3Z", "JCMP" } );
		result.add( new String[] { "002D40", "KCMP" } );
		result.add( new String[] { "002D41", "LCMP" } );
		result.add( new String[] { "002D42", "MCMP" } );
		result.add( new String[] { "002D43", "NCMP" } );
		result.add( new String[] { "002D49", "OCMP" } );
		result.add( new String[] { "002D4B", "PCMP" } );
		result.add( new String[] { "002D4C", "QCMP" } );
		result.add( new String[] { "002D4D", "RCMP" } );
		result.add( new String[] { "002D4E", "SCMP" } );
		result.add( new String[] { "002D4F", "TCMP" } );
		result.add( new String[] { "002D4G", "UCMP" } );
		result.add( new String[] { "002D4H", "VCMP" } );
		result.add( new String[] { "002D4I", "WCMP" } );
		result.add( new String[] { "002D4J", "XCMP" } );
		result.add( new String[] { "002D4K", "YCMP" } );
		result.add( new String[] { "002D4L", "ZCMP" } );
		return result;
	}

}
