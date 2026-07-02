package com.trinet.ambis.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.util.DaoUtils;

/**
 * When client-specific HSA plans are created from clone HSA plans, we want to keep track of
 * the relationship between the original and the specific.  The reason is resubmits.  We 
 * don't want to create hundreds of new plans every time the client resubmits if we can
 * just remember that we already created the client-specific plans the first time.
 * <p>
 * An instance of this class creates the data for a single company and can write the data
 * back when the calling class is done with it.
 * @author mbrothers
 */
public class HSAPlanMapping {

	private static final String NEW_PLAN = "newPlan";
	private static final String CLONE_PLAN = "clonePlan";
	private static final String PARAM_COMPANY = "company";
	
	private static final String HSA_PLAN_MAP = "HSA_PLAN_MAP";
	private static final String MERGE_HSA_PLAN = "MERGE_HSA_PLAN";


	private String company;
	private EntityManager bssEm;
	private Map<String,String> hsaPlanMap;


	public HSAPlanMapping( String company ) {
		this.company = company;
	}


	public void setEntityManager( EntityManager em ) {
		this.bssEm = em;
		if( this.hsaPlanMap == null ) {
			// go query database and build initial map
			this.createMap();
		}
	}


	/**
	 * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
	 * @param clonePlan the clone BENEFIT_PLAN key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
	 */
	public String get( String clonePlan ) {
		return this.hsaPlanMap.get( clonePlan );
	}

	/**
	 * Associates the specified value with the specified key in this map (optional operation). If the map 
	 * previously contained a mapping for the key, the old value is replaced by the specified value.
	 * @param clonePlan key with which the specified value is to be associated
	 * @param hsaPlan value to be associated with the specified key
	 * @return the previous value associated with key, or null if there was no mapping for key.
	 */
	public String put( String clonePlan, String hsaPlan ) {
		return this.hsaPlanMap.put( clonePlan, hsaPlan );
	}

	/**
	 * Save all mappings back to the BSS database
	 * @return
	 */
	public int saveAll() {
		int rowCount = 0;

		this.bssEm.getTransaction().begin();
		
		String sqlName = MERGE_HSA_PLAN;
		Query query = this.bssEm.createNamedQuery( sqlName );

		for( Map.Entry<String,String> entry : this.hsaPlanMap.entrySet() ) {
			// update this optn row
			query.setParameter( PARAM_COMPANY, this.company );
			query.setParameter( CLONE_PLAN, entry.getKey() );
			query.setParameter( NEW_PLAN, entry.getValue() );

			rowCount += this.executeUpdate( query, sqlName );
		}
		this.bssEm.getTransaction().commit();

		return rowCount;
	}


	/**
	 * Part of the instance construction, this method initializes the map with any 
	 * pre-existing mappings for this company
	 */
	private void createMap() {
		this.hsaPlanMap = new HashMap<>();

		String sqlName = HSA_PLAN_MAP;
		// select all HSA plans for the benefit program
		Query query = this.bssEm.createNamedQuery( sqlName );
		query.setParameter( PARAM_COMPANY, this.company );

		Map<String, Object> queryMap = null;
		try {
			queryMap = DaoUtils.generateQueryMap(query);
			List<?> results = query.getResultList();

			for (Object o : results) {
				Object[] row = (Object[]) o;
				this.put((String) row[0], (String) row[1]);
			}
		} catch (Exception e) {
			this.generateException(e, sqlName, queryMap);
		}
	}


	/**
	 * This private method will run the SQL for any statement that alters the database.
	 * @param sql the Query object that represents an INSERT/UPDATE/DELETE action
	 * @param sqlStatementName the "named query" name String
	 * @return the number of rows inserted/updated/deleted
	 */
	private int executeUpdate(Query sql, String sqlStatementName) {
		int rowCount = 0;
		Map<String, Object> queryMap = null;
		// execute the query and parse the result as a List
		try {
			queryMap = DaoUtils.generateQueryMap(sql);
			rowCount = sql.executeUpdate();
		} catch (Exception e) {
			this.generateException(e, sqlStatementName, queryMap);
		}
		return rowCount;
	}

	/**
	 * This private method will generate the standard exception for this class
	 * @param e  the Exception object caught by a method of this class
	 * @param sqlStatementName
	 * @param queryMap
	 */
	private void generateException(Exception e, String sqlStatementName, Map<String, Object> queryMap) {
		BSSApplicationError errorData = new BSSApplicationError(sqlStatementName, BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
				this.getClass().getName(), "Failure executing SQL for HSA plan setup.", sqlStatementName, queryMap);
		throw new BSSApplicationException(e, errorData);
	}
}