package org.snorreware.models;

import java.util.Collection;

import com.snorreware.models.impl.TermPostingsList;

public interface TermPostingsListIntf extends Comparable<TermPostingsList> {
	
	public int getTermId();
	public void setTermId(int termId);
	public Collection<Integer> getPostingsList();
	public void setPostingsList(Collection<Integer> postingsList);
	

}
