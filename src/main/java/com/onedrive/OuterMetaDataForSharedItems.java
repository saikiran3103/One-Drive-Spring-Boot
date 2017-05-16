package com.onedrive;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class OuterMetaDataForSharedItems implements Serializable {

	
	
	

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7771914937150136321L;

	@SerializedName("@odata.context")
    private String odata_context;
	
	private List<MetaDataForSharedItem> value;

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
	 * @return the value
	 */
	public List<MetaDataForSharedItem> getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(List<MetaDataForSharedItem> value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OuterMetaDataForSharedItems [odata_context=" + odata_context + ", value=" + value + "]";
	}

}
