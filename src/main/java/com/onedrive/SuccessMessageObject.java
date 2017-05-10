package com.onedrive;

import java.io.Serializable;

public class SuccessMessageObject implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7202872296316063712L;
	private String message;
	
	
	private String response;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SuccessMessageObject [message=" + message + ", response=" + response + "]";
	}

}
