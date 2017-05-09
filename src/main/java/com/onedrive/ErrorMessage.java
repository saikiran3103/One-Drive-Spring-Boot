package com.onedrive;

public class ErrorMessage {
	
	private ErrorObject error;

	/**
	 * @return the error
	 */
	public ErrorObject getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(ErrorObject error) {
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
