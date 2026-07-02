package com.trinet.ambis.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Parameter;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaoUtils {
	private static final Logger logger = LoggerFactory.getLogger( DaoUtils.class );

	private DaoUtils() {
		throw new IllegalStateException(
				"Utility class " + DaoUtils.class.getName() + " can not be instantiated.");
	}
	
	/**
	 * This public method will run the SQL for any statement that alters the database.
	 * @param sql the Query object that represents an INSERT/UPDATE/DELETE action
	 * @param sqlStatementName the "named query" name String
	 * @return the number of rows inserted/updated/deleted
	 */
	public static int executeUpdate( Query sql, String sqlStatementName ) {
		int rowCount = 0;

		DaoUtils.displayParameters( sql, sqlStatementName );
		Map<String, Object> parameterMap = DaoUtils.generateQueryMap(sql);

		// execute the query and parse the result as a List
		try {
			rowCount = sql.executeUpdate();
		} catch( Exception e ) {
			DaoUtils.logErrorParameters( parameterMap, sqlStatementName );
			throw e;
		}
		return rowCount;
	}
	
	/**
	 * This public method will run the SQL for any statement that gets a result list
	 * @param sql the Query object that represents an INSERT/UPDATE/DELETE action
	 * @param sqlStatementName the "named query" name String
	 * @return the result list as List<Object[]> 
	 */
	@SuppressWarnings("unchecked")
	public static List<Object[]> getResultList( Query sql, String sqlStatementName ) {
		List<Object[]> results;

		Map<String, Object> parameterMap = DaoUtils.generateQueryMap(sql);
		DaoUtils.displayParameters( sql, sqlStatementName );

		// execute the query and parse the result as a List
		try {
			results = sql.getResultList();
		} catch( Exception e ) {
			DaoUtils.logErrorParameters( parameterMap, sqlStatementName );
			throw e;
		}
		return results;
	}
	
	/**
	 * This public method will run the SQL for any statement that gets a result list as strings
	 * @param sql the Query object that represents an INSERT/UPDATE/DELETE action
	 * @param sqlStatementName the "named query" name String
	 * @return the result list as List<String> 
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getResultStringList( Query sql, String sqlStatementName ) {
		List<String> results;

		Map<String, Object> parameterMap = DaoUtils.generateQueryMap(sql);
		DaoUtils.displayParameters( sql, sqlStatementName );

		// execute the query and parse the result as a List
		try {
			results = sql.getResultList();
		} catch( Exception e ) {
			DaoUtils.logErrorParameters( parameterMap, sqlStatementName );
			throw e;
		}
		return results;
	}	
	
	/**
	 * This public method will run the SQL for any statement that gets a single object
	 * @param sql the Query object that represents an INSERT/UPDATE/DELETE action
	 * @param sqlStatementName the "named query" name String
	 * @return the result as an object
	 */
	public static Object getSingleResult( Query sql, String sqlStatementName ) {
		Object result;

		Map<String, Object> parameterMap = DaoUtils.generateQueryMap(sql);
		DaoUtils.displayParameters( sql, sqlStatementName );

		// execute the query and parse the result as a List
		try {
			result = sql.getSingleResult();
		} catch( Exception e ) {
			DaoUtils.logErrorParameters( parameterMap, sqlStatementName );
			throw e;
		}
		return result;
	}

	/**
	 * When working with a javax.persistence.Query object, this method can be called
	 * to write named parameters and their values to the log to aid debugging.  This can
	 * be called with any SELECT/UPDATE/INSERT/DELETE query object. 
	 * <p>
	 * This method only writes to the log when the logger level is INFO.  If INFO is not
	 * enabled, this method exits immediately.
	 * @param sql the Query object that contains named parameters
	 * @param sqlStatementName the query name (if the Query object was created using
	 * createNamedQuery), or any other name you wish to display in the log. 
	 */
	public static void displayParameters( Query sql, String sqlStatementName ) {
		// display parameters if logger is enabled
		if( logger.isInfoEnabled() ) {
			logger.info( "SQL statement is {}", sqlStatementName );
			for( Parameter<?> p : sql.getParameters() ) {
				logger.info( "{}:{}", p.getName(), sql.getParameterValue( p.getName() ) );
			}
		}
	}


	/**
	 * When handling an exception on a javax.persistence.Query object, this method can be called
	 * to build the query map for use with the exception/error object.
	 * 
	 * @param sql
	 * @return a map of query named parameters and their values for use with BSS error handling
	 */
	public static Map<String, Object> generateQueryMap(Query sql) {
		Map<String, Object> queryMap = new HashMap<>();
		for (Parameter<?> p : sql.getParameters()) {
			queryMap.put(p.getName(), sql.getParameterValue(p.getName()));
		}
		return queryMap;
	}

	/**
	 * When handling exceptions with a javax.persistence.Query object, this method can be called
	 * to write named parameters and their values to the log to aid debugging.  This can
	 * be called with a map of parameter names/values. 
	 * <p>
	 * This method only writes to the log when the logger level is ERROR.  If ERROR is not
	 * enabled, this method exits immediately.
	 * @param queryMap the Map of String/Object that contains the parameter name/value pairs
	 * @param sqlStatementName the query name (if the Query object was created using
	 * createNamedQuery), or any other name you wish to display in the log. 
	 */
	public static void logErrorParameters( Map<String,Object> sqlParameters, String sqlStatementName ) {
		// display parameters if logger is enabled
		if( logger.isErrorEnabled() ) {
			logger.error( "SQL statement is {}", sqlStatementName );
			for( Entry<String, Object> p : sqlParameters.entrySet() ) {
				logger.error( "{}:{}", p.getKey(), p.getValue() ) ;
			}
		}
	}

}