package com.onedrive;

public class TokenAndPath {

	
     private String token;
	
	private String path;
	
	private String driveId;
	
	private String userName;

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDriveId() {
		return driveId;
	}

	public void setDriveId(String driveId) {
		this.driveId = driveId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TokenAndPath [token=" + token + ", path=" + path + ", driveId=" + driveId + ", userName=" + userName
				+ "]";
	}
}
