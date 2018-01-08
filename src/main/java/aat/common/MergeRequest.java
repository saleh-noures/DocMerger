package aat.common;

import java.util.List;

public class MergeRequest {

	boolean isFooter;
	boolean isIndex;
	List<String> filesNames;
	
	public boolean isFooter() {
		return isFooter;
	}
	public void setFooter(boolean isFooter) {
		this.isFooter = isFooter;
	}
	public boolean isIndex() {
		return isIndex;
	}
	public void setIndex(boolean isIndex) {
		this.isIndex = isIndex;
	}
	public List getFilesNames() {
		return filesNames;
	}
	public void setFilesNames(List filesNames) {
		this.filesNames = filesNames;
	}

	
}
