package com.onedrive;

public class ErrorObject {
	
	
	private String code;
	
	
	private String message;
	
	
	private InnerError innerError;


	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}


	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}


	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}


	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}


	/**
	 * @return the innerError
	 */
	public InnerError getInnerError() {
		return innerError;
	}


	/**
	 * @param innerError the innerError to set
	 */
	public void setInnerError(InnerError innerError) {
		this.innerError = innerError;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ErrorObject [code=" + code + ", message=" + message + ", innerError=" + innerError + "]";
	}

}
