package com.onedrive;

import java.io.Serializable;

public class SuccessMessageObject implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7202872296316063712L;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SuccessMessageObject [message=" + message + "]";
	}

}
