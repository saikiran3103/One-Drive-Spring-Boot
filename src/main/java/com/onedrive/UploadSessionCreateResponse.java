package com.onedrive;

import com.google.gson.annotations.SerializedName;

public class UploadSessionCreateResponse {
	
	
	
	@SerializedName("@odata.context")
    private String odata_context;
	
	private String expirationDateTime;
	
	private String uploadUrl;

	/**
	 * @return the odata_context
	 */
	public String getOdata_context() {
		return odata_context;
	}

	/**
	 * @param odata_context the odata_context to set
	 */
	public void setOdata_context(String odata_context) {
		this.odata_context = odata_context;
	}

	/**
	 * @return the expirationDateTime
	 */
	public String getExpirationDateTime() {
		return expirationDateTime;
	}

	/**
	 * @param expirationDateTime the expirationDateTime to set
	 */
	public void setExpirationDateTime(String expirationDateTime) {
		this.expirationDateTime = expirationDateTime;
	}

	/**
	 * @return the uploadUrl
	 */
	public String getUploadUrl() {
		return uploadUrl;
	}

	/**
	 * @param uploadUrl the uploadUrl to set
	 */
	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UploadSessionCreateResponse [odata_context=" + odata_context + ", expirationDateTime="
				+ expirationDateTime + ", uploadUrl=" + uploadUrl + "]";
	}

}
