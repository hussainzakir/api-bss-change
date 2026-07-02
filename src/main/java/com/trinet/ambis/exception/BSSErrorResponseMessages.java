/**
 * 
 */
package com.trinet.ambis.exception;

/**
 * @author hliddle
 *
 */
public class BSSErrorResponseMessages {

	private BSSErrorResponseMessages() {
		throw new IllegalStateException(
				"Utility class " + BSSErrorResponseMessages.class.getName() + " can not be instantiated.");
	}
	
	// Error response messages
	public static final String EXCEPTION_MSG_PREFIX = "CUSTOM MESSAGE:";

	// Model Compare
	public static final String MSG_MC_EMPLOYEE_ERROR = "We are having an issue retrieving worksite employee information.";
	public static final String MSG_MC_GENERAL_ERROR = "We are having an issue retrieving the necessary information.";

}
