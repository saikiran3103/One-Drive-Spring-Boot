package com.onedrive;

import java.io.Serializable;

public class ParentRefernceForSharedItem implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4830266767248978011L;
	private String driveId;

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
		return "ParentRefernceForSharedItem [driveId=" + driveId + "]";
	}

}
