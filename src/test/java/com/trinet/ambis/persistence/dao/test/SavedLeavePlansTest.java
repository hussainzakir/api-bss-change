package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.ps.impl.SavedPlanOptns;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class SavedLeavePlansTest {
	SavedPlanOptns savedLeavePlans;
	EntityManager entityManager = null;
	Query mkQryGetLvOPTN = null;
	Query mkQryGetLvPLAN = null;
	Query mkQryGetSvgsPLAN = null;
	Query mkQryGetSvgsOPTN = null;
	Query mkQryGetFsaPLAN = null;
	Query mkQryGetFsaOPTN = null;
	Query mkQryDelPLAN = null;
	Query mkQryDelOPTN = null;
	Query mkQryDelCOST = null;
	Query mkQryGetMaxId = null;
	Query mkQryInsPLAN = null;
	Query mkQryInsOPTN = null;
	Query mkQryInsCOST = null;
	Company comp = null;
	BenefitGroup group = null;
	String cloneBenenProg = "CloneBenProg";
	String effdtStr = null;
	Set<String> planList = new HashSet<String>();
	String bandCode = "bandCode";
	String quarter = "SM";

	@Before
	public void setup() throws ParseException {
		comp = new Company();
		comp.setCode("G48");
		effdtStr = "01-DEC-2018";
		entityManager = mock(EntityManager.class);
		mkQryGetLvOPTN = mock(Query.class);
		mkQryGetLvPLAN = mock(Query.class);
		mkQryGetSvgsPLAN = mock(Query.class);
		mkQryGetSvgsOPTN = mock(Query.class);
		mkQryGetFsaPLAN = mock(Query.class);
		mkQryGetFsaOPTN = mock(Query.class);
		mkQryDelPLAN = mock(Query.class);
		mkQryDelOPTN = mock(Query.class);
		mkQryDelCOST = mock(Query.class);
		mkQryGetMaxId = mock(Query.class);
		mkQryInsPLAN = mock(Query.class);
		mkQryInsOPTN = mock(Query.class);
		mkQryInsCOST = mock(Query.class);

		when(entityManager.createNamedQuery("GET_CURRENT_PLANS")).thenReturn(mkQryGetLvOPTN);
		when(mkQryGetLvOPTN.getResultList()).thenReturn( prepareSaveLeavePlans() );

		when(entityManager.createNamedQuery("GET_BEN_DEFN_LEAVE_PLANS")).thenReturn(mkQryGetLvPLAN);
		when(mkQryGetLvPLAN.getResultList()).thenReturn(prepareBenDefnLeavePlansMockData());

		when(entityManager.createNamedQuery("GET_CURRENT_SAVINGS_PLANS")).thenReturn(mkQryGetSvgsPLAN);
		when(mkQryGetSvgsPLAN.getResultList()).thenReturn(mockSavingsPlanData());

		when(entityManager.createNamedQuery("GET_CURRENT_SAVINGS_OPTNS")).thenReturn(mkQryGetSvgsOPTN);
		when(mkQryGetSvgsOPTN.getResultList()).thenReturn(mockSavingsOptnData());

		when(entityManager.createNamedQuery("GET_CURRENT_FSA_PLANS")).thenReturn(mkQryGetFsaPLAN);
		when(mkQryGetFsaPLAN.getResultList()).thenReturn(mockFsaPlanData());

		when(entityManager.createNamedQuery("GET_CURRENT_FSA_OPTNS")).thenReturn(mkQryGetFsaOPTN);
		when(mkQryGetFsaOPTN.getResultList()).thenReturn(mockFsaOptnData());

		when(entityManager.createNamedQuery("CLEAN_LEAVE_SVNGS_PLAN")).thenReturn(mkQryDelPLAN);
		when(entityManager.createNamedQuery("CLEAN_LEAVE_SVNGS_OPTN")).thenReturn(mkQryDelOPTN);
		when(entityManager.createNamedQuery("CLEAN_LEAVE_SVNGS_COST")).thenReturn(mkQryDelCOST);
		when(mkQryDelPLAN.executeUpdate()).thenReturn(1);
		when(mkQryDelOPTN.executeUpdate()).thenReturn(1);
		when(mkQryDelCOST.executeUpdate()).thenReturn(1);

		when(entityManager.createNamedQuery("GET_MAX_OPTION_COST")).thenReturn(mkQryGetMaxId);
		when(mkQryGetMaxId.getResultList()).thenReturn(mockMaxOptnCostIdData());

		when(entityManager.createNamedQuery("INSERT_SAVED_PLAN")).thenReturn(mkQryInsPLAN);
		when(entityManager.createNamedQuery("INSERT_SAVED_OPTN")).thenReturn(mkQryInsOPTN);
		when(entityManager.createNamedQuery("INSERT_SAVED_COST")).thenReturn(mkQryInsCOST);
		when(mkQryInsPLAN.executeUpdate()).thenReturn(1);
		when(mkQryInsOPTN.executeUpdate()).thenReturn(1);
		when(mkQryInsCOST.executeUpdate()).thenReturn(1);

		savedLeavePlans = new SavedPlanOptns(comp, effdtStr, entityManager);
	}


	@Test
	public void areLeavePlansSaved() {
		boolean actualResult = savedLeavePlans.areLeavePlansSaved();
		assertEquals(false, actualResult);
	}


	@Test
	public void areSavingsPlansSaved() {
		boolean actualResult = savedLeavePlans.areSavingsPlansSaved();
		assertEquals(false, actualResult);
	}


	@Test
	public void areFsaPlansSaved() {
		boolean actualResult = savedLeavePlans.areFsaPlansSaved();
		assertEquals(false, actualResult);
	}


	@Test
	public void saveLeavePlans() {

		boolean result = savedLeavePlans.saveLeavePlans();

		verify(mkQryGetLvOPTN, times(1)).getResultList();
		verify(mkQryGetLvPLAN, times(1)).getResultList();
		assertEquals(true, result);
	}


	@Test
	public void saveCurrentPlansTest() {

		savedLeavePlans.saveCurrentPlans();

		verify(mkQryGetLvOPTN, times(1)).getResultList();
		verify(mkQryGetLvPLAN, times(1)).getResultList();
		verify(mkQryGetSvgsPLAN, times(1)).getResultList();
		verify(mkQryGetSvgsOPTN, times(1)).getResultList();
		verify(mkQryGetFsaPLAN, times(1)).getResultList();
		verify(mkQryGetFsaOPTN, times(1)).getResultList();
		assertEquals(true, savedLeavePlans.areLeavePlansSaved());
		assertEquals(true, savedLeavePlans.areSavingsPlansSaved());
		assertEquals(true, savedLeavePlans.areFsaPlansSaved());
	}


	@Test
	public void restoreLeavePlans() {

		savedLeavePlans.saveLeavePlans();
		int actualResult = savedLeavePlans.restoreLeavePlans("UPP");

		assertEquals( true, savedLeavePlans.areLeavePlansSaved() );
		assertEquals( 3, actualResult );

		verify(mkQryDelPLAN, times(1)).executeUpdate();
		verify(mkQryDelOPTN, times(1)).executeUpdate();
		verify(mkQryDelCOST, times(1)).executeUpdate();
		verify(mkQryGetMaxId, times(1)).getResultList();

		verify(mkQryInsPLAN, times(1)).executeUpdate();
		verify(mkQryInsOPTN, times(2)).executeUpdate();
		verify(mkQryInsCOST, times(0)).executeUpdate();
	}


	@Test
	public void restoreSavingsPlans() {

		savedLeavePlans.saveCurrentPlans();
		int actualResult = savedLeavePlans.restoreSavingsPlans("UPP");

		assertEquals( 4, actualResult );
		verify(mkQryGetSvgsPLAN, times(1)).getResultList();
		verify(mkQryGetSvgsOPTN, times(1)).getResultList();
		verify(mkQryDelPLAN, times(1)).executeUpdate();
		verify(mkQryDelOPTN, times(1)).executeUpdate();
		verify(mkQryDelCOST, times(1)).executeUpdate();
		verify(mkQryGetMaxId, times(1)).getResultList();

		verify(mkQryInsPLAN, times(1)).executeUpdate();
		verify(mkQryInsOPTN, times(2)).executeUpdate();
		verify(mkQryInsCOST, times(1)).executeUpdate();
	}


	@Test
	public void restoreFsaPlansTest() {

		savedLeavePlans.saveCurrentPlans();

		int actualResult = savedLeavePlans.restoreFsaPlans("UPP");

		assertEquals( 8, actualResult );
		verify(mkQryGetFsaPLAN, times(1)).getResultList();
		verify(mkQryGetFsaOPTN, times(1)).getResultList();

		verify(mkQryInsPLAN, times(2)).executeUpdate();
		verify(mkQryInsOPTN, times(6)).executeUpdate();
		verify(mkQryInsCOST, times(0)).executeUpdate();
	}


	private List<Object[]> mockSavingsPlanData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[15];
		r[0] = "UPP";
		r[1] = "4Q";
		r[2] = "4Q";
		r[3] = BigDecimal.valueOf(8);
		r[4] = BigDecimal.valueOf(0);
		r[5] = "Y";
		r[6] = BigDecimal.valueOf(0);
		r[7] = "000H";
		r[8] = "N";
		r[9] = "";
		r[10] = "Y";
		r[11] = "Y";
		r[12] = "Y";
		r[13] = "";
		r[14] = "";
		data.add(r);
		return data;
	}

	private List<Object[]> mockMaxOptnCostIdData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[14];
		r[0] = BigDecimal.valueOf(1111);
		r[1] = BigDecimal.valueOf(2222);
		data.add(r);
		return data;
	}

	private List<Object[]> prepareSaveLeavePlans() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[14];
		r[0] = "50";
		r[1] = "O";
		r[2] = "00247U";
		r[3] = "SCK";
		r[4] = "";
		r[5] = new BigDecimal(60);
		r[6] = "SICK";
		r[7] = "N";
		r[8] = "0120";
		r[9] = "";
		r[10] = "";
		r[11] = "";
		r[12] = new BigDecimal(0);
		r[13] = "";
		data.add(r);
		r = new Object[14];
		r[0] = "52";
		r[1] = "W";
		r[2] = "00247T";
		r[3] = "PER";
		r[4] = "";
		r[5] = new BigDecimal(50);
		r[6] = "PERSON";
		r[7] = "N";
		r[8] = "0120";
		r[9] = "";
		r[10] = "";
		r[11] = "";
		r[12] = new BigDecimal(0);
		r[13] = "";
		data.add(r);
		return data;
	}

	private List<Object[]> mockSavingsOptnData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[23];
		r[0] = "UV6";
		r[1] = "4Q";
		r[2] = new BigDecimal("1052");
		r[3] = BigDecimal.ONE;
		r[4] = "O";
		r[5] = "0029X5";
		r[6] = " ";
		r[7] = "4R2";
		r[8] = new BigDecimal("50");
		r[9] = "4KROTH";
		r[10] = "N";
		r[11] = "22TK";
		r[12] = " ";
		r[13] = " ";
		r[14] = " ";
		r[15] = BigDecimal.ZERO;
		r[16] = " ";
		r[17] = new BigDecimal("1011");
		r[18] = "P";
		r[19] = " ";
		r[20] = " ";
		r[21] = " ";
		r[22] = " ";
		data.add(r);

		r = new Object[23];
		r[0] = "UV6";
		r[1] = "4Q";
		r[2] = new BigDecimal("1053");
		r[3] = new BigDecimal("9999");
		r[4] = "W";
		r[5] = " ";
		r[6] = " ";
		r[7] = "W";
		r[8] = BigDecimal.ZERO;
		r[9] = " ";
		r[10] = "Y";
		r[11] = " ";
		r[12] = " ";
		r[13] = " ";
		r[14] = " ";
		r[15] = BigDecimal.ZERO;
		r[16] = " ";
		r[17] = null;
		r[18] = null;
		r[19] = null;
		r[20] = null;
		r[21] = null;
		r[22] = null;
		data.add( r );
		
		return data;
	}

	private List<Object[]> mockFsaPlanData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[15];
		r[0] = "UV6";
		r[1] = "60";
		r[2] = "60";
		r[3] = BigDecimal.ZERO;
		r[4] = BigDecimal.ZERO;
		r[5] = "Y";
		r[6] = BigDecimal.ZERO;
		r[7] = "007T";
		r[8] = "N";
		r[9] = " ";
		r[10] = "Y";
		r[11] = "Y";
		r[12] = "Y";
		r[13] = " ";
		r[14] = " ";
		data.add(r);

		r = new Object[15];
		r[0] = "UV6";
		r[1] = "61";
		r[2] = "61";
		r[3] = BigDecimal.ZERO;
		r[4] = BigDecimal.ZERO;
		r[5] = "Y";
		r[6] = BigDecimal.ZERO;
		r[7] = "007U";
		r[8] = "N";
		r[9] = " ";
		r[10] = "Y";
		r[11] = "Y";
		r[12] = "Y";
		r[13] = " ";
		r[14] = " ";
		data.add(r);

		return data;
	}

	private List<Object[]> mockFsaOptnData() {
		List<Object[]> data = new ArrayList<Object[]>();

		data.add( makeFsaOptnArray( "UV6","60","985","1","O","000SRU"," ","HFS","50","FSA002","N","0200"," "," "," ","0"," ","","","","","","" ) );
		data.add( makeFsaOptnArray( "UV6","60","986","2","O","000TMG"," ","LUP","50","FSA003","N","0200"," "," "," ","0"," ","","","","","","" ) );
		data.add( makeFsaOptnArray( "UV6","60","987","3","O","002LNC"," ","LUB","60","FSA0B3","N","0200"," "," "," ","0"," ","","","","","","" ) );
		data.add( makeFsaOptnArray( "UV6","60","988","4","O","002LND"," ","HFB","60","FSA0B2","N","0200"," "," "," ","0"," ","","","","","","" ) );
		data.add( makeFsaOptnArray( "UV6","60","989","9999","W"," "," ","W","0"," ","Y"," "," "," "," ","0"," ","","","","","","" ) );
		data.add( makeFsaOptnArray( "UV6","61","990","1","O","000SRV"," ","DFS","50","FSAD01","N","0200"," "," "," ","0"," ","","","","","","" ) );
		data.add( makeFsaOptnArray( "UV6","61","991","2","O","002LNE"," ","DFB","60","FSADB1","N","0200"," "," "," ","0"," ","","","","","","" ) );
		data.add( makeFsaOptnArray( "UV6","61","992","9999","W"," "," ","W","0"," ","Y"," "," "," "," ","0"," ","","","","","","" ) );

		return data;
	}

	private Object[] makeFsaOptnArray( String... elements ) {
		Object[] row = new Object[23];
		row[0] = elements[0];
		row[1] = elements[1];
		row[2] = new BigDecimal( elements[2] );
		row[3] = new BigDecimal( elements[3] );
		row[4] = elements[4];
		row[5] = elements[5];
		row[6] = elements[6];
		row[7] = elements[7];
		row[8] = new BigDecimal( elements[8] );
		row[9] = elements[9];
		row[10] = elements[10];
		row[11] = elements[11];
		row[12] = elements[12];
		row[13] = elements[13];
		row[14] = elements[14];
		row[15] = new BigDecimal( elements[15] );
		row[16] = elements[16];
		row[17] = "".equals( elements[17] ) ? null : new BigDecimal( elements[17] );
		row[18] = "".equals( elements[18] ) ? null : elements[18];
		row[19] = "".equals( elements[19] ) ? null : elements[19];
		row[20] = "".equals( elements[20] ) ? null : elements[20];
		row[21] = "".equals( elements[21] ) ? null : elements[21];
		row[22] = "".equals( elements[22] ) ? null : elements[22];
		return row;
	}

	private List<Object[]> prepareBenDefnLeavePlansMockData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[14];
		r[0] = "50";
		r[1] = "50";
		r[2] = new BigDecimal(0);
		r[3] = new BigDecimal(0);
		r[4] = "N";
		r[5] = new BigDecimal(0);
		r[6] = "000H";
		r[7] = "N";
		r[8] = "";
		r[9] = "Y";
		r[10] = "Y";
		r[11] = "Y";
		r[12] = "";
		r[13] = "";
		data.add(r);
		return data;
	}
}