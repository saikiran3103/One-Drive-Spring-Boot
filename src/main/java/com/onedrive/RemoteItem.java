package com.onedrive;

import java.io.Serializable;
import java.util.HashMap;

public class RemoteItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7045969327998516526L;
	
	private ParentReference parentReference;

	
	
	private HashMap<String, User> createdBy = new HashMap<>();
	
	
	public ParentReference getParentReference() {
		return parentReference;
	}

	public void setParentReference(ParentReference parentReference) {
		this.parentReference = parentReference;
	}
	
	public HashMap<String, User> getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(HashMap<String, User> createdBy) {
		this.createdBy = createdBy;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RemoteItem [parentReference=" + parentReference + ", createdBy=" + createdBy + "]";
	}
}
