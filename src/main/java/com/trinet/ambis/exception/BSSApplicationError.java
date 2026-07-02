/**
 * 
 */
package com.trinet.ambis.exception;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author rvutukuri
 *
 */
public class BSSApplicationError {

	private String code;
	private int status;
	private String timestamp;
	private String source;
	private String url;
	private String emplId;
	private String userRole;
	private String message;
	private String customMessage;
	private String exceptionStackTrace;
	private String query;
	private Map<String, Object> queryParams;
	private String companyCode;

	public BSSApplicationError(String customMessage) {
		this.setCustomMessage(customMessage);
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss z");
		this.setTimestamp(dateFormat.format(date));
	}

	public BSSApplicationError(String code, int status, String source, String customMessage, String query,
			Map<String, Object> queryParams) {
		this.setStatus(status);
		this.setCustomMessage(customMessage);
		this.setSource(source);
		this.setQuery(query);
		this.setCode(code);
		this.setQueryParams(queryParams);
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss z");
		this.setTimestamp(dateFormat.format(date));
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the emplId
	 */
	public String getEmplId() {
		return emplId;
	}

	/**
	 * @param emplId
	 *            the emplId to set
	 */
	public void setEmplId(String emplId) {
		this.emplId = emplId;
	}

	/**
	 * @return the userRole
	 */
	public String getUserRole() {
		return userRole;
	}

	/**
	 * @param userRole
	 *            the userRole to set
	 */
	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the customMessage
	 */
	public String getCustomMessage() {
		return customMessage;
	}

	/**
	 * @param customMessage
	 *            the customMessage to set
	 */
	public void setCustomMessage(String customMessage) {
		this.customMessage = customMessage;
	}

	/**
	 * @return the exceptionStackTrace
	 */
	public String getExceptionStackTrace() {
		return exceptionStackTrace;
	}

	/**
	 * @param exceptionStackTrace
	 *            the exceptionStackTrace to set
	 */
	public void setExceptionStackTrace(String exceptionStackTrace) {
		this.exceptionStackTrace = exceptionStackTrace;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @return the queryParams
	 */
	public Map<String, Object> getQueryParams() {
		return queryParams;
	}

	/**
	 * @param queryParams
	 *            the queryParams to set
	 */
	public void setQueryParams(Map<String, Object> queryParams) {
		this.queryParams = queryParams;
	}

	/**
	 * @return the companyCode
	 */
	public String getCompanyCode() {
		return companyCode;
	}

	/**
	 * @param companyCode the companyCode to set
	 */
	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

}
