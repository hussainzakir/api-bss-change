package com.trinet.ambis.exception;

public class BSSApplicationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BSSApplicationError bssError;

	public BSSApplicationException() {
		super();
	}
	
	public BSSApplicationException(String message) {
		super(message);
	}

	public BSSApplicationException(BSSApplicationError bssError) {
		super(bssError.getCustomMessage());
		this.bssError = bssError;
	}

	public BSSApplicationException(Throwable cause, BSSApplicationError bssError) {
		super(bssError.getCustomMessage(), cause);
		this.setBssError(bssError);
	}

	/**
	 * @return the bssError
	 */
	public BSSApplicationError getBssError() {
		return bssError;
	}

	/**
	 * @param bssError
	 *            the bssError to set
	 */
	public void setBssError(BSSApplicationError bssError) {
		this.bssError = bssError;
	}

}