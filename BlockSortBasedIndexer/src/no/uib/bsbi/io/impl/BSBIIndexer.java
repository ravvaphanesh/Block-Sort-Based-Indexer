package no.uib.bsbi.io.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.uib.bsbi.BSBI;
import no.uib.bsbi.io.IndexerIntf;
import no.uib.bsbi.io.TermListWriterIntf;
import no.uib.bsbi.models.impl.TermPostingsList;


public class BSBIIndexer implements IndexerIntf {

	private BlockingQueue<String[]> normalizedQueue;
	private HashMap<String, Integer> termList;
	private HashMap<String, Integer> documentList;
	private HashMap<Integer, TermPostingsList> termPostingsLists;
	private int blockNumber;
	private BSBI bsbi;
	private boolean finished;


	public BSBIIndexer(BlockingQueue<String[]> normalizedQueue,
			HashMap<String, Integer> termList,
			HashMap<String, Integer> documentList,
			int numberOfDocuments,
			int blockNumber, BSBI bsbi) {
		this.normalizedQueue = normalizedQueue;
		this.termList = termList;
		this.documentList = documentList;
		termPostingsLists = new HashMap<Integer, TermPostingsList>();
		this.blockNumber = blockNumber;
		this.bsbi = bsbi;
		finished = false;

	}


	@Override
	public void run() {

		int timeoutInMillis = 1000;
		int documentId = documentList.size();
		int termId = termList.size();
		boolean indexingFinished = false;

		while(!indexingFinished) {

			try {
				String[] document = normalizedQueue.poll(timeoutInMillis, TimeUnit.MILLISECONDS);
				if(document != null) {
					String documentTitle = document[0];
					String documentBody = document[1];

					documentList.put(documentTitle, documentId);


					String[] terms = documentBody.split(" ");

					// Add new terms to term list
					for(String term : terms) {
						int id;
						if(!termList.containsKey(term)) {
							termList.put(term, termId);
							id = termId;
							termId++;
						} else {
							id = termList.get(term);
						}
						
						
						
						if(!termPostingsLists.containsKey(id)) {
							TermPostingsList tpl = new TermPostingsList(id, new ArrayList<Integer>());
							tpl.insertPosting(documentId);
							termPostingsLists.put(id, tpl);
						} else {
							termPostingsLists.get(id).insertPosting(documentId);
						}
						
					}

					documentId ++;
				} else {
					throw new InterruptedException();
				}


			} catch (InterruptedException e) {
				if(bsbi.getNormalizerFinished()) {
					System.out.println("Normalizer is finished...");
					indexingFinished = true;
				} else {
					//System.out.println("Hmm. Seems the normalizer is slow today..");
					//System.out.println("Try again...");
				}

			}
		}

		System.out.println("Indexer says term postings lists size is: " + termPostingsLists.size());
		TermListWriterIntf writer = new TextFileTermListWriter(termPostingsLists, blockNumber);
		writer.write();
		finished = true;
	}

	public boolean finished() {
		return finished;
	}


}
