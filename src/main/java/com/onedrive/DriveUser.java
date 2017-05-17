package com.onedrive;

import java.io.Serializable;

public class DriveUser implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6774171335309190914L;
	
	
	
	private String driveId;



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DriveUser [driveId=" + driveId + "]";
	}



	public String getDriveId() {
		return driveId;
	}



	public void setDriveId(String driveId) {
		this.driveId = driveId;
	}

}
