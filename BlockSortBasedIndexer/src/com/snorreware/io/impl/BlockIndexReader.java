package com.snorreware.io.impl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.snorreware.BSBI;

import com.snorreware.io.BlockIndexReaderIntf;
import com.snorreware.models.impl.TermPostingsList;


public class BlockIndexReader implements BlockIndexReaderIntf {

	private final BlockingQueue<TermPostingsList> mergeList;
	private final File file;
	private BufferedReader reader;
	private final BSBI bsbi;

	public BlockIndexReader(BSBI bsbi, BlockingQueue<TermPostingsList> list, File file) throws IOException {
		this.bsbi = bsbi;
		mergeList = list;
		this.file = file;
		System.out.println(file.getName());
		reader = new BufferedReader(new FileReader(this.file)); // Read block
	}

	@Override
	public void run() {
		System.out.println("Indexer starts to read...");
		try {
			String line;
			while((line = reader.readLine()) != null) {
				TermPostingsList termPostingsList = generateTermPostingsList(line);
				// Attempt to insert term and postings list into the merge list. 
				// Wait a second before timing out. We wouldn't want that to happen.
				int timeoutInMilliSeconds = 500;
				boolean termPostingsListInserted = false;
				while(!termPostingsListInserted) {

					try {
						mergeList.offer(termPostingsList, timeoutInMilliSeconds, TimeUnit.MILLISECONDS);
						termPostingsListInserted = true;
						//System.out.println("Inserted one item into queue");
					}
					catch (InterruptedException e) {
						// The queue likely timed out from having to wait for so long....
						e.printStackTrace();
						System.err.println("Queue timed out (waited " + timeoutInMilliSeconds + " milliseconds), stack full for too long!");
						System.err.println("Try again....");
					}
				}
			}
			bsbi.incrementBlockFinishedCounter();
			reader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("BlockIndexReader could not read " + file.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("BlockIndexReader could not read from " + file.getName());
		} 
	}

	/**
	 * A helper method that takes as argument a String
	 * that contains a term and postings list (separated by
	 * a semicolon, each posting separated by a comma), and
	 * returns a TermPostingsList-object. 
	 * @param line
	 * @return
	 */
	private TermPostingsList generateTermPostingsList(String line) {

		String[] splitSemiColon = line.split(";");

		int termId = Integer.parseInt(splitSemiColon[0]);
		String postingsString = splitSemiColon[1];
		String[] postingsArray = postingsString.split(",");
		ArrayList<Integer> postingsList = new ArrayList<Integer>(postingsArray.length/2);
		for(String posting : postingsArray) {
			postingsList.add(Integer.parseInt(posting));
		}

		return new TermPostingsList(termId, postingsList);
	}
}
