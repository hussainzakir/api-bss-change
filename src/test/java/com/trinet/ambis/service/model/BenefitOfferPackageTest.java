package com.trinet.ambis.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;



//@RunWith(PowerMockRunner.class)
//
//@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*"})
@RunWith(MockitoJUnitRunner.class)
public class BenefitOfferPackageTest {

	BenefitOfferPackage myPackage;

	@Before
	public void setUp() {
		this.myPackage = new BenefitOfferPackage();
		this.myPackage.setType( "AA" );
		Set<Long> set = new TreeSet<>();
		set.addAll( Arrays.asList( 1L, 13L, 23L, 31L ) );
		this.myPackage.setPlanCarrierIds( set );
		this.myPackage.setPlanPackageId( 54321L );
	}


	@Test
	public void testType() {
		String localType = this.myPackage.getType();
		assertEquals( localType, "AA" );
	}


	@Test
	public void testPlanCarrierIds() {
		assertTrue( this.myPackage.getPlanCarrierIds().contains( 31L ) );
		assertFalse( this.myPackage.getPlanCarrierIds().contains( 7L ) );
	}


	@Test
	public void testPlanPackageId() {
		assertEquals( this.myPackage.getPlanPackageId(), 54321L );
	}

	@Test
	public void testCompareTo() {
		//  compare arg is null
		BenefitOfferPackage nullPackage = null;
		int i = this.myPackage.compareTo( nullPackage );
		assertTrue( i > 0 );

		//  compare arg Type is null
		nullPackage = new BenefitOfferPackage();
		nullPackage.setType( null );
		i = this.myPackage.compareTo( nullPackage );
		assertTrue( i > 0 );

		//  compare arg has same Type as myPackage
		BenefitOfferPackage aaPackage = new BenefitOfferPackage();
		aaPackage.setType( "AA" );
		i = this.myPackage.compareTo( aaPackage );
		assertTrue( i == 0 );

		//  compare arg Type is greater than myPackage Type
		BenefitOfferPackage bbPackage = new BenefitOfferPackage();
		bbPackage.setType( "BB" );
		i = this.myPackage.compareTo( bbPackage );
		assertTrue( i < 0 );

		//  myPackage Type is null
		this.myPackage.setType( null );
		i = this.myPackage.compareTo( bbPackage );
		assertTrue( i < 0 );
	}

}