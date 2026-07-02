package com.trinet.ambis.service.model;

/**
 * @author schaudhari
 *
 *	This class is used as a true/false API response.
 */
public class Response {

	boolean result;

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public Response(boolean result) {
		super();
		this.result = result;
	}

}
