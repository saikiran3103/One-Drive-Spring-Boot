package com.onedrive;

import java.io.Serializable;

public class ErrorMessage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7687509918835761850L;
	private Error error;

	/**
	 * @return the error
	 */
	public Error getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(Error error) {
		this.error = error;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ErrorMessage [error=" + error + "]";
	}

}
