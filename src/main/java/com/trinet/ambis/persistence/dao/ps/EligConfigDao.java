package com.trinet.ambis.persistence.dao.ps;

import javax.persistence.EntityManager;

/**
 * A data access interface for getting and putting data to the ELIG_CONFIG1 lookup table
 * @author mbrothers
 *
 */
public interface EligConfigDao {

	/**
	 * This method can be called to add a single row of detail to the ELIGCNFG component.  If a new effective-dated
	 * instance is required, all Active rows will be copied from the prior effective date to the new date and the
	 * database will be updated with this new instance.
	 * 
	 * @param pfClient
	 * @param effdtStr
	 * @param eligConfig1
	 * @param effStatus
	 * @param descr
	 * @param em an active EntityManager instance
	 * @return number of rows inserted/merged
	 */
	public int putEligConfigRow( String pfClient, String effdtStr, String eligConfig1, String effStatus, String descr, EntityManager em );
}
