package no.uib.bsbi.models;


import java.util.Collection;

import no.uib.bsbi.models.impl.TermPostingsList;

public interface TermPostingsListIntf extends Comparable<TermPostingsList> {
	
	public int getTermId();
	public void setTermId(int termId);
	public Collection<Integer> getPostingsList();
	public void setPostingsList(Collection<Integer> postingsList);
	public void insertPosting(int posting);
	

}
