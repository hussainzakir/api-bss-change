package com.trinet.ambis.persistence.dao.ps.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.ps.EligConfigDao;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.DaoUtils;
import com.trinet.ambis.util.Utils;

public class EligConfigDaoImpl implements EligConfigDao {
	public static final String GET_ELIGCNFG_COMPONENT = "GET_ELIGCNFG_COMPONENT";
	public static final String INSERT_ELIGCFG_EFDT = "INSERT_ELIGCFG_EFDT";
	public static final String INSERT_ELIGCNFG_TBL = "INSERT_ELIGCNFG_TBL";
	
	@Override
	public int putEligConfigRow( String pfClient, String effdtStr, String eligConfig1, String effStatus, String descr, EntityManager em ) {

		java.util.Date effdt = Utils.convertStringToDate( effdtStr, Constants.DATE_FORMAT );
		EligCnfgComponent comp = new EligCnfgComponent();
		int count = 0;

		Query query = em.createNamedQuery( GET_ELIGCNFG_COMPONENT );
		query.setParameter( BSSQueryConstants.PF_CLIENT, pfClient );
		query.setParameter( "effdt", effdt );
		List<Object[]> list = DaoUtils.getResultList(query, GET_ELIGCNFG_COMPONENT );

		if( list.isEmpty() ) {
			comp.details = new HashMap<>();
			comp.pfClient = pfClient;
			comp.effdt = effdt;
		} else {
			for( Object[] row : list ) {
				if( row != null && row.length > 0 ) {
					if( comp.details == null ) {
						comp.details = new HashMap<>();
						comp.pfClient = (String) row[0];
						comp.effdt = (java.util.Date) row[1];
					}
					EligCnfgRow dtl = new EligCnfgRow();
					dtl.eligConfig1 = (String) row[2];
					dtl.effStatus = (String) row[3];
					dtl.descr = (String) row[4];
					comp.details.put( dtl.eligConfig1, dtl );
				}
			}
		}

		// if the date has changed, the whole component must be updated in the database
		if( effdt.equals( comp.effdt ) ) {
			// nothing
		} else {
			comp.effdt = effdt;
			comp.changed = true;

			// first, detach keySet from map, so the map can be updated without affecting this detached array
			String[] keys = comp.details.keySet().toArray( new String[0] );
			// remove inactivated rows; they should not be carried forward to the new effective date
			for( String ecfg1 : keys ) {
				if( comp.details.get( ecfg1 ).effStatus.equals( "I" ) ) {
					comp.details.remove( ecfg1 );
				}
			}
		}
		
		// find the row to be updated and make the updates; create a new row if needed
		EligCnfgRow tempRow = comp.details.get( eligConfig1 );
		if( tempRow == null ) {
			tempRow = new EligCnfgRow();
			tempRow.eligConfig1 = eligConfig1;
			tempRow.effStatus = effStatus;
			if( descr != null && descr.length() > 0 )
				tempRow.descr = descr;
			else
				tempRow.descr = " ";
			tempRow.changed = true;

			// If this is the first record that will be added to the component, set the component changed flag
			if( comp.details.size() == 0 ) {
				comp.changed = true;
			}
			comp.details.put( eligConfig1, tempRow );
		} else {
			tempRow.changed = true;
			if( effStatus != null && effStatus.length() > 0 )  tempRow.effStatus = effStatus;
			if( descr != null && descr.length() > 0 )  tempRow.descr = descr;
		}
		
		// if the component is marked as "changed" then set all the details to "changed" as well
		if( comp.changed ) {
			for( EligCnfgRow r : comp.details.values() ) {
				r.changed = true;
			}
			
			// call the SQL statement to insert/merge the header row
			Query insertHdr = em.createNamedQuery( INSERT_ELIGCFG_EFDT );
			insertHdr.setParameter( BSSQueryConstants.PF_CLIENT, comp.pfClient );
			insertHdr.setParameter( "effdt", comp.effdt );
			count += DaoUtils.executeUpdate(insertHdr, INSERT_ELIGCFG_EFDT );
		}

		
		// for the details marked as "changed" insert/merge the changes
		for( EligCnfgRow r : comp.details.values() ) {
			if( r.changed ) {
				Query insertDtl = em.createNamedQuery( INSERT_ELIGCNFG_TBL );
				insertDtl.setParameter( BSSQueryConstants.PF_CLIENT, comp.pfClient );
				insertDtl.setParameter( "effdt", comp.effdt );
				insertDtl.setParameter( BSSQueryConstants.ELIG_CONFIG_1, r.eligConfig1 );
				insertDtl.setParameter( "effStatus", r.effStatus );
				insertDtl.setParameter( "descr", r.descr );
				count += DaoUtils.executeUpdate(insertDtl, INSERT_ELIGCNFG_TBL );
			}
		}
		
		return count;
	}

	class EligCnfgRow {
		String eligConfig1;
		String effStatus;
		String descr;
		boolean changed = false;
	}
	
	class EligCnfgComponent {
		String pfClient;
		java.util.Date effdt;
		Map<String,EligCnfgRow> details;
		boolean changed = false;
	}
}
