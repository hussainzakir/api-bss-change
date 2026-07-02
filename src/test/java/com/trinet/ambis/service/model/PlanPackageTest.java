package com.trinet.ambis.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;



@RunWith(JUnit4.class)
public class PlanPackageTest {

	PlanPackage myPackage;

	@Before
	public void setUp() {
		this.myPackage = new PlanPackage();
		this.myPackage.setName( "PackageAA" );
		this.myPackage.setId( 54321L );
		this.myPackage.setCompanyContributionPercent( new BigDecimal( "75") );
		this.myPackage.setCoverageLevel( "employee" );
	}


	@Test
	public void testCompanyContributionPercent() {
		assertEquals( this.myPackage.getCompanyContributionPercent(), new BigDecimal( "75") );
	}


	@Test
	public void testCoverageLevel() {
		assertTrue( "employee".equals( this.myPackage.getCoverageLevel() ));
	}


	@Test
	public void testCompareTo() {
		//  compare arg is null
		PlanPackage nullPackage = null;
		assertTrue( this.myPackage.compareTo( nullPackage ) > 0 );

		//  compare arg Name is null
		nullPackage = new PlanPackage();
		nullPackage.setName( null );
		assertTrue( this.myPackage.compareTo( nullPackage ) > 0 );

		//  compare arg has same Name as myPackage
		PlanPackage aaPackage = new PlanPackage();
		aaPackage.setName( "PackageAA" );
		assertTrue( this.myPackage.compareTo( aaPackage ) == 0 );

		//  compare arg Name is greater than myPackage Name
		PlanPackage bbPackage = new PlanPackage();
		bbPackage.setName( "PackageBB" );
		assertTrue( this.myPackage.compareTo( bbPackage ) < 0 );

		//  myPackage Name is null
		this.myPackage.setName( null );
		assertTrue( this.myPackage.compareTo( bbPackage ) < 0 );
	}

}