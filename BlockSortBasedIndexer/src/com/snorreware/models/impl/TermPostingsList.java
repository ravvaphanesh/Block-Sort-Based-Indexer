package com.snorreware.models.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import org.snorreware.models.TermPostingsListIntf;

public class TermPostingsList implements TermPostingsListIntf {
	
	private int termId;
	private TreeSet<Integer> postingsList;
	
	/**
	 * 
	 * @param termPostingsList the object array storing the term postings list values
	 */
	public TermPostingsList(int termId, Collection<Integer> postingsListCollection) {
		
			this.termId = termId;
			this.postingsList = new TreeSet<Integer>();
			this.postingsList.addAll(postingsListCollection);
	}

	/**
	 * Compares a term and postings list-object based on
	 * the objects term id. A lower term id corresponds to 
	 * a "smaller" object and so forth. 
	 */
	@Override
	public int compareTo(TermPostingsList o) {
		if(termId > o.termId) {
			return 1;
		} else if(termId < o.termId) {
			return -1;
		} else {
			return 0;	
		}
	}
	
	@Override
	public int getTermId() {
		// TODO Auto-generated method stub
		return termId;
	}

	@Override
	public void setTermId(int termId) {
		this.termId = termId;
	}

	@Override
	public Collection<Integer> getPostingsList() {
		
		return postingsList;
	}

	@Override
	public void setPostingsList(Collection<Integer> postingsList) {
		
		this.postingsList.addAll(postingsList);
	}

	@Override
	public void insertPosting(int posting) {
		
		postingsList.add(posting);
	}
	
	


	
	
}
