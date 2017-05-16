package com.onedrive;

import java.io.Serializable;

public class Error implements Serializable {
	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 7687509918835761850L;
	
	
	private ErrorInner error;

	/**
	 * @return the error
	 */
	public ErrorInner getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(ErrorInner error) {
		this.error = error;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "s [error=" + error + "]";
	}

}
