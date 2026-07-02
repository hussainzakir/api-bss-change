package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.trinet.ambis.persistence.dao.hrp.impl.BenefitClassDaoImpl;
import com.trinet.ambis.service.impl.BenefitClassServiceImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.persistence.dao.hrp.BenefitClassDao;
import com.trinet.ambis.persistence.dao.test.BenefitClassDaoImplTest;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenefitClassService;


@RunWith(MockitoJUnitRunner.class)
public class BenefitClassServiceImplTest extends ServiceUnitTest {

	@InjectMocks
BenefitClassServiceImpl benefitClassService;

	@Mock
    BenefitClassDaoImpl benefitClassDao;


	@Test
	public void generateClassCodeTest1() {
		//Scenario 1 : When group has eligConfig then return group's eligConfig

		String benProgram = "001JZE";
		String eligConfig1 = "BABC";

		Company company = new Company();
		company.setCode( "ABC" );
		BenefitGroup group = new BenefitGroup();
		group.setBenefitProgram(benProgram);
		group.setEligConfig1(eligConfig1);

		String result = benefitClassService.generateClassCode( company, group );
		assertEquals( eligConfig1, result );
	}


	@Test
	public void generateClassCodeTest2() {
		/* Scenario 2a : When group's eligConfig is null
		 * and eligConfig is found in DB */

		String benProgram = "002D4C";

		Company company = BenefitClassDaoImplTest.setupCompany();
		BenefitGroup group = new BenefitGroup();
		group.setBenefitProgram( benProgram );
		when( benefitClassDao.getEligClass( company, group ) ).thenReturn( getEligClass( group.getBenefitProgram() ));

		String result = benefitClassService.generateClassCode( company, group );
		assertEquals( "QCMP", result );


		/* Scenario 2b : When group's eligConfig is blank
		 * and eligConfig is found in DB */

		group.setEligConfig1( " " );
		group.setBenefitProgram( benProgram );
		when( benefitClassDao.getEligClass( company, group ) ).thenReturn( getEligClass( group.getBenefitProgram() ));

		result = benefitClassService.generateClassCode( company, group );
		assertEquals( "QCMP", result );
	}


	@Test
	public void generateClassCodeTest3() {
		/* Scenario 3a : When group's eligConfig is null
		 * no matching eligConfig is found in DB
		 * no previously derived eligConfig is found
		 * class codes with prefix A-Z already used
		 * brand new eligConfig is derived with "10" prefix */

		String benProgram = "002ZZZ";

		Company company = BenefitClassDaoImplTest.setupCompany();
		BenefitGroup group = new BenefitGroup();
		group.setBenefitProgram( benProgram );
		when( benefitClassDao.getEligClass( company, group ) ).thenReturn( getEligClass( group.getBenefitProgram() ));
		when( benefitClassDao.getBenProgramBenClassMappings( company ) ).thenReturn( getBenProgramBenClassMappings() );

		String result = benefitClassService.generateClassCode( company, group );
		assertEquals( "10CMP", result );

		/*Scenario 3b : group eligConfig is still null
		 * when service was called for Scenario 3a, that result
		 * would have been stored in the internal savedMappings.
		 * By calling again for the same benefit program, 
		 * the savedMappings result will be returned. */
		group = new BenefitGroup();
		group.setBenefitProgram( benProgram );
		String secondResult = benefitClassService.generateClassCode( company, group );
		assertEquals( "10CMP", secondResult );

		/*Scenario 3c : group eligConfig is null
		 * Group is mapped to a blank ben class
		 * when service was called for Scenario 3a, that result
		 * would have been stored in the internal savedMappings.
		 * By calling again for the same benefit program, 
		 * the savedMappings result will be returned. */
		group = new BenefitGroup();
		group.setBenefitProgram( "003033" );
		when( benefitClassDao.getEligClass( company, group ) ).thenReturn( " " );
		String thirdResult = benefitClassService.generateClassCode( company, group );
		assertEquals( "11CMP", thirdResult );

	}

	@Test
	public void generateAllClassCodesTest() {
		/* Scenario 4 : A list of several BenefitGroup objects, some of which have
		 * ben class values, some of which are missing.  Some of the missing values
		 * can be found in the mock DB data, others must be derived.
		 * Just to put a wrinkle, I'm assigning prefix "5" manually and the service
		 * will recognize this and skip from "4" to "6" */

		Company company = new Company();
		company.setCode( "YYC" );
		BenefitGroup groupA = new BenefitGroup();
		BenefitGroup groupB = new BenefitGroup();
		BenefitGroup groupD = new BenefitGroup();
		BenefitGroup group1 = new BenefitGroup();
		BenefitGroup group2 = new BenefitGroup();
		BenefitGroup group3 = new BenefitGroup();
		groupA.setBenefitProgram( "00220A" );
		groupB.setBenefitProgram( "00220B" );
		groupB.setEligConfig1( "1YYC" );
		groupD.setBenefitProgram( "00220D" );
		group1.setBenefitProgram( "003011" );
		group1.setEligConfig1( "5YYC" );
		group2.setBenefitProgram( "003022" );
		group3.setBenefitProgram( "003033" );
		List<BenefitGroup> groups = new ArrayList<>();
		groups.add(group1);
		groups.add(groupA);
		groups.add(groupB);
		groups.add(group2);
		groups.add(groupD);
		groups.add(group3);
		
		when( benefitClassDao.getBenProgramBenClassMappings( company ) ).thenReturn( getYYCBenClassMappings() );
		when( benefitClassDao.getEligClass( any( Company.class ), any( BenefitGroup.class ) ) )
				.thenAnswer( args -> {
					Map<String,String> map = getYYCBenClassMappings();
					BenefitGroup gp = (BenefitGroup) args.getArgument(1);
					return map.get( gp.getBenefitProgram() );
				});

		benefitClassService.generateAllClassCodes( company, groups );
		assertEquals( "0YYC", groupA.getEligConfig1() );
		assertEquals( "1YYC", groupB.getEligConfig1() );
		assertEquals( "3YYC", groupD.getEligConfig1() );
		assertEquals( "5YYC", group1.getEligConfig1() );
		assertEquals( "6YYC", group2.getEligConfig1() );
		assertEquals( "7YYC", group3.getEligConfig1() );
	}

