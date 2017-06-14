package com.onedrive;

import com.google.gson.annotations.SerializedName;

public class Item {

	
	@SerializedName("@microsoft.graph.conflictBehavior")
    private String microsoft_graph_conflictBehavior;

	/**
	 * @return the microsoft_graph_conflictBehavior
	 */
	public String getMicrosoft_graph_conflictBehavior() {
		return microsoft_graph_conflictBehavior;
	}

	/**
	 * @param microsoft_graph_conflictBehavior the microsoft_graph_conflictBehavior to set
	 */
	public void setMicrosoft_graph_conflictBehavior(String microsoft_graph_conflictBehavior) {
		this.microsoft_graph_conflictBehavior = microsoft_graph_conflictBehavior;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Item [microsoft_graph_conflictBehavior=" + microsoft_graph_conflictBehavior + "]";
	}
}
