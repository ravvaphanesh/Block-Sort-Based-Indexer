package no.uib.bsbi.io.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.uib.bsbi.BSBI;
import no.uib.bsbi.io.MergeIndexWriterIntf;
import no.uib.bsbi.models.impl.TermPostingsList;


public class MergeIndexWriter implements MergeIndexWriterIntf {

	private final BlockingQueue<TermPostingsList> mergeList;
	private final File file;
	private PrintWriter writer;
	private final BSBI bsbi;

	public MergeIndexWriter(BSBI bsbi, BlockingQueue<TermPostingsList> list, File file) throws IOException {
		this.bsbi = bsbi;
		mergeList = list;
		initFile(file);
		this.file = file;
		writer = new PrintWriter(new BufferedWriter(new FileWriter(this.file)));
	}

	@Override
	public void run() {
		
		System.out.println("MergeIndexWriter starts to write...");
		ArrayList<TermPostingsList> termPostingsListsToMerge = new ArrayList<TermPostingsList>();
		generateTermPostingsList(termPostingsListsToMerge);
		System.out.println("All producers finished, nothing more to write");
	}

	private void generateTermPostingsList(ArrayList<TermPostingsList> termPostingsListsToMerge) {

		boolean readersFinished = false;
		while(!readersFinished) {

			try {
				TermPostingsList termPostingsList;
				termPostingsList = mergeList.poll(1, TimeUnit.SECONDS);
				if(termPostingsList != null) {
					//System.out.println("Consume an item from the queue!");
					if(!termPostingsListsToMerge.isEmpty()) {
						TermPostingsList previousTPL = termPostingsListsToMerge.get(termPostingsListsToMerge.size()-1);
						if(termPostingsList.getTermId() != previousTPL.getTermId()) {
							TermPostingsList tpl = mergeTermPostingsLists(termPostingsListsToMerge);
							writeTermPostingsList(tpl);
							termPostingsListsToMerge.clear();
							termPostingsListsToMerge.add(termPostingsList);
							System.out.println(tpl.getTermId());
						} else {
							termPostingsListsToMerge.add(termPostingsList);
						}
					} else {
						termPostingsListsToMerge.add(termPostingsList);
					}

				} else {
					throw new InterruptedException();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();

				System.out.println("This takes to long, are the readers finished?");
				if(bsbi.getBlocksFinishedCounter()) {
					System.out.println("Yes they are. Pack your bags and finish up...");
					writer.close();
					readersFinished = true;
				} else {
					System.out.println("Nope. Maybe they are a bit slow today. Try to rest for to seconds");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}

	private TermPostingsList mergeTermPostingsLists(ArrayList<TermPostingsList> termPostingsLists) {

		int termId = termPostingsLists.get(0).getTermId();

		PriorityQueue<Integer> docIds = new PriorityQueue<Integer>();
		for(TermPostingsList tpl : termPostingsLists) {
			for(int docId : tpl.getPostingsList()) {
				docIds.add(docId);
			}
		}

		// Add to ArrayList manually as collections.addall does not keep order...
		ArrayList<Integer> postingsList = new ArrayList<Integer>(docIds.size());
		while (!docIds.isEmpty()) {
			postingsList.add(docIds.remove());
		}

		return new TermPostingsList(termId, postingsList);
	}

	private void writeTermPostingsList(TermPostingsList termPostingsList) {

		ArrayList<Integer> postingsList = new ArrayList<Integer>(termPostingsList.getPostingsList());
		String line ="";
		line += termPostingsList.getTermId() + ";";

		for(int docId : postingsList) {
			line += docId + ",";
		}

		writer.println(line);
	}

	private void initFile(File file) throws IOException {

		File parentDirectory = new File(file.getParent());

		if(!file.exists()) {
			file.createNewFile();
		} else if(file.exists()) {
			file.delete();
			file.createNewFile();
		} else if(!parentDirectory.exists()) { // No parent directory
			parentDirectory.mkdir();
			file.createNewFile();
		}

	}




}
