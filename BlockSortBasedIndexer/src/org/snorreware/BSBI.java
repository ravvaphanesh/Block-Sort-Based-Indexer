package org.snorreware;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

import com.snorreware.io.impl.BlockIndexReader;
import com.snorreware.io.impl.MergeIndexWriter;
import com.snorreware.models.impl.TermPostingsList;

public class BSBI {
	
	private int blockFinishedCounter;
	private int noBlocks;

	public BSBI() {
		
		PriorityBlockingQueue<TermPostingsList> mergeQueue = new PriorityBlockingQueue<TermPostingsList>();
		
		// Directory and file to store indexes in...
		File invertedIndexFile = new File("index" + File.separator + "invertedindex.txt");
		File blocksDirectory = new File("blocks");
		
		blockFinishedCounter = 0;
		noBlocks = 0;

		Thread consumer;
		try {
			consumer = new Thread(
					new MergeIndexWriter(this, mergeQueue, invertedIndexFile)
					);
			
			for(File block : blocksDirectory.listFiles()) {

				Thread producer = new Thread(new BlockIndexReader(this, mergeQueue, block));
				producer.start();
				noBlocks++;
			}
			
			consumer.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("The merge writer or one of the block readers" +
					" encountered an IO problem. Shutting down indexer...");
			System.exit(1);
		}
	}
	
	
	public synchronized void incrementBlockFinishedCounter() {
		blockFinishedCounter++;
	}
	
	public boolean getBlocksFinishedCounter() {
		return blockFinishedCounter == noBlocks;
	}

	public static void main(String[] args) {
		new BSBI();
	}

}
