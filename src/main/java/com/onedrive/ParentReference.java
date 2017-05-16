package com.onedrive;

public class ParentReference {

	
	private String driveId;
    private String id;
    private String path;

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public String getId() {
        return id;
    }
   
    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ParentReference [driveId=" + driveId + ", id=" + id + ", path=" + path + "]";
	}
}