	@Test
	public void generateNewClassCodesSpaceTest() {
		/* Test generating a class code for a new client when the DAO method
		 * returns a benefit program with a single space for the class code.
		 * This case may happen when the CLIENT_OPTN2 row has been created for a new client's 
		 * benefit program and the CLIENT_OPT2A row has been created with a blank ben class. */

		Company company = new Company();
		company.setCode( "YYC" );
		BenefitGroup groupA = new BenefitGroup();
		groupA.setBenefitProgram( "00220A" );

		when( benefitClassDao.getBenProgramBenClassMappings( company ) ).thenReturn( getNewBenClassMapSpace() );
		when( benefitClassDao.getEligClass( any( Company.class ), any( BenefitGroup.class ) ) )
				.thenAnswer( args -> {
					Map<String,String> map = getNewBenClassMapSpace();
					BenefitGroup gp = (BenefitGroup) args.getArgument(1);
					return map.get( gp.getBenefitProgram() );
				});

		groupA.setEligConfig1( benefitClassService.generateClassCode( company, groupA ) );
		assertEquals( "0YYC", groupA.getEligConfig1() );

		BenefitGroup groupB = new BenefitGroup();
		groupB.setBenefitProgram( "00220B" );
		groupB.setEligConfig1( benefitClassService.generateClassCode( company, groupB ) );
		assertEquals( "1YYC", groupB.getEligConfig1() );
}

	@Test
	public void generateNewClassCodesSpaceNull() {
		/* Test generating a class code for a new client when the DAO method
		 * returns a benefit program with null for the class code.
		 * This case may happen when the CLIENT_OPTN2 row has been created for a new client's 
		 * benefit program but no CLIENT_OPT2A row has been created. */

		Company company = new Company();
		company.setCode( "YYC" );
		BenefitGroup groupA = new BenefitGroup();
		groupA.setBenefitProgram( "00220A" );

		when( benefitClassDao.getBenProgramBenClassMappings( company ) ).thenReturn( getNewBenClassMapNull() );
		when( benefitClassDao.getEligClass( any( Company.class ), any( BenefitGroup.class ) ) )
				.thenAnswer( args -> {
					Map<String,String> map = getNewBenClassMapNull();
					BenefitGroup gp = (BenefitGroup) args.getArgument(1);
					return map.get( gp.getBenefitProgram() );
				});

		groupA.setEligConfig1( benefitClassService.generateClassCode( company, groupA ) );
		assertEquals( "0YYC", groupA.getEligConfig1() );

		BenefitGroup groupB = new BenefitGroup();
		groupB.setBenefitProgram( "00220B" );
		groupB.setEligConfig1( benefitClassService.generateClassCode( company, groupB ) );
		assertEquals( "1YYC", groupB.getEligConfig1() );
	}




	private static String getEligClass( String benefitProgram ) {
		Map<String,String> map = getBenProgramBenClassMappings();
		return map.get( benefitProgram );
	}

	private static Map<String,String> getBenProgramBenClassMappings() {
		List<Object[]> result = BenefitClassDaoImplTest.getCMPResultList();
		Map<String,String> programClassMap = new HashMap<>();
		for( Object[] row : result ) {
			programClassMap.put( (String) row[0], (String) row[1] );
		}
		return programClassMap;
	}


	private static Map<String,String> getYYCBenClassMappings() {
		List<Object[]> result = getYYCResultList();
		Map<String,String> programClassMap = new HashMap<>();
		for( Object[] row : result ) {
			programClassMap.put( (String) row[0], (String) row[1] );
		}
		return programClassMap;
	}

	public static List<Object[]> getYYCResultList() {
		List<Object[]> result = new ArrayList<>();
		result.add( new String[] { "00220A", "0YYC" } );
		result.add( new String[] { "00220B", "1YYC" } );
		result.add( new String[] { "00220C", "2YYC" } );
		result.add( new String[] { "00220D", "3YYC" } );
		return result;
	}



	private static Map<String,String> getNewBenClassMapSpace() {
		List<Object[]> result = getResultListSpace();
		Map<String,String> programClassMap = new HashMap<>();
		for( Object[] row : result ) {
			programClassMap.put( (String) row[0], (String) row[1] );
		}
		return programClassMap;
	}

	private static List<Object[]> getResultListSpace() {
		List<Object[]> result = new ArrayList<>();
		result.add( new String[] { "00220A", " " } );
		return result;
	}



	private static Map<String,String> getNewBenClassMapNull() {
		List<Object[]> result = getResultListNull();
		Map<String,String> programClassMap = new HashMap<>();
		for( Object[] row : result ) {
			programClassMap.put( (String) row[0], (String) row[1] );
		}
		return programClassMap;
	}

	private static List<Object[]> getResultListNull() {
		List<Object[]> result = new ArrayList<>();
		result.add( new String[] { "00220A", null } );
		return result;
	}

}